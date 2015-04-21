package uk.ac.ox.cs.pagoda.constraints;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

import org.semanticweb.HermiT.model.Atom;
import org.semanticweb.HermiT.model.AtomicConcept;
import org.semanticweb.HermiT.model.DLClause;
import org.semanticweb.HermiT.model.Term;
import org.semanticweb.HermiT.model.Variable;

public class UpperUnaryBottom extends UnaryBottom {
	
	static final Variable X = Variable.create("X"); 
	
	Map<DLClause, Integer> number = new HashMap<DLClause, Integer>(); 
	
	@Override
	public Collection<DLClause> process(Collection<DLClause> clauses) {
		Collection<DLClause> ret = new LinkedList<DLClause>(); 
		for (DLClause clause: clauses) 
			if (clause.getHeadLength() == 0) {
				ret.add(DLClause.create(getEmptyHead(pickRepresentative(clause.getBodyAtoms()), clause), clause.getBodyAtoms())); 
				ret.add(DLClause.create(getEmptyHead(X), getEmptyHead(X, clause))); 
			}
			else 
				ret.add(clause); 
		return ret;
	}

	@Override
	public boolean isBottomRule(DLClause clause) {
		return clause.getHeadLength() == 1 && clause.getHeadAtom(0).getDLPredicate().toString().contains(AtomicConcept.NOTHING.toString());
	}

	public Atom[] getEmptyHead(Term t, DLClause clause) {
		Integer index = number.get(clause); 
		if (index == null) {
			number.put(clause, index = number.size() + 1); 
		}
		
		return new Atom[] {Atom.create(AtomicConcept.create(AtomicConcept.NOTHING.getIRI() + index), t)};
	}
	
	@Override
	public int getBottomNumber() {
		return number.size() + 1; 
	}

}
