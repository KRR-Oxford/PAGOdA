package uk.ac.ox.cs.pagoda.multistage.treatement;

import uk.ac.ox.cs.JRDFox.JRDFStoreException;
import uk.ac.ox.cs.pagoda.multistage.Violation;
import uk.ac.ox.cs.pagoda.util.disposable.Disposable;

public abstract class Treatment extends Disposable {

    public abstract boolean makeSatisfied(Violation violation) throws JRDFStoreException;

    public abstract void addAdditionalGapTuples();
}
