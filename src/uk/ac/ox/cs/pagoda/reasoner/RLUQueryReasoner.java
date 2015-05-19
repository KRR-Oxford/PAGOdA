package uk.ac.ox.cs.pagoda.reasoner;

import org.semanticweb.owlapi.model.OWLOntology;
import uk.ac.ox.cs.pagoda.multistage.MultiStageQueryEngine;
import uk.ac.ox.cs.pagoda.owl.EqualitiesEliminator;
import uk.ac.ox.cs.pagoda.query.AnswerTuples;
import uk.ac.ox.cs.pagoda.query.QueryRecord;
import uk.ac.ox.cs.pagoda.query.QueryRecord.Step;
import uk.ac.ox.cs.pagoda.reasoner.light.BasicQueryEngine;
import uk.ac.ox.cs.pagoda.rules.DatalogProgram;
import uk.ac.ox.cs.pagoda.util.Timer;
import uk.ac.ox.cs.pagoda.util.Utility;

class RLUQueryReasoner extends QueryReasoner {

	DatalogProgram program; 
	
	BasicQueryEngine rlLowerStore, rlUpperStore;
	
	boolean multiStageTag, equalityTag;
	Timer t = new Timer();
	
	public RLUQueryReasoner(boolean multiStageTag, boolean considerEqualities) {
		this.multiStageTag = multiStageTag;
		this.equalityTag = considerEqualities;
		rlLowerStore = new BasicQueryEngine("rl-lower-bound");
		if(!multiStageTag)
			rlUpperStore = new BasicQueryEngine("rl-upper-bound");
		else
			rlUpperStore = new MultiStageQueryEngine("rl-upper-bound", false);
	}
	
	@Override
	public void evaluate(QueryRecord queryRecord) {
		AnswerTuples ans = null;
		t.reset(); 
		try {
			ans = rlLowerStore.evaluate(queryRecord.getQueryText(), queryRecord.getAnswerVariables());
			Utility.logDebug(t.duration());
			queryRecord.updateLowerBoundAnswers(ans); 
		} finally {
			if (ans != null) ans.dispose();
		}
		queryRecord.addProcessingTime(Step.LowerBound, t.duration());
		
		ans = null; 
		t.reset(); 
		try {
			ans = rlUpperStore.evaluate(queryRecord.getQueryText(), queryRecord.getAnswerVariables());
			Utility.logDebug(t.duration());
			queryRecord.updateUpperBoundAnswers(ans); 
		} finally {
			if (ans != null) ans.dispose();
		}
		queryRecord.addProcessingTime(Step.UpperBound, t.duration());
		
		if (queryRecord.processed())
			queryRecord.setDifficulty(Step.UpperBound);
	}
	
	@Override
	public void evaluateUpper(QueryRecord queryRecord) {
		AnswerTuples ans = null; 
		try {
			ans = rlUpperStore.evaluate(queryRecord.getQueryText(), queryRecord.getAnswerVariables());
			Utility.logDebug(t.duration());
			queryRecord.updateUpperBoundAnswers(ans, true); 
		} finally {
			if (ans != null) ans.dispose();
		}
	}

	@Override
	public void dispose() {
		if (rlLowerStore != null) rlLowerStore.dispose();
		if (rlUpperStore != null) rlUpperStore.dispose();
		super.dispose();
	}

	@Override
	public void loadOntology(OWLOntology o) {
		if (!equalityTag) {
			EqualitiesEliminator eliminator = new EqualitiesEliminator(o);
			o = eliminator.getOutputOntology();
			eliminator.save();
		}			

		OWLOntology ontology = o; 
		program = new DatalogProgram(ontology);
		importData(program.getAdditionalDataFile());
	}

	@Override
	public boolean preprocess() {
		String datafile = importedData.toString(); 
		rlLowerStore.importRDFData("data", datafile);
		rlLowerStore.materialise("lower program", program.getLower().toString());
		
		rlUpperStore.importRDFData("data", datafile);
		rlUpperStore.materialiseRestrictedly(program, null);

		return isConsistent();

	}

	@Override
	public boolean isConsistent() {
		String[] X = new String[] { "X" }; 
		AnswerTuples ans = null; 
		try {
			ans = rlLowerStore.evaluate(QueryRecord.botQueryText, X);
			if (ans.isValid()) return false; 
		} finally {
			if (ans != null) ans.dispose();
		}

		ans = null; 
		try {
			ans = rlUpperStore.evaluate(QueryRecord.botQueryText, X);
			if (!ans.isValid()) return true; 
		} finally { 
			if (ans != null) ans.dispose();
		}
		
		Utility.logDebug("The consistency of the data has not been determined yet.");
		return true; 
	}
	
	
}
