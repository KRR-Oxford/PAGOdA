package uk.ac.ox.cs.pagoda.query.rollup;

import java.util.Map;
import java.util.Set;

import org.semanticweb.HermiT.model.Variable;
import org.semanticweb.owlapi.model.EntityType;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAnnotation;
import org.semanticweb.owlapi.model.OWLAnnotationAssertionAxiom;
import org.semanticweb.owlapi.model.OWLAnnotationProperty;
import org.semanticweb.owlapi.model.OWLAnonymousIndividual;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLDataProperty;
import org.semanticweb.owlapi.model.OWLDataPropertyExpression;
import org.semanticweb.owlapi.model.OWLDatatype;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLEntityVisitor;
import org.semanticweb.owlapi.model.OWLEntityVisitorEx;
import org.semanticweb.owlapi.model.OWLIndividual;
import org.semanticweb.owlapi.model.OWLIndividualVisitor;
import org.semanticweb.owlapi.model.OWLIndividualVisitorEx;
import org.semanticweb.owlapi.model.OWLLiteral;
import org.semanticweb.owlapi.model.OWLNamedIndividual;
import org.semanticweb.owlapi.model.OWLNamedObjectVisitor;
import org.semanticweb.owlapi.model.OWLObject;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.model.OWLObjectPropertyExpression;
import org.semanticweb.owlapi.model.OWLObjectVisitor;
import org.semanticweb.owlapi.model.OWLObjectVisitorEx;
import org.semanticweb.owlapi.model.OWLOntology;

class VariableIndividual implements OWLNamedIndividual {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 3002966246639516395L;
	Variable var; 

	public VariableIndividual(Variable v) {
		var = v; 
	} 
	
	@Override
	public boolean isNamed() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isAnonymous() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public OWLNamedIndividual asOWLNamedIndividual() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public OWLAnonymousIndividual asOWLAnonymousIndividual() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Set<OWLClassExpression> getTypes(OWLOntology ontology) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Set<OWLClassExpression> getTypes(Set<OWLOntology> ontologies) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Map<OWLObjectPropertyExpression, Set<OWLIndividual>> getObjectPropertyValues(
			OWLOntology ontology) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Set<OWLIndividual> getObjectPropertyValues(
			OWLObjectPropertyExpression property, OWLOntology ontology) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean hasObjectPropertyValue(OWLObjectPropertyExpression property,
			OWLIndividual individual, OWLOntology ontology) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean hasDataPropertyValue(OWLDataPropertyExpression property,
			OWLLiteral value, OWLOntology ontology) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean hasNegativeObjectPropertyValue(
			OWLObjectPropertyExpression property, OWLIndividual individual,
			OWLOntology ontology) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public Map<OWLObjectPropertyExpression, Set<OWLIndividual>> getNegativeObjectPropertyValues(
			OWLOntology ontology) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Map<OWLDataPropertyExpression, Set<OWLLiteral>> getDataPropertyValues(
			OWLOntology ontology) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Set<OWLLiteral> getDataPropertyValues(
			OWLDataPropertyExpression property, OWLOntology ontology) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Map<OWLDataPropertyExpression, Set<OWLLiteral>> getNegativeDataPropertyValues(
			OWLOntology ontology) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean hasNegativeDataPropertyValue(
			OWLDataPropertyExpression property, OWLLiteral literal,
			OWLOntology ontology) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public Set<OWLIndividual> getSameIndividuals(OWLOntology ontology) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Set<OWLIndividual> getDifferentIndividuals(OWLOntology ontology) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String toStringID() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void accept(OWLIndividualVisitor visitor) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public <O> O accept(OWLIndividualVisitorEx<O> visitor) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Set<OWLEntity> getSignature() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Set<OWLAnonymousIndividual> getAnonymousIndividuals() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Set<OWLClass> getClassesInSignature() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Set<OWLDataProperty> getDataPropertiesInSignature() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Set<OWLObjectProperty> getObjectPropertiesInSignature() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Set<OWLNamedIndividual> getIndividualsInSignature() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Set<OWLDatatype> getDatatypesInSignature() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Set<OWLClassExpression> getNestedClassExpressions() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void accept(OWLObjectVisitor visitor) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public <O> O accept(OWLObjectVisitorEx<O> visitor) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean isTopEntity() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isBottomEntity() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public int compareTo(OWLObject arg0) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public boolean containsEntityInSignature(OWLEntity owlEntity) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public EntityType<?> getEntityType() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public <E extends OWLEntity> E getOWLEntity(EntityType<E> entityType) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean isType(EntityType<?> entityType) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public Set<OWLAnnotation> getAnnotations(OWLOntology ontology) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Set<OWLAnnotation> getAnnotations(OWLOntology ontology,
			OWLAnnotationProperty annotationProperty) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Set<OWLAnnotationAssertionAxiom> getAnnotationAssertionAxioms(
			OWLOntology ontology) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean isBuiltIn() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isOWLClass() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public OWLClass asOWLClass() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean isOWLObjectProperty() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public OWLObjectProperty asOWLObjectProperty() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean isOWLDataProperty() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public OWLDataProperty asOWLDataProperty() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean isOWLNamedIndividual() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isOWLDatatype() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public OWLDatatype asOWLDatatype() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean isOWLAnnotationProperty() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public OWLAnnotationProperty asOWLAnnotationProperty() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Set<OWLAxiom> getReferencingAxioms(OWLOntology ontology) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Set<OWLAxiom> getReferencingAxioms(OWLOntology ontology,
			boolean includeImports) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void accept(OWLEntityVisitor visitor) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public <O> O accept(OWLEntityVisitorEx<O> visitor) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public IRI getIRI() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void accept(OWLNamedObjectVisitor visitor) {
		// TODO Auto-generated method stub
		
	}
	
}
