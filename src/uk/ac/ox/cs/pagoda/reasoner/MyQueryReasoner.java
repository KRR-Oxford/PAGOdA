package uk.ac.ox.cs.pagoda.reasoner;

import org.semanticweb.karma2.profile.ELHOProfile;
import org.semanticweb.owlapi.model.OWLOntology;
import uk.ac.ox.cs.pagoda.multistage.MultiStageQueryEngine;
import uk.ac.ox.cs.pagoda.owl.EqualitiesEliminator;
import uk.ac.ox.cs.pagoda.owl.OWLHelper;
import uk.ac.ox.cs.pagoda.query.AnswerTuples;
import uk.ac.ox.cs.pagoda.query.GapByStore4ID;
import uk.ac.ox.cs.pagoda.query.QueryRecord;
import uk.ac.ox.cs.pagoda.query.QueryRecord.Step;
import uk.ac.ox.cs.pagoda.reasoner.full.Checker;
import uk.ac.ox.cs.pagoda.reasoner.light.BasicQueryEngine;
import uk.ac.ox.cs.pagoda.reasoner.light.KarmaQueryEngine;
import uk.ac.ox.cs.pagoda.rules.DatalogProgram;
import uk.ac.ox.cs.pagoda.summary.HermitSummaryFilter;
import uk.ac.ox.cs.pagoda.tracking.QueryTracker;
import uk.ac.ox.cs.pagoda.tracking.TrackingRuleEncoder;
import uk.ac.ox.cs.pagoda.tracking.TrackingRuleEncoderDisjVar1;
import uk.ac.ox.cs.pagoda.tracking.TrackingRuleEncoderWithGap;
import uk.ac.ox.cs.pagoda.util.Timer;
import uk.ac.ox.cs.pagoda.util.Utility;
import uk.ac.ox.cs.pagoda.util.tuples.Tuple;

import java.util.Collection;
import java.util.HashMap;

class MyQueryReasoner extends QueryReasoner {

	OWLOntology ontology;
	DatalogProgram program;

//	String additonalDataFile; 
	BasicQueryEngine rlLowerStore = null;
	BasicQueryEngine lazyUpperStore = null;
	BasicQueryEngine limitedSkolemUpperStore;
	OWLOntology elho_ontology;
//	boolean[] namedIndividuals_lazyUpper; 
	KarmaQueryEngine elLowerStore = null;
	BasicQueryEngine trackingStore = null;
	//	boolean[] namedIndividuals_tracking;
	TrackingRuleEncoder encoder;
	private boolean equalityTag;
	private boolean multiStageTag;
	private Timer t = new Timer();
	private Collection<String> predicatesWithGap = null;
	private SatisfiabilityStatus satisfiable;
	private ConsistencyManager consistency = new ConsistencyManager(this);
	private boolean useUpperStores = false;
	public MyQueryReasoner() {
		setup(true, true);
	}
	public MyQueryReasoner(boolean multiStageTag, boolean considerEqualities) {
		setup(multiStageTag, considerEqualities);
	}

	private BasicQueryEngine getUpperStore(String name, boolean checkValidity) {
		if (multiStageTag)
			return new MultiStageQueryEngine(name, checkValidity);
//			return new TwoStageQueryEngine(name, checkValidity);
		else
			return new BasicQueryEngine(name);
	}

	public void setup(boolean multiStageTag, boolean considerEqualities) {
		satisfiable = SatisfiabilityStatus.UNCHECKED;
		this.multiStageTag = multiStageTag;
		this.equalityTag = considerEqualities;

		rlLowerStore = new BasicQueryEngine("rl-lower-bound");
		elLowerStore = new KarmaQueryEngine("elho-lower-bound");

		trackingStore = getUpperStore("tracking", false);
	}
	
	protected void internal_importDataFile(String name, String datafile) {
//		addDataFile(datafile);
		rlLowerStore.importRDFData(name, datafile);
		if (lazyUpperStore != null)
			lazyUpperStore.importRDFData(name, datafile);
		elLowerStore.importRDFData(name, datafile);
		trackingStore.importRDFData(name, datafile);
	}

