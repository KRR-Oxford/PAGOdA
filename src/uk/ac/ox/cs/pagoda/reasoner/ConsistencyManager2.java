package uk.ac.ox.cs.pagoda.reasoner;

import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;

import uk.ac.ox.cs.pagoda.query.AnswerTuples;
import uk.ac.ox.cs.pagoda.query.QueryRecord;
import uk.ac.ox.cs.pagoda.reasoner.full.Checker;
import uk.ac.ox.cs.pagoda.summary.HermitSummaryFilter;
import uk.ac.ox.cs.pagoda.tracking.QueryTracker;
import uk.ac.ox.cs.pagoda.util.Utility;

@Deprecated
public class ConsistencyManager2 extends ConsistencyManager {

	public ConsistencyManager2(MyQueryReasoner reasoner) {
		super(reasoner);
		fragmentExtracted = true; 
	}
	
	protected boolean unsatisfiability(double duration) {
		Utility.logDebug("The ontology and dataset is unsatisfiable."); 
		return false;
	}

	protected boolean satisfiability(double duration) {
		Utility.logDebug("The ontology and dataset is satisfiable."); 
		return true;
	}
	
	@Override
	boolean check() {
//		if (!checkRLLowerBound()) return false; 
//		if (!checkELLowerBound()) return false;  
		if (checkLazyUpper()) return true; 
		AnswerTuples iter = null; 
		
		try {
			iter = m_reasoner.trackingStore.evaluate(fullQueryRecord.getQueryText(), fullQueryRecord.getAnswerVariables());
			fullQueryRecord.updateUpperBoundAnswers(iter);
		} finally {
			if (iter != null) iter.dispose();
		}
		
		if (fullQueryRecord.getNoOfCompleteAnswers() == 0)
			return satisfiability(t.duration());
		
		try {
			extractAxioms();
		} catch (OWLOntologyCreationException e) {
			e.printStackTrace();
		}
		
		Checker checker = new HermitSummaryFilter(fullQueryRecord, true);  // m_reasoner.factory.getSummarisedReasoner(fullQueryRecord);
//		fullQueryRecord.saveRelevantOntology("fragment_bottom.owl");
		boolean satisfiable = checker.isConsistent(); 
		checker.dispose();
		if (!satisfiable) return unsatisfiability(t.duration()); 
		
		return satisfiability(t.duration()); 
	}

	private void extractAxioms() throws OWLOntologyCreationException {
		OWLOntologyManager manager = m_reasoner.encoder.getProgram().getOntology().getOWLOntologyManager(); 
		fullQueryRecord.setRelevantOntology(manager.createOntology());
		QueryTracker tracker = new QueryTracker(m_reasoner.encoder, m_reasoner.rlLowerStore, fullQueryRecord); 
		m_reasoner.encoder.setCurrentQuery(fullQueryRecord);
		tracker.extract(m_reasoner.trackingStore, null, true); 
	}

	@Override
	public QueryRecord[] getQueryRecords() {
		if (botQueryRecords == null)
			botQueryRecords = new QueryRecord[] {fullQueryRecord}; 
		return botQueryRecords; 
	}

}
