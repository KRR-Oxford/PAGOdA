package uk.ac.ox.cs.pagoda.reasoner;

import uk.ac.ox.cs.pagoda.query.AnswerTuples;
import uk.ac.ox.cs.pagoda.util.disposable.Disposable;

import java.util.Collection;

public abstract class QueryEngine extends Disposable {

	public abstract void evaluate(Collection<String> queryTexts, String answerFile);

	public abstract AnswerTuples evaluate(String queryText);

	public abstract AnswerTuples evaluate(String queryText, String[] answerVariables);
}
