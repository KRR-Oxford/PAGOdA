package uk.ac.ox.cs.pagoda.endomorph.plan;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.atomic.AtomicInteger;

import uk.ac.ox.cs.pagoda.endomorph.DependencyGraph;
import uk.ac.ox.cs.pagoda.endomorph.Clique;
import uk.ac.ox.cs.pagoda.query.AnswerTuple;
import uk.ac.ox.cs.pagoda.reasoner.full.Checker;
import uk.ac.ox.cs.pagoda.reasoner.full.HermitChecker;
import uk.ac.ox.cs.pagoda.util.Utility;

public class OpenEndMultiThreadPlan implements CheckPlan {
	
	Checker checker; 
	DependencyGraph dGraph; 

	public OpenEndMultiThreadPlan(Checker checker, DependencyGraph dGraph) {
		this.checker = checker; 
		this.dGraph = dGraph;
	}
	
//	Clique[] topo;
//	AtomicInteger open, end; 
	ConcurrentLinkedDeque<Clique> topo; 
	
	Set<Clique> validated, falsified; 
	
	@Override
	public int check() {
		Collection<Clique> cliques = dGraph.getTopologicalOrder();
//		topo = new LinkedBlockingDeque<Clique>(cliques);
		topo = new ConcurrentLinkedDeque<Clique>(cliques); 
		
//		topo = new Clique[cliques.size()];
//		int index = 0; 
//		for (Clique clique: cliques) topo[index++] = clique;
//		open = new AtomicInteger(); 
//		end = new AtomicInteger(cliques.size() - 1); 
			
//		validated = Collections.synchronizedSet(new HashSet<Clique>()); 
//		falsified = Collections.synchronizedSet(new HashSet<Clique>());
		validated = Collections.newSetFromMap(new ConcurrentHashMap<Clique, Boolean>()); 
		falsified = Collections.newSetFromMap(new ConcurrentHashMap<Clique, Boolean>()); 

		int numOfThreads = 10; 
		Collection<Thread> threads = new LinkedList<Thread>(); 
		for (int i = 0; i < numOfThreads; ++i) 
			threads.add(new Thread(new SubThread(new HermitChecker(checker), i)));
		
		for (Thread thread: threads) thread.start();
		
		for (Thread thread: threads) {
			try {
				thread.join();
			} catch (InterruptedException e) {
				e.printStackTrace();
			} 
		}
		
		Utility.logDebug("HermiT was called " + counter.get() + " times."); 
		
		int count = 0; 
		for (Clique c: dGraph.getTopologicalOrder()) {
			if (validated.contains(c))
				count += c.getNodeTuples().size() + 1; 
		}
		return count; 		
	}

	private void setMarkCascadely(Clique clique, Set<Clique> marked, Map<Clique, Collection<Clique>> edges) {
		marked.add(clique); 
		if (edges.containsKey(clique))
			for (Clique c: edges.get(clique))
				if (!marked.contains(c)) 
					setMarkCascadely(c, marked, edges);
	}
	
	AtomicInteger counter = new AtomicInteger(); 

	class SubThread implements Runnable {
		
		HermitChecker m_checker; 
		int m_ID;  

		public SubThread(HermitChecker checker, int ID) {
			m_checker = checker; 
			m_ID = ID; 
		}

		@Override
		public void run() {
			boolean flag = ((m_ID & 1) == 0); 
			Clique clique;
			AnswerTuple answerTuple; 
			while (!topo.isEmpty())
				if (flag) {
					clique = topo.removeFirst(); 
					if (validated.contains(clique)) continue; 
					if (falsified.contains(clique)) { flag = false; continue; }
					counter.incrementAndGet(); 
					Utility.logDebug("Thread " + m_ID + ": start checking front ... " + (answerTuple = clique.getRepresentative().getAnswerTuple())); 
					if (m_checker.check(answerTuple)) {
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
					counter.incrementAndGet(); 
					Utility.logDebug("Thread " + m_ID + ": start checking back ... " + (answerTuple = clique.getRepresentative().getAnswerTuple())); 
					if (!m_checker.check(answerTuple)) 
						setMarkCascadely(clique, falsified, dGraph.getInComingEdges()); 
					else {
						validated.add(clique);
						flag = true; 
					}
				}
			
			m_checker.dispose(); 
		}
		
	}
}

