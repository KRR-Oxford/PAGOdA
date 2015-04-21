package org.semanticweb.karma2.profile;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.semanticweb.owlapi.model.AxiomType;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLDataHasValue;
import org.semanticweb.owlapi.model.OWLDataIntersectionOf;
import org.semanticweb.owlapi.model.OWLDataOneOf;
import org.semanticweb.owlapi.model.OWLDataProperty;
import org.semanticweb.owlapi.model.OWLDataPropertyAssertionAxiom;
import org.semanticweb.owlapi.model.OWLDataPropertyDomainAxiom;
import org.semanticweb.owlapi.model.OWLDataPropertyRangeAxiom;
import org.semanticweb.owlapi.model.OWLDataSomeValuesFrom;
import org.semanticweb.owlapi.model.OWLEquivalentDataPropertiesAxiom;
import org.semanticweb.owlapi.model.OWLFunctionalDataPropertyAxiom;
import org.semanticweb.owlapi.model.OWLHasKeyAxiom;
import org.semanticweb.owlapi.model.OWLNegativeDataPropertyAssertionAxiom;
import org.semanticweb.owlapi.model.OWLNegativeObjectPropertyAssertionAxiom;
import org.semanticweb.owlapi.model.OWLObjectHasSelf;
import org.semanticweb.owlapi.model.OWLObjectMinCardinality;
import org.semanticweb.owlapi.model.OWLObjectOneOf;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.model.OWLReflexiveObjectPropertyAxiom;
import org.semanticweb.owlapi.model.OWLSubDataPropertyOfAxiom;
import org.semanticweb.owlapi.model.OWLSubPropertyChainOfAxiom;
import org.semanticweb.owlapi.model.OWLTransitiveObjectPropertyAxiom;
import org.semanticweb.owlapi.profiles.OWL2ELProfile;
import org.semanticweb.owlapi.profiles.OWLProfile;
import org.semanticweb.owlapi.profiles.OWLProfileReport;
import org.semanticweb.owlapi.profiles.OWLProfileViolation;
import org.semanticweb.owlapi.profiles.UseOfDataOneOfWithMultipleLiterals;
import org.semanticweb.owlapi.profiles.UseOfIllegalAxiom;
import org.semanticweb.owlapi.profiles.UseOfIllegalClassExpression;
import org.semanticweb.owlapi.profiles.UseOfObjectOneOfWithMultipleIndividuals;
import org.semanticweb.owlapi.util.OWLObjectPropertyManager;
import org.semanticweb.owlapi.util.OWLOntologyWalker;
import org.semanticweb.owlapi.util.OWLOntologyWalkerVisitor;
import uk.ac.ox.cs.pagoda.util.Utility;

public class ELHOProfile implements OWLProfile {
	
	public OWLOntology getFragment(OWLOntology ontology) {
		OWLOntologyManager manager = ontology.getOWLOntologyManager();
		OWLOntology elhoOntology = null;
		try {
			Utility.logDebug("OntologyID: " + ontology.getOntologyID()); 
			try {
				String ontologyIRI = ontology.getOntologyID().getOntologyIRI().toString();
				if (ontologyIRI.contains(".owl"))
					ontologyIRI = ontologyIRI.replace(".owl", "-elho.owl");
				else 
					ontologyIRI = ontologyIRI + "elho";
				elhoOntology = manager.createOntology(IRI.create(ontologyIRI));
			} catch (NullPointerException e) {
//				e.printStackTrace();
				elhoOntology = manager.createOntology(); 
			}
			
		} catch (OWLOntologyCreationException e) {
			e.printStackTrace();
		}
		for (OWLOntology onto: ontology.getImportsClosure()) 
			manager.addAxioms(elhoOntology, onto.getAxioms());

		// TODO to be checked ...
		manager.removeAxioms(elhoOntology, elhoOntology.getAxioms(AxiomType.DIFFERENT_INDIVIDUALS));
		
		OWLProfileReport report = checkOntology(elhoOntology);

		for (OWLProfileViolation violation: report.getViolations()) {
			OWLAxiom axiom = violation.getAxiom();
			manager.removeAxiom(elhoOntology, axiom);
		}
		Utility.logDebug("ELHO fragment extracted ... ");
		
		return elhoOntology; 
	}

	@Override
	public OWLProfileReport checkOntology(OWLOntology ontology) {
		OWL2ELProfile profile = new OWL2ELProfile();
		OWLProfileReport report = profile.checkOntology(ontology);
		Set<OWLProfileViolation> violations = new HashSet<OWLProfileViolation>();
        violations.addAll(report.getViolations());
        MyOWLOntologyWalker ontologyWalker = new MyOWLOntologyWalker(ontology.getImportsClosure());
        ELHOProfileObjectVisitor visitor = new ELHOProfileObjectVisitor(ontologyWalker, ontology.getOWLOntologyManager());
        ontologyWalker.walkStructure(visitor);

        for (Iterator<OWLProfileViolation> iter = violations.iterator(); iter.hasNext(); ) {
        	OWLProfileViolation vio = iter.next(); 
        	if (vio instanceof UseOfIllegalClassExpression) {
        		OWLClassExpression exp = ((UseOfIllegalClassExpression) vio).getOWLClassExpression(); 
        		if (exp instanceof OWLObjectMinCardinality && ((OWLObjectMinCardinality) exp).getCardinality() == 1)
        			iter.remove(); 
        	}
        }
        
        violations.addAll(visitor.getProfileViolations());
		return new OWLProfileReport(this, violations);
	}

	@Override
	public String getName() {
		return "ELHO";
	}
	