	@Override
	public void loadOntology(OWLOntology o) {
		if(!equalityTag) {
			EqualitiesEliminator eliminator = new EqualitiesEliminator(o);
			o = eliminator.getOutputOntology();
			eliminator.save();
		}

		ontology = o;
		program = new DatalogProgram(ontology, properties.getToClassify());
//		program.getLower().save();
//		program.getUpper().save();
//		program.getGeneral().save();

		useUpperStores = multiStageTag && !program.getGeneral().isHorn();
		if(useUpperStores) {
			lazyUpperStore = getUpperStore("lazy-upper-bound", true);
			limitedSkolemUpperStore = getUpperStore("limited-skolem-upper-bound", true);
		}

		importData(program.getAdditionalDataFile());

		elho_ontology = new ELHOProfile().getFragment(ontology);
		elLowerStore.processOntology(elho_ontology);
	}

	public Collection<String> getPredicatesWithGap() {
		return predicatesWithGap;
	}

	@Override
	public boolean preprocess() {
		t.reset();
		Utility.logInfo("Preprocessing... checking satisfiability... ");

		String name = "data", datafile = importedData.toString();
		rlLowerStore.importRDFData(name, datafile);
		rlLowerStore.materialise("lower program", program.getLower().toString());
//		program.getLower().save();
		if(!consistency.checkRLLowerBound()) return false;
		Utility.logInfo("The number of sameAs assertions in RL lower store: " + rlLowerStore.getSameAsNumber());

		String originalMarkProgram = OWLHelper.getOriginalMarkProgram(ontology);

		elLowerStore.importRDFData(name, datafile);
		elLowerStore.materialise("saturate named individuals", originalMarkProgram);
		elLowerStore.materialise("lower program", program.getLower().toString());
		elLowerStore.initialiseKarma();
		if(!consistency.checkELLowerBound()) return false;

		if(lazyUpperStore != null) {
			lazyUpperStore.importRDFData(name, datafile);
			lazyUpperStore.materialise("saturate named individuals", originalMarkProgram);
			int tag = lazyUpperStore.materialiseRestrictedly(program, null);
			if(tag != 1) {
				lazyUpperStore.dispose();
				lazyUpperStore = null;
			}
			if(tag == -1) return false;
		}
		if(consistency.checkUpper(lazyUpperStore)) {
			satisfiable = SatisfiabilityStatus.SATISFIABLE;
			Utility.logInfo("time for satisfiability checking: " + t.duration());
		}

		if(limitedSkolemUpperStore != null) {
			limitedSkolemUpperStore.importRDFData(name, datafile);
			limitedSkolemUpperStore.materialise("saturate named individuals", originalMarkProgram);
			int tag = limitedSkolemUpperStore.materialiseSkolemly(program, null);
			if(tag != 1) {
				limitedSkolemUpperStore.dispose();
				limitedSkolemUpperStore = null;
			}
			if(tag == -1) return false;
		}
		if(satisfiable == SatisfiabilityStatus.UNCHECKED && consistency.checkUpper(limitedSkolemUpperStore)) {
			satisfiable = SatisfiabilityStatus.SATISFIABLE;
			Utility.logInfo("time for satisfiability checking: " + t.duration());
		}

		trackingStore.importRDFData(name, datafile);
		trackingStore.materialise("saturate named individuals", originalMarkProgram);

		GapByStore4ID gap = new GapByStore4ID(trackingStore);
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

		program.deleteABoxTurtleFile();

		if(!isConsistent())
			return false;

		consistency.extractBottomFragment();
		consistency.dispose();

		return true;
	}
	
