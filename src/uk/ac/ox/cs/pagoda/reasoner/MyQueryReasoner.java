package uk.ac.ox.cs.pagoda.reasoner;

import org.semanticweb.karma2.profile.ELHOProfile;
import org.semanticweb.owlapi.model.OWLOntology;
import uk.ac.ox.cs.pagoda.multistage.MultiStageQueryEngine;
import uk.ac.ox.cs.pagoda.owl.EqualitiesEliminator;
import uk.ac.ox.cs.pagoda.owl.OWLHelper;
import uk.ac.ox.cs.pagoda.query.AnswerTuples;
import uk.ac.ox.cs.pagoda.query.GapByStore4ID;
import uk.ac.ox.cs.pagoda.query.GapByStore4ID2;
import uk.ac.ox.cs.pagoda.query.QueryRecord;
import uk.ac.ox.cs.pagoda.query.QueryRecord.Step;
import uk.ac.ox.cs.pagoda.reasoner.light.BasicQueryEngine;
import uk.ac.ox.cs.pagoda.reasoner.light.KarmaQueryEngine;
import uk.ac.ox.cs.pagoda.rules.DatalogProgram;
import uk.ac.ox.cs.pagoda.summary.HermitSummaryFilter;
import uk.ac.ox.cs.pagoda.tracking.QueryTracker;
import uk.ac.ox.cs.pagoda.tracking.TrackingRuleEncoder;
import uk.ac.ox.cs.pagoda.tracking.TrackingRuleEncoderDisjVar1;
import uk.ac.ox.cs.pagoda.tracking.TrackingRuleEncoderWithGap;
import uk.ac.ox.cs.pagoda.util.PagodaProperties;
import uk.ac.ox.cs.pagoda.util.Timer;
import uk.ac.ox.cs.pagoda.util.Utility;
import uk.ac.ox.cs.pagoda.util.disposable.DisposedException;
import uk.ac.ox.cs.pagoda.util.tuples.Tuple;

import java.util.Collection;

class MyQueryReasoner extends QueryReasoner {

    OWLOntology ontology;
    OWLOntology elho_ontology;
    DatalogProgram program;

    BasicQueryEngine rlLowerStore = null;
    KarmaQueryEngine elLowerStore = null;
    MultiStageQueryEngine lazyUpperStore = null;
    MultiStageQueryEngine trackingStore = null;
    TrackingRuleEncoder encoder;

    private boolean equalityTag;
    protected Timer timer = new Timer();

    protected Collection<String> predicatesWithGap = null;
    protected ConsistencyStatus isConsistent;
    protected ConsistencyManager consistency = new ConsistencyManager(this);
//    private int relevantOntologiesCounter = 0;

    public MyQueryReasoner() {
        setup(true);
    }

    public MyQueryReasoner(boolean multiStageTag, boolean considerEqualities) {
        if(!multiStageTag)
            throw new IllegalArgumentException(
                    "Value \"true\" for parameter \"multiStageTag\" is no longer supported");

        setup(considerEqualities);
    }

    @Override
    public void loadOntology(OWLOntology o) {
        if(isDisposed()) throw new DisposedException();
        if(!equalityTag) {
            EqualitiesEliminator eliminator = new EqualitiesEliminator(o);
            o = eliminator.getOutputOntology();
            eliminator.save();
        }

        ontology = o;
        program = new DatalogProgram(ontology);
		program.getLower().save();
		program.getUpper().save();
		program.getGeneral().save();

        if(!program.getGeneral().isHorn())
            lazyUpperStore = new MultiStageQueryEngine("lazy-upper-bound", true);

        importData(program.getAdditionalDataFile());

        elho_ontology = new ELHOProfile().getFragment(ontology);
        elLowerStore.processOntology(elho_ontology);
    }

    public Collection<String> getPredicatesWithGap() {
        if(isDisposed()) throw new DisposedException();
        return predicatesWithGap;
    }

