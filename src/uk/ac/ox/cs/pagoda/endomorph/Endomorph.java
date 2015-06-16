package uk.ac.ox.cs.pagoda.endomorph;

import org.semanticweb.owlapi.model.OWLOntology;
import uk.ac.ox.cs.pagoda.endomorph.plan.CheckPlan;
import uk.ac.ox.cs.pagoda.endomorph.plan.OpenEndPlan;
import uk.ac.ox.cs.pagoda.query.AnswerTuple;
import uk.ac.ox.cs.pagoda.query.AnswerTuples;
import uk.ac.ox.cs.pagoda.query.QueryRecord;
import uk.ac.ox.cs.pagoda.reasoner.full.Checker;
import uk.ac.ox.cs.pagoda.summary.Graph;
import uk.ac.ox.cs.pagoda.summary.NodeTuple;
import uk.ac.ox.cs.pagoda.util.Timer;
import uk.ac.ox.cs.pagoda.util.Utility;
import uk.ac.ox.cs.pagoda.util.disposable.DisposedException;

import java.util.Collection;
import java.util.LinkedList;

public class Endomorph extends Checker {
	
	Checker fullReasoner; 
	DependencyGraph dGraph; 
	Graph graph; 
	QueryRecord m_record; 

	/**
	 * constructor using HermiTChecker ...  
	 * @throws Exception 
	 */
	
	public Endomorph(QueryRecord record, Checker checker) {
		this.m_record = record; 
		fullReasoner = checker; 
		graph = new Graph(record.getRelevantOntology());
		dGraph = new DependencyGraph(graph); 
	}
	
	@Override
	public int check(AnswerTuples answerTuples) {
		if(isDisposed()) throw new DisposedException();

		Collection<NodeTuple> nodes = new LinkedList<NodeTuple>(); 
		int counter = 0; 
		
		for (; answerTuples.isValid(); answerTuples.moveNext()) {
			++counter; 
			nodes.add(graph.getNodeTuple(answerTuples.getTuple()));
		}
		answerTuples.dispose();

		Timer t = new Timer(); 
		dGraph.build(nodes);
//		dGraph.print();
		Utility.logDebug("@TIME to group individuals in the gap: " + t.duration());
		Utility.logInfo("The number of different groups: " + dGraph.cliques.size()); 
		
		Utility.logInfo("The number of individuals to be checked by Homomorphism checker: " + counter);
//		CheckPlan plan = new PlainPlan(this.checker, dGraph.cliques);
//		CheckPlan plan = new OpenEndMultiThreadPlan(this.checker, dGraph); 		
		CheckPlan plan = new OpenEndPlan(fullReasoner, dGraph, m_record); 		
		int answerCounter = plan.check(); 
		
		Utility.logDebug("The number of correct answers: " + answerCounter);
		return answerCounter; 
	}

	public OWLOntology getOntology() {
		if(isDisposed()) throw new DisposedException();

		return m_record.getRelevantOntology(); 
	}

	@Override
	public boolean check(AnswerTuple answerTuple) {
		if(isDisposed()) throw new DisposedException();

		return fullReasoner.check(answerTuple);
	}

	@Override
	public boolean isConsistent() {
		if(isDisposed()) throw new DisposedException();

		return fullReasoner.isConsistent();
	}

	@Override
	public int getNoOfCalls() {
		return fullReasoner.getNoOfCalls();
	}

	@Override
	public void dispose() {
		super.dispose();

		if(fullReasoner != null) {
//			Utility.logInfo("Hermit was called " + fullReasoner.getNoOfCalls() + " times");
			fullReasoner.dispose();
		}
	}
	
	public Graph getGraph() {
		if(isDisposed()) throw new DisposedException();

		return graph; 
	}
	
	public Checker getChecker() {
		if(isDisposed()) throw new DisposedException();

		return fullReasoner; 
	}

	public DependencyGraph getDependencyGraph() {
		if(isDisposed()) throw new DisposedException();

		return dGraph;
	}
	
}
