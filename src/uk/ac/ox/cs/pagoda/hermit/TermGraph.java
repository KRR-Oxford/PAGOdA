package uk.ac.ox.cs.pagoda.hermit;

import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Set;

import org.semanticweb.HermiT.model.AnnotatedEquality;
import org.semanticweb.HermiT.model.Atom;
import org.semanticweb.HermiT.model.AtomicConcept;
import org.semanticweb.HermiT.model.AtomicRole;
import org.semanticweb.HermiT.model.Constant;
import org.semanticweb.HermiT.model.DLClause;
import org.semanticweb.HermiT.model.DLPredicate;
import org.semanticweb.HermiT.model.DatatypeRestriction;
import org.semanticweb.HermiT.model.Equality;
import org.semanticweb.HermiT.model.Individual;
import org.semanticweb.HermiT.model.NodeIDLessEqualThan;
import org.semanticweb.HermiT.model.NodeIDsAscendingOrEqual;
import org.semanticweb.HermiT.model.Term;
import org.semanticweb.HermiT.model.Variable;

import uk.ac.ox.cs.pagoda.util.Namespace;
import uk.ac.ox.cs.pagoda.util.Utility;

public class TermGraph {
	
	Map<Term, Collection<Edge>> graph = new HashMap<Term, Collection<Edge>>(); 
	Map<Term, Collection<AtomicConcept>> term2concepts = new HashMap<Term, Collection<AtomicConcept>>();
	Set<Term> choosen = new HashSet<Term>(), allTerms = new HashSet<Term>(); 
	DLClause clause;
	Set<Atom> newBodyAtoms = new HashSet<Atom>(); 

	public TermGraph(DLClause clause) {
		this.clause = clause; 
		for (Atom atom: clause.getHeadAtoms())
			for (int i = 0; i < atom.getArity(); ++i)
				allTerms.add(atom.getArgument(i)); 
		for (Atom atom: clause.getBodyAtoms())
			for (int i = 0; i < atom.getArity(); ++i)
				allTerms.add(atom.getArgument(i)); 
	}
	
	public DLClause simplify() {
		constructGraph(); 
		chooseTerms(); 
		
		PriorityQueue<Term> queue = new PriorityQueue<Term>(allTerms.size(), new Comparator<Term>() {

			@Override
			public int compare(Term o1, Term o2) {
				int diff = 0; 
				@SuppressWarnings("rawtypes")
				Collection set1 = term2concepts.get(o1), set2 = term2concepts.get(o2);
				int num1 = size(set1), num2 = size(set2); 
				
				if ((diff = num2 - num1) != 0) 
					return diff;
				
				set1 = graph.get(o1); set2 = graph.get(o2); 
				num1 = size(set1); num2 = size(set2); 
				
				return num2 - num1; 
			}
			
			private int size(@SuppressWarnings("rawtypes") Collection c) {
				return c == null ? 0 : c.size();
			}
			
		});
		
		for (Term term: allTerms)
			queue.add(term);
		
		while (!queue.isEmpty()) {
			Term term = queue.remove();
			if (!choosen.contains(term) && isRepresentative(term)) 
				choosen.add(term);
		}

		boolean flag; 
		for (Iterator<Atom> iter = newBodyAtoms.iterator(); iter.hasNext(); ) {
			flag = true; 
			Atom atom = iter.next(); 
			for (int i = 0; i < atom.getArity(); ++i) {
				if (!choosen.contains(atom.getArgument(i))) {
					flag = false; 
					break; 
				}
			}
			if (!flag) iter.remove(); 
		}
		
		return DLClause.create(clause.getHeadAtoms(), newBodyAtoms.toArray(new Atom[0])); 
	}
	
	private boolean isRepresentative(Term term) {
		boolean flag = true; 
		for (Term otherTerm: choosen) 
			if (!term.equals(otherTerm) && isSubsumedBy(term, otherTerm)) {
				flag = false; 
			}
		return flag;
	}
	
