package uk.ac.ox.cs.pagoda.model;

import uk.ac.ox.cs.pagoda.util.Namespace;

public class IRI extends AnswerTerm {

	int id; 
	String iri; 
	
	private IRI(String iri, int id) {
		this.iri = iri; 
		this.id = id; 
	}
	
	public IRI create(String iri) {
		IRI instance = (IRI) instances.find(iri); 
		if (instance != null) return instance;
		instance = new IRI(iri, iri.startsWith(Namespace.PAGODA_ANONY) ? --SkolemCounter : ++OriginalCounter);
		instances.insert(iri, instance);
		return instance; 
	}

	@Override
	public String toString() {
		return "<" + iri + ">";
	}
	
}
