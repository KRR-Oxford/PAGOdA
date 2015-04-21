package org.semanticweb.karma2.model;

import java.io.Serializable;

import org.semanticweb.HermiT.Prefixes;
import org.semanticweb.HermiT.model.DLPredicate;

/**
 * Represents the equality predicate.
 */
public class Equality implements DLPredicate,Serializable {
    private static final long serialVersionUID=8308051741088513244L;

    public static final Equality INSTANCE=new Equality();
    
    protected Equality () {
    }
    public int getArity() {
        return 2;
    }
    public String toString(Prefixes prefixes) {
        return "<http://www.w3.org/2002/07/owl#sameas>";
    }
    public String toOrderedString(Prefixes prefixes) {
        return toString(prefixes);
    }
    public String toString() {
        return toString(Prefixes.STANDARD_PREFIXES);
    }
    protected Object readResolve() {
        return INSTANCE;
    }
    public static Equality create() {
        return INSTANCE;
    }
}
