package uk.ac.ox.cs.pagoda.query.rollup;

import org.semanticweb.HermiT.model.*;
import org.semanticweb.owlapi.model.*;
import uk.ac.ox.cs.pagoda.util.Namespace;

import java.util.*;

public class QueryGraph {
	
	Set<Variable> freeVars = new HashSet<Variable>(); 
	Set<Variable> existVars = new HashSet<Variable>(); 
	Set<Individual> constants = new HashSet<Individual>();
	
	MultiMap<Term, OWLClassExpression> concepts = new MultiMap<Term, OWLClassExpression>();
	MultiMap<Term, ObjectEdge> rollable_edges = new MultiMap<Term, ObjectEdge>();
	MultiMap<Term, ObjectEdge> edges = new MultiMap<Term, ObjectEdge>();
	OWLOntology ontology; 
	OWLDataFactory factory; 
	
	public QueryGraph(Atom[] bodyAtoms, String[] distinguishedVars, OWLOntology onto) {
		for (String vName: distinguishedVars)
			freeVars.add(Variable.create(vName));
		
		ontology = onto; 
		factory = onto.getOWLOntologyManager().getOWLDataFactory(); 
		
		for (Atom atom: bodyAtoms) {
			if (atom.getArity() == 1) {
				updateExistentiallyVariables(atom.getArgumentVariable(0));
				String id = ((AtomicConcept) atom.getDLPredicate()).getIRI(); 
				if (!id.equals(Namespace.PAGODA_ORIGINAL))
					concepts.add(atom.getArgument(0), factory.getOWLClass(IRI.create(id)));
			}
			else if (atom.getArity() == 2) {
				updateExistentiallyVariables(atom.getArgumentVariable(0)); 
				updateExistentiallyVariables(atom.getArgumentVariable(1)); 
				if (atom.getArgument(0).equals(atom.getArgument(1)) && atom.getArgument(0) instanceof Variable) {
					concepts.add(atom.getArgument(0), factory.getOWLObjectHasSelf(factory.getOWLObjectProperty(IRI.create(((AtomicRole) atom.getDLPredicate()).getIRI()))));
				}
				else createEdges(atom.getArgument(0), (AtomicRole) atom.getDLPredicate(), atom.getArgument(1));
			}
		}
		
		rollup(); 
	}

	public void createEdges(Term u, AtomicRole r, Term v) {
		if(ontology.containsDataPropertyInSignature(IRI.create(r.getIRI()))) {
//			edges.add(u, new DataEdge(r, v));
			Constant c = (Constant) v;
			OWLLiteral l = factory.getOWLLiteral(c.getLexicalForm(), c.getDatatypeURI());
			concepts.add(u, factory.getOWLDataHasValue(factory.getOWLDataProperty(IRI.create(r.getIRI())), l));
		}
		else {
			boolean rollable = existVars.contains(u) || existVars.contains(v);

			ObjectEdge edge = new ObjectEdge(r, v, false);
			if(rollable) {
				rollable_edges.add(u, edge);
				edge = new ObjectEdge(r, u, true);
				rollable_edges.add(v, edge);
			}
			else edges.add(u, edge);

		}
	}

	public Set<OWLAxiom> getPropertyAssertions(Map<Variable, Term> assignment) {
		OWLIndividual sub, obj;
		Set<OWLAxiom> axioms = new HashSet<OWLAxiom>();
		for(Map.Entry<Term, Set<ObjectEdge>> entry : edges.map.entrySet()) {
			sub = factory.getOWLNamedIndividual(IRI.create(getIndividual(entry.getKey(), assignment).getIRI()));
			for(ObjectEdge edge : entry.getValue()) {
				Individual individual = getIndividual(edge.v, assignment);
				String iri = individual.getIRI();
				obj = factory.getOWLNamedIndividual(IRI.create(iri));
				axioms.add(factory.getOWLObjectPropertyAssertionAxiom(edge.p, sub, obj));
			}
		}
		return axioms;
	}

//	public Set<OWLClassExpression> getExistentialConditions(Map<Variable, Term> assignment) {
//		if(!rollable_edges.isEmpty()) return null;
//
//		OWLIndividual sub;
//		Visitor visitor = new Visitor(factory, assignment);
//		Set<OWLClassExpression> axioms = new HashSet<>();
//		for(Map.Entry<Term, Set<OWLClassExpression>> entry : concepts.map.entrySet()) {
//			// TODO check correctness!!!
//			if(existVars.contains(entry.getKey())) {
//				OWLClassExpression conjunction =
//						factory.getOWLObjectIntersectionOf(factory.getOWLThing());
//				for(OWLClassExpression owlClassExpression : entry.getValue()) {
//					conjunction = factory.getOWLObjectIntersectionOf(conjunction, owlClassExpression.accept(visitor));
//				}
//				axioms.add(conjunction);
////				continue; // previously the "then" contained only this
//			}
//		}
//		return axioms;
//	}

