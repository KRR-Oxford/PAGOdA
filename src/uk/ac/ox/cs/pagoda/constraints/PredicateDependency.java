package uk.ac.ox.cs.pagoda.constraints;

import org.semanticweb.HermiT.model.*;
import uk.ac.ox.cs.pagoda.rules.approximators.OverApproxExist;
import uk.ac.ox.cs.pagoda.util.Namespace;
import uk.ac.ox.cs.pagoda.util.Utility;

import java.util.*;


public class PredicateDependency extends DependencyGraph<DLPredicate> {

	private static final DLPredicate equality = AtomicRole.create(Namespace.EQUALITY);
	private static final DLPredicate inequality = AtomicRole.create(Namespace.INEQUALITY);
	Collection<DLClause> m_clauses;
	Map<PredicatePair, LinkedList<DLClause>> edgeLabels = new HashMap<PredicatePair, LinkedList<DLClause>>();
	Set<DLPredicate> reachableToBottom = null;

	public PredicateDependency(Collection<DLClause> clauses) {
		m_clauses = clauses;
		build();
	}

	@Override
	protected void build() {
		update(m_clauses);

		addLink(equality, AtomicConcept.NOTHING);
		addLink(inequality, AtomicConcept.NOTHING);
	}

	private void addEdgeLabel(DLPredicate body, DLPredicate head, DLClause clause) {
		PredicatePair key = new PredicatePair(body, head);
		LinkedList<DLClause> value;
		if ((value = edgeLabels.get(key)) == null)
			edgeLabels.put(key, value = new LinkedList<DLClause>());
		value.add(clause);
	}

	private void addLinks4Negation(AtomicConcept c, DLClause clause) {
		addLink(c, AtomicConcept.NOTHING);
		addEdgeLabel(c, AtomicConcept.NOTHING, clause);
		String iri = c.getIRI();
		addLink(c = AtomicConcept.create(iri.substring(0, iri.length() - 4)), AtomicConcept.NOTHING);
		addEdgeLabel(c, AtomicConcept.NOTHING, clause);
	}

	public Set<DLPredicate> collectPredicate(Atom[] atoms) {
		Set<DLPredicate> predicates = new HashSet<DLPredicate>();
		for (Atom atom : atoms)
			predicates.addAll(getAtomicPredicates(atom.getDLPredicate()));
		return predicates;
	}
	
	private Set<DLPredicate> getAtomicPredicates(DLPredicate predicate) {
		Set<DLPredicate> predicates = new HashSet<DLPredicate>();
		if (predicate instanceof AtLeastConcept)
			predicates.addAll(getAtomicPredicates((AtLeastConcept) predicate));
		else {
			if ((predicate = getAtomicPredicate(predicate)) != null)
				predicates.add(predicate);
		}
		return predicates;
	}
	
	private Set<DLPredicate> getAtomicPredicates(AtLeastConcept alc) {
		Set<DLPredicate> set = new HashSet<DLPredicate>();
		if (alc.getOnRole() instanceof AtomicRole)
			set.add((AtomicRole) alc.getOnRole());
		else
			set.add(((InverseRole) alc.getOnRole()).getInverseOf());

		if (alc.getToConcept() instanceof AtomicConcept)
			if (alc.getToConcept().equals(AtomicConcept.THING)) ;
			else set.add((AtomicConcept) alc.getToConcept());
		else
			set.add(OverApproxExist.getNegationConcept(((AtomicNegationConcept) alc.getToConcept()).getNegatedAtomicConcept()));
		return set;
	}
	
	private DLPredicate getAtomicPredicate(DLPredicate p) {
		if (p instanceof Equality || p instanceof AnnotatedEquality)
			return equality;
		if (p instanceof Inequality)
			return inequality;
		if (p instanceof AtomicConcept)
			if (p.equals(AtomicConcept.THING))
				return null;
			else return p;
		if (p instanceof AtomicRole)
			return p;
		if (p instanceof AtLeastDataRange) {
			AtLeastDataRange aldr = (AtLeastDataRange) p;
			if (aldr.getOnRole() instanceof AtomicRole)
				return (AtomicRole) aldr.getOnRole();
			else
				return ((InverseRole) aldr.getOnRole()).getInverseOf();
		}
		Utility.logDebug("Unknown DLPredicate in PredicateDependency: " + p);
		return null;
	}
	
