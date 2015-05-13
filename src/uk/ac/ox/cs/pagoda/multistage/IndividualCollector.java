package uk.ac.ox.cs.pagoda.multistage;

import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.Value;
import org.openrdf.model.impl.URIImpl;
import org.openrdf.rio.RDFHandler;
import org.openrdf.rio.RDFHandlerException;
import uk.ac.ox.cs.JRDFox.model.Individual;
import uk.ac.ox.cs.pagoda.rules.approximators.SkolemTermsManager;
import uk.ac.ox.cs.pagoda.util.Namespace;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public class IndividualCollector implements RDFHandler {

	boolean addedSkolemised = false; 
	Set<Individual> individuals = new HashSet<Individual>(); 
	
	@Override
	public void startRDF() throws RDFHandlerException {
		// TODO Auto-generated method stub

	}

	@Override
	public void endRDF() throws RDFHandlerException {
		// TODO Auto-generated method stub

	}

	@Override
	public void handleNamespace(String prefix, String uri)
			throws RDFHandlerException {
		// TODO Auto-generated method stub

	}

	@Override
	public void handleStatement(Statement st) throws RDFHandlerException {
		Resource sub = st.getSubject(); 
		if (sub instanceof URIImpl)
			individuals.add(Individual.create(sub.toString()));
		if (!st.getPredicate().toString().equals(Namespace.RDF_TYPE)) {
			Value obj = st.getObject();
			if (obj instanceof URIImpl)
				individuals.add(Individual.create(sub.toString()));
		}
	}

	@Override
	public void handleComment(String comment) throws RDFHandlerException {
		// TODO Auto-generated method stub

	}

	public Collection<Individual> getAllIndividuals() {
		if (!addedSkolemised) {
			int number = SkolemTermsManager.getInstance().getNumberOfSkolemisedIndividual();
			for (int i = 0; i < number; ++i)
				individuals.add(Individual.create(SkolemTermsManager.skolemisedIndividualPrefix + i));
			addedSkolemised = true; 
		}
		return individuals; 
	}

}
