package uk.ac.ox.cs.hermit;

import org.semanticweb.HermiT.Configuration;
import org.semanticweb.HermiT.Reasoner;
import org.semanticweb.HermiT.model.Atom;
import org.semanticweb.HermiT.model.DLClause;
import org.semanticweb.HermiT.model.DLOntology;
import org.semanticweb.HermiT.structural.OWLClausification;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLNamedIndividual;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.reasoner.Node;

import uk.ac.ox.cs.pagoda.owl.OWLHelper;

public class HermitTester {

	public static void main(String[] args) {
		OWLOntology onto = OWLHelper.loadOntology("imported.owl");
		Reasoner hermit = new Reasoner(onto);
		OWLDataFactory f = onto.getOWLOntologyManager().getOWLDataFactory();
		OWLClass concept = f.getOWLClass(IRI.create("http://semantics.crl.ibm.com/univ-bench-dl.owl#Query12")); 
		
		for (OWLOntology o: onto.getImportsClosure()) {
			System.out.println(o.containsEntityInSignature(concept));
			for (OWLAxiom axiom: o.getAxioms())
				if (axiom.getClassesInSignature().contains(concept))
					System.out.println(axiom); 
		}
		
		for (Node<OWLNamedIndividual> node : hermit.getInstances(concept, false))
			for (OWLNamedIndividual i: node.getEntities()) {
				System.out.println(i.toStringID()); 
			}
	
//		clausifierTest(); 
	}

	@SuppressWarnings("unused")
	private static void clausifierTest() {
		OWLOntology onto = OWLHelper.loadOntology("/users/yzhou/ontologies/travel.owl");
		OWLClausification clausifier = new OWLClausification(new Configuration());
		DLOntology dlOntology = (DLOntology)clausifier.preprocessAndClausify(onto, null)[1];
		
		for (DLClause clause: dlOntology.getDLClauses())
			System.out.println(clause); 
		for (Atom atom : dlOntology.getPositiveFacts())
			System.out.println(atom); 

	}
	
}
