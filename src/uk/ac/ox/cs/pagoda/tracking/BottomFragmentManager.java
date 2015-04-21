package uk.ac.ox.cs.pagoda.tracking;

import java.util.HashSet;
import java.util.Set;

import org.semanticweb.HermiT.model.AtomicConcept;
import org.semanticweb.HermiT.model.AtomicRole;
import org.semanticweb.HermiT.model.DLClause;
import org.semanticweb.HermiT.model.DLPredicate;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassAssertionAxiom;
import org.semanticweb.owlapi.model.OWLDataProperty;
import org.semanticweb.owlapi.model.OWLDataPropertyAssertionAxiom;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.model.OWLObjectPropertyAssertionAxiom;

import uk.ac.ox.cs.pagoda.constraints.PredicateDependency;
import uk.ac.ox.cs.pagoda.query.QueryRecord;
import uk.ac.ox.cs.pagoda.rules.ApproxProgram;
import uk.ac.ox.cs.pagoda.rules.GeneralProgram;
import uk.ac.ox.cs.pagoda.util.Utility;

public class BottomFragmentManager {
	
	QueryRecord m_record;
	GeneralProgram m_program; 
	PredicateDependency m_graph;  

	public BottomFragmentManager(QueryRecord record) {
		m_record = record;
		m_program = new GeneralProgram(record.getRelevantClauses(), record.getRelevantOntology());
		m_graph = m_program.buildDependencyGraph();
	}
	
	public Set<DLClause> relevantClauses(Set<DLClause> disjuntiveRules) {
		Set<DLClause> relevant = new HashSet<DLClause>(); 
		Set<DLClause> now = new HashSet<DLClause>();
		Set<DLClause> last = new HashSet<DLClause>();
		
		for (DLClause rule: disjuntiveRules) 
			for (DLPredicate p: m_graph.collectPredicate(rule.getHeadAtoms()))
				now.addAll(m_graph.pathToBottom(p)); 

		while (!relevant.containsAll(now)) {
			relevant.addAll(now); 
			last.clear();
			last = now; 
			now = new HashSet<DLClause>();
			
			for (DLClause rule: last) {
				for (DLPredicate p: m_graph.collectPredicate(rule.getHeadAtoms()))
					now.addAll(m_graph.pathToBottom(p)); 
				for (DLPredicate p: m_graph.collectPredicate(rule.getBodyAtoms()))
					now.addAll(m_graph.pathTo(p)); 
			}
		}
		
		Utility.logDebug("There are " + relevant.size() + " clauses in the bottom fragment related to this query."); 
		return relevant; 
	}
	
	public Set<OWLAxiom> relevantOntology(Set<DLClause> clauses, ApproxProgram upperProgram) {
		Set<OWLAxiom> axioms = new HashSet<OWLAxiom>(); 
		Set<DLPredicate> predicates = new HashSet<DLPredicate>();
		for (DLClause clause: clauses) {
			OWLAxiom axiom = upperProgram.getEquivalentAxiom(clause); 
			axioms.add(axiom);
			predicates.addAll(m_graph.collectPredicate(clause.getHeadAtoms())); 
			predicates.addAll(m_graph.collectPredicate(clause.getBodyAtoms()));
		}
		
		int tboxCounter = axioms.size(); 
		Utility.logDebug("There are " + tboxCounter + " TBox axioms in the bottom fragment related to this query."); 
		String name; 
		for (OWLAxiom axiom: m_record.getRelevantOntology().getABoxAxioms(true)) {
			if (axiom instanceof OWLClassAssertionAxiom) {
				OWLClass cls = (OWLClass) ((OWLClassAssertionAxiom) axiom).getClassExpression();
				name = cls.getIRI().toString();
				if (predicates.contains(AtomicConcept.create(name)))
					axioms.add(axiom); 
			}
			else if (axiom instanceof OWLObjectPropertyAssertionAxiom) {
				OWLObjectProperty prop = (OWLObjectProperty) ((OWLObjectPropertyAssertionAxiom) axiom).getProperty(); 
				name = prop.getIRI().toString(); 
				if (predicates.contains(AtomicRole.create(name)))
					axioms.add(axiom); 
			}
			else if (axiom instanceof OWLDataPropertyAssertionAxiom) {
				OWLDataProperty prop = (OWLDataProperty) ((OWLDataPropertyAssertionAxiom) axiom).getProperty(); 
				name = prop.getIRI().toString();
				if (predicates.contains(AtomicRole.create(name)))
					axioms.add(axiom);
			}
		}
		
		Utility.logDebug("There are " + (axioms.size() - tboxCounter) + " ABox axioms in the bottom fragment related to this query."); 
		return axioms; 
	}
}
