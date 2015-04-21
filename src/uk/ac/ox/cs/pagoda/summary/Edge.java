package uk.ac.ox.cs.pagoda.summary;

import java.util.Comparator;

public class Edge {
	
	Node from, to;
	String label; 
	
	public String getLabel() { return label; }
	public Node getFromNode() { return from; }
	public Node getToNode() {return to; }
	public String getFromNodeName() { return from.name; }
	public String getToNodeName() { return to.name; }
	
	public Edge(Node u, Node v, String stringID) {
		from = u; 
		to = v; 
		label = stringID; 
	}
	
	public String toString() {
		return label + "(\n\t" + from.name + ",\n\t" + to.name + ")"; 
	}
	
	public static int compareLabels(Edge[] list1, Edge[] list2) {
		int result = list1.length - list2.length; 
		if (result != 0) return result; 
		for (int i = 0; i < list1.length; ++i) {
			if ((result = list1[i].label.compareTo(list2[i].label)) != 0)
				return result; 
		}
		return 0; 
	}
	
	public Node getDestinationNode(boolean isOutGoingEdges) {
		return isOutGoingEdges ? to : from; 
	}

}

class EdgeComparatorByNodeName implements Comparator<Edge> {

	@Override
	public int compare(Edge o1, Edge o2) {
		int result = o1.label.compareTo(o2.label); 
		if (result != 0) return result; 
		result = o1.from.name.compareTo(o2.from.name); 
		if (result != 0) return result; 
		return o1.to.name.compareTo(o2.to.name);
	}
}

class EdgeComparatorByNodeLabel implements Comparator<Edge> {

	@Override
	public int compare(Edge o1, Edge o2) {
		int result = o1.label.compareTo(o2.label); 
		if (result != 0) return result; 
		result = o1.from.getLabel().compareTo(o2.from.getLabel()); 
		if (result != 0) return result; 
		result = o1.to.getLabel().compareTo(o2.to.getLabel());
		if (result != 0) return result; 
		result = o1.from.getName().compareTo(o2.from.getName()); 
		if (result != 0) return result; 
		result = o1.to.getName().compareTo(o2.to.getName());
		return result;  
	}
}

