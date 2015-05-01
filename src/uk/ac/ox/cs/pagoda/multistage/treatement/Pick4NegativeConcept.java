package uk.ac.ox.cs.pagoda.multistage.treatement;

import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;

import org.semanticweb.HermiT.model.AnnotatedEquality;
import org.semanticweb.HermiT.model.Atom;
import org.semanticweb.HermiT.model.AtomicConcept;
import org.semanticweb.HermiT.model.AtomicRole;
import org.semanticweb.HermiT.model.DLClause;
import org.semanticweb.HermiT.model.DLPredicate;
import org.semanticweb.HermiT.model.Equality;
import org.semanticweb.HermiT.model.Inequality;
import org.semanticweb.HermiT.model.Variable;

import uk.ac.ox.cs.pagoda.constraints.PredicateDependency;
import uk.ac.ox.cs.pagoda.hermit.DLClauseHelper;
import uk.ac.ox.cs.pagoda.multistage.AnswerTupleID;
import uk.ac.ox.cs.pagoda.multistage.MultiStageQueryEngine;
import uk.ac.ox.cs.pagoda.multistage.MultiStageUpperProgram;
import uk.ac.ox.cs.pagoda.multistage.Violation;
import uk.ac.ox.cs.pagoda.query.GapTupleIterator;
import uk.ac.ox.cs.pagoda.reasoner.light.RDFoxTripleManager;
import uk.ac.ox.cs.pagoda.util.Namespace;
import uk.ac.ox.cs.pagoda.util.SparqlHelper;
import uk.ac.ox.cs.pagoda.util.Utility;
import uk.ac.ox.cs.JRDFox.JRDFStoreException;
import uk.ac.ox.cs.JRDFox.store.TupleIterator;

public abstract class Pick4NegativeConcept implements Treatment {
	
	MultiStageQueryEngine engine; 
	MultiStageUpperProgram program; 
	RDFoxTripleManager tripleManager; 
	PredicateDependency dependencyGraph;
	boolean addGap = false; 
	
	public Pick4NegativeConcept(MultiStageQueryEngine store, MultiStageUpperProgram multiProgram) {
		this.engine = store; 
		this.program = multiProgram;
		this.tripleManager = new RDFoxTripleManager(store.getDataStore(), true);
	}
	
	void addTripleByID(Atom atom, Atom gapAtom, Map<Variable, Integer> assignment) {
		int[] newTuple = tripleManager.getInstance(atom, assignment); 
		tripleManager.addTripleByID(newTuple);
		if (addGap) 
			tripleManager.addTripleByID(tripleManager.getInstance(gapAtom, assignment));
	}

	public Set<Atom> addedGroundAtoms = new HashSet<Atom>(); 

