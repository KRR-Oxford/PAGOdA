package uk.ac.ox.cs.pagoda.tracking;

import org.semanticweb.HermiT.model.*;
import uk.ac.ox.cs.pagoda.MyPrefixes;
import uk.ac.ox.cs.pagoda.multistage.Normalisation;
import uk.ac.ox.cs.pagoda.query.QueryRecord;
import uk.ac.ox.cs.pagoda.reasoner.light.BasicQueryEngine;
import uk.ac.ox.cs.pagoda.rules.UpperDatalogProgram;
import uk.ac.ox.cs.pagoda.rules.approximators.OverApproxExist;

public class TrackingRuleEncoderDisj2 extends TrackingRuleEncoderDisj {

	public TrackingRuleEncoderDisj2(UpperDatalogProgram program, BasicQueryEngine store) {
		super(program, store);
	}

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
	protected DLPredicate generateAuxiliaryRule(AtomicConcept p) {
		return getTrackingDLPredicate(p); 
	}
	
	@Override
	protected DLPredicate generateAuxiliaryRule(AtomicRole p) {
		return getTrackingDLPredicate(p); 
	}

	private Variable X = Variable.create("X"); 
	
	@Override
	protected DLPredicate generateAuxiliaryRule(AtLeastConcept p, DLClause original, Individual[] individuals) {
		DLPredicate ret = AtomicConcept.create(getTrackingPredicate(Normalisation.getAuxiliaryConcept4Disjunct(p, individuals)));
		Atom[] headAtom = new Atom[] {Atom.create(ret, X)};
		
		AtomicRole role = p.getOnRole() instanceof AtomicRole ? 
				(AtomicRole) p.getOnRole(): 
					((InverseRole) p.getOnRole()).getInverseOf(); 
			
		AtomicConcept concept =	p.getToConcept() instanceof AtomicConcept ? 
				(AtomicConcept) p.getToConcept() : 
				(AtomicConcept) OverApproxExist.getNegationPredicate(((AtomicNegationConcept) p.getToConcept()).getNegatedAtomicConcept());
						
		Term[] roleArgs, conceptArg;
		for (Individual i: individuals) {
//		Variable i = Variable.create("Y"); 
			if (p.getOnRole() instanceof AtomicRole) {
				roleArgs = new Term[] {X, i};
				conceptArg = new Term[] {i}; 
			}
			else {
				roleArgs = new Term[] {i, X};
				conceptArg = new Term[] {i}; 
			}
				
			addTrackingClause(
					DLClause.create(headAtom, 
							new Atom[] {Atom.create(getTrackingDLPredicate(role), roleArgs)}));
			
			Atom guard = Atom.create(role, roleArgs);  
					
			if (!concept.equals(AtomicConcept.THING)) {
				addTrackingClause(
						DLClause.create(headAtom, 
								new Atom[] {guard, Atom.create(getTrackingDLPredicate(concept), conceptArg)}));
			}
		}
		
		return ret;  
	}
	
	@Override
	protected void encodingRule(DLClause clause) {
		DLClause original = program.getCorrespondingClause(clause);
		if (original.getHeadLength() <= 1) {
			super.encodingRule(clause);
		}
		else addDisjunctiveRule(original, clause);
	}
	
	@Override
	public String getTrackingProgram() {
		StringBuilder sb = getTrackingProgramBody();
		sb.insert(0, MyPrefixes.PAGOdAPrefixes.prefixesText()); 
		return sb.toString(); 
	}

	@Override
	protected void encodingAtomicQuery(QueryRecord[] botQuerRecords) {
		super.encodingAtomicQuery(botQuerRecords, true);
	}

	@Override
	protected DLPredicate generateAuxiliaryRule(Inequality instance) {
		// TODO Auto-generated method stub
		return null;
	}
}
