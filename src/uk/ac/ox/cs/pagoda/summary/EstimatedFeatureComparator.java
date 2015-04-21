package uk.ac.ox.cs.pagoda.summary;

import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

public class EstimatedFeatureComparator implements Comparator<Node> {
	
	Graph graph; 
	Map<Node, EstimatedFeature> node2features = new HashMap<Node, EstimatedFeature>();
	
	public EstimatedFeatureComparator(Graph graph) {
		this.graph = graph;
		EstimatedFeature feature; 
		for (Node node: graph.getNodes()) {
			feature = new EstimatedFeature(graph, node); 
			node2features.put(node, feature);
		}
	}
	
	@Override
	public int compare(Node o1, Node o2) {
		EstimatedFeature f1 = node2features.get(o1), f2 = node2features.get(o2);
		int result;
		if ((result = o1.getLabel().compareTo(o2.getLabel())) != 0) return result; 
		if ((result = f1.outGoingNodeCount - f2.outGoingNodeCount) != 0) return result; 
		if ((result = f1.inComingNodeCount - f2.inComingNodeCount) != 0) return result; 
		if ((result = Edge.compareLabels(graph.getOutGoingEdges(o1), graph.getOutGoingEdges(o2))) != 0) return result; 
		result = Edge.compareLabels(graph.getInComingEdges(o1), graph.getInComingEdges(o2));
		return result; 
	}

}

class EstimatedFeature {
	
	int outGoingNodeCount, inComingNodeCount; 
	
	public EstimatedFeature(Graph graph, Node node) {
		HashSet<String> neighbours = new HashSet<String>(); 
		for (Edge edge: graph.getOutGoingEdges(node))
			neighbours.add(edge.getToNodeName());
		outGoingNodeCount = neighbours.size(); 
		
		neighbours.clear(); 
		for (Edge edge: graph.getInComingEdges(node)) 
			neighbours.add(edge.getFromNodeName());
		inComingNodeCount = neighbours.size(); 
	}
}


