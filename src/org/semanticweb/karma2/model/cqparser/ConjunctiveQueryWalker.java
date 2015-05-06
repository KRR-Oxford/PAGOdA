package org.semanticweb.karma2.model.cqparser;



import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.antlr.runtime.tree.CommonTree;
import org.semanticweb.HermiT.model.Atom;
import org.semanticweb.HermiT.model.AtomicConcept;
import org.semanticweb.HermiT.model.AtomicRole;
import org.semanticweb.HermiT.model.Individual;
import org.semanticweb.HermiT.model.Term;
import org.semanticweb.HermiT.model.Variable;
import org.semanticweb.karma2.exception.IllegalInputQueryException;
import org.semanticweb.karma2.model.ConjunctiveQuery;

import uk.ac.ox.cs.JRDFox.Prefixes;
import uk.ac.ox.cs.pagoda.util.Utility;




public class ConjunctiveQueryWalker {


	public ConjunctiveQueryWalker() {

	}

	@SuppressWarnings("unchecked")
	private List<CommonTree> childrenOf(CommonTree node) {
		return (List<CommonTree>) node.getChildren();
	}
	
	
	private boolean isSafe(Term[] headTerms, Atom[] atoms) {
		for (Term t : headTerms) {
			if (t instanceof Variable) {
				boolean res = false;
				for (Atom a : atoms) {
					if (a.getArity()==1) {
						if (a.getArgument(0).equals(t))
							res = true;
					}
					if (a.getArity()==2) {
						if (a.getArgument(0).equals(t) || a.getArgument(1).equals(t))
							res = true;
					}
				}
				if(!res)
					return false;
			}
		}
		return true;
	}

	
	public ConjunctiveQuery walkExpressionNode(CommonTree ruleNode) throws IllegalInputQueryException {

		assert (ruleNode.getType() == ConjunctiveQueryLexer.EXPRESSION);

		Iterator<CommonTree> iterator = childrenOf(ruleNode).iterator();

		CommonTree prefixList = iterator.next();
		assert (prefixList.getType() == ConjunctiveQueryLexer.PREFIX_LIST);
		Prefixes prefixes = walkPrefixList(prefixList);
		CommonTree rulebody = iterator.next();
		assert (rulebody.getType() == ConjunctiveQueryLexer.RULE);
		return walkRuleNode(rulebody, prefixes);

	}
	
	public Prefixes walkPrefixList(CommonTree prefixlist) throws IllegalInputQueryException {
		assert (prefixlist.getType() == ConjunctiveQueryLexer.PREFIX_LIST);
		Prefixes pref = new Prefixes();
		for (CommonTree prefixNode : childrenOf(prefixlist)) {
			walkPrefixNode(prefixNode, pref);
		}
		return pref;

	}
	
	private void walkPrefixNode(CommonTree prefixNode, Prefixes pref) throws IllegalInputQueryException {
		Iterator<CommonTree> iterator = childrenOf(prefixNode).iterator();
		CommonTree shortID = iterator.next();
		CommonTree longID = iterator.next();
		pref.declarePrefix(shortID.getText() + ":", longID.getText());
	}


	public ConjunctiveQuery walkRuleNode(CommonTree ruleNode, Prefixes prefixes) throws IllegalInputQueryException {

		assert (ruleNode.getType() == ConjunctiveQueryLexer.RULE);

		Iterator<CommonTree> iterator = childrenOf(ruleNode).iterator();

		CommonTree headNode = iterator.next();
		assert (headNode.getType() == ConjunctiveQueryLexer.HEADATOM);
		Term[] headTerms = walkHeadAtomNode(headNode);
		Atom[] atoms = walkAtomList(iterator.next());
		if (!isSafe(headTerms, atoms))
			throw new IllegalInputQueryException("query is not safe");
		return new ConjunctiveQuery(atoms, headTerms, prefixes);

	}

	private Term[] walkHeadAtomNode(CommonTree node) throws IllegalInputQueryException {
		List<Term> terms = new ArrayList<Term>();
		for (CommonTree termNode : childrenOf(node)) {
			terms.add(walkTermNode(termNode));
		}
		return terms.toArray(new Term[terms.size()]);
	}
	
	
	private String walkCompositeId(CommonTree compositeID) {
		Iterator<CommonTree> iterator = childrenOf(compositeID).iterator();
		return iterator.next().getText() + ":" + iterator.next().getText() ;
	}
	
	private String walkSimpleId(CommonTree termNode) {
		Iterator<CommonTree> it = childrenOf(termNode).iterator();
		it.next();
		CommonTree t = it.next();
		return t.getText();
	}
	
	private Term walkTermNode(CommonTree termNode) throws IllegalInputQueryException {
		if (termNode.getType() == ConjunctiveQueryLexer.VARIABLE) {
			return Variable.create("?" + childrenOf(termNode).iterator().next().getText());
		}
		if (termNode.getType() == ConjunctiveQueryLexer.CONSTANT) {
			Individual newind =  Individual.create(walkCompositeId(childrenOf(termNode).iterator().next()));
			Utility.logError(newind);
			return newind;
		}
		if (termNode.getType() == ConjunctiveQueryLexer.SCONSTANT) {
			Individual newind = Individual.create(walkSimpleId(termNode));
			return newind;
		}
		throw new IllegalArgumentException();
	}



	public Atom[] walkAtomList(CommonTree node) throws IllegalInputQueryException {
		assert (node.getType() == ConjunctiveQueryLexer.ATOM_LIST);
		List<Atom> atoms = new ArrayList<Atom>();
		for (CommonTree atomNode : childrenOf(node)) {
			atoms.add(walkAtomNode(atomNode));
		}
		return atoms.toArray(new Atom[atoms.size()]);

	}

	private Atom walkAtomNode(CommonTree atomNode) throws IllegalInputQueryException {
		assert (atomNode.getType() == ConjunctiveQueryLexer.ATOM);
		Iterator<CommonTree> iterator = childrenOf(atomNode).iterator();
		CommonTree id = iterator.next();
		String predicatename = walkCompositeId(id);
		List<Term> listofterms = new ArrayList<Term>();
		while (iterator.hasNext()){
			listofterms.add(walkTermNode(iterator.next()));
		}
		if(listofterms.isEmpty() || (listofterms.size()>2))
			throw new IllegalInputQueryException("Problem parsing terms in the query");
		Term[] terms = listofterms.toArray(new Term[listofterms.size()]);
		if (terms.length == 1) 
			return Atom.create(AtomicConcept.create(predicatename), terms);
		if (terms.length == 2) 
			return Atom.create(AtomicRole.create(predicatename), terms);
		throw new IllegalInputQueryException("Problem parsing terms in the query");
	}

	
}