	protected boolean makeSatisfied(Violation violation, Comparator<Atom> comp) {
		LinkedList<AnswerTupleID> tuples = violation.getTuples();
		DLClause constraint = violation.getConstraint();
		Map<Variable, Integer> assignment = new HashMap<Variable, Integer>();
		
		if (constraint.getHeadLength() > 1) {
			Atom[] orderedAtoms = new Atom[constraint.getHeadLength()];
			int index = 0; 
			
			for (Atom headAtom: constraint.getHeadAtoms()) {
				orderedAtoms[index++] = headAtom;
			}
			
			Arrays.sort(orderedAtoms, comp);
	
			Set<AnswerTupleID> negTuples = new HashSet<AnswerTupleID>(); 
			String negativeQuery; 
			String[] subVars; 
			for (Atom headAtom: orderedAtoms) {
				Atom negativeAtom = MultiStageUpperProgram.getNegativeAtom(headAtom); 
				if (negativeAtom == null) continue; 
				negativeQuery = SparqlHelper.getSPARQLQuery(new Atom[] { negativeAtom }, 
						subVars = MultiStageUpperProgram.getVarSubset(violation.getVariables(), headAtom));
				negTuples.clear();
				Atom gapHeadAtom = addGap ? getGapAtom(headAtom) : null; 
				TupleIterator negAnswers = null; 
				try {
					negAnswers = engine.internal_evaluateNotExpanded(negativeQuery); 
					for (long multi = negAnswers.open(); multi != 0; multi = negAnswers.getNext()) 
						negTuples.add(new AnswerTupleID(negAnswers)); 
				} catch (JRDFStoreException e) {
					e.printStackTrace();
				} finally {
					if (negAnswers != null) negAnswers.dispose();
				}

				if (!tuples.isEmpty())
//					program.addUpdatedPredicates(dependencyGraph.getDependence(headAtom.getDLPredicate()));
					program.addUpdatedPredicate(headAtom.getDLPredicate());

				Comparator<AnswerTupleID> tComp = new TupleComparator(subVars); 
				Collections.sort(tuples, tComp);
				
				AnswerTupleID lastAdded = null; 
				
				for (Iterator<AnswerTupleID> iter = tuples.iterator(); iter.hasNext(); ) {
					
					AnswerTupleID tuple = iter.next(); 
					if (negTuples.contains(MultiStageUpperProgram.project(tuple, violation.getVariables(), subVars))) ;
					else {
						if (lastAdded == null || tComp.compare(lastAdded, tuple) != 0) {
							lastAdded = tuple; 
							tuple.getAssignment(violation.getVariables(), assignment);
							addTripleByID(headAtom, gapHeadAtom, assignment);
						}
						iter.remove();
					}
				}
//				tuples.reset();
				
				if (tuples.isEmpty()) 
					return true; 
			}
			
			if (!tuples.isEmpty()) return false; 
			
		}
		else {
			Set<Atom> headAtoms = new HashSet<Atom>(); 
			for (DLClause clause: program.convertExist(constraint, violation.getClause())) {
				if (DLClauseHelper.hasSubsetBodyAtoms(clause, constraint)) {
					Atom tHeadAtom = clause.getHeadAtom(0);
					Atom tGapHeadAtom = addGap ? getGapAtom(tHeadAtom) : null; 
					if (DLClauseHelper.isGround(tHeadAtom)) {
						if (!addedGroundAtoms.contains(tHeadAtom)) {
							program.addUpdatedPredicate(tHeadAtom.getDLPredicate());
							addTripleByID(tHeadAtom, tGapHeadAtom, null);
							addedGroundAtoms.add(tHeadAtom);
						}
					}
					else headAtoms.add(tHeadAtom); 
				}
				else {
					Utility.logError("There might be an error here... Can't happend!!!"); 
				}
			}
			if (!tuples.isEmpty())
				for (Atom atom: headAtoms)
					program.addUpdatedPredicate(atom.getDLPredicate()); 

			for (AnswerTupleID tuple: tuples) { 
				tuple.getAssignment(violation.getVariables(), assignment); 
				for (Atom atom: headAtoms) {
					addTripleByID(atom, getGapAtom(atom), assignment);
				}
			}
		}
		
		assignment.clear(); 
		return true;
	}
	
	private Atom getGapAtom(Atom atom) {
		if (!addGap) return null; 
		String gapPredicate = GapTupleIterator.getGapPredicate(getPredicateIRI(atom.getDLPredicate()));
		Atom gapAtom = atom.getArity() == 1 ? Atom.create(AtomicConcept.create(gapPredicate), atom.getArgument(0)) : 
			Atom.create(AtomicRole.create(gapPredicate), atom.getArgument(0), atom.getArgument(1)); 
		return gapAtom;
	}

	private String getPredicateIRI(DLPredicate dlPredicate) {
		if (dlPredicate instanceof Equality || dlPredicate instanceof AnnotatedEquality)
			return Namespace.EQUALITY; 
		if (dlPredicate instanceof Inequality)
			return Namespace.INEQUALITY; 
		if (dlPredicate instanceof AtomicConcept)
			return ((AtomicConcept) dlPredicate).getIRI(); 
		if (dlPredicate instanceof AtomicRole)
			return ((AtomicRole) dlPredicate).getIRI(); 
		return null; 
	}

	@Override
	public void dispose() {
//		tripleManager.outputAddedInstance();
//		addedGroundAtoms.clear();
	}

	@Override
	public void addAdditionalGapTuples() {
		addGap = true; 
	}
	
}

class TupleComparator implements Comparator<AnswerTupleID> {

	int[] validIndexes; 
	
	public TupleComparator(String[] validTerms) {
		int num = 0; 
		for (int i = 0; i < validTerms.length; ++i)
			if (validTerms[i] != null) 
				++num; 
		validIndexes = new int[num]; 
		for (int i = 0, j = 0; i < validTerms.length; ++i)
			if (validTerms[i] != null)
				validIndexes[j++] = i; 
	}
	
	@Override
	public int compare(AnswerTupleID o1, AnswerTupleID o2) {
		int delta = 0; 
		for (int i = 0; i < validIndexes.length; ++i)
			if ((delta = o1.getTerm(validIndexes[i]) - o2.getTerm(validIndexes[i])) != 0) 
				return delta;
		return 0; 
	}
	
}