package uk.ac.ox.cs.pagoda.model;

public class Literal extends AnswerTerm {

	int id; 
	String lexicalForm, datatype; 
	
	public Literal(String lexicalForm2, String datatype2, int i) {
		this.lexicalForm = lexicalForm2; 
		this.datatype = datatype2; 
		this.id = i; 
	}

	public static Literal create(String lexicalForm, String datatype) {
		String key = lexicalForm + "^^" + datatype; 
		Literal instance = (Literal) instances.find(key);
		if (instance != null) return instance;
		instance = new Literal(lexicalForm, datatype, ++OriginalCounter); 
		instances.insert(key, instance);
		return instance;
	}

	@Override
	public String toString() {
		return "\"" + lexicalForm + "\"^^" + datatype; 
	}
	
}
