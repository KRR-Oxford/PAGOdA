package uk.ac.ox.cs.pagoda.reasoner;

import org.semanticweb.karma2.profile.ELHOProfile;
import org.semanticweb.owlapi.model.OWLOntology;
import uk.ac.ox.cs.pagoda.constraints.UnaryBottom;
import uk.ac.ox.cs.pagoda.query.AnswerTuples;
import uk.ac.ox.cs.pagoda.query.QueryRecord;
import uk.ac.ox.cs.pagoda.query.QueryRecord.Step;
import uk.ac.ox.cs.pagoda.reasoner.light.KarmaQueryEngine;
import uk.ac.ox.cs.pagoda.rules.LowerDatalogProgram;
import uk.ac.ox.cs.pagoda.util.Timer;
import uk.ac.ox.cs.pagoda.util.Utility;

class ELHOQueryReasoner extends QueryReasoner {

	LowerDatalogProgram program; 
	
	OWLOntology elho_ontology; 
	KarmaQueryEngine elLowerStore = null; 

	private Timer t = new Timer(); 
	
	public ELHOQueryReasoner() {
		elLowerStore = new KarmaQueryEngine("el");
	}
	
	@Override
	public void evaluate(QueryRecord queryRecord) {
		AnswerTuples elAnswer = null;
		t.reset(); 
		try {
			elAnswer = elLowerStore.evaluate(queryRecord.getQueryText(), queryRecord.getAnswerVariables());
			queryRecord.updateLowerBoundAnswers(elAnswer); 
		} finally {
			if (elAnswer != null) elAnswer.dispose();
		}
		queryRecord.addProcessingTime(Step.EL_LOWER_BOUND, t.duration());

		queryRecord.setDifficulty(Step.EL_LOWER_BOUND);
		queryRecord.markAsProcessed();
	}

	@Override
	public void evaluateUpper(QueryRecord queryRecord) {
		evaluate(queryRecord); 
	}

	@Override
	public void dispose() {
		if (elLowerStore != null) elLowerStore.dispose();
		super.dispose();
	}

	@Override
	public void loadOntology(OWLOntology ontology) {
		program = new LowerDatalogProgram(properties.getToClassify()); 
		program.load(ontology, new UnaryBottom());
		program.transform();
		
		importData(program.getAdditionalDataFile());
		
		elho_ontology = new ELHOProfile().getFragment(ontology);
		elLowerStore.processOntology(elho_ontology);
	}

	@Override
	public boolean preprocess() {
		elLowerStore.importRDFData("data", importedData.toString()); 
		String rlLowerProgramText = program.toString();
//		program.save();
		elLowerStore.materialise("lower program", rlLowerProgramText);
		elLowerStore.initialiseKarma(); 
		
		if (!isConsistent()) {
			Utility.logDebug("The dataset is not consistent with the ontology."); 
			return false; 
		}
		return true; 
	}

	@Override
	public boolean isConsistent() {
		String[] X = new String[] {"X"}; 
		AnswerTuples ans = null;
		try {
			ans = elLowerStore.evaluate(QueryRecord.botQueryText, X); 
			if (ans.isValid()) return false; 
		} finally {
			if (ans != null) ans.dispose();
		}
		
		return true;
	}

}
