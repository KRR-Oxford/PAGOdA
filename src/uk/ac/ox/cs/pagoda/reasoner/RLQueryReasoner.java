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
import uk.ac.ox.cs.pagoda.util.disposable.DisposedException;

class RLQueryReasoner extends QueryReasoner {
	
	RDFoxQueryEngine rlLowerStore = null;

	LowerDatalogProgram program;
	Timer t = new Timer();

	public RLQueryReasoner() {
		rlLowerStore = new BasicQueryEngine("rl");
	}
	
	@Override
	public void evaluate(QueryRecord queryRecord) {
		if(isDisposed()) throw new DisposedException();
		AnswerTuples rlAnswer = null; 
		t.reset();
		try {
			rlAnswer = rlLowerStore.evaluate(queryRecord.getQueryText(), queryRecord.getAnswerVariables());
			queryRecord.updateLowerBoundAnswers(rlAnswer);
		} finally {
			if (rlAnswer != null) rlAnswer.dispose();
		}
		queryRecord.addProcessingTime(Step.LOWER_BOUND, t.duration());
		queryRecord.setDifficulty(Step.LOWER_BOUND);
		queryRecord.markAsProcessed();
	}

	@Override
	public void dispose() {
		super.dispose();
		if(rlLowerStore != null) rlLowerStore.dispose();
	}

	@Override
	public void loadOntology(OWLOntology ontology) {
		if(isDisposed()) throw new DisposedException();
		program = new LowerDatalogProgram(); 
		program.load(ontology, new UnaryBottom());
		program.transform();
		
		importData(program.getAdditionalDataFile());
	}

	@Override
	public boolean preprocess() {
		if(isDisposed()) throw new DisposedException();
		rlLowerStore.importRDFData("data", getImportedData());
		rlLowerStore.materialise("lower program", program.toString());

		return isConsistent();
	}

	@Override
	public boolean isConsistent() {
		if(isDisposed()) throw new DisposedException();
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
		if(isDisposed()) throw new DisposedException();
		evaluate(record); 
	}

}
