package uk.ac.ox.cs.pagoda.constraints;

import java.util.Collection;

import org.semanticweb.HermiT.model.Atom;
import org.semanticweb.HermiT.model.DLClause;
import org.semanticweb.HermiT.model.Term;

public class NullaryBottom implements BottomStrategy {
	
	@Override
	public Collection<DLClause> process(Collection<DLClause> clauses) {
		return clauses;
	}

	@Override
	public boolean isBottomRule(DLClause clause) {
		return clause.getHeadLength() == 0;
	}

	@Override
	public Atom[] getEmptyHead(Term t) {
		return new Atom[0];
	}

	@Override
	public int getBottomNumber() {
		return 1;
	}

}
