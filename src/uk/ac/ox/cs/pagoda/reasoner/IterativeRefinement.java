package uk.ac.ox.cs.pagoda.reasoner;

import org.semanticweb.owlapi.model.OWLOntology;
import uk.ac.ox.cs.pagoda.constraints.BottomStrategy;
import uk.ac.ox.cs.pagoda.constraints.UpperUnaryBottom;
import uk.ac.ox.cs.pagoda.multistage.MultiStageQueryEngine;
import uk.ac.ox.cs.pagoda.query.AnswerTuples;
import uk.ac.ox.cs.pagoda.query.QueryRecord;
import uk.ac.ox.cs.pagoda.reasoner.light.BasicQueryEngine;
import uk.ac.ox.cs.pagoda.rules.GeneralProgram;
import uk.ac.ox.cs.pagoda.tracking.QueryTracker;
import uk.ac.ox.cs.pagoda.util.Utility;

import java.io.File;

public class IterativeRefinement {
	
	private static final int depthLimit = 1; 
	
	QueryRecord m_record;
	QueryTracker m_tracker; 
	BasicQueryEngine m_trackingStore;
	QueryRecord[] botQueryRecords;  
	
	int m_depth = 0;
	String tempDataFile = "temp.ttl";
	
	public IterativeRefinement(QueryRecord queryRecord, QueryTracker tracker, BasicQueryEngine trackingStore, QueryRecord[] botQueryRecords) {
		m_record = queryRecord;
		m_tracker = tracker;
		m_trackingStore = trackingStore;
		this.botQueryRecords = botQueryRecords;
	}

	public OWLOntology extractWithFullABox(String dataset, BottomStrategy upperBottom) {
		GeneralProgram program; 
		boolean update; 
		while (m_depth < depthLimit) {
			++m_depth;
			program = new GeneralProgram(m_record.getRelevantClauses(), m_record.getRelevantOntology());
			
			MultiStageQueryEngine tEngine = new MultiStageQueryEngine("query-tracking", true);
			try {
				tEngine.importRDFData("data", dataset);
				if (tEngine.materialise4SpecificQuery(program, m_record, upperBottom) != 1) {
					return m_record.getRelevantOntology(); 
				}
				
				AnswerTuples ans = null; 
				try {
					ans = tEngine.evaluate(m_record.getQueryText());
					update = m_record.updateUpperBoundAnswers(ans);
				} finally {
					if (ans != null) ans.dispose();
				}				
			} finally {
				tEngine.dispose();
			}

			if(m_record.isProcessed())
				return null;
			
			if (!update) break; 
			
			m_record.updateSubID();
			m_tracker.extract(m_trackingStore, botQueryRecords, true); 
		}
		
		return m_record.getRelevantOntology(); 
	}
	
	public OWLOntology extract(UpperUnaryBottom upperBottom) {
		GeneralProgram program; 
		boolean update; 
		while (m_depth < depthLimit) {
			m_record.saveABoxInTurtle(tempDataFile); 
			program = new GeneralProgram(m_record.getRelevantClauses(), m_record.getRelevantOntology());
			
			MultiStageQueryEngine tEngine = new MultiStageQueryEngine("query-tracking", true);
			try {
				tEngine.importRDFData("fragment abox", tempDataFile);
				if (tEngine.materialise4SpecificQuery(program, m_record, upperBottom) != 1) {
					return m_record.getRelevantOntology(); 
				}
				
				AnswerTuples ans = null; 
				try {
					ans = tEngine.evaluate(m_record.getQueryText()); 
					update = m_record.updateUpperBoundAnswers(ans);
				} finally {
					if (ans != null) ans.dispose();
				}
			} finally {
				tEngine.dispose();
			}

			if(m_record.isProcessed())
				return null;
			
			if (!update) break; 
			
			m_record.updateSubID();
			m_tracker.extract(m_trackingStore, botQueryRecords, true); 
		}
		
		return m_record.getRelevantOntology(); 
	}
	
	public void dispose() {
		File file = new File(tempDataFile);  
		if (file.exists()) {
			file.delete(); 
			Utility.logDebug(file.getAbsolutePath() + " is deleted.");
		}
	}

}
