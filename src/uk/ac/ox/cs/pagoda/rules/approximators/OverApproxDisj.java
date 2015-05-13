package uk.ac.ox.cs.pagoda.rules.approximators;

import org.semanticweb.HermiT.model.*;
import uk.ac.ox.cs.pagoda.hermit.DLClauseHelper;

import java.util.*;

public class OverApproxDisj implements Approximator {

	/**
	 * Splits a disjunctive rule into a bunch of rules.
	 * <p>
	 * It returns a collection containing a rule for each atom in the head of the input rule.
	 * Each rule has the same body of the input rule,
	 * and the relative head atom as head.
	 * */
	@Override
	public Collection<DLClause> convert(DLClause clause, DLClause originalClause) {
		LinkedList<DLClause> distincts = new LinkedList<DLClause>();
		Atom[] headAtoms = clause.getHeadAtoms(), bodyAtoms = clause.getBodyAtoms();
		LinkedList<DLClause> newClauses = new LinkedList<DLClause>();
		DLClause newClause;
		if (headAtoms.length > 1) {
			for (Atom headAtom: headAtoms) {
				newClause = DLClause.create(new Atom[] {headAtom}, bodyAtoms);
				newClauses.add(newClause);
//				distincts.add(newClause); 
			}
			
			for (DLClause cls: newClauses) {
				newClause = DLClauseHelper.simplified(cls);
				if (!isSubsumedBy(newClause, distincts)) 
					distincts.add(newClause);
			}
		}
		else distincts.add(clause);
		
		return distincts;
	}

	public static boolean isSubsumedBy(DLClause newClause, Collection<DLClause> distinctClauses) {
		Map<Variable, Term> unifier;
		Set<Atom> bodyAtoms = new HashSet<Atom>();
		for (DLClause clause: distinctClauses) { 
			if (newClause.getHeadLength() > 0 && clause.getHeadLength() > 0 && 
					(unifier = isSubsumedBy(newClause.getHeadAtom(0), clause.getHeadAtom(0))) == null)
				continue;
			else 
				unifier = new HashMap<Variable, Term>();
			
			for (Atom atom: clause.getBodyAtoms())
				bodyAtoms.add(rename(atom, unifier));
			unifier.clear();
			
			for (Atom atom: newClause.getBodyAtoms()) 
				if (!bodyAtoms.contains(atom)) 
					continue;
			
			return true;
		}
		
		return false;
	}
	
	public static Map<Variable, Term> isSubsumedBy(Atom atom1, Atom atom2) {
		DLPredicate predicate = atom1.getDLPredicate();
		if (!predicate.equals(atom2.getDLPredicate()))
			return null;

		Map<Variable, Term> unifier = new HashMap<Variable, Term>();
		Term term1, term2;
		for (int index = 0; index < predicate.getArity(); ++index) {
			term1 = rename(atom1.getArgument(index), unifier);
			term2 = rename(atom2.getArgument(index), unifier);
			
			if (term1.equals(term2));
			else if (term1 instanceof Variable)
					unifier.put((Variable) term1, term2);
			else
				return null;
		}
		return unifier;
	}
	
	public static Atom rename(Atom atom, Map<Variable, Term> unifier) {
		Term[] arguments = new Term[atom.getArity()];
		for (int i = 0; i < atom.getArity(); ++i)
			arguments[i] = rename(atom.getArgument(i), unifier);
		return Atom.create(atom.getDLPredicate(), arguments);
	}

	public static Term rename(Term argument, Map<Variable, Term> unifier) {
		Term newArg;
		while ((newArg = unifier.get(argument)) != null) 
			return newArg;
		return argument;
	}


}
