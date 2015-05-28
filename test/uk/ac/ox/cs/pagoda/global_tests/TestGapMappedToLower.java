package uk.ac.ox.cs.pagoda.global_tests;

import junit.framework.Assert;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.*;
import org.testng.annotations.Test;
import uk.ac.ox.cs.pagoda.query.AnswerTuple;
import uk.ac.ox.cs.pagoda.query.AnswerTuples;
import uk.ac.ox.cs.pagoda.reasoner.QueryReasoner;
import uk.ac.ox.cs.pagoda.util.Namespace;

public class TestGapMappedToLower {

	public static final String ns = "http://example.org/test#%s";
	
	public IRI getEntityIRI(String name) {
		return IRI.create(String.format(ns, name)); 
	}
	
	@Test
	public void test() throws OWLOntologyCreationException {
		OWLOntologyManager manager = OWLManager.createOWLOntologyManager(); 
		OWLDataFactory factory = manager.getOWLDataFactory(); 
		OWLOntology ontology = manager.createOntology(); 
		OWLClass A = factory.getOWLClass(getEntityIRI("A"));
		manager.addAxiom(ontology, factory.getOWLDeclarationAxiom(A)); 
		OWLClass B = factory.getOWLClass(getEntityIRI("B"));
		manager.addAxiom(ontology, factory.getOWLDeclarationAxiom(B)); 
		OWLClass C = factory.getOWLClass(getEntityIRI("C"));
		manager.addAxiom(ontology, factory.getOWLDeclarationAxiom(C)); 
		OWLClass A1 = factory.getOWLClass(getEntityIRI("A1"));
		manager.addAxiom(ontology, factory.getOWLDeclarationAxiom(A1)); 
		OWLClass A2 = factory.getOWLClass(getEntityIRI("A2"));
		manager.addAxiom(ontology, factory.getOWLDeclarationAxiom(A2)); 
		OWLNamedIndividual a = factory.getOWLNamedIndividual(getEntityIRI("a")); 
		OWLNamedIndividual b = factory.getOWLNamedIndividual(getEntityIRI("b"));
		OWLNamedIndividual c = factory.getOWLNamedIndividual(getEntityIRI("c")); 
		OWLObjectProperty r = factory.getOWLObjectProperty(IRI.create(String.format(ns, "r")));
		manager.addAxiom(ontology, factory.getOWLDeclarationAxiom(r));

		manager.addAxiom(ontology, factory.getOWLClassAssertionAxiom(A, a));	// A(a)
		manager.addAxiom(ontology, factory.getOWLClassAssertionAxiom(B, b));	// B(b)
		manager.addAxiom(ontology, factory.getOWLClassAssertionAxiom(C, c));	// C(c)
		manager.addAxiom(ontology, factory.getOWLObjectPropertyAssertionAxiom(r, a, b));	// r(a,b)
		manager.addAxiom(ontology, factory.getOWLObjectPropertyAssertionAxiom(r, a, c));	// r(a,c)
		manager.addAxiom(ontology, factory.getOWLSubClassOfAxiom(A, factory.getOWLObjectUnionOf(A1, A2)));	// A \sqsubseteq A1 \sqcup A2
		manager.addAxiom(ontology, factory.getOWLSubClassOfAxiom(A1, factory.getOWLObjectMaxCardinality(1, r)));	// A1 \sqsubseteq \leq 1 r.\top 
		manager.addAxiom(ontology, factory.getOWLSubClassOfAxiom(A2, factory.getOWLObjectMaxCardinality(1, r)));	// A2 \sqsubseteq \leq 1 r.\top 

		QueryReasoner pagoda = QueryReasoner.getInstance(ontology); 
		pagoda.loadOntology(ontology);
		if (pagoda.preprocess()) {
			String sparql = "select ?x where { "
					+ "?x <" + r.toStringID() + "> ?y . "
					+ "?y " + Namespace.RDF_TYPE_QUOTED + " <" + B.toStringID() + "> . "
					+ "?y " + Namespace.RDF_TYPE_QUOTED + " <" + C.toStringID() + "> . } ";
			AnswerTuples rs = pagoda.evaluate(sparql);
			int count = 0; 
			for (AnswerTuple ans; rs.isValid(); rs.moveNext()) {
				ans = rs.getTuple(); 
				System.out.println(ans.getGroundTerm(0)); 
				++count; 
			}
			Assert.assertEquals(1, count);
		}
	}

}
