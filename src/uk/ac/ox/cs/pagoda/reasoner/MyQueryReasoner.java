package uk.ac.ox.cs.pagoda.reasoner;

import java.util.Collection;

import org.semanticweb.karma2.profile.ELHOProfile;
import org.semanticweb.owlapi.model.OWLOntology;

import uk.ac.ox.cs.pagoda.multistage.*; 
import uk.ac.ox.cs.pagoda.owl.EqualitiesEliminator;
import uk.ac.ox.cs.pagoda.owl.OWLHelper;
import uk.ac.ox.cs.pagoda.query.*;
import uk.ac.ox.cs.pagoda.query.QueryRecord.Step;
import uk.ac.ox.cs.pagoda.reasoner.full.Checker;
import uk.ac.ox.cs.pagoda.reasoner.light.BasicQueryEngine;
import uk.ac.ox.cs.pagoda.reasoner.light.KarmaQueryEngine;
import uk.ac.ox.cs.pagoda.rules.DatalogProgram;
import uk.ac.ox.cs.pagoda.summary.HermitSummaryFilter;
import uk.ac.ox.cs.pagoda.tracking.*;
import uk.ac.ox.cs.pagoda.util.Timer;
import uk.ac.ox.cs.pagoda.util.Utility;

public class MyQueryReasoner extends QueryReasoner {

	OWLOntology ontology; 

//	String additonalDataFile; 
	
	DatalogProgram program; 

	BasicQueryEngine rlLowerStore = null;
	BasicQueryEngine lazyUpperStore = null;  
//	boolean[] namedIndividuals_lazyUpper; 
	
	OWLOntology elho_ontology; 
	KarmaQueryEngine elLowerStore = null; 
	
	BasicQueryEngine trackingStore = null;
//	boolean[] namedIndividuals_tracking; 
	
	boolean equalityTag; 
	boolean multiStageTag;
	
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
		satisfiable = null; 
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
		if (!equalityTag) {
			EqualitiesEliminator eliminator = new EqualitiesEliminator(o);
			o = eliminator.getOutputOntology();
			eliminator.save();
		}			

		ontology = o; 
		program = new DatalogProgram(ontology, !forSemFacet);
//		program.getLower().save();
//		program.getUpper().save();
//		program.getGeneral().save();
		
		if (multiStageTag && !program.getGeneral().isHorn()) { 
			lazyUpperStore =  getUpperStore("lazy-upper-bound", true); // new MultiStageQueryEngine("lazy-upper-bound", true); //
		}
		
		importData(program.getAdditionalDataFile());
	
		elho_ontology = new ELHOProfile().getFragment(ontology);
		elLowerStore.processOntology(elho_ontology);
	}

	private Collection<String> predicatesWithGap = null; 
	
	public Collection<String> getPredicatesWithGap() {
		return predicatesWithGap; 
	}	
	
	@Override
	public boolean preprocess() {
		t.reset(); 
		Utility.logInfo("Preprocessing ... checking satisfiability ... ");

		String name = "data", datafile = importedData.toString(); 
		rlLowerStore.importRDFData(name, datafile);
		rlLowerStore.materialise("lower program", program.getLower().toString());
//		program.getLower().save();
		if (!consistency.checkRLLowerBound()) return false;
		Utility.logInfo("The number of sameAs assertions in RL lower store: " + rlLowerStore.getSameAsNumber());
		
		String originalMarkProgram = OWLHelper.getOriginalMarkProgram(ontology);
		
		elLowerStore.importRDFData(name, datafile);
		elLowerStore.materialise("saturate named individuals", originalMarkProgram);
		elLowerStore.materialise("lower program", program.getLower().toString());
		elLowerStore.initialiseKarma();
		if (!consistency.checkELLowerBound()) return false; 			

		if (lazyUpperStore != null) {
			lazyUpperStore.importRDFData(name, datafile);
			lazyUpperStore.materialise("saturate named individuals", originalMarkProgram);
			int tag = lazyUpperStore.materialiseRestrictedly(program, null);  
			if (tag != 1) {
				lazyUpperStore.dispose();
				lazyUpperStore = null;
			}
			if (tag == -1) return false; 
		}
		if (consistency.checkLazyUpper()) {
			satisfiable = true; 
			Utility.logInfo("time for satisfiability checking: " + t.duration()); 
		}
			
		trackingStore.importRDFData(name, datafile);
		trackingStore.materialise("saturate named individuals", originalMarkProgram);
		
//		materialiseFullUpper();
		GapByStore4ID gap = new GapByStore4ID(trackingStore); 
		trackingStore.materialiseFoldedly(program, gap);
		predicatesWithGap = gap.getPredicatesWithGap(); 
		gap.clear();
		
		if (program.getGeneral().isHorn())
			encoder = new TrackingRuleEncoderWithGap(program.getUpper(), trackingStore);
		else 
			encoder = new TrackingRuleEncoderDisjVar1(program.getUpper(), trackingStore); 
//			encoder = new TrackingRuleEncoderDisj1(program.getUpper(), trackingStore); 
//			encoder = new TrackingRuleEncoderDisjVar2(program.getUpper(), trackingStore); 
//			encoder = new TrackingRuleEncoderDisj2(program.getUpper(), trackingStore); 
		
		program.deleteABoxTurtleFile(); 

		if (!isConsistent())
			return false; 
		
		consistency.extractBottomFragment();
		return true; 
	}
	
	private Boolean satisfiable;
	private ConsistencyManager consistency = new ConsistencyManager(this); 
	
	TrackingRuleEncoder encoder;

	@Override
	public boolean isConsistent() {
		if (satisfiable == null) {
			satisfiable = consistency.check(); 		
			Utility.logInfo("time for satisfiability checking: " + t.duration());
		}
		return satisfiable; 
	}
	
	Timer t = new Timer();

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
		rlAnswer = null;
		
		t.reset();
		BasicQueryEngine upperStore = queryRecord.isBottom() || lazyUpperStore == null ? trackingStore : lazyUpperStore;  
			
		String[] extendedQuery = queryRecord.getExtendedQueryText(); 
		
		queryUpperBound(upperStore, queryRecord, queryRecord.getQueryText(), queryRecord.getAnswerVariables());
		if (!queryRecord.processed() && !queryRecord.getQueryText().equals(extendedQuery[0]))
			queryUpperBound(upperStore, queryRecord, extendedQuery[0], queryRecord.getAnswerVariables());
		if (!queryRecord.processed() && queryRecord.hasNonAnsDistinguishedVariables()) 
			queryUpperBound(upperStore, queryRecord, extendedQuery[1], queryRecord.getDistinguishedVariables());
			
		queryRecord.addProcessingTime(Step.UpperBound, t.duration());
		if (queryRecord.processed()) {
			queryRecord.setDifficulty(Step.UpperBound); 
			return null;
		}
		
		t.reset();
		try {
			elAnswer = elLowerStore.evaluate(extendedQuery[0], queryRecord.getAnswerVariables(), queryRecord.getLowerBoundAnswers());
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
		
		OWLOntology knowledgebase; 
		t.reset(); 
//		if (program.getGeneral().isHorn()) {
//			knowledgebase = tracker.extract(lazyUpperStore, consistency.getQueryRecords(), true);
//			queryRecord.addProcessingTime(Step.Fragment, t.duration());
//			return knowledgebase; 
//		}
//		else { 
			knowledgebase = tracker.extract(trackingStore, consistency.getQueryRecords(), true);
			queryRecord.addProcessingTime(Step.Fragment, t.duration());
//		}
		
			if (knowledgebase.isEmpty() || queryRecord.isBottom()) 
				return knowledgebase; 
			
		if (program.getGeneral().isHorn()) return knowledgebase; 

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
		
		return knowledgebase; 
	}

