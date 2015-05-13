package uk.ac.ox.cs.pagoda.tracking;

import org.semanticweb.HermiT.model.*;
import uk.ac.ox.cs.pagoda.hermit.DLClauseHelper;
import uk.ac.ox.cs.pagoda.reasoner.light.BasicQueryEngine;
import uk.ac.ox.cs.pagoda.rules.UpperDatalogProgram;
import uk.ac.ox.cs.pagoda.rules.approximators.OverApproxExist;
import uk.ac.ox.cs.pagoda.util.Namespace;

import java.util.*;

public abstract class TrackingRuleEncoderDisj extends TrackingRuleEncoderWithGap {

	public TrackingRuleEncoderDisj(UpperDatalogProgram program, BasicQueryEngine store) {
		super(program, store);
	}
	
	protected Map<DLClause, Collection<DLClause>> disjunctiveRules = new HashMap<DLClause, Collection<DLClause>>();

	/**
	 * 
	 */
	protected void processDisjunctiveRules() {
		Map<Atom, Collection<DLClause>> auxiliaryAtoms = new HashMap<Atom, Collection<DLClause>>(); 
		Map<Individual, Collection<DLClause>> skolemisedAtoms = new HashMap<Individual, Collection<DLClause>>(); 
		
		for (Map.Entry<DLClause, Collection<DLClause>> entry: disjunctiveRules.entrySet()) {
			DLClause original = entry.getKey();
			Collection<DLClause> overClauses = entry.getValue(); 
			
			int index = 0; 
			for (Iterator<DLClause> iter = overClauses.iterator(); iter.hasNext();) {
				DLClause subClause = iter.next(); 
				if (DLClauseHelper.hasSubsetBodyAtoms(subClause, original)) {
					Atom headAtom = subClause.getHeadAtom(0);
					if ((index = OverApproxExist.indexOfSkolemisedIndividual(headAtom)) != -1) {
						Individual i = (Individual) headAtom.getArgument(index);
						Collection<DLClause> clauses = skolemisedAtoms.get(i);
						if (clauses == null) {
							clauses = new HashSet<DLClause>(); 
							skolemisedAtoms.put(i, clauses); 
						}
						clauses.add(subClause); 
					}
					else 
						auxiliaryAtoms.put(getAuxiliaryAtom(original, subClause.getHeadAtom(0)), Collections.singleton(subClause));
				}
				else 
					super.encodingRule(subClause);
			}
			
			for (Atom headAtom: original.getHeadAtoms()) 
				if (headAtom.getDLPredicate() instanceof AtLeastConcept) {
					AtLeastConcept alc = (AtLeastConcept) headAtom.getDLPredicate();
					Collection<DLClause> clauses = new HashSet<DLClause>();
					Individual[] individuals = new Individual[alc.getNumber()]; 
					for (int i = 0; i < alc.getNumber(); ++i) {
						individuals[i] = OverApproxExist.getNewIndividual(original, i);
						clauses.addAll(skolemisedAtoms.get(individuals[i])); 
					}
					auxiliaryAtoms.put(getAuxiliaryAtom(original, headAtom, individuals), clauses);
				}

			index = 0; 
			Atom[] auxAtoms = auxiliaryAtoms.keySet().toArray(new Atom[0]);  
			for (Atom atom: auxAtoms) {
				for (DLClause subClause: auxiliaryAtoms.get(atom)) 
					encodingDisjunctiveRule(subClause, index, auxAtoms);
				index++; 
			}
						
			auxiliaryAtoms.clear();
		}
	}
	
	private Atom getAuxiliaryAtom(DLClause original, Atom headAtom, Individual... individuals) {
		DLPredicate p = headAtom.getDLPredicate(); 
		if (p instanceof AtLeastConcept) {
//			AtLeastConcept alc = (AtLeastConcept) p; 
//			Individual[] individuals = new Individual[alc.getNumber()]; 
//			for (int i = 0; i < alc.getNumber(); ++i)
//				individuals[i] = OverApproxExist.getNewIndividual(original, i); 
			return Atom.create(generateAuxiliaryRule((AtLeastConcept) p, original, individuals), headAtom.getArgument(0)); 
		}
		if (p instanceof AtomicConcept) 
			return Atom.create(generateAuxiliaryRule((AtomicConcept) p), headAtom.getArgument(0)); 
		if (p instanceof AtomicRole) 
			return Atom.create(generateAuxiliaryRule((AtomicRole) p), headAtom.getArgument(0), headAtom.getArgument(1));
		if (p instanceof Equality || p instanceof AnnotatedEquality) 
			return Atom.create(generateAuxiliaryRule(Equality.INSTANCE), headAtom.getArgument(0), headAtom.getArgument(1)); 
		if (p instanceof Inequality) 
			return Atom.create(generateAuxiliaryRule((Inequality) p), headAtom.getArgument(0), headAtom.getArgument(1)); 

		return null;
	}

	private void encodingDisjunctiveRule(DLClause clause, int index, Atom[] auxAtoms) {
		int validHeadLength = auxAtoms.length;
		Atom headAtom = clause.getHeadAtom(0); 
		Atom[] bodyAtoms = clause.getBodyAtoms();
		
		LinkedList<Atom> newHeadAtoms = new LinkedList<Atom>();
		DLPredicate selected = AtomicConcept.create(getSelectedPredicate()); 
		newHeadAtoms.add(Atom.create(selected, getIndividual4GeneralRule(clause)));
		
		for (Atom atom: bodyAtoms) {
			Atom newAtom = Atom.create(
					getTrackingDLPredicate(atom.getDLPredicate()), 
					DLClauseHelper.getArguments(atom));
			newHeadAtoms.add(newAtom);
		}

		DLClause newClause;
		Atom[] newBodyAtoms = new Atom[bodyAtoms.length + validHeadLength + 1];
		
		newBodyAtoms[0] = Atom.create(
				getTrackingDLPredicate(headAtom.getDLPredicate()), 
				DLClauseHelper.getArguments(headAtom));
		
		newBodyAtoms[1] = Atom.create(
				getGapDLPredicate(headAtom.getDLPredicate()), 
				DLClauseHelper.getArguments(headAtom));
		
		for (int i = 0; i < validHeadLength; ++i)
			if (i != index)
				newBodyAtoms[i + (i < index ? 2 : 1)] = auxAtoms[i]; 

		for (int i = 0; i < bodyAtoms.length; ++i)
			newBodyAtoms[i + validHeadLength + 1] = bodyAtoms[i]; 
		
		for (Atom atom: newHeadAtoms) {
			newClause = DLClause.create(new Atom[] {atom}, newBodyAtoms); 
			addTrackingClause(newClause);
		}
	}
	
	protected void addTrackingClause(DLClause clause) {
		trackingClauses.add(clause); 
	}

	protected void addDisjunctiveRule(DLClause key, DLClause clause) {
		Collection<DLClause> value = disjunctiveRules.get(key);
		if (value == null) {
			value = new LinkedList<DLClause>();
			disjunctiveRules.put(key, value);
		}
		value.add(clause);
	}

	protected abstract DLPredicate generateAuxiliaryRule(AtLeastConcept p, DLClause original, Individual[] individuals); 

	protected abstract DLPredicate generateAuxiliaryRule(AtomicRole p); 
	
	protected abstract DLPredicate generateAuxiliaryRule(AtomicConcept p);

	protected DLPredicate generateAuxiliaryRule(Equality instance) {
		return generateAuxiliaryRule(AtomicRole.create(Namespace.EQUALITY));
	}

	protected abstract DLPredicate generateAuxiliaryRule(Inequality instance); 
	
}
