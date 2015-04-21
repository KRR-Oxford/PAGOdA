package uk.ac.ox.cs.pagoda.constraints;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

public abstract class DependencyGraph<T> {
	
	protected abstract void build(); 

	protected Map<T, Set<T>> edges = new HashMap<T, Set<T>>();
	protected Map<T, Set<T>> reverseEdges = new HashMap<T, Set<T>>(); 
	
	public void addLink(T subEntity, T superEntity) {
		Set<T> dests = edges.get(subEntity);
		if (dests == null)  
			edges.put(subEntity, dests = new HashSet<T>());
		dests.add(superEntity);
		
		Set<T> srcs = reverseEdges.get(superEntity); 
		if (srcs == null) 
			reverseEdges.put(superEntity, srcs = new HashSet<T>()); 
		srcs.add(subEntity); 
	}
	
	public void output() {
		for (Map.Entry<T, Set<T>> pair: edges.entrySet()) {
			T src = pair.getKey(); 
			for (T dest: pair.getValue())
				System.out.println(src + " -> " + dest); 
		}
	}
	
	public int distance(Set<T> dsts, T src) {
		Set<T> visited = new HashSet<T>(); 
		if (dsts.contains(src)) return 0; 
		visited.add(src); 
		return distance(dsts, visited); 
	}
	
	public int distance(Set<T> dsts, T src1, T src2) {
		Set<T> visited = new HashSet<T>(); 
		if (dsts.contains(src1)) return 0; 
		if (dsts.contains(src2)) return 0; 
		visited.add(src1); 
		visited.add(src2); 
		return distance(dsts, visited); 
	}
	
	private int distance(Set<T> dsts, Set<T> visited) {
		Queue<Entry> queue = new LinkedList<Entry>();
		for (T src: visited)
			queue.add(new Entry(src, 0, visited));

		Entry entry;
		Set<T> edge; 
		while (!queue.isEmpty()) {
			entry = queue.poll(); 
			edge = edges.get(entry.m_entity); 
			if (edge != null)
				for (T next: edge) {
					if (dsts.contains(next)) return entry.m_dist + 1;
					
					if (!visited.contains(next)) 
						queue.add(new Entry(next, entry.m_dist + 1, visited));
				}
		}

		return Integer.MAX_VALUE;
	}
	
	public Set<T> getAncesters(T p) {
		return getDependency(p, false); 
	}
	
	public Set<T> getSuccessors(T p) {
		return getDependency(p, true); 
	}
	
	private Set<T> getDependency(T p, boolean succ) {
		return succ ? getDependency(p, edges) : getDependency(p, reverseEdges); 
	}
	
	private Set<T> getDependency(T p, Map<T, Set<T>> graph) {
		Set<T> visited = new HashSet<T>(); 
		Queue<T> queue = new LinkedList<T>();
		visited.add(p);
		queue.add(p);
		Set<T> edge;
		
		while (!queue.isEmpty()) {
			if ((edge = graph.get(queue.poll())) != null)
				for (T next: edge) 
					if (!visited.contains(next)) {
						queue.add(next); 
						visited.add(next); 
					}
		}
		
		return visited; 
	}
	
	private class Entry {
		
		T m_entity; 
		int m_dist; 
		
		public Entry(T entity, int distance, Set<T> v) {
			m_entity = entity; 
			m_dist = distance; 
			v.add(entity); 
		}
		
	}
	
}