    @Override
    public boolean preprocess() {
        if(isDisposed()) throw new DisposedException();

        timer.reset();
        Utility.logInfo("Preprocessing (and checking satisfiability)...");

        String name = "data", datafile = getImportedData();
        rlLowerStore.importRDFData(name, datafile);
        rlLowerStore.materialise("lower program", program.getLower().toString());
//		program.getLower().save();
        if(!consistency.checkRLLowerBound()) {
            Utility.logDebug("time for satisfiability checking: " + timer.duration());
            isConsistent = ConsistencyStatus.INCONSISTENT;
            return false;
        }
        Utility.logDebug("The number of sameAs assertions in RL lower store: " + rlLowerStore.getSameAsNumber());

        String originalMarkProgram = OWLHelper.getOriginalMarkProgram(ontology);

        elLowerStore.importRDFData(name, datafile);
        elLowerStore.materialise("saturate named individuals", originalMarkProgram);
        elLowerStore.materialise("lower program", program.getLower().toString());
        elLowerStore.initialiseKarma();
        if(!consistency.checkELLowerBound()) {
            Utility.logDebug("time for satisfiability checking: " + timer.duration());
            isConsistent = ConsistencyStatus.INCONSISTENT;
            return false;
        }

        if(lazyUpperStore != null) {
            lazyUpperStore.importRDFData(name, datafile);
            lazyUpperStore.materialise("saturate named individuals", originalMarkProgram);
            int tag = lazyUpperStore.materialiseRestrictedly(program, null);
            if(tag == -1) {
                Utility.logDebug("time for satisfiability checking: " + timer.duration());
                isConsistent = ConsistencyStatus.INCONSISTENT;
                return false;
            }
            else if(tag != 1) {
                lazyUpperStore.dispose();
                lazyUpperStore = null;
            }
        }
        if(consistency.checkUpper(lazyUpperStore)) {
            isConsistent = ConsistencyStatus.CONSISTENT;
            Utility.logDebug("time for satisfiability checking: " + timer.duration());
        }

        trackingStore.importRDFData(name, datafile);
        trackingStore.materialise("saturate named individuals", originalMarkProgram);

//		materialiseFullUpper();
//		GapByStore4ID gap = new GapByStore4ID(trackingStore);
        GapByStore4ID gap = new GapByStore4ID2(trackingStore, rlLowerStore);
        trackingStore.materialiseFoldedly(program, gap);
        predicatesWithGap = gap.getPredicatesWithGap();
        gap.clear();

        if(program.getGeneral().isHorn())
            encoder = new TrackingRuleEncoderWithGap(program.getUpper(), trackingStore);
        else
            encoder = new TrackingRuleEncoderDisjVar1(program.getUpper(), trackingStore);
//			encoder = new TrackingRuleEncoderDisj1(program.getUpper(), trackingStore);
//			encoder = new TrackingRuleEncoderDisjVar2(program.getUpper(), trackingStore);
//			encoder = new TrackingRuleEncoderDisj2(program.getUpper(), trackingStore);

        // TODO? add consistency check by Skolem-upper-bound

        if(!isConsistent())
            return false;

        consistency.extractBottomFragment();

        return true;
    }

    @Override
    public boolean isConsistent() {
        if(isDisposed()) throw new DisposedException();

        if(isConsistent == ConsistencyStatus.UNCHECKED) {
            isConsistent = consistency.check() ? ConsistencyStatus.CONSISTENT : ConsistencyStatus.INCONSISTENT;
            Utility.logDebug("time for satisfiability checking: " + timer.duration());
        }
        if(isConsistent == ConsistencyStatus.CONSISTENT) {
            Utility.logInfo("The ontology is consistent!");
            return true;
        }
        else {
            Utility.logInfo("The ontology is inconsistent!");
            return false;
        }
    }

    @Override
    public void evaluate(QueryRecord queryRecord) {
        if(isDisposed()) throw new DisposedException();

        if(queryLowerAndUpperBounds(queryRecord))
            return;

        OWLOntology relevantOntologySubset = extractRelevantOntologySubset(queryRecord);

//        queryRecord.saveRelevantOntology("/home/alessandro/Desktop/test-relevant-ontology-"+relevantOntologiesCounter+".owl");
//        relevantOntologiesCounter++;

        if(properties.getSkolemUpperBound() == PagodaProperties.SkolemUpperBoundOptions.BEFORE_SUMMARISATION
                && querySkolemisedRelevantSubset(relevantOntologySubset, queryRecord)) {
            return;
        }

        Utility.logInfo(">> Summarisation <<");
        HermitSummaryFilter summarisedChecker = new HermitSummaryFilter(queryRecord, properties.getToCallHermiT());
        if(summarisedChecker.check(queryRecord.getGapAnswers()) == 0) {
            summarisedChecker.dispose();
            return;
        }

        if(properties.getSkolemUpperBound() == PagodaProperties.SkolemUpperBoundOptions.AFTER_SUMMARISATION
                && querySkolemisedRelevantSubset(relevantOntologySubset, queryRecord)) {
            summarisedChecker.dispose();
            return;
        }

        Utility.logInfo(">> Full reasoning <<");
        Timer t = new Timer();
        summarisedChecker.checkByFullReasoner(queryRecord.getGapAnswers());
        Utility.logDebug("Total time for full reasoner: " + t.duration());

        if(properties.getToCallHermiT())
            queryRecord.markAsProcessed();
        summarisedChecker.dispose();
    }

