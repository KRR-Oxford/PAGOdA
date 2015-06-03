package uk.ac.ox.cs.pagoda.reasoner.full;

import uk.ac.ox.cs.pagoda.query.AnswerTuple;
import uk.ac.ox.cs.pagoda.query.AnswerTuples;
import uk.ac.ox.cs.pagoda.util.disposable.Disposable;

public abstract class Checker extends Disposable {

	public abstract int check(AnswerTuples answers);

	public abstract boolean check(AnswerTuple answer);

	public abstract boolean isConsistent();

}
