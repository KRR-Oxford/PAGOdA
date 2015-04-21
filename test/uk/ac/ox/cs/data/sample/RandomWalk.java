package uk.ac.ox.cs.data.sample;

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.Stack;

import org.openrdf.rio.RDFHandlerException;
import org.openrdf.rio.turtle.TurtleWriter;

import uk.ac.ox.cs.pagoda.util.Utility;

public class RandomWalk extends Sampler {
	
	public RandomWalk(RDFGraph graph, TurtleWriter writer) {
		super(graph, writer);
	}

	protected Random rand = new Random(); 
	
	protected int noOfStatements = 0, statementLimit = 0;
	protected Set<Integer> visited = new HashSet<Integer>();
	
	@Override
	public void setLimit(int limit) {
		statementLimit = limit; 
	}
	
	@Override
	public void sample() throws RDFHandlerException {
		int u, v, pick, index; 
		RDFEdge edge; 
		List<RDFEdge> edges;
		Stack<Integer> stack = new Stack<Integer>();
		while (true) {
			if (noOfStatements >= statementLimit) {
				return ; 
			}
			if (stack.isEmpty()) {
				stack.add(v = rand.nextInt(m_graph.numberOfIndividuals));
				Utility.logInfo("A new start: " + m_graph.getRawString(v));
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

	protected void visit(int node) throws RDFHandlerException {
		if (visited.contains(node)) return ; 
		visited.add(node); 
		List<Integer> list = m_graph.labels.get(node);
		if (list == null) return ; 
		for (Iterator<Integer> iter = list.iterator(); iter.hasNext(); )  
			m_writer.handleStatement(m_graph.getStatement(node, iter.next()));
		noOfStatements += list.size(); 
	}

	@Override
	public void dispose() {
		visited.clear();
	}
	
	
}
