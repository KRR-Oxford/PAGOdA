package uk.ac.ox.cs.pagoda.multistage.treatement;

import org.semanticweb.HermiT.model.Atom;
import uk.ac.ox.cs.JRDFox.JRDFStoreException;
import uk.ac.ox.cs.pagoda.multistage.Violation;
import uk.ac.ox.cs.pagoda.util.disposable.Disposable;

import java.util.Set;

public abstract class Treatment extends Disposable {

    public abstract Set<AtomWithIDTriple> makeSatisfied(Violation violation) throws JRDFStoreException;

    public abstract void addAdditionalGapTuples();

    public class AtomWithIDTriple {

        private Atom atom;
        private int[] IDTriple;

        public AtomWithIDTriple(Atom atom, int[] IDTriple) {
            this.atom = atom;
            this.IDTriple = IDTriple;
        }

        public Atom getAtom() {
            return atom;
        }

        public int[] getIDTriple() {
            return IDTriple;
        }

    }
}
