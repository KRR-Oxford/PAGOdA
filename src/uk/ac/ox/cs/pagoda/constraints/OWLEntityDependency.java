package uk.ac.ox.cs.pagoda.constraints;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.semanticweb.owlapi.model.OWLAnnotationAssertionAxiom;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassAssertionAxiom;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLDataPropertyDomainAxiom;
import org.semanticweb.owlapi.model.OWLDataPropertyRangeAxiom;
import org.semanticweb.owlapi.model.OWLDeclarationAxiom;
import org.semanticweb.owlapi.model.OWLDisjointClassesAxiom;
import org.semanticweb.owlapi.model.OWLEquivalentClassesAxiom;
import org.semanticweb.owlapi.model.OWLEquivalentObjectPropertiesAxiom;
import org.semanticweb.owlapi.model.OWLFunctionalObjectPropertyAxiom;
import org.semanticweb.owlapi.model.OWLInverseFunctionalObjectPropertyAxiom;
import org.semanticweb.owlapi.model.OWLInverseObjectPropertiesAxiom;
import org.semanticweb.owlapi.model.OWLLogicalEntity;
import org.semanticweb.owlapi.model.OWLObject;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.model.OWLObjectPropertyAssertionAxiom;
import org.semanticweb.owlapi.model.OWLObjectPropertyDomainAxiom;
import org.semanticweb.owlapi.model.OWLObjectPropertyExpression;
import org.semanticweb.owlapi.model.OWLObjectPropertyRangeAxiom;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLSubClassOfAxiom;
import org.semanticweb.owlapi.model.OWLSubObjectPropertyOfAxiom;
import org.semanticweb.owlapi.model.OWLSymmetricObjectPropertyAxiom;
import org.semanticweb.owlapi.model.OWLTransitiveObjectPropertyAxiom;

import uk.ac.ox.cs.pagoda.MyPrefixes;
import uk.ac.ox.cs.pagoda.owl.OWLHelper;
import uk.ac.ox.cs.pagoda.util.Utility;

public class OWLEntityDependency extends DependencyGraph<OWLLogicalEntity> {
	
	OWLOntology m_ontology;
	OWLClass m_nothing;
	Map<String, OWLLogicalEntity> map = new HashMap<String, OWLLogicalEntity>(); 

	public OWLEntityDependency(OWLOntology ontology) {
		m_ontology = ontology;
		m_nothing = ontology.getOWLOntologyManager().getOWLDataFactory().getOWLNothing(); 
		build(); 
	}

	@Override
	protected void build() {
		for (OWLOntology o: m_ontology.getImportsClosure())
			for (OWLAxiom a: o.getAxioms()) 
				if (a instanceof OWLDisjointClassesAxiom)
					addLinks((OWLDisjointClassesAxiom) a);
				else if (a instanceof OWLSymmetricObjectPropertyAxiom)
					addLinks((OWLSymmetricObjectPropertyAxiom) a); 
				else if (a instanceof OWLFunctionalObjectPropertyAxiom)
					;		
				else if (a instanceof OWLInverseFunctionalObjectPropertyAxiom)
					;
				else if (a instanceof OWLTransitiveObjectPropertyAxiom)
					addLinkes((OWLTransitiveObjectPropertyAxiom) a); 
				else if (a instanceof OWLInverseObjectPropertiesAxiom)
					addLinks((OWLInverseObjectPropertiesAxiom) a); 
				else if (a instanceof OWLSubClassOfAxiom) 
					addLinks((OWLSubClassOfAxiom) a); 
				else if (a instanceof OWLSubObjectPropertyOfAxiom)
					addLinks((OWLSubObjectPropertyOfAxiom) a); 
				else if (a instanceof OWLEquivalentClassesAxiom) 
					addLinks((OWLEquivalentClassesAxiom) a); 
				else if (a instanceof OWLEquivalentObjectPropertiesAxiom) 
					addLinks((OWLEquivalentObjectPropertiesAxiom) a); 
				else if (a instanceof OWLObjectPropertyDomainAxiom) 
					addLinks((OWLObjectPropertyDomainAxiom) a); 
				else if (a instanceof OWLObjectPropertyRangeAxiom)
					addLinks((OWLObjectPropertyRangeAxiom) a);
				else if (a instanceof OWLDataPropertyDomainAxiom) 
					addLinks((OWLDataPropertyDomainAxiom) a); 
				else if (a instanceof OWLDataPropertyRangeAxiom)
					addLinks((OWLDataPropertyRangeAxiom) a);
				else if (a instanceof OWLDeclarationAxiom)
					;		
				else if (a instanceof OWLAnnotationAssertionAxiom)
					;
				else if (a instanceof OWLClassAssertionAxiom)
					;
				else if (a instanceof OWLObjectPropertyAssertionAxiom) 
					;
				else {
					Utility.logError("Unknowledge OWL Axiom: " + a.getClass().getName() + "\n" + a); 
				}
//		Utility.LOGS.info("DONE\n----------------------------"); 
	}
	
