package uk.ac.ox.cs.pagoda.multistage.treatement;

import uk.ac.ox.cs.pagoda.constraints.PredicateDependency;
import uk.ac.ox.cs.pagoda.multistage.MultiStageQueryEngine;
import uk.ac.ox.cs.pagoda.multistage.MultiStageUpperProgram;
import uk.ac.ox.cs.pagoda.multistage.Violation;

import uk.ac.ox.cs.JRDFox.JRDFStoreException;

public class Pick4NegativeConceptNaive extends Pick4NegativeConcept {

	public Pick4NegativeConceptNaive(MultiStageQueryEngine store, MultiStageUpperProgram multiProgram) {
		super(store, multiProgram);
		dependencyGraph = new PredicateDependency(multiProgram.getClauses()); 
	}
	
	SimpleComparator comp = new SimpleComparator(); 
	
	@Override
	public boolean makeSatisfied(Violation violation) throws JRDFStoreException {
		return makeSatisfied(violation, comp);
	}

}

