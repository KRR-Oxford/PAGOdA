package uk.ac.ox.cs.pagoda.approx;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;

import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLIndividual;
import org.semanticweb.owlapi.model.OWLObjectAllValuesFrom;
import org.semanticweb.owlapi.model.OWLObjectHasSelf;
import org.semanticweb.owlapi.model.OWLObjectOneOf;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.model.OWLSubClassOfAxiom;

import uk.ac.ox.cs.pagoda.owl.OWLHelper;
import uk.ac.ox.cs.pagoda.util.Utility;

public class RLOntology extends RLPlusOntology {

	@Override
	public void transform() {
		super.transform();
		
		eliminateSelf();
		eliminateNominals();
		eliminateOWLThing();
		
		save();
		if (aBox.getAxiomCount() != 0)
			save(aBox); 
	}
	
	private void eliminateSelf() {
		Collection<OWLAxiom> axioms = new LinkedList<OWLAxiom>(outputOntology.getAxioms()); 
		OWLClassExpression subExp, superExp, newSubExp, newSuperExp;
		for (OWLAxiom axiom: axioms) 
			if (axiom instanceof OWLSubClassOfAxiom) {
				subExp = ((OWLSubClassOfAxiom) axiom).getSubClass();
				superExp = ((OWLSubClassOfAxiom) axiom).getSuperClass();
				newSubExp = approximateSelf4Sub(subExp);  
				newSuperExp = approximateSelf4Super(superExp);	
				if (newSubExp != subExp || newSuperExp != superExp) 
					replaceAxiom4output(axiom, factory.getOWLSubClassOfAxiom(newSubExp, newSuperExp));
			}
	}
	
	private void replaceAxiom4output(OWLAxiom oldAxiom, OWLAxiom newAxiom) {
		manager.removeAxiom(outputOntology, oldAxiom);
		manager.addAxiom(outputOntology, newAxiom);
		correspondence.put(newAxiom, correspondence.remove(oldAxiom));
	}

	private boolean hasSelf(OWLClassExpression conjunction) {
		for (OWLClassExpression conjunct: conjunction.asConjunctSet())
			if (conjunct instanceof OWLObjectHasSelf)
				return true;
		return false;
	}
	
	private OWLClassExpression approximateSelf4Sub(OWLClassExpression exp) {
		if (!hasSelf(exp)) return exp;
		Set<OWLClassExpression> newConjuncts = new HashSet<OWLClassExpression>();
		for (OWLClassExpression conjunct: exp.asConjunctSet())
			if (conjunct instanceof OWLObjectHasSelf)
				newConjuncts.add(factory.getOWLObjectSomeValuesFrom(((OWLObjectHasSelf) exp).getProperty(), factory.getOWLThing()));
			else 
				newConjuncts.add(conjunct);
		return OWLHelper.getSimplifiedConjunction(factory, newConjuncts);
	}

	private OWLClassExpression approximateSelf4Super(OWLClassExpression exp) {
		if (!hasSelf(exp)) return exp;
		Set<OWLClassExpression> newConjuncts = new HashSet<OWLClassExpression>();
		for (OWLClassExpression conjunct: exp.asConjunctSet())
			if (conjunct instanceof OWLObjectHasSelf) {
				OWLIndividual freshNominal = getNewIndividual(outputOntology, rlCounter++);
				newConjuncts.add(factory.getOWLObjectOneOf(freshNominal));
				newConjuncts.add(factory.getOWLObjectHasValue(((OWLObjectHasSelf) exp).getProperty(), freshNominal));
			}
			else 
				newConjuncts.add(conjunct);
				
		return OWLHelper.getSimplifiedConjunction(factory, newConjuncts);
	}

	private void eliminateNominals() {
		Collection<OWLAxiom> axioms = new LinkedList<OWLAxiom>(outputOntology.getAxioms()); 
		OWLClassExpression superExp, newSuperExp; 
		for (OWLAxiom axiom: axioms)
			if (axiom instanceof OWLSubClassOfAxiom) {
				superExp = ((OWLSubClassOfAxiom) axiom).getSuperClass();
				newSuperExp = approximateNominals(superExp);
				if (newSuperExp != superExp)
					replaceAxiom4output(axiom, factory.getOWLSubClassOfAxiom(((OWLSubClassOfAxiom) axiom).getSubClass(), newSuperExp));
			}
	}
	
