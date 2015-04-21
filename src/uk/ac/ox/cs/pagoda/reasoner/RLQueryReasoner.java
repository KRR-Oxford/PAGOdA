package uk.ac.ox.cs.pagoda.reasoner;

import org.semanticweb.owlapi.model.OWLOntology;
import uk.ac.ox.cs.pagoda.constraints.UnaryBottom;
import uk.ac.ox.cs.pagoda.query.AnswerTuples;
import uk.ac.ox.cs.pagoda.query.QueryRecord;
import uk.ac.ox.cs.pagoda.query.QueryRecord.Step;
import uk.ac.ox.cs.pagoda.reasoner.light.BasicQueryEngine;
import uk.ac.ox.cs.pagoda.reasoner.light.RDFoxQueryEngine;
import uk.ac.ox.cs.pagoda.rules.LowerDatalogProgram;
import uk.ac.ox.cs.pagoda.util.Timer;

public class RLQueryReasoner extends QueryReasoner {
	
	RDFoxQueryEngine rlLowerStore = null;

	LowerDatalogProgram program; 
	
	public RLQueryReasoner() {
		rlLowerStore = new BasicQueryEngine("rl"); 
	}
	
	Timer t = new Timer(); 
	
	@Override
	public void evaluate(QueryRecord queryRecord) {
		AnswerTuples rlAnswer = null; 
		t.reset();
		try {
			rlAnswer = rlLowerStore.evaluate(queryRecord.getQueryText(), queryRecord.getAnswerVariables());
			queryRecord.updateLowerBoundAnswers(rlAnswer);
		} finally {
			if (rlAnswer != null) rlAnswer.dispose();
		}
		queryRecord.addProcessingTime(Step.LowerBound, t.duration());
		queryRecord.setDifficulty(Step.LowerBound);
		queryRecord.markAsProcessed();
	}

	@Override
	public void dispose() {
		if (rlLowerStore != null) rlLowerStore.dispose();
		super.dispose();
	}

	@Override
	public void loadOntology(OWLOntology ontology) {
		program = new LowerDatalogProgram(); 
		program.load(ontology, new UnaryBottom());
		program.transform();
		
		importData(program.getAdditionalDataFile());
	}

	@Override
	public boolean preprocess() {
		rlLowerStore.importRDFData("data", importedData.toString());
		rlLowerStore.materialise("lower program", program.toString());
		
		if (!isConsistent())  
			return false; 
		return true; 
	}

	@Override
	public boolean isConsistent() {
		AnswerTuples ans = null; 
		try {
			ans = rlLowerStore.evaluate(QueryRecord.botQueryText, new String[] {"X"});
			return !ans.isValid(); 
		} finally {
			if (ans != null) ans.dispose();
				
		}
		
	}

	@Override
	public void evaluateUpper(QueryRecord record) {
		evaluate(record); 
	}

}
