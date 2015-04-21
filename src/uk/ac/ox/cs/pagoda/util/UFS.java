package uk.ac.ox.cs.pagoda.util;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class UFS<T> {
	
	private Map<T, T> groups = new HashMap<T, T>();

	public boolean merge(T t1, T t2) {
		t1 = find(t1); t2 = find(t2);
		if (t1.equals(t2)) return false;
		if (t2.toString().contains("cs.ox.ac.uk"))
			groups.put(t2, t1); 
		else 
			groups.put(t1, t2); 
		return true; 
	}
	
	public Set<T> keySet() {
		return groups.keySet(); 
	}

	public T find(T u) {
		T v, w = u;
		while ((v = groups.get(u)) != null) 
			u = v; 
		
		while ((v = groups.get(w)) != null) {
			groups.put(w, u);
			w = v; 
		}
		
		return u; 
	}
	
	public void clear() {
		groups.clear();
	}

	public boolean isEmpty() {
		return groups.isEmpty();
	}
}
