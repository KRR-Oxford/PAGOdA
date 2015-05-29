package uk.ac.ox.cs.pagoda.multistage.treatement;

import uk.ac.ox.cs.JRDFox.JRDFStoreException;
import uk.ac.ox.cs.pagoda.constraints.PredicateDependency;
import uk.ac.ox.cs.pagoda.multistage.MultiStageQueryEngine;
import uk.ac.ox.cs.pagoda.multistage.MultiStageUpperProgram;
import uk.ac.ox.cs.pagoda.multistage.Violation;
import uk.ac.ox.cs.pagoda.util.disposable.DisposedException;

public class Pick4NegativeConceptNaive extends Pick4NegativeConcept {

	SimpleComparator comp = new SimpleComparator();
	
	public Pick4NegativeConceptNaive(MultiStageQueryEngine store, MultiStageUpperProgram multiProgram) {
		super(store, multiProgram);
		dependencyGraph = new PredicateDependency(multiProgram.getClauses());
	}
	
	@Override
	public boolean makeSatisfied(Violation violation) throws JRDFStoreException {
		if(isDisposed()) throw new DisposedException();
		return makeSatisfied(violation, comp);
	}
}