	@Override
	public boolean isConsistent() {
		if(satisfiable == SatisfiabilityStatus.UNCHECKED) {
			satisfiable = consistency.check() ? SatisfiabilityStatus.SATISFIABLE : SatisfiabilityStatus.UNSATISFIABLE;
			Utility.logInfo("time for satisfiability checking: " + t.duration());
		}
		return satisfiable == SatisfiabilityStatus.SATISFIABLE;
	}

	// TODO why the following???
	private void queryUpperStore(BasicQueryEngine upperStore, QueryRecord queryRecord, Tuple<String> extendedQuery) {

//		Utility.logInfo("1");
//		queryUpperBound(upperStore, queryRecord, queryRecord.getQueryText(), queryRecord.getAnswerVariables());
//
//		boolean conditionA = !queryRecord.processed() && !queryRecord.getQueryText().equals(extendedQuery.get(0));
//		boolean conditionB = !queryRecord.processed() && queryRecord.hasNonAnsDistinguishedVariables();
//
//		if(conditionA ^ conditionB)
//			throw new Error("It really happened! I thought it was impossible");
//
//		if (conditionA) {
//			Utility.logInfo("2");
		queryUpperBound(upperStore, queryRecord, extendedQuery.get(0), queryRecord.getAnswerVariables());
//		}
//		if (conditionB) {
//			Utility.logInfo("3");
//			queryUpperBound(upperStore, queryRecord, extendedQuery.get(1), queryRecord.getDistinguishedVariables());
//		}
	}

	/**
	 * Returns the part of the ontology relevant for Hermit, while computing the bound answers.
	 * */
	private OWLOntology relevantPart(QueryRecord queryRecord) {
		AnswerTuples rlAnswer = null, elAnswer = null;

		t.reset();
		try {
			rlAnswer = rlLowerStore.evaluate(queryRecord.getQueryText(), queryRecord.getAnswerVariables());
			Utility.logDebug(t.duration());
			queryRecord.updateLowerBoundAnswers(rlAnswer);
		} finally {
			if (rlAnswer != null) rlAnswer.dispose();
		}
		queryRecord.addProcessingTime(Step.LowerBound, t.duration());

		t.reset();

		Tuple<String> extendedQueryTexts = queryRecord.getExtendedQueryText();

//		BasicQueryEngine upperStore;
//		if(queryRecord.isBottom()) upperStore = trackingStore;
//		upperStore = queryRecord.isBottom() || lazyUpperStore == null ? trackingStore : lazyUpperStore;

		// TODO
//		if(queryRecord.isBottom() || (lazyUpperStore == null && limitedSkolemUpperStore == null))
//			queryUpperStore(trackingStore, queryRecord, extendedQueryTexts);
//		else {

		queryUpperStore(trackingStore, queryRecord, extendedQueryTexts);
		if(!(queryRecord.isBottom() || (lazyUpperStore == null && limitedSkolemUpperStore == null))) {
			if(lazyUpperStore != null) queryUpperStore(lazyUpperStore, queryRecord, extendedQueryTexts);
			if(limitedSkolemUpperStore != null)
				queryUpperStore(limitedSkolemUpperStore, queryRecord, extendedQueryTexts);
		}

		queryRecord.addProcessingTime(Step.UpperBound, t.duration());
		if (queryRecord.processed()) {
			queryRecord.setDifficulty(Step.UpperBound);
			return null;
		}

		t.reset();
		try {
			elAnswer = elLowerStore.evaluate(extendedQueryTexts.get(0), queryRecord.getAnswerVariables(),
											 queryRecord.getLowerBoundAnswers());
			Utility.logDebug(t.duration());
			queryRecord.updateLowerBoundAnswers(elAnswer);
		} finally {
			if (elAnswer != null) elAnswer.dispose();
		}
		queryRecord.addProcessingTime(Step.ELLowerBound, t.duration());

		if (queryRecord.processed()) {
			queryRecord.setDifficulty(Step.ELLowerBound);
			return null;
		}

		t.reset();

		QueryTracker tracker = new QueryTracker(encoder, rlLowerStore, queryRecord);

		OWLOntology knowledgeBase;
		t.reset();
//		if (program.getGeneral().isHorn()) {
//			knowledgebase = tracker.extract(lazyUpperStore, consistency.getQueryRecords(), true);
//			queryRecord.addProcessingTime(Step.Fragment, t.duration());
//			return knowledgebase;
//		}
//		else {
		knowledgeBase = tracker.extract(trackingStore, consistency.getQueryRecords(), true);
			queryRecord.addProcessingTime(Step.Fragment, t.duration());
//		}

		if(knowledgeBase.isEmpty() || queryRecord.isBottom())
			return knowledgeBase;

		if(program.getGeneral().isHorn()) return knowledgeBase;

//		t.reset();
//		if (queryRecord.isHorn() && lazyUpperStore != null) {
////			knowledgebase = tracker.extract(lazyUpperStore, consistency.getQueryRecords(), true);
//		} else if (queryRecord.getArity() < 3) {
//			IterativeRefinement iterativeRefinement = new IterativeRefinement(queryRecord, tracker, trackingStore, consistency.getQueryRecords());
//			knowledgebase = iterativeRefinement.extractWithFullABox(importedData.toString(), program.getUpperBottomStrategy());
//		}
//
//		queryRecord.addProcessingTime(Step.FragmentRefinement, t.duration());
//
//		if (knowledgebase == null)
//			queryRecord.setDifficulty(Step.FragmentRefinement);

		return knowledgeBase;
	}