	private OWLClassExpression approximateNominals(OWLClassExpression exp) {
		if (!hasIllegalNominals(exp)) return exp;
		Set<OWLIndividual> nominals; 
		Set<OWLClassExpression> newConjuncts = new HashSet<OWLClassExpression>();
		for (OWLClassExpression conjunct: exp.asConjunctSet()) {
			if (conjunct instanceof OWLObjectOneOf) {
				nominals = ((OWLObjectOneOf) conjunct).getIndividuals();
				newConjuncts.add(approximateNominal(nominals));
			}
			else if (conjunct instanceof OWLObjectAllValuesFrom) {
				OWLObjectAllValuesFrom allValuesFrom = ((OWLObjectAllValuesFrom) conjunct); 
				if (allValuesFrom.getFiller() instanceof OWLObjectOneOf) {
					nominals = ((OWLObjectOneOf) allValuesFrom.getFiller()).getIndividuals();
					newConjuncts.add(factory.getOWLObjectAllValuesFrom(allValuesFrom.getProperty(), 
							approximateNominal(nominals))); 
				}
			}
		}
		return OWLHelper.getSimplifiedConjunction(factory, newConjuncts);
	}

	private OWLClassExpression approximateNominal(Set<OWLIndividual> nominals) {
		if (nominals.size() > 1) {
			Utility.logError("Error: more than one nominal appearing in OWLObjectOneOf"); 
			return null;
		}
		OWLIndividual nominal = nominals.iterator().next();
		OWLObjectProperty freshProperty = getNewRole4Nominal(nominal);
		addAxiom2output(factory.getOWLInverseFunctionalObjectPropertyAxiom(freshProperty), null);
		manager.addAxiom(aBox, factory.getOWLObjectPropertyAssertionAxiom(freshProperty, nominal, nominal));
		return factory.getOWLObjectHasValue(freshProperty, nominal);
	}

	Map<OWLIndividual, OWLObjectProperty> role4nominal = new HashMap<OWLIndividual, OWLObjectProperty>();
	
	private OWLObjectProperty getNewRole4Nominal(OWLIndividual nominal) {
		OWLObjectProperty property; 
		if ((property = role4nominal.get(nominal)) == null)
			role4nominal.put(nominal, property = getNewRole(outputOntology, rlCounter++));
		return property;
	}

	private boolean hasIllegalNominals(OWLClassExpression exp) {
		for (OWLClassExpression conjunct: exp.asConjunctSet()) {
			if (conjunct instanceof OWLObjectOneOf) return true; 
			if (conjunct instanceof OWLObjectAllValuesFrom) {
				OWLObjectAllValuesFrom allValuesFrom = ((OWLObjectAllValuesFrom) conjunct); 
				if (allValuesFrom.getFiller() instanceof OWLObjectOneOf)
					return true;
			}
		}
		return false;
	}

	private void eliminateOWLThing() {
		OWLClassExpression subExp;
		boolean mark = false; 
		for (Clause clause: clauses) {
			subExp = OWLHelper.getSimplifiedConjunction(factory, clause.getSubClasses());
			if (subExp.equals(factory.getOWLThing())) {
				mark = true;
			}
		}
		
		if (mark) {
			Utility.logDebug("Top appears in the left of an axiom.");
		
			OWLSubClassOfAxiom subClassAxiom;
			OWLClass TOP = factory.getOWLClass(IRI.create(ontologyIRI + "#TOP"));
			for (OWLAxiom axiom: new HashSet<OWLAxiom>(outputOntology.getAxioms()))
				if (axiom instanceof OWLSubClassOfAxiom && (subClassAxiom = (OWLSubClassOfAxiom) axiom).getSubClass().equals(factory.getOWLThing())) 
					replaceAxiom4output(axiom, factory.getOWLSubClassOfAxiom(TOP, subClassAxiom.getSuperClass())); 
			
			for (OWLClass c: outputOntology.getClassesInSignature(true)) {
				if (!c.equals(factory.getOWLThing()))
					addAxiom2output(factory.getOWLSubClassOfAxiom(c, TOP), null);
				else
					addAxiom2output(factory.getOWLSubClassOfAxiom(TOP, c), null);
			}
			for (OWLObjectProperty p: outputOntology.getObjectPropertiesInSignature(true)) {
				addAxiom2output(factory.getOWLObjectPropertyDomainAxiom(p, TOP), null);
				addAxiom2output(factory.getOWLObjectPropertyRangeAxiom(p, TOP), null);
			}
		}
	}
	
}
