package uk.ac.ox.cs.pagoda.model;

public abstract class AnswerTerm {

	protected static Trie instances = new Trie(); 
	protected static int SkolemCounter = 0; 
	protected static int OriginalCounter = 0; 

	public abstract String toString();
}