	private boolean isSubsumedBy(Term t1, Term t2) {
		Collection<AtomicConcept> set1 = term2concepts.get(t1);
		Collection<AtomicConcept> set2 = term2concepts.get(t2);
		
		if (set1 != null)
			if (set2 == null)
				return false;
			else if (!set2.containsAll(set1))
				return false; 
		
		Collection<Edge> edgeSet1 = graph.get(t1);  
		Collection<Edge> edgeSet2 = graph.get(t2);
		if (edgeSet1 != null)
			if (edgeSet2 == null)
				return false; 
			else {
				if (!edgeSet2.containsAll(edgeSet1))
					return false;
//				else {
//					Collection<Edge> restEdge1 = new HashSet<Edge>(); 
//					for (Edge edge: edgeSet1)
//						if (edge.term == t2); 
//						else restEdge1.add(edge);
//					
//					Collection<Edge> restEdge2 = new HashSet<Edge>(); 
//					for (Edge edge: edgeSet2)
//						if (edge.term == t1); 
//						else restEdge2.add(edge);
//					
//					return restEdge2.containsAll(edgeSet1);
//				}
			}
		
		return true; 
	}

	private void chooseTerms() {
		Set<Variable> headVariables = new HashSet<Variable>(); 
		for (Atom atom: clause.getHeadAtoms()) {
			atom.getVariables(headVariables);
		}
		choosen.addAll(headVariables); 
		
		for (Term term: allTerms) 
			if (term instanceof Constant || term instanceof Individual)
				choosen.add(term);

		if (choosen.isEmpty()) choosen.add(Variable.create("X")); 
//		propagate();
	}

//	private void propagate() {
//		LinkedList<Term> queue = new LinkedList<Term>(choosen);
//		Term term; 
//		while (!queue.isEmpty()) {
//			term = queue.pop(); 
//			if (graph.containsKey(term))
//				for (Edge edge: graph.get(term)) 
//					if (!choosen.contains(edge.term)) {
//						choosen.add(edge.term); 
//						queue.add(term); 
//					}
//		}
//	}

	private void constructGraph() {
		DLPredicate p; 
		for (Atom bodyAtom: clause.getBodyAtoms()) {
			p = bodyAtom.getDLPredicate(); 
			if (p instanceof AtomicConcept) {
				newBodyAtoms.add(bodyAtom); 
				addConcept(bodyAtom.getArgument(0), (AtomicConcept) p);
			}
			else if (p instanceof AtomicRole) {
				newBodyAtoms.add(bodyAtom); 
				addEdges(bodyAtom.getArgument(0), bodyAtom.getArgument(1), (AtomicRole) p);
			}
			else if (p instanceof DatatypeRestriction) 
				newBodyAtoms.add(bodyAtom); 
			else if (p instanceof NodeIDLessEqualThan || p instanceof NodeIDsAscendingOrEqual);  
			else if (p instanceof Equality || p instanceof AnnotatedEquality) {
				newBodyAtoms.add(bodyAtom); 
				addEdges(bodyAtom.getArgument(0), bodyAtom.getArgument(1), AtomicRole.create(Namespace.EQUALITY)); 
			} else {
				Utility.logError("Unknown DLPredicate in TermGraph: " + p);
			}
		}
	}
	
	private void addEdges(Term t1, Term t2, AtomicRole p) {
		addEdge(t1, new Edge(p, t2, false));
		addEdge(t2, new Edge(p, t1, true)); 
	}

	private void addEdge(Term t, Edge edge) {
		Collection<Edge> edges; 
		if ((edges = graph.get(t)) == null) {
			graph.put(t, edges = new HashSet<Edge>()); 
		}
		edges.add(edge); 		
	}

	private void addConcept(Term t, AtomicConcept p) {
		Collection<AtomicConcept> concepts; 
		if ((concepts = term2concepts.get(t)) == null) {
			term2concepts.put(t, concepts = new HashSet<AtomicConcept>()); 
		}
		concepts.add(p); 		
	}

	public boolean mark(Term t) {
		return choosen.add(t);  
	}
	
}

class Edge {
	boolean isInverse; 
	AtomicRole role; 
	Term term; 
	
	public Edge(AtomicRole role, Term term, boolean isInverse) {
		this.role = role; 
		this.term = term; 
		this.isInverse = isInverse; 
	}
	
	@Override
	public int hashCode() {
		return role.hashCode() * 1997 + term.hashCode() * 2 + (isInverse ? 1 : 0); 
	}
	
	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof Edge)) return false; 
		Edge other = (Edge) obj; 
		return isInverse == other.isInverse && role.equals(other.role) && term.equals(other.term);  
	}
	
	@Override
	public String toString() {
		if (isInverse)
			return "inv_" + role.toString() + "_" + term.toString();
		return role.toString() + "_" + term.toString();
	}
	
}


