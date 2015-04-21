package uk.ac.ox.cs.pagoda.endomorph;

import uk.ac.ox.cs.pagoda.summary.Edge;
import uk.ac.ox.cs.pagoda.summary.Graph;
import uk.ac.ox.cs.pagoda.summary.Node;
import uk.ac.ox.cs.pagoda.summary.NodeTuple;

public class EndomorphChecker1 implements EndomorphChecker {
	
	private Graph graph; 
	
	public EndomorphChecker1(Graph graph) {
		this.graph = graph;
	}
	
	public boolean check(Node u, Node v) {
		if (!u.isSubConceptOf(v)) return false; 
		if (!isSubsetOf(graph.getOutGoingEdges(u), graph.getOutGoingEdges(v), true)) return false; 
		if (!isSubsetOf(graph.getInComingEdges(u), graph.getInComingEdges(v), false)) return false; 
		return true; 
	}

	private boolean isSubsetOf(Edge[] e1, Edge[] e2, boolean out) {
		int j = 0; 
		for (int i = 0; i < e1.length; ++i) {
			while (j < e2.length && equals(e1[i], e2[j], out))
				++j; 
		}
		return j < e2.length;			
	}

	private boolean equals(Edge edge, Edge edge2, boolean out) {
		if (!edge.getLabel().equals(edge2.getLabel())) return false;   
		return out ? edge.getToNode().equals(edge2.getToNode()) : edge.getFromNode().equals(edge2.getFromNode()); 
	}

	@Override
	public void clearMappings() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setMapping(NodeTuple u, NodeTuple v) {
		// TODO Auto-generated method stub
		
	}

}
