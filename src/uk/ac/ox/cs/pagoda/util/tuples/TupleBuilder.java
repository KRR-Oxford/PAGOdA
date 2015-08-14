package uk.ac.ox.cs.pagoda.util.tuples;

import java.util.Collections;

/**
 * Allows to create an immutable <tt>Tuple</tt> in a non-atomic way.
 * It can create only one <tt>Tuple</tt>.
 * */
public class TupleBuilder<T> {

    private Tuple<T> tuple = new Tuple<T>();

    private boolean building = true;

    public TupleBuilder<T> append(T t) {
        if(building) {
            tuple.elements.add(t);
            return this;
        }
        return null;
    }

    public TupleBuilder<T> append(T[] t) {
        if(building) {
            Collections.addAll(tuple.elements, t);
            return this;
        }
        return null;
    }

    public Tuple<T> build() {
        if(building) {
            building = false;
            return tuple;
        }
        return null;
    }
}
