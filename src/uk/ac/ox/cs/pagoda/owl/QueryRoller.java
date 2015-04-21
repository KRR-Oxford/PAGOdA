package uk.ac.ox.cs.pagoda.owl;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;

import org.semanticweb.HermiT.model.AnnotatedEquality;
import org.semanticweb.HermiT.model.Atom;
import org.semanticweb.HermiT.model.AtomicConcept;
import org.semanticweb.HermiT.model.AtomicRole;
import org.semanticweb.HermiT.model.DLClause;
import org.semanticweb.HermiT.model.DLPredicate;
import org.semanticweb.HermiT.model.Equality;
import org.semanticweb.HermiT.model.Inequality;
import org.semanticweb.HermiT.model.Term;
import org.semanticweb.HermiT.model.Variable;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLNamedIndividual;
import org.semanticweb.owlapi.model.OWLObjectHasValue;
import org.semanticweb.owlapi.model.OWLObjectPropertyExpression;
import org.semanticweb.owlapi.model.OWLObjectSomeValuesFrom;

import uk.ac.ox.cs.pagoda.summary.Summary;
import uk.ac.ox.cs.pagoda.util.Namespace;
import uk.ac.ox.cs.pagoda.util.Utility;

public class QueryRoller {
	
	private OWLDataFactory factory; 
	
	protected Map<String, LinkedList<String>> edges, concepts;
	
	public QueryRoller(OWLDataFactory factory) {
		this.factory = factory; 
	}
	
	private OWLClassExpression getOWLClassExpression(String u, String father, Set<String> visited) {
		visited.add(u);
		String[] temp;
		Set<OWLClassExpression> exps = new HashSet<OWLClassExpression>();
		OWLObjectPropertyExpression prop;
		if (concepts.containsKey(u))
			for (String concept: concepts.get(u)) {
				exps.add(factory.getOWLClass(IRI.create(concept)));
			}
		
		if (edges.containsKey(u))
			for (String pair: edges.get(u)) {
				temp = pair.split(" ");
				if (temp[0].startsWith("-"))
					prop = factory.getOWLObjectInverseOf(factory.getOWLObjectProperty(IRI.create(temp[0].substring(1))));
				else 
					prop = factory.getOWLObjectProperty(IRI.create(temp[0]));

				if (temp[1].startsWith("@"))
					exps.add(factory.getOWLObjectHasValue(prop, factory.getOWLNamedIndividual(IRI.create(OWLHelper.removeAngles(temp[1].substring(1))))));
				else if (visited.contains(temp[1])) {
					if (father != null && father.equals(temp[1]))
						continue; 
					if (!u.equals(temp[1])) {
						Utility.logError("The query cannot be rolled up!");
						return null; 
					}
					exps.add(factory.getOWLObjectHasSelf(prop));
				}
				else 
					exps.add(factory.getOWLObjectSomeValuesFrom(prop, getOWLClassExpression(temp[1], u, visited)));
			}
		
		if (exps.size() == 0)
			return factory.getOWLThing();
		if (exps.size() == 1)
			return exps.iterator().next();
		else 
			return factory.getOWLObjectIntersectionOf(exps);
	}
	
	String currentMainVariable; 

	public OWLClassExpression rollUp(DLClause query, String var) {
		currentMainVariable = var; 
		query = removeSameAs(query);  
		edges = new HashMap<String, LinkedList<String>>();
		concepts = new HashMap<String, LinkedList<String>>();
		String arg1, arg2, predicate;
		DLPredicate dlPredicate;
		Term t1, t2; 
		for (Atom atom: query.getBodyAtoms()) {
			dlPredicate = atom.getDLPredicate(); 
			if (dlPredicate instanceof AtomicRole) {
				arg1 = (t1 = atom.getArgument(0)).toString();
				arg2 = (t2 = atom.getArgument(1)).toString();
				predicate = ((AtomicRole) dlPredicate).getIRI(); 
				if (!predicate.equals(Namespace.RDF_TYPE)) {
					if (t1 instanceof Variable)
						if (t2 instanceof Variable) {
							addEntry(edges, arg1, predicate + " " + arg2);
							addEntry(edges, arg2, "-" + predicate + " " + arg1);
						}
						else 
							addEntry(edges, arg1, predicate + " @" + arg2);
					else
						addEntry(edges, arg2, "-" + predicate + " @" + arg1);
				}
				else {
					if (t2 instanceof Variable) return null; 
					addEntry(concepts, arg1, arg2); 
				}
			}
			else 
				addEntry(concepts, atom.getArgument(0).toString(), ((AtomicConcept) dlPredicate).getIRI()); 
		}
		
		return getOWLClassExpression(var, null, new HashSet<String>());
	}

