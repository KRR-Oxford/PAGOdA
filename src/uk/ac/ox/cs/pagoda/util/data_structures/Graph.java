package uk.ac.ox.cs.pagoda.util.data_structures;

import java.util.*;

public class Graph<V> {

    private final boolean isDirected;

    private Map<V, Set<V>> outEdgesOf = new HashMap<>();
    public Graph(boolean isDirected) {
        this.isDirected = isDirected;
    }

    public Graph() {
        this(false);
    }
    public void addNode(V v) {
        if(!outEdgesOf.containsKey(v))
            outEdgesOf.put(v, new HashSet<V>());
    }

    public void addEdge(V v, V u) {
        addNode(v);
        addNode(u);
        outEdgesOf.get(v).add(u);

        if(isDirected)
            outEdgesOf.get(u).add(v);
    }

    public Iterator<V> getOutNeighbors(V v) {
        return outEdgesOf.get(v).iterator();
    }

    public boolean isDirected() {
        return isDirected;
    }
}
