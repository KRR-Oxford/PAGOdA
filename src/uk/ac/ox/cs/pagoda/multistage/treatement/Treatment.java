package uk.ac.ox.cs.pagoda.multistage.treatement;

import uk.ac.ox.cs.pagoda.multistage.Violation;

import uk.ac.ox.cs.JRDFox.JRDFStoreException;

public interface Treatment {

	public boolean makeSatisfied(Violation violation) throws JRDFStoreException;  
	
	public void dispose();

	public void addAdditionalGapTuples(); 
}
