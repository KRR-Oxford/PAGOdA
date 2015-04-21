package org.semanticweb.karma2.model;

import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.semanticweb.HermiT.model.Atom;
import org.semanticweb.HermiT.model.Term;
import org.semanticweb.HermiT.model.Variable;

import uk.ac.ox.cs.JRDFox.Prefixes;


public class ExtendedConjunctiveQuery extends ConjunctiveQuery {

	private Term[] ansTerms;
	private Term[] terms;

	public ExtendedConjunctiveQuery(
			Atom[] queryAtoms, Term[] answerTerms, Prefixes pref) {
		super(queryAtoms, getExtendedHead(queryAtoms, answerTerms), pref);
		this.ansTerms = answerTerms.clone(); 
		terms = getQueryTerms(queryAtoms);
	}

	public int getNumberOfRealAnswerTerms() {
		return ansTerms.length;
	}

	public Term getRealAnswerTerm(int termIndex) {
		return ansTerms[termIndex];
	}

	public int getNumberOfTerms() {
		return terms.length;
	}

	public Term[] getTerms() {
		return terms;
	}
	
	
	public Atom[] getAtoms() {
		return m_queryAtoms;
	}
	
	public Term[] getRealAnswerTerms() {
		return ansTerms;
	}

	private static  Term[] getExtendedHead(Atom[] queryAtoms, Term[] answerTerms) {
		List<Term> terms = new LinkedList<Term>();
		for (Term t :answerTerms) {
			terms.add(t);
		}
		for (Atom a : queryAtoms) {
			if (a.getArgument(0) instanceof Variable && !terms.contains(a.getArgument(0)))
				terms.add(a.getArgument(0));
			if (a.getArity()> 1 && a.getArgument(1) instanceof Variable && !terms.contains(a.getArgument(1)))
				terms.add(a.getArgument(1));
		}
		return terms.toArray(new Term[terms.size()]);
		
	}
	
	private static  Term[] getQueryTerms(Atom[] queryAtoms) {
		Set<Term> terms = new LinkedHashSet<Term>();
		for (Atom a : queryAtoms) {
			terms.add(a.getArgument(0));
			if (a.getArity()> 1)
				terms.add(a.getArgument(1));
		}
		return terms.toArray(new Term[terms.size()]);
	}
	
	
	public static ExtendedConjunctiveQuery computeExtension(ConjunctiveQuery q) {
		Term[] answerTerms = new Term[q.getNumberOfAnswerTerms()];
		for (int i = 0; i < q.getNumberOfAnswerTerms(); i++)
			answerTerms[i] = q.getAnswerTerm(i);
		Atom[] atoms = new Atom[q.getNumberOfQueryAtoms()];
		for (int i = 0; i < q.getNumberOfQueryAtoms(); i++)
			atoms[i] = q.getQueryAtom(i);
		return new ExtendedConjunctiveQuery(atoms, answerTerms,q.prefixes);
		
	}

	public Term getTerm(int i) {
		return terms[i];
	}
	

}
