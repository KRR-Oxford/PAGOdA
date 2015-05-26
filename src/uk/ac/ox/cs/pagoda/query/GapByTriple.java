package uk.ac.ox.cs.pagoda.query;

import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.Collection;

import org.semanticweb.HermiT.model.Atom;
import org.semanticweb.HermiT.model.AtomicConcept;
import org.semanticweb.HermiT.model.AtomicRole;
import org.semanticweb.HermiT.model.DLClause;
import org.semanticweb.HermiT.model.Individual;

import uk.ac.ox.cs.pagoda.MyPrefixes;
import uk.ac.ox.cs.pagoda.owl.OWLHelper;
import uk.ac.ox.cs.pagoda.reasoner.light.BasicQueryEngine;
import uk.ac.ox.cs.pagoda.reasoner.light.RDFoxTripleManager;
import uk.ac.ox.cs.pagoda.util.Namespace;
import uk.ac.ox.cs.pagoda.util.Utility;
import uk.ac.ox.cs.JRDFox.JRDFStoreException;
import uk.ac.ox.cs.JRDFox.Prefixes;
import uk.ac.ox.cs.JRDFox.store.DataStore;
import uk.ac.ox.cs.JRDFox.store.Parameters;
import uk.ac.ox.cs.JRDFox.store.TupleIterator;

public class GapByTriple extends GapTupleIterator<String> {
	
	private static final String RDF_TYPE = Namespace.RDF_NS + "type"; 
	private static final String BRIEF_RDF_TYPE = "rdf:type"; 
	
	static final String allTripleQuery = "SELECT ?X ?Y ?Z WHERE { ?X ?Y ?Z }";
	
	DataStore lowerStore, upperStore;
	long multi; 
	TupleIterator iterator;
	String sub, obj, predicate; 
//	GroundTerm subTerm, objTerm; 
	Prefixes prefixes; 
	Parameters parameters;

	public GapByTriple(BasicQueryEngine lowerStore, BasicQueryEngine upperStore) {
		this.lowerStore = lowerStore.getDataStore(); 
		this.upperStore = upperStore.getDataStore();
		prefixes = MyPrefixes.PAGOdAPrefixes.getRDFoxPrefixes();
		parameters = new Parameters();
	}
	
	public void compile(Collection<DLClause> clauses) throws JRDFStoreException {
		iterator = this.upperStore.compileQuery(allTripleQuery, prefixes, parameters);
		multi = iterator.open();
	}
	
	@Override
	public boolean hasNext() {
		TupleIterator iter = null;
		boolean inGap; 
		StringBuffer queryBuffer = new StringBuffer();
		try {
			for (; multi != 0; multi = iterator.getNext()) {
//			iterator.getRawGroundTerm(0); 
//			iterator.getRawGroundTerm(1); 
//			iterator.getRawGroundTerm(2); 
				
				sub = RDFoxTripleManager.getQuotedTerm(iterator.getResource(0)); 
				predicate = RDFoxTripleManager.getQuotedTerm(iterator.getResource(1));
				obj = RDFoxTripleManager.getQuotedTerm(iterator.getResource(2));
							
				if (!obj.startsWith("<")) {
					// This fragment of code ignores data types assertions.
//					Utility.LOGS.info(sub + " " + predicate + " " + obj); 
					continue;
				}
				
				queryBuffer.setLength(0);
				queryBuffer.append("SELECT WHERE { ").append(sub).append(" ").append(predicate).append(" ").append(obj).append(" }");

				try {
					iter = lowerStore.compileQuery(queryBuffer.toString(), prefixes, parameters);
					inGap = iter.open() != 0;
				} finally {
					if (iter != null) iter.dispose();
					iter = null; 
				}
				if (inGap) 
					return true;
			}
		} catch (JRDFStoreException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return false;
	}
	
	@Override
	public String next() {
		try {
			multi = iterator.getNext();
		} catch (JRDFStoreException e) {
			e.printStackTrace();
		}
		StringBuilder sb = new StringBuilder(); 
		if (isRDF_TYPE()) {
			sb.append(sub).append(" ").append(predicate).append(" ").append(getGapPredicate(obj)).append(".");
		}
		else sb.append(sub).append(" ").append(getGapPredicate(predicate)).append(" ").append(obj).append(".");  
		return sb.toString(); 
	}
	
	private boolean isRDF_TYPE() {
		return predicate.equals(RDF_TYPE) || predicate.equals(BRIEF_RDF_TYPE);
	}

	@Override
	public void remove() {
		Utility.logError("Unsupported operation!"); 
	}
	
	public void save(String file) {
		int tupleCounter = 0; 
		try {
			BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file)));
			String tuple; 
			while (hasNext()) {
				tuple = next(); 
				writer.write(tuple);
				writer.newLine(); 
				++tupleCounter; 
			}
			writer.close(); 
		} catch (IOException e) {
			e.printStackTrace();
		} 
		
		Utility.logError("There are " + tupleCounter + " tuples in the gap between lower and upper bound materialisation."); 
	}

	public void addTo(DataStore store) throws JRDFStoreException {
		int tupleCounter = 0; 
		RDFoxTripleManager tripleManager = new RDFoxTripleManager(store, false); 
		while (hasNext()) {
			multi = iterator.getNext();
			++tupleCounter; 
			if (isRDF_TYPE()) {
				obj = OWLHelper.removeAngles(obj); 
				tripleManager.addTripleByTerm(
							Atom.create(AtomicConcept.create(getGapPredicate(obj)), Individual.create(sub))); 
			}
			else {
				predicate = OWLHelper.removeAngles(predicate);
				tripleManager.addTripleByTerm(
							Atom.create(AtomicRole.create(getGapPredicate(predicate)), Individual.create(sub), Individual.create(obj))); 
			}
			if (tupleCounter % 10000 == 0)
				Utility.logDebug(tupleCounter); 
		}
		
		Utility.logDebug("There are " + tupleCounter + " tuples in the gap between lower and upper bound materialisation."); 
	}

	@Override
	public void addBackTo() throws JRDFStoreException {
		addTo(upperStore);
	}
	
	@Override
	public void clear() {
		iterator.dispose();		
	}
}
