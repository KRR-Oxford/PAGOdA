package uk.ac.ox.cs.pagoda.query;

public interface AnswerTuples {
	
	public void reset(); 
	
	public boolean isValid();
	
	public int getArity();  
	
	public String[] getAnswerVariables(); 
	
	public void moveNext(); 
	
	public void dispose(); 
	
	public AnswerTuple getTuple(); 
	
 	public boolean contains(AnswerTuple t);
 	
 	public void remove(); 

}
