package uk.ac.ox.cs.pagoda.approx;

import org.semanticweb.owlapi.model.OWLOntology;
import uk.ac.ox.cs.pagoda.constraints.BottomStrategy;

public interface KnowledgeBase {

	void load(OWLOntology ontology, BottomStrategy botStrategy); 

	void transform();
	
	void save();
	
	String getOutputPath();
	
	String getDirectory();
	
}
