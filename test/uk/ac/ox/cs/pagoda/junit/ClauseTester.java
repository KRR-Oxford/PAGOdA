package uk.ac.ox.cs.pagoda.junit;

import static org.junit.Assert.*;

import org.junit.Test;
import org.semanticweb.HermiT.model.Atom;
import org.semanticweb.HermiT.model.AtomicConcept;
import org.semanticweb.HermiT.model.AtomicRole;
import org.semanticweb.HermiT.model.DLClause;
import org.semanticweb.HermiT.model.Equality;
import org.semanticweb.HermiT.model.Variable;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyManager;

import uk.ac.ox.cs.pagoda.approx.Clause;
import uk.ac.ox.cs.pagoda.approx.Clausifier;

public class ClauseTester {

	@Test
	public void test_simple() {
		Variable x = Variable.create("X"), y1 = Variable.create("y1"), y2 = Variable.create("y2");
		AtomicConcept A = AtomicConcept.create("A"); 
		AtomicRole r = AtomicRole.create("r"); 
		Atom[] bodyAtoms = new Atom[] {
				Atom.create(A, x),
				Atom.create(r, x, y1),
				Atom.create(r, x, y2)
		}; 
		
		Atom[] headAtoms = new Atom[] {
				Atom.create(Equality.INSTANCE, y1, y2) 
		}; 
		
		OWLOntologyManager m = OWLManager.createOWLOntologyManager(); 
		OWLOntology emptyOntology = null; 
		try {
			emptyOntology = m.createOntology(); 
		} catch (Exception e) {
			e.printStackTrace();
			fail("failed to create a new ontology"); 
		}
		Clause c = new Clause(Clausifier.getInstance(emptyOntology), DLClause.create(headAtoms, bodyAtoms));
		System.out.println(c.toString()); 
	}
	
	@Test
	public void test_more() {
		Variable x = Variable.create("X"), y1 = Variable.create("y1"), y2 = Variable.create("y2"), y3 = Variable.create("y3");
		AtomicConcept A = AtomicConcept.create("A"); 
		AtomicRole r = AtomicRole.create("r"); 
		Atom[] bodyAtoms = new Atom[] {
				Atom.create(A, x),
				Atom.create(r, x, y1),
				Atom.create(r, x, y2), 
				Atom.create(r, x, y3), 
		}; 
		
		Atom[] headAtoms = new Atom[] {
				Atom.create(Equality.INSTANCE, y1, y2), 
				Atom.create(Equality.INSTANCE, y1, y3), 
				Atom.create(Equality.INSTANCE, y2, y3)
		}; 
		
		OWLOntologyManager m = OWLManager.createOWLOntologyManager(); 
		OWLOntology emptyOntology = null; 
		try {
			emptyOntology = m.createOntology(); 
		} catch (Exception e) {
			e.printStackTrace();
			fail("failed to create a new ontology"); 
		}
		Clause c = new Clause(Clausifier.getInstance(emptyOntology), DLClause.create(headAtoms, bodyAtoms));
		System.out.println(c.toString()); 
	}
	
	@Test
	public void test_inverse() {
		Variable x = Variable.create("X"), y1 = Variable.create("y1"), y2 = Variable.create("y2");
		AtomicConcept A = AtomicConcept.create("A"); 
		AtomicRole r = AtomicRole.create("r"); 
		Atom[] bodyAtoms = new Atom[] {
				Atom.create(A, x),
				Atom.create(r, y1, x),
				Atom.create(r, y2, x)
		}; 
		
		Atom[] headAtoms = new Atom[] {
				Atom.create(Equality.INSTANCE, y1, y2) 
		}; 
		
		OWLOntologyManager m = OWLManager.createOWLOntologyManager(); 
		OWLOntology emptyOntology = null; 
		try {
			emptyOntology = m.createOntology(); 
		} catch (Exception e) {
			e.printStackTrace();
			fail("failed to create a new ontology"); 
		}
		Clause c = new Clause(Clausifier.getInstance(emptyOntology), DLClause.create(headAtoms, bodyAtoms));
		System.out.println(c.toString()); 
	}
	
	@Test
	public void test_fillter() {
		Variable x = Variable.create("X"), y1 = Variable.create("y1"), y2 = Variable.create("y2");
		AtomicConcept A = AtomicConcept.create("A"); 
		AtomicConcept B = AtomicConcept.create("B"); 
		AtomicRole r = AtomicRole.create("r"); 
		Atom[] bodyAtoms = new Atom[] {
				Atom.create(A, x),
				Atom.create(r, y1, x),
				Atom.create(r, y2, x), 
				Atom.create(B, y1), 
				Atom.create(B, y2) 
		}; 
		
		Atom[] headAtoms = new Atom[] {
				Atom.create(Equality.INSTANCE, y1, y2) 
		}; 
		
		OWLOntologyManager m = OWLManager.createOWLOntologyManager(); 
		OWLOntology emptyOntology = null; 
		try {
			emptyOntology = m.createOntology(); 
		} catch (Exception e) {
			e.printStackTrace();
			fail("failed to create a new ontology"); 
		}
		Clause c = new Clause(Clausifier.getInstance(emptyOntology), DLClause.create(headAtoms, bodyAtoms));
		System.out.println(c.toString()); 
	}
	
	@Test
	public void test_negFillter() {
		Variable x = Variable.create("X"), y1 = Variable.create("y1"), y2 = Variable.create("y2");
		AtomicConcept A = AtomicConcept.create("A"); 
		AtomicConcept B = AtomicConcept.create("B"); 
		AtomicRole r = AtomicRole.create("r"); 
		Atom[] bodyAtoms = new Atom[] {
				Atom.create(A, x),
				Atom.create(r, y1, x),
				Atom.create(r, y2, x)
		}; 
		
		Atom[] headAtoms = new Atom[] {
				Atom.create(Equality.INSTANCE, y1, y2), 
				Atom.create(B, y1), 
				Atom.create(B, y2)  
		}; 
		
		OWLOntologyManager m = OWLManager.createOWLOntologyManager(); 
		OWLOntology emptyOntology = null; 
		try {
			emptyOntology = m.createOntology(); 
		} catch (Exception e) {
			e.printStackTrace();
			fail("failed to create a new ontology"); 
		}
		Clause c = new Clause(Clausifier.getInstance(emptyOntology), DLClause.create(headAtoms, bodyAtoms));
		System.out.println(c.toString()); 
	}
	
}
