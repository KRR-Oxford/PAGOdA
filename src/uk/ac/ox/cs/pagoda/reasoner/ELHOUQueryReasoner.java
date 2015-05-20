package uk.ac.ox.cs.pagoda.reasoner;

import org.semanticweb.karma2.profile.ELHOProfile;
import org.semanticweb.owlapi.model.OWLOntology;
import uk.ac.ox.cs.pagoda.multistage.MultiStageQueryEngine;
import uk.ac.ox.cs.pagoda.owl.EqualitiesEliminator;
import uk.ac.ox.cs.pagoda.owl.OWLHelper;
import uk.ac.ox.cs.pagoda.query.AnswerTuples;
import uk.ac.ox.cs.pagoda.query.QueryRecord;
import uk.ac.ox.cs.pagoda.query.QueryRecord.Step;
import uk.ac.ox.cs.pagoda.reasoner.light.BasicQueryEngine;
import uk.ac.ox.cs.pagoda.reasoner.light.KarmaQueryEngine;
import uk.ac.ox.cs.pagoda.rules.DatalogProgram;
import uk.ac.ox.cs.pagoda.util.Timer;
import uk.ac.ox.cs.pagoda.util.Utility;

class ELHOUQueryReasoner extends QueryReasoner {

	DatalogProgram program; 
	
	BasicQueryEngine rlLowerStore; 
	BasicQueryEngine rlUpperStore; 
	
	OWLOntology elho_ontology; 
	KarmaQueryEngine elLowerStore = null; 
	
	boolean multiStageTag, equalityTag;
	String originalMarkProgram;
	private Timer t = new Timer();

	public ELHOUQueryReasoner(boolean multiStageTag, boolean considerEqualities) {
		this.multiStageTag = multiStageTag;
		this.equalityTag = considerEqualities;
		rlLowerStore = new BasicQueryEngine("rl-lower-bound");
		elLowerStore = new KarmaQueryEngine("el-lower-bound");

		if(!multiStageTag)
			rlUpperStore = new BasicQueryEngine("rl-upper-bound");
		else
			rlUpperStore = new MultiStageQueryEngine("rl-upper-bound", false);
	}

	@Override
	public void evaluate(QueryRecord queryRecord) {
		AnswerTuples rlAnswer = null;
		t.reset();
		try {
			rlAnswer = rlLowerStore.evaluate(queryRecord.getQueryText(), queryRecord.getAnswerVariables());
			queryRecord.updateLowerBoundAnswers(rlAnswer);
		} finally {
			if(rlAnswer != null) rlAnswer.dispose();
		}
		queryRecord.addProcessingTime(Step.LOWER_BOUND, t.duration());

		String extendedQueryText = queryRecord.getExtendedQueryText().get(0);
		String[] toQuery = queryRecord.getQueryText().equals(extendedQueryText) ?
				new String[]{queryRecord.getQueryText()} :
				new String[] {queryRecord.getQueryText(), extendedQueryText};

		for (String queryText: toQuery) {
			rlAnswer = null;
			t.reset();
			try {
				rlAnswer = rlUpperStore.evaluate(queryText, queryRecord.getAnswerVariables());
				queryRecord.updateUpperBoundAnswers(rlAnswer);
			} finally {
				if(rlAnswer != null) rlAnswer.dispose();
			}
			queryRecord.addProcessingTime(Step.UPPER_BOUND, t.duration());

			if (queryRecord.processed()) {
				queryRecord.setDifficulty(Step.UPPER_BOUND);
				return;
			}
		}

		AnswerTuples elAnswer = null;
		t.reset();
		try {
			elAnswer =
					elLowerStore.evaluate(extendedQueryText, queryRecord.getAnswerVariables(), queryRecord.getLowerBoundAnswers());
			queryRecord.updateLowerBoundAnswers(elAnswer);
		} finally {
			if (elAnswer != null) elAnswer.dispose();
		}
		queryRecord.addProcessingTime(Step.EL_LOWER_BOUND, t.duration());
	}

	@Override
	public void evaluateUpper(QueryRecord queryRecord) {
		AnswerTuples rlAnswer = null;
		try {
			rlAnswer = rlUpperStore.evaluate(queryRecord.getQueryText(), queryRecord.getAnswerVariables());
			queryRecord.updateUpperBoundAnswers(rlAnswer, true);
		} finally {
			if(rlAnswer != null) rlAnswer.dispose();
		}
	}

	@Override
	public void dispose() {
		if (elLowerStore != null) elLowerStore.dispose();
		if(rlUpperStore != null) rlUpperStore.dispose();
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
		program = new DatalogProgram(ontology, properties.getToClassify());

		importData(program.getAdditionalDataFile());

		elho_ontology = new ELHOProfile().getFragment(ontology);
		elLowerStore.processOntology(elho_ontology);
		originalMarkProgram = OWLHelper.getOriginalMarkProgram(ontology);
	}

	@Override
	public boolean preprocess() {
		String name = "data", datafile = importedData.toString(); 

		String lowername = "lower program";
		String rlLowerProgramText = program.getLower().toString();
		
		rlUpperStore.importRDFData(name, datafile);
		rlUpperStore.materialise("saturate named individuals", originalMarkProgram);
		
		int flag = rlUpperStore.materialiseRestrictedly(program, null); 
		if (flag != 1) {
			if (flag == -1) return false; 		
			rlUpperStore.dispose();
			
			if (!multiStageTag) 
				rlUpperStore = new BasicQueryEngine("rl-upper-bound"); 
			else  
				rlUpperStore = new MultiStageQueryEngine("rl-upper-bound", false); 
			rlUpperStore.importRDFData(name, datafile);
			rlUpperStore.materialise("saturate named individuals", originalMarkProgram);
			rlUpperStore.materialiseFoldedly(program, null); 
		}
		Utility.logInfo("upper store ready.");
		
		rlLowerStore.importRDFData(name, datafile);
		rlLowerStore.materialise(lowername, rlLowerProgramText);
		Utility.logInfo("lower store ready.");
		
		elLowerStore.importRDFData(name, datafile);
		elLowerStore.materialise("saturate named individuals", originalMarkProgram);
		elLowerStore.materialise(lowername, rlLowerProgramText);
		
		elLowerStore.initialiseKarma(); 
		Utility.logInfo("EL lower store ready.");
		
		if (!isConsistent()) {
			Utility.logInfo("The dataset is not consistent with the ontology."); 
			return false;
		}
		Utility.logInfo("The dataset is consistent.");
		return true; 
	}

	@Override
	public boolean isConsistent() {
		Utility.logInfo("Start checking consistency... ");
		String[] X = new String[] {"X"}; 
		AnswerTuples ans = null; 
		try {
			ans = rlUpperStore.evaluate(QueryRecord.botQueryText, X);
			if (!ans.isValid()) return true; 
		} finally {
			if (ans != null) ans.dispose();
		}

		ans = null; 
		try {
			ans = elLowerStore.evaluate(QueryRecord.botQueryText, X);
			if (ans.isValid()) return false; 
		} finally {
			if (ans != null) ans.dispose();
		}
		
		Utility.logDebug("The consistency of the data has not been determined yet.");
		return true; 
	}

}
