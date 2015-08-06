package uk.ac.ox.cs.pagoda.model;

import org.semanticweb.HermiT.Prefixes;
import org.semanticweb.HermiT.model.DLPredicate;
import org.semanticweb.HermiT.model.InterningManager;

/***
 * Represents a first-order unary predicate.
 */
public class UnaryPredicate implements DLPredicate {

    private final String iri;
    private static InterningManager<UnaryPredicate> s_interningManager=new InterningManager<UnaryPredicate>() {
        protected boolean equal(UnaryPredicate object1, UnaryPredicate object2) {
            return object1.iri.equals(object2.iri);
        }
        protected int getHashCode(UnaryPredicate object) {
            return object.iri.hashCode();
        }
    };

    public String getIri() {
        return iri;
    }

    private UnaryPredicate(String iri) {
        this.iri=iri;
    }

    public static UnaryPredicate create(String uri) {
        return s_interningManager.intern(new UnaryPredicate(uri));
    }

    @Override
    public int getArity() {
        return 1;
    }

    @Override
    public String toString(Prefixes prefixes) {
        return prefixes.abbreviateIRI(iri);
    }

    @Override
    public String toString() {
        return toString(Prefixes.STANDARD_PREFIXES);
    }
}
