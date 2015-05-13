package uk.ac.ox.cs.pagoda.multistage;

import org.semanticweb.HermiT.model.Individual;

import java.util.ArrayList;

/**
 * Just a list of <tt>Individual</tt>s.
 * */
public class ViolationTuple extends ArrayList<Individual> {

    public ViolationTuple() {
        super();
    }

    public ViolationTuple(int size) {
        super(size);
    }
}
