package uk.ac.ox.cs.pagoda.endomorph;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import uk.ac.ox.cs.pagoda.summary.NodeTuple;

public class Clique {
	NodeTuple representative; 
	Set<NodeTuple> nodeTuples = null; 
	
	public Clique(NodeTuple u) {
		nodeTuples = new HashSet<NodeTuple>(); 
		representative = u;
	}

	public boolean addNodeTuple(NodeTuple nodeTuple) {
		return nodeTuples.add(nodeTuple); 
	}
	
	public NodeTuple getRepresentative() {
		return representative; 
	}
	
	@Override
	public String toString() {
		return representative.toString(); 
	}

	public Collection<NodeTuple> getNodeTuples() {
		return nodeTuples; 
	}
	
}