	public Set<OWLAxiom> getExistentialAxioms(Map<Variable, Term> assignment) {
		if(!rollable_edges.isEmpty()) return null;

		Visitor visitor = new Visitor(factory, assignment);
		Set<OWLAxiom> axioms = new HashSet<>();
		for(Map.Entry<Term, Set<OWLClassExpression>> entry : concepts.map.entrySet()) {
			if(existVars.contains(entry.getKey())) {
				OWLClassExpression conjunction = factory.getOWLThing();
				for(OWLClassExpression owlClassExpression : entry.getValue()) {
					conjunction = factory.getOWLObjectIntersectionOf(conjunction, owlClassExpression.accept(visitor));
				}
				axioms.add(factory.getOWLSubClassOfAxiom(conjunction, factory.getOWLNothing()));
			}
		}
		return axioms;
	}

	public Set<OWLAxiom> getAssertions(Map<Variable, Term> assignment) {
		if(!rollable_edges.isEmpty()) return null;

		OWLIndividual sub;
		Visitor visitor = new Visitor(factory, assignment);
		Set<OWLAxiom> axioms = getPropertyAssertions(assignment);
		for(Map.Entry<Term, Set<OWLClassExpression>> entry : concepts.map.entrySet()) {
			// TODO check correctness!!!
			if(existVars.contains(entry.getKey())) {
//				OWLClassExpression conjunction =
//						factory.getOWLObjectIntersectionOf(factory.getOWLThing());
//				for(OWLClassExpression owlClassExpression : entry.getValue()) {
//					conjunction = factory.getOWLObjectIntersectionOf(conjunction, owlClassExpression.accept(visitor));
//				}
//				axioms.add(factory.getOWLSubClassOfAxiom(conjunction, factory.getOWLNothing()));
				continue; // previously the "then" contained only this
			}
			else {
				sub = factory.getOWLNamedIndividual(IRI.create(getIndividual(entry.getKey(), assignment).getIRI()));
				for(OWLClassExpression clsExp : entry.getValue()) {
					axioms.add(factory.getOWLClassAssertionAxiom(clsExp.accept(visitor), sub));
				}
			}
		}
		return axioms;
	}

	private void updateExistentiallyVariables(Variable argumentVariable) {
		if(freeVars.contains(argumentVariable)) return;
		existVars.add(argumentVariable);
	}
	
	private void rollup() {
		for (boolean updated = true; updated; ) {
			updated = false;

			Set<ObjectEdge> set;
			for (Variable var: existVars) {
				if ((set = rollable_edges.map.get(var)) != null && set.size() == 1) {
					updated = true;
					ObjectEdge edge = set.iterator().next();
					rollupEdge(edge.v, edge.p.getInverseProperty().getSimplified(), var, true);
					set.clear();
				}
			}
			if(updated) continue;

			for (Variable var: existVars) {
				set = rollable_edges.map.get(var);
				if(set == null) continue;
				for (Iterator<ObjectEdge> iter = set.iterator(); iter.hasNext(); ) {
					ObjectEdge edge = iter.next();
					if (constants.contains(edge.v) || freeVars.contains(edge.v)) {
						updated = true;
						rollupEdge(var, edge.p, edge.v, false);
						iter.remove();
					}
				}
			}
		}

	}

	private void rollupEdge(Term u, OWLObjectPropertyExpression op, Term v, boolean inverse) {
		if (existVars.contains(v)) {
			Set<OWLClassExpression> exps = concepts.get(v);
			if (exps == null) exps = new HashSet<OWLClassExpression>();
			concepts.add(u, factory.getOWLObjectSomeValuesFrom(op, factory.getOWLObjectIntersectionOf(exps)));
		}
		else {
			OWLIndividual obj = getOWLIndividual(v);
			concepts.add(u, factory.getOWLObjectHasValue(op, obj));
		}

		if(inverse)
			removeRollableEdge(u, op, v);
		else
			removeRollableEdge(v, op.getInverseProperty().getSimplified(), u);
	}

	private void removeRollableEdge(Term u, OWLObjectPropertyExpression op, Term v) {
		Set<ObjectEdge> set = rollable_edges.get(u);
		ObjectEdge edge;
		if (set != null)
			for (Iterator<ObjectEdge> iter = set.iterator(); iter.hasNext(); ) {
				edge = iter.next();
				if(edge.p.equals(op) && edge.v.equals(v)) iter.remove();
			}
	}

	OWLNamedIndividual getOWLIndividual(Term t) {
		if (freeVars.contains(t))
			return new VariableIndividual((Variable) t);
		else if (t instanceof Variable)
			return null;
		else
			return factory.getOWLNamedIndividual(IRI.create(((Individual) t).getIRI()));
	}

	private Individual getIndividual(Term key, Map<Variable, Term> assignment) {
		if(key instanceof Individual)
			return (Individual) key;
		else
			return (Individual) assignment.get(key);
	}

	class ObjectEdge {
		OWLObjectPropertyExpression p;
		Term v;

