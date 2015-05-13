package uk.ac.ox.cs.pagoda.rules.approximators;

import org.semanticweb.HermiT.model.DLClause;

import java.util.Collection;

public interface Approximator {

    Collection<DLClause> convert(DLClause clause, DLClause originalClause);

}

// TODO remove
//class IgnoreExist implements Approximator {
//
//	@Override
//	public Collection<DLClause> convert(DLClause clause, DLClause originalClause) {
//		Collection<DLClause> ret = new LinkedList<DLClause>();
//		DLPredicate p;
//		for (Atom headAtom: clause.getHeadAtoms()) {
//			p = headAtom.getDLPredicate();
//			if (p instanceof AtLeast) return ret;
//		}
//
//		ret.add(clause);
//		return ret;
//	}
//
//}
//
//
//
//class IgnoreDisj implements Approximator {
//
//	@Override
//	public Collection<DLClause> convert(DLClause clause, DLClause originalClause) {
//		Collection<DLClause> ret = new LinkedList<DLClause>();
//		if (clause.getHeadLength() > 1) return ret;
//		ret.add(clause);
//		return ret;
//	}
//}
