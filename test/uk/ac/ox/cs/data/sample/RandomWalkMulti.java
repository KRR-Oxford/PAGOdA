package uk.ac.ox.cs.data.sample;

import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.Stack;

import org.openrdf.rio.RDFHandlerException;
import org.openrdf.rio.turtle.TurtleWriter;

import uk.ac.ox.cs.pagoda.util.Utility;


public class RandomWalkMulti extends RandomWalk {
	
	public RandomWalkMulti(RDFGraph graph, TurtleWriter writer) {
		super(graph, writer);
	}

	Queue<Integer> queue = new LinkedList<Integer>();

	@Override
	public void sample() throws RDFHandlerException {
		getStartNodes();

		Utility.logInfo(queue.size());
		
		int u, v, pick, index; 
		int individualLimit = statementLimit / queue.size(), currentLimit = 0; 
		RDFEdge edge; 
		List<RDFEdge> edges;
		Stack<Integer> stack = new Stack<Integer>();
		while (true) {
			if (noOfStatements >= statementLimit) {
				System.out.println("The number of statements in the sampling: " + noOfStatements); 
				return ;
			}
			if (noOfStatements >= currentLimit) {
				stack.clear();
			}
			
			if (stack.isEmpty()) {
				if (queue.isEmpty())
					v = rand.nextInt(m_graph.numberOfIndividuals); 
				else {
					v = queue.poll();
					currentLimit += individualLimit;
				}
				stack.add(v);
//				Utility.logInfo(noOfStart + " new start: " + m_graph.getRawString(v));
				visit(v);
			}
			u = stack.peek(); 
			if (rand.nextInt(100) < 15) {
				stack.pop(); 
				continue;
			}
			if ((edges = m_graph.edges.get(u)) == null || edges.size() == 0) {
				stack.clear();
				continue; 
			}
			
			index = 0;
			pick = rand.nextInt(edges.size()); 
			for (Iterator<RDFEdge> iter = edges.iterator(); iter.hasNext(); ++index) {
				edge = iter.next(); 
				if (index == pick) {
					stack.add(v = edge.m_dst);
					visit(v);
					m_writer.handleStatement(m_graph.getStatement(u, edge.m_label, edge.m_dst));
					++noOfStatements; 
					iter.remove(); 
				}
					
			}
		}
	}

	private void getStartNodes() throws RDFHandlerException {
		Set<Integer> coveredConcepts = new HashSet<Integer>();
		Integer concept;
		
		Iterator<Integer> iter; 
		for (Map.Entry<Integer, LinkedList<Integer>> entry: m_graph.labels.entrySet()) {
			iter = entry.getValue().iterator();
			concept = null; 
			
			while (iter.hasNext()) {
				if (!(coveredConcepts.contains(concept = iter.next()))) {
					break;
				}
				else concept = null; 

			}
			
			if (concept == null) continue;
			else {
				queue.add(entry.getKey());
				coveredConcepts.add(concept); 
				while (iter.hasNext()) 
					coveredConcepts.add(iter.next()); 
			}
		}

	}
	

}
