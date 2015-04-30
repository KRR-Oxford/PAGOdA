package uk.ac.ox.cs.data.datatype;

import java.io.File;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.model.OWLOntologyStorageException;
import uk.ac.ox.cs.pagoda.owl.OWLHelper;

public class DataPropertyEliminator {

	private static final String FLAG = "-minus-datatype"; 
	
	public static void main(String[] args) {
		// for NPD dataset 
//		args = "/home/yzhou/ontologies/npd/npd-all.owl".split("\\ ");
		
		args = "/home/yzhou/ontologies/dbpedia/integratedOntology-all-in-one.owl".split("\\ ");
		
		String file = args[0]; 
		String newFile = file.replace(".owl", FLAG + ".owl"); 
		
		OWLOntologyManager manager = OWLManager.createOWLOntologyManager(); 
		OWLOntology onto = OWLHelper.loadOntology(manager, file);
		OWLOntology newOntology; 

		try {
			if (onto.getOntologyID().getOntologyIRI() != null) {
				String iri = onto.getOntologyID().getOntologyIRI().toString(); 
				iri = iri.replace(".owl", FLAG + ".owl"); 
				newOntology = manager.createOntology(IRI.create(iri)); 
			}
			else newOntology = manager.createOntology(); 
			
			for (OWLOntology o: onto.getImportsClosure())
				for (OWLAxiom axiom: o.getAxioms()) {
					if (axiom.getDatatypesInSignature().isEmpty() && axiom.getDataPropertiesInSignature().isEmpty()) {
						manager.addAxiom(newOntology, axiom);
					}
				}
			
			manager.saveOntology(newOntology, IRI.create(new File(newFile)));
		}
		catch (OWLOntologyCreationException e) {
			e.printStackTrace();
		} catch (OWLOntologyStorageException e) {
			e.printStackTrace();
		}
			
	}
	
}