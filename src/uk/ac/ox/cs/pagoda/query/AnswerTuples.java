package uk.ac.ox.cs.pagoda.query;

import uk.ac.ox.cs.pagoda.util.disposable.Disposable;

public abstract class AnswerTuples extends Disposable {

    public abstract void reset();

    public abstract boolean isValid();

    public abstract int getArity();

    public abstract String[] getAnswerVariables();

    public abstract void moveNext();

    public abstract AnswerTuple getTuple();

    public abstract boolean contains(AnswerTuple t);

    public abstract void remove();
}
