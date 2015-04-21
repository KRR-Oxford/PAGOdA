package uk.ac.ox.cs.pagoda.reasoner.full;

import uk.ac.ox.cs.pagoda.query.AnswerTuple;
import uk.ac.ox.cs.pagoda.query.AnswerTuples;

public interface Checker {

	public int check(AnswerTuples answers);
	
	public boolean check(AnswerTuple answer);

	public boolean isConsistent();

	public void dispose(); 
}
