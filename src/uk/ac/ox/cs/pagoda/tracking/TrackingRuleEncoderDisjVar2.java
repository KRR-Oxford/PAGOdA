package uk.ac.ox.cs.pagoda.tracking;

import org.semanticweb.HermiT.model.*;
import uk.ac.ox.cs.pagoda.hermit.DLClauseHelper;
import uk.ac.ox.cs.pagoda.multistage.Normalisation;
import uk.ac.ox.cs.pagoda.query.QueryRecord;
import uk.ac.ox.cs.pagoda.reasoner.light.BasicQueryEngine;
import uk.ac.ox.cs.pagoda.rules.UpperDatalogProgram;
import uk.ac.ox.cs.pagoda.rules.approximators.OverApproxExist;
import uk.ac.ox.cs.pagoda.util.Namespace;

import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;

public class TrackingRuleEncoderDisjVar2 extends TrackingRuleEncoderWithGap {

	public TrackingRuleEncoderDisjVar2(UpperDatalogProgram program, BasicQueryEngine store) {
		super(program, store);
	}
	
	private Set<DLClause> disjunctiveRules = new HashSet<DLClause>();

	@Override
	public boolean encodingRules() {
		if (ruleEncoded) return false;
		ruleEncoded = true; 
		
		for (DLClause clause: program.getClauses()) {
			encodingRule(clause);
		}
		
		if (disjunctiveRules.isEmpty())
			return true;
		
		processDisjunctiveRules(); 
		return false; 
	}
	
	@Override
	protected void encodingRule(DLClause clause) {
		DLClause original = program.getCorrespondingClause(clause);
		if (original.getHeadLength() <= 1) {
			super.encodingRule(clause);
		}
		else {
			if (!DLClauseHelper.hasSubsetBodyAtoms(clause, original))
				super.encodingRule(clause);
			addDisjunctiveRule(original);
		}
	}
	
	private void processDisjunctiveRules() {
		for (DLClause clause: disjunctiveRules)
			encodingDisjunctiveRule(clause);
	}
	
