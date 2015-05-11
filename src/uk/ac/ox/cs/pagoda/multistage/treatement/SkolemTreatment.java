package uk.ac.ox.cs.pagoda.multistage.treatement;

import uk.ac.ox.cs.JRDFox.JRDFStoreException;
import uk.ac.ox.cs.pagoda.multistage.FoldedApplication;
import uk.ac.ox.cs.pagoda.multistage.MultiStageQueryEngine;
import uk.ac.ox.cs.pagoda.multistage.Violation;

// TODO should I implement something like this?
public class SkolemTreatment implements Treatment {

	public SkolemTreatment(MultiStageQueryEngine multiStageQueryEngine,	FoldedApplication program) {
		// TODO Auto-generated constructor stub
	}

	@Override
	public boolean makeSatisfied(Violation violation) throws JRDFStoreException {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void dispose() {
		// TODO Auto-generated method stub

	}

	@Override
	public void addAdditionalGapTuples() {
		// TODO Auto-generated method stub

	}

}
