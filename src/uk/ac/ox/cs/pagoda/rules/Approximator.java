package uk.ac.ox.cs.pagoda.rules;

import java.util.Collection;
import java.util.LinkedList;

import org.semanticweb.HermiT.model.AtLeast;
import org.semanticweb.HermiT.model.Atom;
import org.semanticweb.HermiT.model.DLClause;
import org.semanticweb.HermiT.model.DLPredicate;

public interface Approximator {

	public Collection<DLClause> convert(DLClause clause, DLClause originalClause); 
	
}

class IgnoreExist implements Approximator {
		
	@Override
	public Collection<DLClause> convert(DLClause clause, DLClause originalClause) {
		Collection<DLClause> ret = new LinkedList<DLClause>(); 
		DLPredicate p; 
		for (Atom headAtom: clause.getHeadAtoms()) {
			p = headAtom.getDLPredicate(); 
			if (p instanceof AtLeast) return ret; 
		}
		
		ret.add(clause);
		return ret; 
	}
	
}

class IgnoreBoth implements Approximator {
	
	@Override
	public Collection<DLClause> convert(DLClause clause, DLClause originalClause) {
		Collection<DLClause> ret = new LinkedList<DLClause>();
		
		if (clause.getHeadLength() > 1) return ret;
		
		if (clause.getHeadLength() > 0) {
			DLPredicate predicate = clause.getHeadAtom(0).getDLPredicate();
			
			if (predicate instanceof AtLeast) return ret; 
		}
		
		ret.add(clause); 
		return ret; 
	}
}

class IgnoreDisj implements Approximator {
	
	@Override
	public Collection<DLClause> convert(DLClause clause, DLClause originalClause) {
		Collection<DLClause> ret = new LinkedList<DLClause>(); 
		if (clause.getHeadLength() > 1) return ret;  
		ret.add(clause); 
		return ret; 
	}
}