    @Override
    public void evaluateUpper(QueryRecord queryRecord) {
        if(isDisposed()) throw new DisposedException();
        // TODO? add new upper store
        AnswerTuples rlAnswer = null;
        boolean useFull = queryRecord.isBottom() || lazyUpperStore == null;
        try {
            rlAnswer =
                    (useFull ? trackingStore : lazyUpperStore).evaluate(queryRecord.getQueryText(), queryRecord.getAnswerVariables());
            queryRecord.updateUpperBoundAnswers(rlAnswer, true);
        } finally {
            if(rlAnswer != null) rlAnswer.dispose();
        }
    }

    @Override
    public void dispose() {
        super.dispose();

        if(encoder != null) encoder.dispose();
        if(rlLowerStore != null) rlLowerStore.dispose();
        if(lazyUpperStore != null) lazyUpperStore.dispose();
        if(elLowerStore != null) elLowerStore.dispose();
        if(trackingStore != null) trackingStore.dispose();
        if(consistency != null) consistency.dispose();
        if(program != null) program.dispose();
    }

    private void setup(boolean considerEqualities) {
        if(isDisposed()) throw new DisposedException();

        isConsistent = ConsistencyStatus.UNCHECKED;
        this.equalityTag = considerEqualities;

        rlLowerStore = new BasicQueryEngine("rl-lower-bound");
        elLowerStore = new KarmaQueryEngine("elho-lower-bound");

        trackingStore = new MultiStageQueryEngine("tracking", false);
    }

    protected void internal_importDataFile(String name, String datafile) {
//		addDataFile(datafile);
        rlLowerStore.importRDFData(name, datafile);
        if(lazyUpperStore != null)
            lazyUpperStore.importRDFData(name, datafile);
        elLowerStore.importRDFData(name, datafile);
        trackingStore.importRDFData(name, datafile);
    }

    /**
     * It deals with blanks nodes differently from variables
     * according to SPARQL semantics for OWL2 Entailment Regime.
     * <p>
     * In particular variables are matched only against named individuals,
     * and blank nodes against named and anonymous individuals.
     */
    private boolean queryUpperStore(BasicQueryEngine upperStore, QueryRecord queryRecord,
                                    Tuple<String> extendedQuery, Step step) {
        timer.reset();

        Utility.logDebug("First query type");
        queryUpperBound(upperStore, queryRecord, queryRecord.getQueryText(), queryRecord.getAnswerVariables());
        if(!queryRecord.isProcessed() && !queryRecord.getQueryText().equals(extendedQuery.get(0))) {
            Utility.logDebug("Second query type");
            queryUpperBound(upperStore, queryRecord, extendedQuery.get(0), queryRecord.getAnswerVariables());
        }
        if(!queryRecord.isProcessed() && queryRecord.hasNonAnsDistinguishedVariables()) {
            Utility.logDebug("Third query type");
            queryUpperBound(upperStore, queryRecord, extendedQuery.get(1), queryRecord.getDistinguishedVariables());
        }

        queryRecord.addProcessingTime(step, timer.duration());
        if(queryRecord.isProcessed()) {
            queryRecord.setDifficulty(step);
            return true;
        }
        return false;
    }

