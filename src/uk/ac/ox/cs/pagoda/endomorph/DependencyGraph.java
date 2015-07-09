 package uk.ac.ox.cs.pagoda.endomorph;

 import uk.ac.ox.cs.pagoda.summary.Edge;
 import uk.ac.ox.cs.pagoda.summary.Graph;
 import uk.ac.ox.cs.pagoda.summary.Node;
 import uk.ac.ox.cs.pagoda.summary.NodeTuple;
 import uk.ac.ox.cs.pagoda.util.Timer;
 import uk.ac.ox.cs.pagoda.util.Utility;

 import java.util.*;

public class DependencyGraph {

	Set<Clique> entrances = new HashSet<Clique>();

	public Set<Clique> getEntrances() {
		return entrances;
	}
	
	Set<Clique> exits = new HashSet<Clique>();  
	
	public Set<Clique> getExits() {
		return exits;
	}
	
	Map<Clique, Collection<Clique>> outGoingEdges = new HashMap<Clique, Collection<Clique>>();
	
	public Map<Clique, Collection<Clique>> getOutGoingEdges() {
		return outGoingEdges;
	}

	Map<Clique, Collection<Clique>> inComingEdges = new HashMap<Clique, Collection<Clique>>();
	
	public Map<Clique, Collection<Clique>> getInComingEdges() {
		return inComingEdges;
	}

	Graph graph; 
	EndomorphChecker homomorphismChecker;
	Set<Clique> cliques = new HashSet<Clique>(); 
	
	public DependencyGraph(Graph graph) {
		this.graph = graph; 
		homomorphismChecker = new EndomorphChecker2(graph);
//		homomorphismChecker = new EndomorphChecker1(graph);
	}
	
	private int compareNodeTuple(NodeTuple t1, NodeTuple t2) {
		Iterator<Node> iter1 = t1.getNodes().iterator(); 
		Iterator<Node> iter2 = t2.getNodes().iterator();
		int ret; 
		int index = 0;
		Node u, v; 
		while (iter1.hasNext()) {
			u = iter1.next(); v = iter2.next();
			if (u == null && v == null) {
				if ((ret = t1.getAnswerTuple().getGroundTerm(index).toString().compareTo(t2.getAnswerTuple().getGroundTerm(index).toString())) != 0)
					return ret;
			}
			else if (u != null && v != null) { 
				if ((ret = compareNode(u, v)) != 0) return ret;
			}
			else if (u == null)
				return 1; 
			else if (v == null)
				return -1; 
				
		}
		
		return 0; 
	}
	
	private int compareNode(Node o1, Node o2) {
		int result = o1.getConcepts().size() - o2.getConcepts().size();
		if (result != 0) return dir(result);
		Edge[] edge1 = graph.getOutGoingEdges(o1), edge2 = graph.getOutGoingEdges(o2);
		int len1 = edge1 == null ? 0 : edge1.length, len2 = edge2 == null ? 0 : edge2.length; 
		result = len1 - len2; 
		if (result != 0) return dir(result);
		
		edge1 = graph.getInComingEdges(o1); edge2 = graph.getInComingEdges(o2); 
		len1 = edge1 == null ? 0 : edge1.length; len2 = edge2 == null ? 0 : edge2.length; 
		result = len1 - len2;
		if (result != 0) return dir(result);
		
		else return o1.getLabel().compareTo(o2.getLabel()); 
	}
	
	private int dir(int flag) { return - flag; }
	
	public void build(Collection<NodeTuple> nodeTuples) {
		
		if (nodeTuples.isEmpty()) return ;
		
		Timer t = new Timer(); 
		
		NodeTuple[] nodeTupleArray = new NodeTuple[nodeTuples.size()]; 
		nodeTuples.toArray(nodeTupleArray); 
		
		Arrays.sort(nodeTupleArray, new Comparator<NodeTuple>() {
			@Override
			public int compare(NodeTuple t1, NodeTuple t2) {
				return compareNodeTuple(t1, t2); 
			}
		});
		
		Utility.logDebug(nodeTupleArray[0].toString(), 
				nodeTupleArray[nodeTupleArray.length - 1].toString()); 
		
		for (NodeTuple nodeTuple: nodeTupleArray) addNodeTuple(nodeTuple);
		
		Utility.logDebug("The number of times to call homomorphism checker: " + homomorphismCheckCounter + "(" + outGoingCheckCounter + "," + inComingCheckCounter + ").", 
				"Time to compute endomorphism relation: " + t.duration());
		
//		print(); 
		
		topologicalOrder = null;
		Utility.logDebug("link: " + link);
	}
	
	LinkedList<Clique> topologicalOrder = null;

