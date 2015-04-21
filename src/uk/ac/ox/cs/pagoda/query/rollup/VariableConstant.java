package uk.ac.ox.cs.pagoda.query.rollup;

import java.util.Set;

import org.semanticweb.HermiT.model.Variable;
import org.semanticweb.owlapi.model.OWLAnnotationValueVisitor;
import org.semanticweb.owlapi.model.OWLAnnotationValueVisitorEx;
import org.semanticweb.owlapi.model.OWLAnonymousIndividual;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLDataProperty;
import org.semanticweb.owlapi.model.OWLDataVisitor;
import org.semanticweb.owlapi.model.OWLDataVisitorEx;
import org.semanticweb.owlapi.model.OWLDatatype;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLLiteral;
import org.semanticweb.owlapi.model.OWLNamedIndividual;
import org.semanticweb.owlapi.model.OWLObject;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.model.OWLObjectVisitor;
import org.semanticweb.owlapi.model.OWLObjectVisitorEx;

class VariableConstant implements OWLLiteral {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 5089014375729171030L;
	Variable var; 

	public VariableConstant(Variable v) {
		var = v; 
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
	public void accept(OWLAnnotationValueVisitor visitor) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public <O> O accept(OWLAnnotationValueVisitorEx<O> visitor) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean isRDFPlainLiteral() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public String getLiteral() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public OWLDatatype getDatatype() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean hasLang() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public String getLang() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean hasLang(String lang) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isInteger() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public int parseInteger() throws NumberFormatException {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public boolean isBoolean() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean parseBoolean() throws NumberFormatException {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isDouble() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public double parseDouble() throws NumberFormatException {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public boolean isFloat() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public float parseFloat() throws NumberFormatException {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void accept(OWLDataVisitor visitor) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public <O> O accept(OWLDataVisitorEx<O> visitor) {
		// TODO Auto-generated method stub
		return null;
	} 
	
}