	 protected class ELHOProfileObjectVisitor extends OWLOntologyWalkerVisitor<Object> {

	        private final OWLOntologyManager man;

	        private OWLObjectPropertyManager propertyManager;

	        private final Set<OWLProfileViolation> profileViolations = new HashSet<OWLProfileViolation>();

	        public ELHOProfileObjectVisitor(OWLOntologyWalker walker, OWLOntologyManager man) {
	            super(walker);
	            this.man = man;
	        }

	        public Set<OWLProfileViolation> getProfileViolations() {
	            return new HashSet<OWLProfileViolation>(profileViolations);
	        }

	        @SuppressWarnings("unused")
			private OWLObjectPropertyManager getPropertyManager() {
	            if (propertyManager == null) {
	                propertyManager = new OWLObjectPropertyManager(man, getCurrentOntology());
	            }
	            return propertyManager;
	        }


	        
	        @Override
			public Object visit(OWLDataProperty p) {
	            profileViolations.add(new UseOfIllegalAxiom(getCurrentOntology(), getCurrentAxiom()));
	            return null;
	        }

	       
	        @Override
			public Object visit(OWLObjectOneOf desc) {
	            if (desc.getIndividuals().size() != 1) {
	                profileViolations.add(new UseOfObjectOneOfWithMultipleIndividuals(getCurrentOntology(), getCurrentAxiom(), desc));
	            }
	            return null;
	        }
	        
	        @Override
			public Object visit(OWLDataHasValue desc) {
	                profileViolations.add(new UseOfIllegalClassExpression(getCurrentOntology(), getCurrentAxiom(), desc));
	            return null;
	        }
	        
	        @Override
			public Object visit(OWLDataSomeValuesFrom desc) {
	                profileViolations.add(new UseOfIllegalClassExpression(getCurrentOntology(), getCurrentAxiom(), desc));
	            return null;
	        }
	        
	        @Override
			public Object visit(OWLDataIntersectionOf desc) {
	                profileViolations.add(new UseOfIllegalAxiom(getCurrentOntology(), getCurrentAxiom()));
	            return null;
	        }
	        
	        @Override
			public Object visit(OWLSubDataPropertyOfAxiom desc) {
	                profileViolations.add(new UseOfIllegalAxiom(getCurrentOntology(), getCurrentAxiom()));
	            return null;
	        }
	        
	        @Override
			public Object visit(OWLEquivalentDataPropertiesAxiom desc) {
	                profileViolations.add(new UseOfIllegalAxiom(getCurrentOntology(), getCurrentAxiom()));
	            return null;
	        }
	        
	        @Override
			public Object visit(OWLTransitiveObjectPropertyAxiom desc) {
	                profileViolations.add(new UseOfIllegalAxiom(getCurrentOntology(), getCurrentAxiom()));
	            return null;
	        }
	        
	        @Override
			public Object visit(OWLReflexiveObjectPropertyAxiom desc) {
	                profileViolations.add(new UseOfIllegalAxiom(getCurrentOntology(), getCurrentAxiom()));
	            return null;
	        }
	        
	        @Override
			public Object visit(OWLDataPropertyDomainAxiom desc) {
	                profileViolations.add(new UseOfIllegalAxiom(getCurrentOntology(), getCurrentAxiom()));
	            return null;
	        }
	        
	        @Override
			public Object visit(OWLDataPropertyRangeAxiom desc) {
	                profileViolations.add(new UseOfIllegalAxiom(getCurrentOntology(), getCurrentAxiom()));
	            return null;
	        }
	        
	        
	        
	        @Override
			public Object visit(OWLDataPropertyAssertionAxiom desc) {
	                profileViolations.add(new UseOfIllegalAxiom(getCurrentOntology(), getCurrentAxiom()));
	            return null;
	        }
	        
	        @Override
			public Object visit(OWLNegativeDataPropertyAssertionAxiom desc) {
	                profileViolations.add(new UseOfIllegalAxiom(getCurrentOntology(), getCurrentAxiom()));
	            return null;
	        }
	        
	        @Override
			public Object visit(OWLNegativeObjectPropertyAssertionAxiom desc) {
	                profileViolations.add(new UseOfIllegalAxiom(getCurrentOntology(), getCurrentAxiom()));
	            return null;
	        }
	        
	        @Override
			public Object visit(OWLFunctionalDataPropertyAxiom desc) {
	                profileViolations.add(new UseOfIllegalAxiom(getCurrentOntology(), getCurrentAxiom()));
	            return null;
	        }
	        
	        @Override
			public Object visit(OWLHasKeyAxiom desc) {
	                profileViolations.add(new UseOfIllegalAxiom(getCurrentOntology(), getCurrentAxiom()));
	            return null;
	        }
	       
	        
	        
	        
	        @Override
			public Object visit(OWLObjectHasSelf node) {
	               profileViolations.add(new UseOfIllegalClassExpression(getCurrentOntology(), getCurrentAxiom(), node));
	            return null;
	        }

	        
	        @Override
			public Object visit(OWLDataOneOf node) {
	               profileViolations.add(new UseOfDataOneOfWithMultipleLiterals(getCurrentOntology(), getCurrentAxiom(), node));
	            return null;
	        }

	        

	        @Override
			public Object visit(OWLSubPropertyChainOfAxiom axiom) {
	        	profileViolations.add(new UseOfIllegalAxiom(getCurrentOntology(), getCurrentAxiom()));

	            return null;
	        }

	        @Override
			public Object visit(OWLOntology ontology) {
	            propertyManager = null;
	            return null;
	        }
	    }

	@Override
	public IRI getIRI() {
		return null;
	}


}