	private String toJsonKeyValuePair(String key, Object value) {
		HashMap<String, Object> map = new HashMap<>();
		map.put(key, value);
		return QueryRecord.GsonCreator.getInstance().toJson(map);
	}

	private void queryUpperBound(BasicQueryEngine upperStore, QueryRecord queryRecord, String queryText, String[] answerVariables) {
		AnswerTuples rlAnswer = null;
		try {
			Utility.logDebug(queryText);
			rlAnswer = upperStore.evaluate(queryText, answerVariables);
			Utility.logDebug(t.duration());
			queryRecord.updateUpperBoundAnswers(rlAnswer);
		} finally {
			if(rlAnswer != null) rlAnswer.dispose();
			rlAnswer = null;
		}
	}

//	int counter = 0; 

	@Override
	public void evaluate(QueryRecord queryRecord) {
		OWLOntology knowledgeBase = relevantPart(queryRecord);

		if(knowledgeBase == null) {
			Utility.logDebug("Difficulty of this query: " + queryRecord.getDifficulty());
			return;
		}

		int aBoxCount = knowledgeBase.getABoxAxioms(true).size();
		Utility.logDebug("ABox axioms: " + aBoxCount + " TBox axioms: " + (knowledgeBase.getAxiomCount() - aBoxCount));
//		queryRecord.saveRelevantOntology("fragment_query" + queryRecord.getQueryID() + ".owl");

		Timer t = new Timer();
		Checker summarisedChecker = new HermitSummaryFilter(queryRecord, properties.getToCallHermiT());
//		int validNumber =
		summarisedChecker.check(queryRecord.getGapAnswers());
		summarisedChecker.dispose();
		Utility.logDebug("Total time for full reasoner: " + t.duration());
//		if (validNumber == 0) {
		queryRecord.markAsProcessed();
		Utility.logDebug("Difficulty of this query: " + queryRecord.getDifficulty());
//		}
	}

	@Override
	public void evaluateUpper(QueryRecord queryRecord) {
		// TODO add new upper store
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
		if (encoder != null) encoder.dispose();
		if (rlLowerStore != null) rlLowerStore.dispose();
		if (lazyUpperStore != null) lazyUpperStore.dispose();
		if (elLowerStore != null) elLowerStore.dispose();
		if (trackingStore != null) trackingStore.dispose();
		if(limitedSkolemUpperStore != null) limitedSkolemUpperStore.dispose();
		super.dispose();
	}

	enum SatisfiabilityStatus {SATISFIABLE, UNSATISFIABLE, UNCHECKED}

}