	private Atom getAuxiliaryAtom(Atom headAtom) {
		DLPredicate p = headAtom.getDLPredicate(); 
		if (p instanceof AtLeastConcept) {
			return Atom.create(generateAuxiliaryRule((AtLeastConcept) p), headAtom.getArgument(0)); 
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

	private Atom getGapAtom(Atom headAtom) {
		DLPredicate p = headAtom.getDLPredicate(); 
		if (p instanceof AtLeastConcept) {
			return Atom.create(getGapDLPredicate(AtomicConcept.create(Normalisation.getAuxiliaryConcept4Disjunct((AtLeastConcept) p))), headAtom.getArgument(0)); 
		}
		if (p instanceof AtomicConcept) 
			return Atom.create(getGapDLPredicate(p), headAtom.getArgument(0));
		if (p instanceof AtomicRole) 
			return Atom.create(getGapDLPredicate(p), headAtom.getArgument(0), headAtom.getArgument(1));
		if (p instanceof Equality || p instanceof AnnotatedEquality) 
			return Atom.create(getGapDLPredicate(Equality.INSTANCE), headAtom.getArgument(0), headAtom.getArgument(1)); 
		if (p instanceof Inequality) 
			return Atom.create(getGapDLPredicate(p), headAtom.getArgument(0), headAtom.getArgument(1));

		return null;
	}

	private void encodingDisjunctiveRule(DLClause clause) {
		int headLength = clause.getHeadLength();
		
		Atom[] auxAtoms = new Atom[headLength];
		for (int i = 0; i < headLength; ++i)
			auxAtoms[i] = getAuxiliaryAtom(clause.getHeadAtom(i));
		
		Atom[] gapAtoms = new Atom[headLength];
		for (int i = 0; i < headLength; ++i)
			gapAtoms[i] = getGapAtom(clause.getHeadAtom(i));

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
		for (int j = 0; j < headLength; ++j) {
			Atom[] newBodyAtoms = new Atom[headLength + bodyAtoms.length + 1];
			newBodyAtoms[0] = gapAtoms[j];
			
			for (int i = 0; i < headLength; ++i)
//				newBodyAtoms[i] = auxAtoms[i]; 
				newBodyAtoms[i + 1] = auxAtoms[i]; 
	
			for (int i = 0; i < bodyAtoms.length; ++i)
//				newBodyAtoms[i + headLength] = bodyAtoms[i]; 
				newBodyAtoms[i + headLength + 1] = bodyAtoms[i]; 
			
			for (Atom atom: newHeadAtoms) {
				newClause = DLClause.create(new Atom[] {atom}, newBodyAtoms); 
				addTrackingClause(newClause);
			}
		}
	}
	
	private void addTrackingClause(DLClause clause) {
		trackingClauses.add(clause); 
	}

	private void addDisjunctiveRule(DLClause clause) {
		disjunctiveRules.add(clause);
	}
	
	protected DLPredicate generateAuxiliaryRule(AtLeastConcept p) {
		AtomicConcept ac = AtomicConcept.create(Normalisation.getAuxiliaryConcept4Disjunct(p));
		int num = p.getNumber(); 
		Variable X = Variable.create("X"); 
		Variable[] Ys = new Variable[num]; 
		for (int i = 0; i < num; ++i) Ys[i] = Variable.create("Y" + (i + 1));
		Collection<Atom> expandedAtom = new LinkedList<Atom>(); 
		Collection<Atom> representativeAtom = new LinkedList<Atom>(); 
		if (p.getOnRole() instanceof AtomicRole) {
			AtomicRole r = (AtomicRole) p.getOnRole(); 
			for (int i = 0; i < num; ++i) 
				expandedAtom.add(Atom.create(r, X, Ys[i]));
			representativeAtom.add(Atom.create(r, X, Ys[0])); 
		}
		else {
			AtomicRole r = ((InverseRole) p.getOnRole()).getInverseOf(); 
			for (int i = 0; i < num; ++i) 
				expandedAtom.add(Atom.create(r, Ys[i], X));
			representativeAtom.add(Atom.create(r, Ys[0], X)); 
			
		}
		
		if (num > 1) {
			representativeAtom.add(Atom.create(Inequality.INSTANCE, Ys[0], Ys[1])); 
		}
		for (int i = 0; i < num; ++i)
			for (int j = i + 1; j < num; ++i)
				expandedAtom.add(Atom.create(Inequality.INSTANCE, Ys[i], Ys[j])); 
		
		if (!p.getToConcept().equals(AtomicConcept.THING)) {
			AtomicConcept c; 
			if (p.getToConcept() instanceof AtomicConcept) 
				c = (AtomicConcept) p.getToConcept();
			else 
				c = OverApproxExist.getNegationConcept(((AtomicNegationConcept) p.getToConcept()).getNegatedAtomicConcept());
			for (int i = 0; i < num; ++i)
				expandedAtom.add(Atom.create(c, Ys[i])); 
			representativeAtom.add(Atom.create(c, Ys[0]));
		}
		
		DLPredicate auxPredicate = getTrackingDLPredicate(ac);
		DLPredicate gapPredicate = getGapDLPredicate(ac);
		for (Atom atom: representativeAtom) {
			Atom[] bodyAtoms = new Atom[expandedAtom.size() + 1]; 
			bodyAtoms[0] = getAuxiliaryAtom(atom);
			int i = 0; 
			for (Atom bodyAtom: expandedAtom)
				bodyAtoms[++i] = bodyAtom;  
			addTrackingClause(DLClause.create(new Atom[] {Atom.create(auxPredicate, X)}, bodyAtoms));
			
			bodyAtoms = new Atom[expandedAtom.size() + 1]; 
			if (atom.getArity() == 1)
				bodyAtoms[0] = Atom.create(getGapDLPredicate(atom.getDLPredicate()), atom.getArgument(0));
			else 
				bodyAtoms[0] = Atom.create(getGapDLPredicate(atom.getDLPredicate()), atom.getArgument(0), atom.getArgument(1));
			i = 0; 
			for (Atom bodyAtom: expandedAtom)
				bodyAtoms[++i] = bodyAtom;  
			addTrackingClause(DLClause.create(new Atom[] {Atom.create(gapPredicate, X)}, bodyAtoms));
		}
		
		return auxPredicate;
	}

	private DLPredicate generateAuxiliaryRule(AtomicConcept p) {
		return getTrackingDLPredicate(p); 
	}
	
	private DLPredicate generateAuxiliaryRule(AtomicRole p) {
		return getTrackingDLPredicate(p); 
	}

	protected DLPredicate generateAuxiliaryRule(Equality instance) {
		return getTrackingDLPredicate(AtomicRole.create(Namespace.EQUALITY));
	}

	protected DLPredicate generateAuxiliaryRule(Inequality instance) {
		return getTrackingDLPredicate(AtomicRole.create(Namespace.INEQUALITY)); 
	}

	@Override
	protected void encodingAtomicQuery(QueryRecord[] botQuerRecords) {
		encodingAtomicQuery(botQuerRecords, true);
	}

}