	public Set<DLClause> pathTo(DLPredicate p) {
		Set<DLClause> rules = new HashSet<DLClause>();
		Set<DLPredicate> visited = new HashSet<DLPredicate>();

		Queue<DLPredicate> queue = new LinkedList<DLPredicate>();
		queue.add(p);
		visited.add(p);

		Set<DLPredicate> edge;
		Collection<DLClause> clauses;

		while (!queue.isEmpty()) {
			if ((edge = reverseEdges.get(p = queue.poll())) != null) {
				for (DLPredicate pred: edge) {
					if (!visited.contains(pred)) {
						queue.add(pred);
						visited.add(pred);
					}
					clauses = edgeLabelsBetween(pred, p);
					if (clauses != null) rules.addAll(clauses);
				}
			}
		}
		return rules;
	}

	private LinkedList<DLClause> edgeLabelsBetween(DLPredicate p, DLPredicate q) {
		PredicatePair pair = new PredicatePair(p, q);
		return edgeLabels.get(pair);
	}
	
	public Set<DLClause> pathToBottom(DLPredicate p) {
		if (reachableToBottom == null) {
			reachableToBottom = getAncesters(AtomicConcept.NOTHING); 
			reachableToBottom.add(AtomicConcept.NOTHING); 
		}
		
		Set<DLClause> rules = new HashSet<DLClause>(); 
		Set<DLPredicate> visited = new HashSet<DLPredicate>(); 
		
		Queue<DLPredicate> queue = new LinkedList<DLPredicate>(); 
		queue.add(p); 
		visited.add(p);
		
		Set<DLPredicate> edge; 
		Collection<DLClause> clauses; 
		
		while (!queue.isEmpty()) {
			if ((edge = edges.get(p = queue.poll())) != null) {
				for (DLPredicate next: edge) 
					if (reachableToBottom.contains(next)) {
						if (!visited.contains(next)) {
							queue.add(next); 
							visited.add(next); 
						}
						clauses = edgeLabelsBetween(p, next);
						if (clauses != null) rules.addAll(clauses); 
					}
			}
		}
		return rules; 
	}

	public void update(Collection<DLClause> clauses) {
		Set<DLPredicate> headPredicates, bodyPredicates; 
		
		for (DLClause clause: clauses) {
			headPredicates = collectPredicate(clause.getHeadAtoms());
			bodyPredicates = collectPredicate(clause.getBodyAtoms()); 

			for (DLPredicate body: bodyPredicates)
				for (DLPredicate head: headPredicates) {
					addLink(body, head);
					addEdgeLabel(body, head, clause); 
					
					if (body instanceof AtomicConcept && body.toString().contains("_neg")) 
						addLinks4Negation((AtomicConcept) body, clause); 
					if (head instanceof AtomicConcept && head.toString().contains("_neg")) 
						addLinks4Negation((AtomicConcept) head, clause); 
				}
			
			for (DLPredicate body: bodyPredicates)
				addLink(equality, body);
			
			for (DLPredicate head: headPredicates)
				addLink(equality, head);
		}		
	}

}

class PredicatePair {
	
	DLPredicate p, q; 
	
	public PredicatePair(DLPredicate p, DLPredicate q) {
		this.p = p; this.q = q; 
	}
	
	public int hashCode() {
		return p.hashCode() * 1997 + q.hashCode(); 
	}
	
	public boolean equals(Object o) {
		if (!(o instanceof PredicatePair)) return false; 
		PredicatePair thatPair = (PredicatePair) o; 
		return p.equals(thatPair.p) && q.equals(thatPair.q); 
	}
	
	public String toString() {
		return "<" + p.toString() + "," + q.toString() + ">"; 
	}
}
