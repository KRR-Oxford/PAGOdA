package uk.ac.ox.cs.pagoda.tracking;

import java.util.LinkedList;

import org.semanticweb.HermiT.model.AtLeastConcept;
import org.semanticweb.HermiT.model.Atom;
import org.semanticweb.HermiT.model.AtomicConcept;
import org.semanticweb.HermiT.model.AtomicNegationConcept;
import org.semanticweb.HermiT.model.AtomicRole;
import org.semanticweb.HermiT.model.DLClause;
import org.semanticweb.HermiT.model.DLPredicate;
import org.semanticweb.HermiT.model.Individual;
import org.semanticweb.HermiT.model.Inequality;
import org.semanticweb.HermiT.model.InverseRole;
import org.semanticweb.HermiT.model.Term;
import org.semanticweb.HermiT.model.Variable;

import uk.ac.ox.cs.pagoda.MyPrefixes;
import uk.ac.ox.cs.pagoda.hermit.DLClauseHelper;
import uk.ac.ox.cs.pagoda.multistage.Normalisation;
import uk.ac.ox.cs.pagoda.reasoner.light.BasicQueryEngine;
import uk.ac.ox.cs.pagoda.rules.OverApproxExist;
import uk.ac.ox.cs.pagoda.rules.UpperDatalogProgram;

public class TrackingRuleEncoderDisj1 extends TrackingRuleEncoderDisj {

	public TrackingRuleEncoderDisj1(UpperDatalogProgram program, BasicQueryEngine store) {
		super(program, store);
	}
	
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
			super.encodingRule(clause);
			return ; 
		}
		
		DLClause original = program.getCorrespondingClause(clause);
		if (original.getHeadLength() <= 1) {
			super.encodingRule(clause);
		}
		else addDisjunctiveRule(original, clause);
	}
	
	private DLPredicate getAuxPredicate(DLPredicate p, Individual... individuals) {
		if (p instanceof AtLeastConcept) {
			StringBuilder builder = new StringBuilder(
					Normalisation.getAuxiliaryConcept4Disjunct((AtLeastConcept) p, individuals));
			builder.append("_AUXa").append(currentQuery.getQueryID()); 
			return AtomicConcept.create(builder.toString()); 
		}
		
		return getDLPredicate(p, "_AUXa" + currentQuery.getQueryID());
	}

	private DLPredicate getTrackingBottomDLPredicate(DLPredicate p) {
		return getDLPredicate(p, getTrackingSuffix("0"));
	}

	protected DLPredicate generateAuxiliaryRule(AtLeastConcept p, DLClause original, Individual[] individuals) {
		DLPredicate ret = getAuxPredicate(p, individuals); 
		Atom[] headAtom = new Atom[] {Atom.create(ret, X)};
		
		AtomicRole role = p.getOnRole() instanceof AtomicRole ? 
				(AtomicRole) p.getOnRole(): 
					((InverseRole) p.getOnRole()).getInverseOf(); 
			
		AtomicConcept concept =	p.getToConcept() instanceof AtomicConcept ? 
				(AtomicConcept) p.getToConcept() : 
					OverApproxExist.getNegationConcept(((AtomicNegationConcept) p.getToConcept()).getNegatedAtomicConcept());
						
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
			
			addTrackingClause(
					DLClause.create(headAtom, 
							new Atom[] {Atom.create(getTrackingBottomDLPredicate(role), roleArgs)}));
			
			Atom guard = Atom.create(role, roleArgs);  
					
			if (!concept.equals(AtomicConcept.THING)) {
				addTrackingClause(
						DLClause.create(headAtom, 
								new Atom[] {guard, Atom.create(getTrackingDLPredicate(concept), conceptArg)}));
				
				addTrackingClause(
						DLClause.create(headAtom, 
								new Atom[] {guard, Atom.create(getTrackingBottomDLPredicate(concept), conceptArg)}));
			}
		}
		
		return ret;  
	}

	protected DLPredicate generateAuxiliaryRule(AtomicRole p) {
		DLPredicate ret = getAuxPredicate(p); 
		Atom[] headAtom = new Atom[] {Atom.create(ret, X, Y)};

		addTrackingClause(
				DLClause.create(headAtom, new Atom[] {Atom.create(getTrackingDLPredicate(p), X, Y)})); 
		addTrackingClause(
				DLClause.create(headAtom, new Atom[] {Atom.create(getTrackingBottomDLPredicate(p), X, Y)})); 
		
		return ret; 
	}
	
	private Variable X = Variable.create("X"), Y = Variable.create("Y"); 

	protected DLPredicate generateAuxiliaryRule(AtomicConcept p) {
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

	protected DLPredicate generateAuxiliaryRule(Inequality instance) {
		// TODO:
		return null; 
	}
	
	@Override
	public boolean isAuxPredicate(String iri) {
		return iri.contains("_AUXa"); 
//		if (iri.startsWith("<")) 
//			return iri.endsWith("_AUXa" + currentQuery.getQueryID() + ">"); 
//		return iri.endsWith("_AUXa" + currentQuery.getQueryID()); 
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

}
