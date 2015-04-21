package uk.ac.ox.cs.pagoda.multistage;

import java.util.HashMap;
import java.util.Map;

import org.semanticweb.HermiT.model.AnnotatedEquality;
import org.semanticweb.HermiT.model.Atom;
import org.semanticweb.HermiT.model.DLPredicate;
import org.semanticweb.HermiT.model.Equality;
import org.semanticweb.HermiT.model.Variable;
import uk.ac.ox.cs.pagoda.util.Namespace;

import uk.ac.ox.cs.JRDFox.JRDFStoreException;
import uk.ac.ox.cs.JRDFox.store.TupleIterator;

public class EqualityManager {
	
	private Map<Integer, Integer> equalityGroups = new HashMap<Integer, Integer>();
	private static final String equalitySPARQLQuery = "select ?x ?y where { ?x " + Namespace.EQUALITY_QUOTED + "?y . }"; 
	
	public boolean update(MultiStageQueryEngine engine, boolean incrementally) {
		TupleIterator tuples = null;
		AnswerTupleID tuple;
		boolean updated = false; 
		try {
			tuples = engine.internal_evaluate(equalitySPARQLQuery, incrementally);
			for (; tuples.isValid(); tuples.getNext()) {
				tuple = new AnswerTupleID(tuples); 
				if (merge(tuple.getTerm(0), tuple.getTerm(1)))
					updated = true; 
			}
		} catch (JRDFStoreException e) {
			e.printStackTrace();
		} finally {
			if (tuples != null) tuples.dispose();
		}
		return updated; 
	}

	public boolean merge(int t1, int t2) {
		t1 = find(t1); t2 = find(t2);
		if (t1 == t2) return false; 
		equalityGroups.put(t1, t2); 
		return true; 
	}

	public int find(int u) {
		Integer v, w = u;
		while ((v = equalityGroups.get(u)) != null) 
			u = v; 
		
		while ((v = equalityGroups.get(w)) != null) {
			equalityGroups.put(w, u);
			w = v; 
		}
		
		return u; 
	}

	public boolean isFreeEqualityAtom(Atom atom) {
		DLPredicate p = atom.getDLPredicate(); 
		if (p instanceof Equality || p instanceof AnnotatedEquality)
			if (atom.getArgument(0) instanceof Variable && atom.getArgument(1) instanceof Variable)
				return true; 
		return false;
	}


}