	public LinkedList<Clique> getTopologicalOrder() {
		if (topologicalOrder != null) return topologicalOrder;
		
		topologicalOrder = new LinkedList<Clique>();
		Queue<Clique> toVisit = new LinkedList<Clique>(entrances); 
		Map<Clique, Integer> toVisitedInComingDegree = new HashMap<Clique, Integer>();
		
		int count; 
		while (!toVisit.isEmpty()) {
			Clique cu = toVisit.remove();
			topologicalOrder.add(cu);
			if (outGoingEdges.containsKey(cu))
				for (Clique cv: outGoingEdges.get(cu)) {
					if (toVisitedInComingDegree.containsKey(cv)) {
						count = toVisitedInComingDegree.get(cv) - 1; 
						toVisitedInComingDegree.put(cv, count); 
					}
					else 
						toVisitedInComingDegree.put(cv, count = inComingEdges.get(cv).size() - 1); 
					if (count == 0) 
						toVisit.add(cv); 
				}
		}
		
		return topologicalOrder;
	}

	private void addNodeTuple(NodeTuple u) {
		Queue<Clique> toCompare = new LinkedList<Clique>(entrances);
		
		Map<Clique, Integer> toVisitedDegree = new HashMap<Clique, Integer>();
		Collection<Clique> directSuccessors = new LinkedList<Clique>(); 
		
		int count; 
		while (!toCompare.isEmpty()) {
			Clique cv = toCompare.remove();
			++outGoingCheckCounter; 
			if (checkHomomorphism(u, cv.representative)) {
				directSuccessors.add(cv); 
			}
			else {
				if (outGoingEdges.containsKey(cv))
					for (Clique c: outGoingEdges.get(cv)) {
						if (toVisitedDegree.containsKey(c)) {
							count = toVisitedDegree.get(c) - 1; 
							toVisitedDegree.put(c, count); 
						}
						else 
							toVisitedDegree.put(c, count = inComingEdges.get(c).size() - 1);
						
						if (count == 0)	
							toCompare.add(c);
					}
			}
		}
		
		if (directSuccessors.size() == 1) {
			Clique clique = directSuccessors.iterator().next();
			if (checkHomomorphism(clique.representative, u)) {
				clique.addNodeTuple(u);
				return ;
			}
		}
		
		Clique cu = new Clique(u);
		cliques.add(cu); 
		
		toCompare.clear();
		Set<Clique> visited = new HashSet<Clique>();  
		
		if (directSuccessors.size() == 0) 
			toCompare.addAll(exits); 
		else {
			for (Clique cv: directSuccessors)
				if (inComingEdges.containsKey(cv))
					for (Clique cw: inComingEdges.get(cv)) {
						visited.add(cw); 
						toCompare.add(cw);
					}
		}
		
		
		
		Collection<Clique> directPredecessors = new LinkedList<Clique>(); 
			
		while (!toCompare.isEmpty()) {
			Clique cv = toCompare.remove();
			++inComingCheckCounter; 
			if (checkHomomorphism(cv.representative, u)) 
				directPredecessors.add(cv); 
			else {
				if (inComingEdges.containsKey(cv))
					for (Clique c: inComingEdges.get(cv)) 
						if (!visited.contains(c)) {
							visited.add(c); 
							toCompare.add(c);
						}
			}
		}
		
		for (Clique cv: directSuccessors) {
			if (entrances.contains(cv)) entrances.remove(cv); 
			link(cu, cv); 
		}
		
		for (Clique cw: directPredecessors) {
			if (exits.contains(cw)) exits.remove(cw); 
			link(cw, cu);
		}
		
		if (directPredecessors.size() == 0) entrances.add(cu); 
		if (directSuccessors.size() == 0) exits.add(cu); 
	}
	
	private int homomorphismCheckCounter = 0; 
	private int outGoingCheckCounter = 0, inComingCheckCounter = 0; 
	
	private boolean checkHomomorphism(NodeTuple u, NodeTuple v) {
		++homomorphismCheckCounter; 
		homomorphismChecker.setMapping(u, v);

		// TODO recently added, test it
		if(!homomorphismChecker.isMappingTo(u, v))
			return false;

		try {
			Node node1, node2; 
			for (Iterator<Node> iter1 = u.getNodes().iterator(), iter2 = v.getNodes().iterator(); iter1.hasNext(); ) {
				node1 = iter1.next(); node2 = iter2.next(); 
				if (node1 != node2 && !homomorphismChecker.check(node1, node2)) {
//					System.out.println(homomorphismChecker.check(node1, node2)); 
					return false; 
				}
			}
			return true;
		} finally {
			homomorphismChecker.clearMappings();
		}
	}

	private void link(Clique u, Clique v) {
		++link; 
		addToList(outGoingEdges, u, v); 
		addToList(inComingEdges, v, u); 
	}
	
	private int link = 0; 

	private void addToList(Map<Clique, Collection<Clique>> map, Clique u, Clique v) {
		Collection<Clique> temp; 
		if ((temp = map.get(u)) == null) {
			temp = new LinkedList<Clique>(); 
			map.put(u, temp); 
		}
		temp.add(v); 
	}
	
	public void print() {
		System.out.println("---------- Dependency Graph -------------"); 
		for (Clique u: cliques)
			if (outGoingEdges.containsKey(u))
				for (Clique v: outGoingEdges.get(u)) 
					System.out.println(u + " -> " + v);
		System.out.println("-----------------------------------------"); 
	}
	

}

