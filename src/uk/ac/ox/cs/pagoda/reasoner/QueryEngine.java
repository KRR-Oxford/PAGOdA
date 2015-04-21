package uk.ac.ox.cs.pagoda.reasoner;

import java.util.Collection;

import uk.ac.ox.cs.pagoda.query.AnswerTuples;

public interface QueryEngine {

	public void evaluate(Collection<String> queryTexts, String answerFile);
	
	public AnswerTuples evaluate(String queryText);
	
	public AnswerTuples evaluate(String queryText, String[] answerVariables);
	
	public void dispose(); 
	
}
