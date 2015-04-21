package uk.ac.ox.cs.pagoda.constraints;

import java.util.Collection;
import java.util.LinkedList;

import org.semanticweb.HermiT.model.Atom;
import org.semanticweb.HermiT.model.DLClause;
import org.semanticweb.HermiT.model.Term;

public class ToBeRemovedBottom implements BottomStrategy {

	@Override
	public Collection<DLClause> process(Collection<DLClause> clauses) {
		Collection<DLClause> ret = new LinkedList<DLClause>(); 
		for (DLClause clause: clauses)
			if (clause.getHeadLength() != 0)
				ret.add(clause); 
		return ret;
	}

	@Override
	public boolean isBottomRule(DLClause clause) {
		return false;
	}

	@Override
	public Atom[] getEmptyHead(Term t) {
		return null;
	}

	@Override
	public int getBottomNumber() {
		return 0;
	}

}
