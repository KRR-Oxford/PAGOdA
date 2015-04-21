package uk.ac.ox.cs.pagoda.endomorph.plan;

import java.util.Collection;
import java.util.Deque;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;

import uk.ac.ox.cs.pagoda.endomorph.*;
import uk.ac.ox.cs.pagoda.query.AnswerTuple;
import uk.ac.ox.cs.pagoda.query.QueryRecord;
import uk.ac.ox.cs.pagoda.query.QueryRecord.Step;
import uk.ac.ox.cs.pagoda.reasoner.full.Checker;
import uk.ac.ox.cs.pagoda.summary.NodeTuple;
import uk.ac.ox.cs.pagoda.util.Timer;
import uk.ac.ox.cs.pagoda.util.Utility;

public class OpenEndPlan implements CheckPlan {
	
	public static final int TIME_OUT_MIN = 1; 
	
	Checker checker; 
	DependencyGraph dGraph; 
	QueryRecord m_record; 

	public OpenEndPlan(Checker checker, DependencyGraph dGraph, QueryRecord record) {
		this.checker = checker; 
		this.dGraph = dGraph;
		m_record = record; 
	}
	
	@Override
	public int check() {
		Deque<Clique> topo = new LinkedList<Clique>(dGraph.getTopologicalOrder());
		Utility.logInfo("Entrances: " + dGraph.getEntrances().size() + " Exists: " + dGraph.getExits().size()); 
		Set<Clique> validated = new HashSet<Clique>(); 
		Set<Clique> falsified = new HashSet<Clique>(); 

		boolean flag = true;
		Clique clique; 
		Timer t = new Timer(); 
		

		AnswerTuple answerTuple; 
		while (!topo.isEmpty()) { 
			if (flag) {
				clique = topo.removeFirst(); 
				if (validated.contains(clique)) continue; 
				if (falsified.contains(clique)) { flag = false; continue; }
				Utility.logDebug("start checking front ... " + (answerTuple = clique.getRepresentative().getAnswerTuple())); 
				if (checker.check(answerTuple)) {
					Utility.logDebug(answerTuple.toString() + " is verified.");
					setMarkCascadely(clique, validated, dGraph.getOutGoingEdges()); 
				}
				else {
					falsified.add(clique);
					flag = false; 
				}
			}
			else {
				clique = topo.removeLast(); 
				if (falsified.contains(clique)) continue; 
				if (validated.contains(clique)) { flag = true; continue; }
				Utility.logDebug("start checking back ... " + (answerTuple = clique.getRepresentative().getAnswerTuple())); 
				if (!checker.check(answerTuple)) 
					setMarkCascadely(clique, falsified, dGraph.getInComingEdges()); 
				else {
					Utility.logDebug(answerTuple.toString() + " is verified.");
					validated.add(clique);
					flag = true; 
				}
			}
		}
		
//		Utility.logDebug("HermiT was called " + times + " times."); 
		
		int count = 0; 
		AnswerTuple ans; 
		Collection<AnswerTuple> validAnswers = new LinkedList<AnswerTuple>(); 
		for (Clique c: dGraph.getTopologicalOrder()) 
			if (validated.contains(c)) {
				count += c.getNodeTuples().size() + 1;
				validAnswers.add(c.getRepresentative().getAnswerTuple()); 
				
				for (NodeTuple nodeTuple: c.getNodeTuples()) {
					ans = nodeTuple.getAnswerTuple(); 
					validAnswers.add(ans);
					Utility.logDebug(ans + " is verified."); 
				}
			}
		
		m_record.addLowerBoundAnswers(validAnswers);
		m_record.addProcessingTime(Step.FullReasoning, t.duration());
		return count; 		
	}

	private void setMarkCascadely(Clique clique, Set<Clique> marked, Map<Clique, Collection<Clique>> edges) {
		marked.add(clique); 
		if (edges.containsKey(clique))
			for (Clique c: edges.get(clique))
				if (!marked.contains(c)) 
					setMarkCascadely(c, marked, edges);
	}

}
