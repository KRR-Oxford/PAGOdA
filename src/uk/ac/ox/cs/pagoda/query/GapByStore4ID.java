package uk.ac.ox.cs.pagoda.query;

import uk.ac.ox.cs.JRDFox.JRDFStoreException;
import uk.ac.ox.cs.JRDFox.store.DataStore;
import uk.ac.ox.cs.JRDFox.store.TupleIterator;
import uk.ac.ox.cs.pagoda.MyPrefixes;
import uk.ac.ox.cs.pagoda.reasoner.light.BasicQueryEngine;
import uk.ac.ox.cs.pagoda.reasoner.light.RDFoxTripleManager;
import uk.ac.ox.cs.pagoda.util.Namespace;
import uk.ac.ox.cs.pagoda.util.Timer;
import uk.ac.ox.cs.pagoda.util.Utility;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

//import uk.ac.ox.cs.pagoda.multistage.AnswerTupleID;

//public class GapByStore4ID extends GapTupleIterator<AnswerTupleID> {
public class GapByStore4ID extends GapTupleIterator<int[]> {

	protected MyPrefixes prefixes = MyPrefixes.PAGOdAPrefixes;
	protected TupleIterator iterator = null; 
	
//	AnswerTupleID tuple;
protected int[] tuple;
	protected BasicQueryEngine m_engine;
	protected DataStore m_store;
	protected RDFoxTripleManager tripleManager;
	protected long multi;
	Map<Integer, Integer> original2gap = new HashMap<Integer, Integer>();
	LinkedList<String> predicatesWithGap = new LinkedList<String>();
	
	public GapByStore4ID(BasicQueryEngine engine) {
		m_engine = engine;
		m_store = engine.getDataStore();
		tripleManager = new RDFoxTripleManager(m_store, false);
	}
	
	@Override
	public void compile(String program) throws JRDFStoreException {
		clear();

		boolean incrementally = true;
		Timer t = new Timer();
		long oldTripleCount = m_store.getTriplesCount();

		if (program != null) {
//			m_store.addRules(new String[] {program});
			m_store.importRules(program);
			incrementally = false;
		}

		m_store.applyReasoning(incrementally);

		long tripleCount = m_store.getTriplesCount();

		Utility.logDebug("current store after materialising upper related rules: " + tripleCount + " (" + (tripleCount - oldTripleCount) + " new)",
				"current store finished the materialisation of upper related rules in " + t.duration() + " seconds.");

		m_engine.setExpandEquality(false);
		iterator = m_engine.internal_evaluateAgainstIDBs("select ?x ?y ?z where { ?x ?y ?z . }");
		m_engine.setExpandEquality(true);

		multi = iterator.open();
		Utility.logDebug("gap query evaluted ...");
	}
	
	@Override
	public boolean hasNext() {
		if(iterator == null) return false;
		try {
//			tuple = new AnswerTupleID(3);
			tuple = new int[3];
			Integer predicate;
			for (; multi != 0; multi = iterator.getNext()) {
				for (int i = 0; i < 3; ++i)
//					tuple.setTerm(i, (int) iterator.getResourceID(i));
					tuple[i] = iterator.getResourceID(i);

				if (isRDF_TYPE()) {
//					predicate = getGapPredicateID(tuple.getTerm(2));
					predicate = getGapPredicateID(tuple[2]);
					if(predicate == null) continue;
//					tuple.setTerm(2, predicate);
					tuple[2] = predicate;
				}
				else {
//					predicate = getGapPredicateID(tuple.getTerm(1));
					predicate = getGapPredicateID(tuple[1]);
					if(predicate == null) continue;
//					tuple.setTerm(1, predicate);
					tuple[1] = predicate;
				}
				return true;
			}
		} catch (JRDFStoreException e) {
			e.printStackTrace();
			return false;
		}
		return false;
	}

	@Override
//	public AnswerTupleID next() {
	public int[] next() {
		try {
			multi = iterator.getNext();
		} catch (JRDFStoreException e) {
			e.printStackTrace();
		}

		return tuple;
	}
	
	public LinkedList<String> getPredicatesWithGap() {
		return predicatesWithGap; 
	}

	protected Integer getGapPredicateID(int originalID) {
		Integer gapID; 
		if ((gapID = original2gap.get(originalID)) != null) 
			return gapID;
		
		String originalPredicate = tripleManager.getRawTerm(originalID);
		if (isAuxPredicate(originalPredicate)) {
//			Utility.LOGS.info(originalPredicate); 
			return null; 
		}

		predicatesWithGap.add(originalPredicate);  
		String gapPredicate = prefixes.expandIRI(getGapPredicate(originalPredicate));
		gapID = tripleManager.getResourceID(gapPredicate);
		original2gap.put(originalID, gapID); 
		
		return gapID;
	}

	protected boolean isAuxPredicate(String originalPredicate) {
		if(originalPredicate.equals(Namespace.EQUALITY_QUOTED)) return false;
		return originalPredicate.contains("_AUX") ||
				originalPredicate.startsWith("<" + Namespace.OWL_NS) ||
				originalPredicate.startsWith("<" + Namespace.PAGODA_ORIGINAL);
	}

	protected boolean isRDF_TYPE() {
//		return tripleManager.isRdfTypeID(tuple.getTerm(1)); 
		return tripleManager.isRdfTypeID(tuple[1]); 
	}

	@Override
	public void remove() {
		Utility.logError("Unsupported operation!"); 
	}
	
	@Override
	public void save(String file) {
		Utility.logError("Unsupported Operation..."); 
	}
	
	@Override
	public void addBackTo() throws JRDFStoreException { 
		int tupleCounter = 0;
		Timer t = new Timer(); 
		long oldTripleCounter; 
		Utility.logDebug("current store before importing gap tuples: " + (oldTripleCounter = m_store.getTriplesCount())); 
		while (hasNext()) {
			next(); 
			++tupleCounter;
			tripleManager.addTripleByID(tuple);
		}

		long tripleCounter = m_store.getTriplesCount(); 
		Utility.logDebug("There are " + tupleCounter + " tuples in the gap between lower and upper bound materialisation.", 
				"current store after importing gap tuples: " + tripleCounter + " (" + (tripleCounter - oldTripleCounter) + ").", 
				"current store finished importing gap tuples: " + tripleCounter + " in " + t.duration() + "."); 
	}
	
	public void clear() {
		if (iterator != null) {
			iterator.dispose();
			iterator = null; 
		}
	}
		
	@Override
	public void addTo(DataStore store) throws JRDFStoreException {
		Utility.logError("Unsupported Operation..."); 
	}
}
