package uk.ac.ox.cs.pagoda.ore;

import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.reasoner.IllegalConfigurationException;
import org.semanticweb.owlapi.reasoner.OWLReasoner;
import org.semanticweb.owlapi.reasoner.OWLReasonerConfiguration;
import org.semanticweb.owlapi.reasoner.OWLReasonerFactory;

public class PagodaReasonerFactory implements OWLReasonerFactory {

	@Override
	public String getReasonerName() {
		return "PAGOdA";
	}

	@Override
	public OWLReasoner createNonBufferingReasoner(OWLOntology ontology) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public OWLReasoner createReasoner(OWLOntology ontology) {
		return new PagodaOWLReasoner(ontology);
	}

	@Override
	public OWLReasoner createNonBufferingReasoner(OWLOntology ontology,	OWLReasonerConfiguration config) throws IllegalConfigurationException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public OWLReasoner createReasoner(OWLOntology ontology,	OWLReasonerConfiguration config) throws IllegalConfigurationException {
		// TODO Auto-generated method stub
		return null;
	}

}
