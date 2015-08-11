package uk.ac.ox.cs.pagoda.model;


import org.semanticweb.HermiT.Prefixes;
import org.semanticweb.HermiT.model.DLPredicate;
import org.semanticweb.HermiT.model.InterningManager;

/***
 * Represents a first-order binary predicate.
 */
public class BinaryPredicate implements DLPredicate {

    public String getIRI() {
        return iri;
    }

    private final String iri;
    private static InterningManager<BinaryPredicate> s_interningManager=new InterningManager<BinaryPredicate>() {
        protected boolean equal(BinaryPredicate object1, BinaryPredicate object2) {
            return object1.iri.equals(object2.iri);
        }
        protected int getHashCode(BinaryPredicate object) {
            return object.iri.hashCode();
        }
    };

    private BinaryPredicate(String iri) {
        this.iri=iri;
    }

    public static BinaryPredicate create(String uri) {
        return s_interningManager.intern(new BinaryPredicate(uri));
    }

    @Override
    public int getArity() {
        return 2;
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
