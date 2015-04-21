package uk.ac.ox.cs.pagoda.summary;

import java.util.Collection;
import java.util.LinkedList;

import uk.ac.ox.cs.pagoda.query.AnswerTuple;

public class NodeTuple {

	AnswerTuple m_tuple; 
	Collection<Node> nodes = new LinkedList<Node>(); 

	public NodeTuple(AnswerTuple tuple) {
		m_tuple = tuple;
	}
	
	void addNode(Node node) {
		nodes.add(node); 
	}

	public Collection<Node> getNodes() {
		return nodes;
	}
	
	public AnswerTuple getAnswerTuple() {
		return m_tuple; 
	}
	
	
	public String toString() {
		StringBuilder sb = new StringBuilder("(");
		for (Node node: nodes) {
			if (sb.length() > 1) sb.append(", "); 
			sb.append(node.toString());
		}
		sb.append(")"); 
		return sb.toString();
	}
}
