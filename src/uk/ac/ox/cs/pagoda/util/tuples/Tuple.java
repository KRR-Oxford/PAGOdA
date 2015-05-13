package uk.ac.ox.cs.pagoda.util.tuples;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Spliterator;
import java.util.function.Consumer;

public class Tuple<T> implements Iterable<T> {

    final ArrayList<T> elements = new ArrayList<>();

    Tuple() { }

    public Tuple(T... elements) {
        for(T t: elements) {
            this.elements.add(t);
        }
    }
    
    public Tuple(Iterable<T> iterable) {
        for (T t : iterable) {
            this.elements.add(t);
        }
    }

    public T get(int i) {
        return elements.get(i);
    }

    @Override
    public Iterator<T> iterator() {
        return elements.iterator();
    }

    @Override
    public void forEach(Consumer<? super T> action) {
        elements.forEach(action);
    }

    @Override
    public Spliterator<T> spliterator() {
        return elements.spliterator();
    }
}
