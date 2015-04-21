package uk.ac.ox.cs.pagoda.summary;

import java.util.Collection;
import java.util.Iterator;
import java.util.TreeSet;

public class Node {

	String name; 
	Collection<String> concepts = new TreeSet<String>();
	private String label; 
	
	public Node(String nodeName) {
		name = nodeName; 
	}
	
	public String getName() { return name; }
	
	public void addConcept(String className) {
		concepts.add(className);
		label = null; 
	}
	
	public String getLabel() {
		if (label == null) {
			StringBuilder sb = null; 
			for (Iterator<String> it = concepts.iterator(); it.hasNext(); ) {
				if (sb == null)	sb = new StringBuilder(); 
				else sb.append("^");
				sb.append(it.next()); 				
			}
			label = sb == null ? "" : sb.toString(); 
		}
		return label; 
	}
	
	//TODO to be removed (just used for debug) ... 
	String simplifiedLabel = null;
	
	public String toString() {
		if (simplifiedLabel == null)
			simplifiedLabel = getLabel(); 
		return name  + "@" + simplifiedLabel; 
	}

	public boolean isSubConceptOf(Node v) {
		String s, t = ""; 
		for (Iterator<String> uIter = concepts.iterator(), vIter = v.concepts.iterator(); uIter.hasNext(); ) {
			s = uIter.next();
			if (!vIter.hasNext()) return false; 
			while (vIter.hasNext() && !s.equals(t = vIter.next()));
			if (!s.equals(t)) return false; 
		}
		return true;
	}

	public Collection<String> getConcepts() {
		return concepts; 
	}

}




