package uk.ac.ox.cs.pagoda.query;

import uk.ac.ox.cs.JRDFox.JRDFStoreException;
import uk.ac.ox.cs.JRDFox.store.DataStore;

import java.util.Iterator;

public abstract class GapTupleIterator<T> implements Iterator<T> {
	
	public static final String gapPredicateSuffix = "_AUXg"; 
	
	public static final String getGapPredicate(String predicateIRI) {
		if (predicateIRI.startsWith("<"))
			return predicateIRI.replace(">", gapPredicateSuffix + ">"); 
		return predicateIRI + gapPredicateSuffix;
	}
	
	public void compile(String programText) throws JRDFStoreException {}
	
	public abstract void save(String file); 
	
	public abstract void addBackTo() throws JRDFStoreException;
	
	public abstract void addTo(DataStore store) throws JRDFStoreException;
	
	public abstract void clear();

}
