package uk.ac.ox.cs.pagoda.util.tuples;

/**
 * Allows to create an immutable <tt>Tuple</tt> in a non-atomic way.
 * It can create only one <tt>Tuple</tt>.
 * */
public class TupleBuilder<T> {

    private Tuple tuple = new Tuple();

    private boolean building = true;

    public boolean append(T t) {
        if(building) tuple.elements.add(t);
        return building;
    }

    public Tuple<T> build() {
        if(building) {
            building = false;
            return tuple;
        }
        return null;
    }
}
