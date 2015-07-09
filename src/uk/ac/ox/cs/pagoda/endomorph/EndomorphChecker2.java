package uk.ac.ox.cs.pagoda.endomorph;

import uk.ac.ox.cs.pagoda.summary.Edge;
import uk.ac.ox.cs.pagoda.summary.Graph;
import uk.ac.ox.cs.pagoda.summary.Node;
import uk.ac.ox.cs.pagoda.summary.NodeTuple;
import uk.ac.ox.cs.pagoda.util.Timer;

import java.util.*;

public class EndomorphChecker2 implements EndomorphChecker {
	
	private Graph graph; 
//	DependencyGraph dGraph; 
	
	public EndomorphChecker2(Graph graph) {
		this.graph = graph;
	}
	
	private Timer timer = new Timer();
	private boolean time_out = false; 
	private static final int TIME_OUT = 60;
//	private static final int TIME_OUT = 99999999;

	public boolean check(NodeTuple u, NodeTuple v) {
		int length = u.getNodes().size(); 
		Edge[][] ss = new Edge[1][length], tt = new Edge[1][length];
		int index = 0;
		Iterator<Node> iter1 = u.getNodes().iterator(); 
		Iterator<Node> iter2 = v.getNodes().iterator(); 
		for (; iter1.hasNext(); ) {
			ss[0][index] = new Edge(null, iter1.next(), "auxilary" + index);
			tt[0][index++] = new Edge(null, iter2.next(), "auxilary" + index); 
		}
		return checkSortedEdges(ss, tt, 0, 0); 
	}
	
	public boolean check(Node u, Node v) {
		if (!u.isSubConceptOf(v)) return false;
        return checkSortedEdges(new Edge[][]{graph.getOutGoingEdges(u), graph.getInComingEdges(u)},
                new Edge[][]{graph.getOutGoingEdges(v), graph.getInComingEdges(v)}, 0, 0);
    }

    /***
     * Checks whether the found mapping is actually a mapping from tuple u to tuple v.
     *
     * @param u
     * @param v
     * @return
     */
    @Override
    public boolean isMappingTo(NodeTuple u, NodeTuple v) {
        Iterator<Node> uIterator = u.getNodes().iterator();
        Iterator<Node> vIterator = v.getNodes().iterator();

        while(uIterator.hasNext() && vIterator.hasNext()) {
            Node uNode = uIterator.next();
            Node vNode = vIterator.next();
            if(mappings.containsKey(uNode) && !mappings.get(uNode).equals(vNode))
                return false;
            else if(!mappings.containsKey(uNode) && !uNode.equals(vNode))
                return false;
        }
        return !uIterator.hasNext() && !vIterator.hasNext();
    }

    Map<Node, Node> mappings = new HashMap<Node, Node>();

	public void clearMappings() {
		mappings.clear();
		timer.pause();
	}
	
	public void setMapping(NodeTuple key, NodeTuple value) {
		timer.resume(); 
		// FIXME
		timer.setTimeout(TIME_OUT);
//		time_out = false;
		for (Iterator<Node> key_iter = key.getNodes().iterator(), value_iter = value.getNodes().iterator(); key_iter.hasNext(); ) {
			mappings.put(key_iter.next(), value_iter.next());
		}
	}
	
	private boolean checkSortedEdges(Edge[][] ss, Edge[][] st, int dim, int index) {
		if (time_out || timer.timeOut()) {
			time_out = true; 
			return false; 
		}
		
		while (ss[dim] == null || index >= ss[dim].length)
			if (++dim >= ss.length) return true;
			else index = 0;
		Edge[] s = ss[dim], t = st[dim];

		if (t.length > 10) return false; 
		
		Node u = s[index].getDestinationNode(dim == 0), w;
		Set<Node> candidates = new HashSet<Node>(); 
		
		for (int j = findFirstEdge(t, s[index].getLabel()); j < t.length; ++j) {
			if (j == -1) break;
			if (s[index].getLabel().equals(t[j].getLabel())) { 
				w = t[j].getDestinationNode(dim == 0); 
				candidates.add(w); 
			}
			else break; 
		}

		if ((w = mappings.get(u)) != null) 
			if (candidates.contains(w))
				return checkSortedEdges(ss, st, dim, index + 1); 
			else return false; 
		
		if (candidates.contains(u)) {
			mappings.put(u, u); 
			if (checkSortedEdges(ss, st, dim, index + 1))
				return true; 
			mappings.remove(u);
			return false; 
		}

        for (Node v: candidates) {
			mappings.put(u, v); 
			if (check(u, v) && checkSortedEdges(ss, st, dim, index + 1))
				return true; 
			mappings.remove(u); 
		}
			
		return false;
	}

	private int findFirstEdge(Edge[] edges, String label) {
		int left = 0, right = edges.length - 1, mid; 
		while (left < right) {
			mid = left + (right - left) / 2; 
			if (edges[mid].getLabel().compareTo(label) < 0) left = mid + 1; 
			else right = mid; 
		}
		return right; 
	}
	
}
