package uk.ac.ox.cs.pagoda.tracking;

import org.semanticweb.HermiT.model.*;
import uk.ac.ox.cs.pagoda.MyPrefixes;
import uk.ac.ox.cs.pagoda.hermit.DLClauseHelper;
import uk.ac.ox.cs.pagoda.multistage.Normalisation;
import uk.ac.ox.cs.pagoda.reasoner.light.BasicQueryEngine;
import uk.ac.ox.cs.pagoda.rules.UpperDatalogProgram;
import uk.ac.ox.cs.pagoda.rules.approximators.OverApproxExist;
import uk.ac.ox.cs.pagoda.util.Namespace;
import uk.ac.ox.cs.pagoda.util.Utility;

import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;

public class TrackingRuleEncoderDisjVar1 extends TrackingRuleEncoderWithGap {

	public TrackingRuleEncoderDisjVar1(UpperDatalogProgram program, BasicQueryEngine store) {
		super(program, store);
	}
	
	private Set<DLClause> disjunctiveRules = new HashSet<DLClause>();

	@Override
	public boolean encodingRules() {
		if (super.encodingRules()) {
			processDisjunctiveRules();
			return true; 
		}
		return false; 
	}
	
	@Override
	protected void encodingRule(DLClause clause) {
		if (currentQuery.isBottom()) {
//			super.encodingRule(clause);
			encodingBottomQueryClause(clause); 
			return ; 
		}
		
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
		if (p instanceof AtLeast || p instanceof AtLeast) {
			return Atom.create(generateAuxiliaryRule((AtLeast) p, true), headAtom.getArgument(0)); 
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

	private Atom getTrackingAtom(Atom headAtom) {
		DLPredicate p = headAtom.getDLPredicate(); 
		if (p instanceof AtLeast) {
			p = Normalisation.toAtLeastConcept((AtLeast) p); 
			return Atom.create(getTrackingDLPredicate(AtomicConcept.create(Normalisation.getAuxiliaryConcept4Disjunct((AtLeastConcept) p))), headAtom.getArgument(0)); 
		}
		if (p instanceof AtomicConcept) 
			return Atom.create(getTrackingDLPredicate(p), headAtom.getArgument(0));
		if (p instanceof AtomicRole) 
			return Atom.create(getTrackingDLPredicate(p), headAtom.getArgument(0), headAtom.getArgument(1));
		if (p instanceof Equality || p instanceof AnnotatedEquality) 
			return Atom.create(getTrackingDLPredicate(Equality.INSTANCE), headAtom.getArgument(0), headAtom.getArgument(1)); 
		if (p instanceof Inequality) 
			return Atom.create(getTrackingDLPredicate(p), headAtom.getArgument(0), headAtom.getArgument(1));

		return null;
	}

	private Atom getGapAtom(Atom headAtom) {
		DLPredicate p = headAtom.getDLPredicate(); 
		if (p instanceof AtLeast) {
			p = Normalisation.toAtLeastConcept((AtLeast) p); 
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
		if (p instanceof DatatypeRestriction)
			return Atom.create(getGapDLPredicate(p), headAtom.getArgument(0));
		Utility.logError(p + " is not recognised.");
		return null;
	}

	private void encodingDisjunctiveRule(DLClause clause) {
		int headLength = clause.getHeadLength();
		
		Atom[] auxAtoms = new Atom[headLength];
		for (int i = 0; i < headLength; ++i)
			auxAtoms[i] = getAuxiliaryAtom(clause.getHeadAtom(i));
		
		Atom[] trackingAtoms = new Atom[headLength];
		for (int i = 0; i < headLength; ++i)
			trackingAtoms[i] = getTrackingAtom(clause.getHeadAtom(i));
		
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
		int index; 
		for (int j = 0; j < headLength; ++j) {
			Atom[] newBodyAtoms = new Atom[headLength * 2 + bodyAtoms.length];
			index = 0; 
			for (int i = 0; i < headLength; ++i, ++index)
				newBodyAtoms[index] = gapAtoms[i]; 
			for (int i = 0; i < headLength; ++i, ++index)
				if (i != j)
					newBodyAtoms[index] = auxAtoms[i];
				else 
					newBodyAtoms[index] = trackingAtoms[i]; 
			
			for (int i = 0; i < bodyAtoms.length; ++i, ++index)
				newBodyAtoms[index] = bodyAtoms[i]; 
			
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
	
	private DLPredicate getAuxPredicate(DLPredicate p) {
		if (p instanceof AtLeastConcept) {
			StringBuilder builder = new StringBuilder(
					Normalisation.getAuxiliaryConcept4Disjunct((AtLeastConcept) p));
			builder.append("_AUXa").append(currentQuery.getQueryID()); 
			return AtomicConcept.create(builder.toString()); 
		}
		
		return getDLPredicate(p, "_AUXa" + currentQuery.getQueryID());
	}

	private DLPredicate getTrackingBottomDLPredicate(DLPredicate p) {
		return getDLPredicate(p, getTrackingSuffix("0"));
	}

	private DLPredicate generateAuxiliaryRule(AtLeast p1, boolean withAux) {
		AtLeastConcept p = Normalisation.toAtLeastConcept(p1); 
		
		int num = p.getNumber(); 
		Variable[] Ys = new Variable[num]; 
		if (num > 1)
			for (int i = 0; i < num; ++i) 
				Ys[i] = Variable.create("Y" + (i + 1));
		else 
			Ys[0] = Y; 
		
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
			for (int j = i + 1; j < num; ++j)
				expandedAtom.add(Atom.create(Inequality.INSTANCE, Ys[i], Ys[j])); 
		
		if (!p.getToConcept().equals(AtomicConcept.THING)) {
			AtomicConcept c; 
			if (p.getToConcept() instanceof AtomicConcept) 
				c = (AtomicConcept) p.getToConcept();
			else {
				c = OverApproxExist.getNegationConcept(((AtomicNegationConcept) p.getToConcept()).getNegatedAtomicConcept());
			}
			for (int i = 0; i < num; ++i)
				expandedAtom.add(Atom.create(c, Ys[i])); 
			representativeAtom.add(Atom.create(c, Ys[0]));
		}

		AtomicConcept ac = AtomicConcept.create(Normalisation.getAuxiliaryConcept4Disjunct(p));
		DLPredicate trackingPredicate = getTrackingDLPredicate(ac); 
		DLPredicate gapPredicate = getGapDLPredicate(ac); 
		DLPredicate auxPredicate = withAux ? getAuxPredicate(p) : null;
		
		for (Atom atom: representativeAtom) {
			Atom[] bodyAtoms = new Atom[expandedAtom.size() + 1]; 
			if (atom.getArity() == 1)
				bodyAtoms[0] = Atom.create(getTrackingDLPredicate(atom.getDLPredicate()), atom.getArgument(0));
			else 
				bodyAtoms[0] = Atom.create(getTrackingDLPredicate(atom.getDLPredicate()), atom.getArgument(0), atom.getArgument(1));
			int i = 0; 
			for (Atom bodyAtom: expandedAtom)
				bodyAtoms[++i] = bodyAtom;  
			addTrackingClause(DLClause.create(new Atom[] {Atom.create(trackingPredicate, X)}, bodyAtoms));
			
			bodyAtoms = new Atom[expandedAtom.size() + 1]; 
			if (atom.getArity() == 1)
				bodyAtoms[0] = Atom.create(getGapDLPredicate(atom.getDLPredicate()), atom.getArgument(0));
			else 
				bodyAtoms[0] = Atom.create(getGapDLPredicate(atom.getDLPredicate()), atom.getArgument(0), atom.getArgument(1));
			i = 0; 
			for (Atom bodyAtom: expandedAtom)
				bodyAtoms[++i] = bodyAtom;  
			addTrackingClause(DLClause.create(new Atom[] {Atom.create(gapPredicate, X)}, bodyAtoms));
			
			if (withAux) {
				bodyAtoms = new Atom[expandedAtom.size() + 1]; 
				bodyAtoms[0] = getAuxiliaryAtom(atom);
				i = 0; 
				for (Atom bodyAtom: expandedAtom)
					bodyAtoms[++i] = bodyAtom;  
				addTrackingClause(DLClause.create(new Atom[] {Atom.create(auxPredicate, X)}, bodyAtoms));
			}
		}
		
		return withAux ? auxPredicate : trackingPredicate;
	}

	private DLPredicate generateAuxiliaryRule(AtomicRole p) {
		if (currentQuery.isBottom()) 
			return getTrackingDLPredicate(p);
		
		DLPredicate ret = getAuxPredicate(p); 
		Atom[] headAtom = new Atom[] {Atom.create(ret, X, Y)};

		addTrackingClause(
				DLClause.create(headAtom, new Atom[] {Atom.create(getTrackingDLPredicate(p), X, Y)})); 
		addTrackingClause(
				DLClause.create(headAtom, new Atom[] {Atom.create(getTrackingBottomDLPredicate(p), X, Y)})); 
		
		return ret; 
	}
	
	private Variable X = Variable.create("X"), Y = Variable.create("Y"); 

	private DLPredicate generateAuxiliaryRule(AtomicConcept p) {
		if (currentQuery.isBottom())
			return getTrackingDLPredicate(p); 
		
		DLPredicate ret = getAuxPredicate(p); 
		Atom[] headAtom = new Atom[] {Atom.create(ret, X)}; 
		addTrackingClause(
				DLClause.create(headAtom, 
						new Atom[] { Atom.create(getTrackingDLPredicate(p), X)})); 
		addTrackingClause(
				DLClause.create(headAtom, 
						new Atom[] { Atom.create(getTrackingBottomDLPredicate(p), X)}));
		
		return ret; 
	}

	private DLPredicate generateAuxiliaryRule(Equality instance) {
		return generateAuxiliaryRule(AtomicRole.create(Namespace.EQUALITY));
	}

	private DLPredicate generateAuxiliaryRule(Inequality instance) {
		return generateAuxiliaryRule(AtomicRole.create(Namespace.INEQUALITY)); 
	}
	
	@Override
	public String getTrackingProgram() {
		StringBuilder sb = getTrackingProgramBody();
		if (currentQuery.isBottom())
			sb.append(getBottomTrackingProgram()); 
		sb.insert(0, MyPrefixes.PAGOdAPrefixes.prefixesText()); 
		return sb.toString(); 
	}

	private String bottomTrackingProgram = null; 

	private String getBottomTrackingProgram() {
		if (bottomTrackingProgram != null) return bottomTrackingProgram.replace("_tn", getTrackingPredicate(""));  
		
		String bottomSuffix = getTrackingSuffix("0"); 
		LinkedList<DLClause> clauses = new LinkedList<DLClause>();  
		Variable X = Variable.create("X"); 
		for (String concept: unaryPredicates)
			clauses.add(DLClause.create(new Atom[] {Atom.create(AtomicConcept.create(concept + bottomSuffix) , X)}, 
										new Atom[] {Atom.create(AtomicConcept.create(concept + "_tn"), X)}));
		Variable Y = Variable.create("Y"); 
		for (String role: binaryPredicates)
			clauses.add(DLClause.create(new Atom[] {Atom.create(AtomicRole.create(role + bottomSuffix) , X, Y)}, 
										new Atom[] {Atom.create(AtomicRole.create(role + "_tn"), X, Y) }));
		
		StringBuilder builder = new StringBuilder(DLClauseHelper.toString(clauses));
		bottomTrackingProgram = builder.toString();
		return bottomTrackingProgram.replace("_tn", getTrackingPredicate("")); 
	}

	private void encodingBottomQueryClause(DLClause clause) {
		if (!clause.toString().contains("owl:Nothing"))
			clause = program.getCorrespondingClause(clause);
		
//		Term t;
//		for (Atom tAtom: clause.getHeadAtoms()) {
//			for (int i = 0; i < tAtom.getArity(); ++i)
//				if ((t = tAtom.getArgument(i)) instanceof Individual) 
//					if (((Individual) t).getIRI().startsWith(OverApproxExist.skolemisedIndividualPrefix))
//						clause = program.getCorrespondingClause(clause); 				
//		}
		
		LinkedList<Atom> newHeadAtoms = new LinkedList<Atom>();
		Atom selectAtom = Atom.create(selected, getIndividual4GeneralRule(program.getCorrespondingClause(clause)));
		
		for (Atom atom: clause.getBodyAtoms()) {
			atom = Atom.create(
					getTrackingDLPredicate(atom.getDLPredicate()), 
					DLClauseHelper.getArguments(atom));
			newHeadAtoms.add(atom);
		}

		DLClause newClause;
		
		boolean botInHead = clause.getBodyLength() == 1 && clause.getBodyAtom(0).getDLPredicate().toString().contains("owl:Nothing");
		
		DLPredicate[] trackingPredicates = new DLPredicate[clause.getHeadLength()];
		DLPredicate[] predicates = new DLPredicate[clause.getHeadLength()];
		int headIndex = 0; 
		DLPredicate trackingPredicate, p; 
		for (Atom headAtom: clause.getHeadAtoms()) {
			if ((p = headAtom.getDLPredicate()) instanceof AtLeastConcept) {
				trackingPredicate = generateAuxiliaryRule((AtLeastConcept) p, false);
				p = AtomicConcept.create(Normalisation.getAuxiliaryConcept4Disjunct((AtLeastConcept) p));
				trackingClauses.add(DLClause.create(
						new Atom[] { Atom.create(getDLPredicate(p, getTrackingSuffix("0")), X) }, 
						new Atom[] { Atom.create(trackingPredicate, X) })); 
			}
			else 
				trackingPredicate = getTrackingDLPredicate(p);
			
			trackingPredicates[headIndex] = trackingPredicate;
			predicates[headIndex] = p; 
			++headIndex; 			
		}
		
		headIndex = 0;
		int headLength = clause.getHeadLength(); 
		Atom[] gapAtoms = new Atom[headLength];
		for (int i = 0; i < headLength; ++i)
			gapAtoms[i] = getGapAtom(clause.getHeadAtom(i)); 
//			Atom.create(getGapDLPredicate(predicates[headIndex]), DLClauseHelper.getArguments(clause.getHeadAtom(i))); 
		int index, selectIndex; 
		for (Atom headAtom: clause.getHeadAtoms()) {
			index = 0; selectIndex = 0; 
			Atom[] newBodyAtoms = new Atom[clause.getBodyLength() + 1 + headLength - 1 + (botInHead ? 0 : headLength)];
			Atom[] selectBodyAtoms = new Atom[clause.getBodyLength() + 1 + (botInHead ? 0 : headLength)];
			newBodyAtoms[index++] = selectBodyAtoms[selectIndex++] = Atom.create(trackingPredicates[headIndex], DLClauseHelper.getArguments(headAtom));
			
			if (!botInHead) {
				for (int i = 0; i < headLength; ++i)
					newBodyAtoms[index++] = selectBodyAtoms[selectIndex++] = gapAtoms[i];
			}
	
			for (int i = 0; i < headLength; ++i)
				if (i != headIndex) {
					newBodyAtoms[index++] = Atom.create(getDLPredicate(predicates[i], getTrackingSuffix("0")), DLClauseHelper.getArguments(clause.getHeadAtom(i)));
				}
				
			for (int i = 0; i < clause.getBodyLength(); ++i) 
				newBodyAtoms[index++] = selectBodyAtoms[selectIndex++] = clause.getBodyAtom(i);

			for (Atom atom: newHeadAtoms) {
				newClause = DLClause.create(new Atom[] {atom}, newBodyAtoms); 
				trackingClauses.add(newClause);
			}
			trackingClauses.add(DLClause.create(new Atom[] {selectAtom}, selectBodyAtoms)); 
			++headIndex; 
		}
	}
	
}