    /**
     * Returns the part of the ontology relevant for Hermit, while computing the bound answers.
     */
    protected boolean queryLowerAndUpperBounds(QueryRecord queryRecord) {

        Utility.logInfo(">> Base bounds <<");

        AnswerTuples rlAnswer = null, elAnswer = null;

        timer.reset();
        try {
            rlAnswer = rlLowerStore.evaluate(queryRecord.getQueryText(), queryRecord.getAnswerVariables());
            Utility.logDebug(timer.duration());
            queryRecord.updateLowerBoundAnswers(rlAnswer);
        } finally {
            if(rlAnswer != null) rlAnswer.dispose();
        }
        queryRecord.addProcessingTime(Step.LOWER_BOUND, timer.duration());

        Tuple<String> extendedQueryTexts = queryRecord.getExtendedQueryText();

        if(properties.getUseAlwaysSimpleUpperBound() || lazyUpperStore == null) {
            Utility.logDebug("Tracking store");
            if(queryUpperStore(trackingStore, queryRecord, extendedQueryTexts, Step.SIMPLE_UPPER_BOUND))
                return true;
        }

        if(!queryRecord.isBottom()) {
            Utility.logDebug("Lazy store");
            if(lazyUpperStore != null && queryUpperStore(lazyUpperStore, queryRecord, extendedQueryTexts, Step.LAZY_UPPER_BOUND))
                return true;
        }

        timer.reset();
        try {
            elAnswer = elLowerStore.evaluate(extendedQueryTexts.get(0),
                                             queryRecord.getAnswerVariables(),
                                             queryRecord.getLowerBoundAnswers());
            Utility.logDebug(timer.duration());
            queryRecord.updateLowerBoundAnswers(elAnswer);
        } finally {
            if(elAnswer != null) elAnswer.dispose();
        }
        queryRecord.addProcessingTime(Step.EL_LOWER_BOUND, timer.duration());

        if(queryRecord.isProcessed()) {
            queryRecord.setDifficulty(Step.EL_LOWER_BOUND);
            return true;
        }

        return false;
    }

    private OWLOntology extractRelevantOntologySubset(QueryRecord queryRecord) {
        Utility.logInfo(">> Relevant ontology-subset extraction <<");

        timer.reset();

        QueryTracker tracker = new QueryTracker(encoder, rlLowerStore, queryRecord);
        OWLOntology relevantOntologySubset = tracker.extract(trackingStore, consistency.getQueryRecords(), true);

        queryRecord.addProcessingTime(Step.FRAGMENT, timer.duration());

        int numOfABoxAxioms = relevantOntologySubset.getABoxAxioms(true).size();
        int numOfTBoxAxioms = relevantOntologySubset.getAxiomCount() - numOfABoxAxioms;
        Utility.logInfo("Relevant ontology-subset has been extracted: |ABox|="
                                + numOfABoxAxioms + ", |TBox|=" + numOfTBoxAxioms);

        return relevantOntologySubset;
    }

    private void queryUpperBound(BasicQueryEngine upperStore, QueryRecord queryRecord, String queryText, String[] answerVariables) {
        AnswerTuples rlAnswer = null;
        try {
            Utility.logDebug(queryText);
            rlAnswer = upperStore.evaluate(queryText, answerVariables);
            Utility.logDebug(timer.duration());
            queryRecord.updateUpperBoundAnswers(rlAnswer);
        } finally {
            if(rlAnswer != null) rlAnswer.dispose();
        }
    }

    private boolean querySkolemisedRelevantSubset(OWLOntology relevantSubset, QueryRecord queryRecord) {
        Utility.logInfo(">> Semi-Skolemisation <<");
        timer.reset();

        DatalogProgram relevantProgram = new DatalogProgram(relevantSubset);

        MultiStageQueryEngine relevantStore =
                new MultiStageQueryEngine("Relevant-store", true); // checkValidity is true

        relevantStore.importDataFromABoxOf(relevantSubset);
        String relevantOriginalMarkProgram = OWLHelper.getOriginalMarkProgram(relevantSubset);

        int queryDependentMaxTermDepth = properties.getSkolemDepth();
        relevantStore.materialise("Mark original individuals", relevantOriginalMarkProgram);
        int materialisationTag = relevantStore.materialiseSkolemly(relevantProgram, null,
                                                                   queryDependentMaxTermDepth);
        queryRecord.addProcessingTime(Step.SKOLEM_UPPER_BOUND, timer.duration());
        if(materialisationTag == -1) {
            throw new Error("A consistent ontology has turned out to be " +
                                    "inconsistent in the Skolemises-relevant-upper-store");
        }
        else if(materialisationTag != 1) {
            Utility.logInfo("Semi-Skolemised relevant upper store cannot be employed");
            return false;
        }

        Utility.logInfo("Querying semi-Skolemised upper store...");
        boolean isFullyProcessed = queryUpperStore(relevantStore, queryRecord,
                                                   queryRecord.getExtendedQueryText(),
                                                   Step.SKOLEM_UPPER_BOUND);

        relevantStore.dispose();
        Utility.logInfo("Semi-Skolemised relevant upper store has been evaluated");
        return isFullyProcessed;
    }

    protected enum ConsistencyStatus {CONSISTENT, INCONSISTENT, UNCHECKED}

}
