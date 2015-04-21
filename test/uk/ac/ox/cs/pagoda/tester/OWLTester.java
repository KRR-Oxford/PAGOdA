package uk.ac.ox.cs.pagoda.tester;

import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;

import uk.ac.ox.cs.pagoda.owl.OWLHelper;

public class OWLTester {

	public static void main(String[] args) throws OWLOntologyCreationException {
//		OWLOntology onto = OWLHelper.loadOntology("dbpedia_imported.owl");
		OWLOntology onto = OWLHelper.loadOntology("reactome_imported.owl");
		OWLOntologyManager manager = onto.getOWLOntologyManager();
//		OWLOntology data = manager.loadOntology(IRI.create("file:/media/krr-nas-share/Yujiao/ontologies/bio2rdf/reactome/graph\ sampling/sample_1_new.ttl"));
//		System.out.println("data: " + data.getAxiomCount() + " " + data.getABoxAxioms(true).size()); 
		for (OWLOntology t: manager.getOntologies()) {
			System.out.println(t.getOntologyID());
			System.out.println(t.getAxiomCount() + " " + onto.getABoxAxioms(true).size()); 
		}
		System.out.println("In closure: " + onto.getImportsClosure().size()); 
		for (OWLOntology t: onto.getImportsClosure())
			System.out.println(t.getOntologyID());
		
		System.out.println(onto.getAxiomCount() + " " + onto.getABoxAxioms(true).size()); 
	}

}