	private void addLinks(OWLDisjointClassesAxiom a) {
		for (OWLClassExpression exp: a.getClassExpressions())
			addLinks(exp, m_nothing); 
	}

	private void addLinks(OWLSymmetricObjectPropertyAxiom a) {
		// TODO Auto-generated method stub
		
	}

	private void addLinks(OWLInverseObjectPropertiesAxiom a) {
		// TODO Auto-generated method stub
		
	}

	private void addLinks(OWLDataPropertyRangeAxiom a) {
		addLinks(a.getProperty(), a.getRange()); 		
	}

	private void addLinks(OWLDataPropertyDomainAxiom a) {
		addLinks(a.getProperty(), a.getDomain()); 
	}

	private void addLinks(OWLEquivalentObjectPropertiesAxiom a) {
		for (OWLObjectPropertyExpression exp1: a.getProperties())
			for (OWLObjectPropertyExpression exp2: a.getProperties())
				if(!exp1.equals(exp2))
					addLinks(exp1, exp2); 
	}

	private void addLinkes(OWLTransitiveObjectPropertyAxiom a) {
		addLinks(a.getProperty(), a.getProperty());		
	}

	private void addLinks(OWLObjectPropertyRangeAxiom a) {
		addLinks(a.getProperty(), a.getRange());
	}

	private void addLinks(OWLObjectPropertyDomainAxiom a) {
		addLinks(a.getProperty(), a.getDomain()); 
		
	}

	private void addLinks(OWLEquivalentClassesAxiom a) {
		for (OWLClassExpression exp1: a.getClassExpressions())
			for (OWLClassExpression exp2: a.getClassExpressions())
				if (!exp1.equals(exp2))
					addLinks(exp1, exp2); 
	}

	private void addLinks(OWLSubObjectPropertyOfAxiom a) {
		addLinks(a.getSubProperty(), a.getSuperProperty()); 
	}

	private void addLinks(OWLSubClassOfAxiom a) {
		addLinks(a.getSubClass(), a.getSuperClass());
		
	}
	
	private void addLinks(OWLObject body, OWLObject head) {
		Set<OWLLogicalEntity> bodyEntities = new HashSet<OWLLogicalEntity>();  
		Set<OWLLogicalEntity> headEntities = new HashSet<OWLLogicalEntity>();
		for (OWLClass c: body.getClassesInSignature()) {
			bodyEntities.add(c);
			map.put(c.toStringID(), c); 
		}
		for (OWLObjectProperty p: body.getObjectPropertiesInSignature()) {
			bodyEntities.add(p); 
			map.put(p.toStringID(), p); 
		}
		
		for (OWLClass c: head.getClassesInSignature()) { 
			headEntities.add(c); 
			map.put(c.toStringID(), c); 
		}
		for (OWLObjectProperty p: head.getObjectPropertiesInSignature()) { 
			headEntities.add(p);
			map.put(p.toString(), p); 
		}
		
		for (OWLLogicalEntity subEntity: bodyEntities)
			for (OWLLogicalEntity superEntity: headEntities)
				addLink(subEntity, superEntity); 
	}

	public OWLLogicalEntity getLogicalEntity(String iri) {
		iri = MyPrefixes.PAGOdAPrefixes.expandIRI(iri); 
		return map.get(iri);  
	}
	
	public static void main(String[] args) {
		args = ("/users/yzhou/ontologies/uobm/univ-bench-dl.owl").split("\\ ");
		
		OWLOntology onto = OWLHelper.loadOntology(args[0]); 
		OWLEntityDependency dependency = new OWLEntityDependency(onto); 
		dependency.output(); 
	}
	
}
