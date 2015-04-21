package uk.ac.ox.cs.pagoda.constraints;

import java.util.Collection;

import org.semanticweb.HermiT.model.Atom;
import org.semanticweb.HermiT.model.DLClause;
import org.semanticweb.HermiT.model.Term;

public interface BottomStrategy {

	public Collection<DLClause> process(Collection<DLClause> clauses);
	
	public boolean isBottomRule(DLClause clause);
	
	public Atom[] getEmptyHead(Term t);

	public int getBottomNumber(); 

}
