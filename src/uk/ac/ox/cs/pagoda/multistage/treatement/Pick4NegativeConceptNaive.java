package uk.ac.ox.cs.pagoda.multistage.treatement;

import uk.ac.ox.cs.JRDFox.JRDFStoreException;
import uk.ac.ox.cs.pagoda.constraints.PredicateDependency;
import uk.ac.ox.cs.pagoda.multistage.MultiStageQueryEngine;
import uk.ac.ox.cs.pagoda.multistage.MultiStageUpperProgram;
import uk.ac.ox.cs.pagoda.multistage.Violation;
import uk.ac.ox.cs.pagoda.reasoner.light.RDFoxTripleManager;
import uk.ac.ox.cs.pagoda.util.disposable.DisposedException;

import java.util.Set;

public class Pick4NegativeConceptNaive extends Pick4NegativeConcept {

	SimpleComparator comp = new SimpleComparator();

	public Pick4NegativeConceptNaive(MultiStageQueryEngine store, MultiStageUpperProgram multiProgram) {
		super(store, multiProgram);
		dependencyGraph = new PredicateDependency(multiProgram.getClauses());
	}

	public Pick4NegativeConceptNaive(MultiStageQueryEngine store, MultiStageUpperProgram multiProgram, RDFoxTripleManager rdFoxTripleManager) {
		super(store, multiProgram, rdFoxTripleManager);
		dependencyGraph = new PredicateDependency(multiProgram.getClauses());
	}
	
	@Override
	public Set<AtomWithIDTriple> makeSatisfied(Violation violation) throws JRDFStoreException {
		if(isDisposed()) throw new DisposedException();
		return makeSatisfied(violation, comp);
	}
}

