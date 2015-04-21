package uk.ac.ox.cs.pagoda.endomorph;

import uk.ac.ox.cs.pagoda.summary.Node;
import uk.ac.ox.cs.pagoda.summary.NodeTuple;

public interface EndomorphChecker {

	void clearMappings();

	void setMapping(NodeTuple u, NodeTuple v);

	boolean check(Node next, Node next2);

}