//	int counter = 0; 

	private void queryUpperBound(BasicQueryEngine upperStore, QueryRecord queryRecord, String queryText, String[] answerVariables) {
		AnswerTuples rlAnswer = null; 
		try {
			Utility.logDebug(queryText);
			rlAnswer = upperStore.evaluate(queryText, answerVariables);
			Utility.logDebug(t.duration());
			queryRecord.updateUpperBoundAnswers(rlAnswer); 
			rlAnswer.dispose();
		} finally {
			if (rlAnswer != null) rlAnswer.dispose();
		}
		rlAnswer = null;
	}

	@Override
	public void evaluate(QueryRecord queryRecord) {
		OWLOntology knowledgebase = relevantPart(queryRecord);
		
		if (knowledgebase == null) {
			Utility.logDebug("Difficulty of this query: " + queryRecord.getDifficulty());
			return ; 
		}
		
		int aboxcount = knowledgebase.getABoxAxioms(true).size(); 
		Utility.logDebug("ABox axioms: " + aboxcount + " TBox axioms: " + (knowledgebase.getAxiomCount() - aboxcount)); 
//		queryRecord.saveRelevantOntology("fragment_query" + queryRecord.getQueryID() + ".owl"); 
		
		Timer t = new Timer(); 
		Checker summarisedChecker = new HermitSummaryFilter(queryRecord); 
		int validNumber = summarisedChecker.check(queryRecord.getGapAnswers()); 
		summarisedChecker.dispose();
		Utility.logDebug("Total time for full reasoner: " + t.duration());
		if (!forSemFacet || validNumber == 0) { 
			queryRecord.markAsProcessed(); 
			Utility.logDebug("Difficulty of this query: " + queryRecord.getDifficulty());
		}
	}
	
	@Override
	public void evaluateUpper(QueryRecord queryRecord) {
		AnswerTuples rlAnswer = null; 
		boolean useFull = queryRecord.isBottom() || lazyUpperStore == null; 
		try {
			rlAnswer = (useFull ? trackingStore: lazyUpperStore).evaluate(queryRecord.getQueryText(), queryRecord.getAnswerVariables());
			queryRecord.updateUpperBoundAnswers(rlAnswer, true); 
		} finally {
			if (rlAnswer != null) rlAnswer.dispose();
		}
	}
	
	@Override
	public void dispose() {
		if (encoder != null) encoder.dispose();
		if (rlLowerStore != null) rlLowerStore.dispose();
		if (lazyUpperStore != null) lazyUpperStore.dispose();
		if (elLowerStore != null) elLowerStore.dispose();
		if (trackingStore != null) trackingStore.dispose();
		super.dispose();
	}

}