	private DLClause removeSameAs(DLClause query) {
		int equalityStatement = 0; 
		
		Map<Term, Term> ufs = new HashMap<Term, Term>(); 
		for (Atom atom: query.getBodyAtoms()) 
			if (isEquality(atom.getDLPredicate())) {
				++equalityStatement; 
				merge(ufs, atom.getArgument(0), atom.getArgument(1));
			}
		
		if (equalityStatement == 0) return query; 
		
		Atom[] bodyAtoms = new Atom[query.getBodyLength() - equalityStatement]; 
		int index = 0; 
		for (Atom atom: query.getBodyAtoms())
			if (!isEquality(atom.getDLPredicate())) {
				if (atom.getArity() == 1)
					bodyAtoms[index++] = Atom.create(atom.getDLPredicate(), find(ufs, atom.getArgument(0)));
				else
					bodyAtoms[index++] = Atom.create(atom.getDLPredicate(), find(ufs, atom.getArgument(0)), find (ufs, atom.getArgument(1)));
			}
		
		return DLClause.create(query.getHeadAtoms(), bodyAtoms); 
	}

	private boolean isEquality(DLPredicate p) {
		return p instanceof Equality ||  p instanceof AnnotatedEquality || p instanceof Inequality 
				|| p.toString().equals(Namespace.EQUALITY_QUOTED)
				|| p.toString().equals(Namespace.EQUALITY_ABBR);  
	}

	private Term find(Map<Term, Term> ufs, Term u) {
		Term v = u, w; 
		while ((w = ufs.get(v)) != null) v = w; 
		while ((w = ufs.get(u)) != null) {
			ufs.put(u, v); 
			u = w; 
		}
		return v;
	}

	private void merge(Map<Term, Term> ufs, Term u, Term v) {
		u = find(ufs, u); 
		v = find(ufs, v); 
		if (compare(u, v) <= 0) ufs.put(v, u); 
		else ufs.put(u, v); 
	}

	private int compare(Term u, Term v) {
		int ret = rank(u) - rank(v);
		if (ret != 0) return ret; 
		else 
			return u.toString().compareTo(v.toString()); 
	}

	private int rank(Term u) {
		if (u instanceof Variable) { 
			Variable v = (Variable) u;
			if (v.getName().equals(currentMainVariable)) 
				return 0; 
			else
				return 2; 
		}
		return 1;
	}

	private void addEntry(Map<String, LinkedList<String>> map, String key, String value) {
		LinkedList<String> list;
		if ((list = map.get(key)) == null) {
			list = new LinkedList<String>();
			map.put(key, list);
		}
		list.add(value);
	}

	@Deprecated
	public OWLClassExpression summarise(Summary sum, OWLClassExpression exp) {
		if (exp == null) return null; 
		
		Set<OWLClassExpression> exps = exp.asConjunctSet();
		if (exps.size() == 1) {
			OWLClassExpression tempExp = exps.iterator().next();
			
			// TODO reference: getOWLClassExpression(String) 
			
			if (tempExp instanceof OWLObjectHasValue) {
				OWLObjectHasValue hasValue = (OWLObjectHasValue) tempExp;
				OWLNamedIndividual individual = sum.getRepresentativeIndividual(hasValue.getValue().toStringID()); 
				return factory.getOWLObjectHasValue(hasValue.getProperty(), individual);
				
			}
			if (tempExp instanceof OWLObjectSomeValuesFrom) {
				OWLObjectSomeValuesFrom someValuesFrom = (OWLObjectSomeValuesFrom) tempExp; 
				return factory.getOWLObjectSomeValuesFrom(someValuesFrom.getProperty(), summarise(sum, someValuesFrom.getFiller())); 
			}
			return tempExp; 
		}
		
		Set<OWLClassExpression> newExps = new HashSet<OWLClassExpression>(); 
		for (OWLClassExpression clsExp: exps) 
			newExps.add(summarise(sum, clsExp)); 
		
		return factory.getOWLObjectIntersectionOf(newExps); 
	}


}
