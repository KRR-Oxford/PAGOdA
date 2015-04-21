package uk.ac.ox.cs.pagoda.rules;

import java.util.Collection;

import org.semanticweb.HermiT.model.DLClause;
import org.semanticweb.owlapi.model.OWLOntology;

public interface IncrementalProgram {
	
	public OWLOntology getOntology(); 

	public void enrich(Collection<DLClause> delta); 
	
}
