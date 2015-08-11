package uk.ac.ox.cs.pagoda.constraints;

import org.semanticweb.HermiT.model.*;

import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;

// TODO -RULES- adapt
public class UnaryBottom implements BottomStrategy {
	
	@Override
	public Collection<DLClause> process(Collection<DLClause> clauses) {
		Collection<DLClause> ret = new LinkedList<DLClause>(); 
		for (DLClause clause: clauses) 
			if (clause.getHeadLength() == 0) {
				ret.add(DLClause.create(getEmptyHead(pickRepresentative(clause.getBodyAtoms())), clause.getBodyAtoms()));
			}
			else 
				ret.add(clause); 
		return ret;
	}

	protected Term pickRepresentative(Atom[] atoms) {
		Term rep = null; 
		Set<Variable> vars = new HashSet<Variable>(); 
		for (Atom atom: atoms) {
			atom.getVariables(vars);
			for (Variable v: vars)
				if (rep == null || ((Variable) rep).getName().compareTo(v.getName()) > 0)
					rep = v;
			vars.clear(); 
		}
		if (rep != null) return rep; 
		
		Set<Individual> inds = new HashSet<Individual>(); 
		for (Atom atom: atoms) {
			atom.getIndividuals(inds);
			for (Individual i: inds)
				if (rep == null || ((Individual) rep).getIRI().compareTo(i.getIRI()) > 0)
					rep = i;
			inds.clear();
		}
		
		return rep;
	}

	@Override
	public boolean isBottomRule(DLClause clause) {
		return clause.getHeadLength() == 1 && clause.getHeadAtom(0).getDLPredicate().equals(AtomicConcept.NOTHING);
	}

	public Atom[] getEmptyHead(Term t) {
		return new Atom[] {Atom.create(AtomicConcept.NOTHING, t)};
	}

	@Override
	public int getBottomNumber() {
		return 1;
	}

}