		public ObjectEdge(AtomicRole r, Term t, boolean inverse) {
			p = factory.getOWLObjectProperty(IRI.create(r.getIRI()));
			if(inverse) p = p.getInverseProperty();
			v = t;

		}
	}

	class MultiMap<K, V> {

		HashMap<K, Set<V>> map = new HashMap<K, Set<V>>();

		public Set<V> get(K v) {
			return map.get(v);
		}

		public boolean isEmpty() {
			for(Map.Entry<K, Set<V>> entry : map.entrySet())
				if(!entry.getValue().isEmpty())
					return false;
			return true;
		}

		void add(K key, V value) {
			Set<V> list = map.get(key);
			if(list == null)
				map.put(key, list = new HashSet<V>());
			list.add(value);
		}

	}
}

class Visitor implements OWLClassExpressionVisitorEx<OWLClassExpression> {

	OWLDataFactory factory; 
	Map<Variable, Term> assignment; 
	
	public Visitor(OWLDataFactory factory, Map<Variable, Term> assignment) {
		this.factory = factory; 
		this.assignment = assignment; 
	}

	@Override
	public OWLClassExpression visit(OWLClass ce) {
		// TODO Auto-generated method stub
		return ce;
	}

	@Override
	public OWLClassExpression visit(OWLObjectIntersectionOf ce) {
		Set<OWLClassExpression> clsExps = new HashSet<OWLClassExpression>();
		OWLClassExpression newExp; 
		boolean updated = false; 
		for (OWLClassExpression clsExp: ce.asConjunctSet()) {
			clsExps.add(newExp = clsExp.accept(this)); 
			if (newExp != clsExp) updated = true; 
		}
			
		if (updated) return factory.getOWLObjectIntersectionOf(clsExps); 
		else return ce;
	}

	@Override
	public OWLClassExpression visit(OWLObjectUnionOf ce) {
		// TODO Auto-generated method stub
		return ce;
	}

	@Override
	public OWLClassExpression visit(OWLObjectComplementOf ce) {
		// TODO Auto-generated method stub
		return ce;
	}

	@Override
	public OWLClassExpression visit(OWLObjectSomeValuesFrom ce) {
		OWLClassExpression newFiller = ce.getFiller().accept(this); 
		if (newFiller == ce.getFiller()) return ce;
		return factory.getOWLObjectSomeValuesFrom(ce.getProperty(), newFiller); 
	}

	@Override
	public OWLClassExpression visit(OWLObjectAllValuesFrom ce) {
		// TODO Auto-generated method stub
		return ce;
	}

	@Override
	public OWLClassExpression visit(OWLObjectHasValue ce) {
		if (ce.getValue() instanceof VariableIndividual) {
			Individual c = (Individual) assignment.get(((VariableIndividual) ce.getValue()).var); 
			OWLIndividual l = factory.getOWLNamedIndividual(IRI.create(c.getIRI())); 
			return factory.getOWLObjectHasValue(ce.getProperty(), l); 
		}
		return ce;
	}

	@Override
	public OWLClassExpression visit(OWLObjectMinCardinality ce) {
		// TODO Auto-generated method stub
		return ce;
	}

	@Override
	public OWLClassExpression visit(OWLObjectExactCardinality ce) {
		// TODO Auto-generated method stub
		return ce;
	}

	@Override
	public OWLClassExpression visit(OWLObjectMaxCardinality ce) {
		// TODO Auto-generated method stub
		return ce;
	}

	@Override
	public OWLClassExpression visit(OWLObjectHasSelf ce) {
		// TODO Auto-generated method stub
		return ce;
	}

	@Override
	public OWLClassExpression visit(OWLObjectOneOf ce) {
		// TODO Auto-generated method stub
		return ce;
	}

	@Override
	public OWLClassExpression visit(OWLDataSomeValuesFrom ce) {
		// TODO Auto-generated method stub
		return ce;
	}

	@Override
	public OWLClassExpression visit(OWLDataAllValuesFrom ce) {
		// TODO Auto-generated method stub
		return ce;
	}

	@Override
	public OWLClassExpression visit(OWLDataHasValue ce) {
		if (ce.getValue() instanceof VariableConstant) {
			Constant c = (Constant) assignment.get(((VariableConstant) ce.getValue()).var); 
			OWLLiteral l = factory.getOWLLiteral(c.getLexicalForm(), c.getDatatypeURI()); 
			return factory.getOWLDataHasValue(ce.getProperty(), l); 
		}
		return ce;
	}

	@Override
	public OWLClassExpression visit(OWLDataMinCardinality ce) {
		// TODO Auto-generated method stub
		return ce;
	}

	@Override
	public OWLClassExpression visit(OWLDataExactCardinality ce) {
		// TODO Auto-generated method stub
		return ce;
	}

	@Override
	public OWLClassExpression visit(OWLDataMaxCardinality ce) {
		// TODO Auto-generated method stub
		return ce;
	}
	
}