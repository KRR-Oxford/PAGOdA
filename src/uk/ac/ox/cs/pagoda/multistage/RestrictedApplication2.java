package uk.ac.ox.cs.pagoda.multistage;

import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.LinkedList;

import org.semanticweb.HermiT.model.AtLeastConcept;
import org.semanticweb.HermiT.model.Atom;
import org.semanticweb.HermiT.model.AtomicConcept;
import org.semanticweb.HermiT.model.DLClause;
import org.semanticweb.HermiT.model.DLPredicate;

import uk.ac.ox.cs.pagoda.hermit.DLClauseHelper;
import uk.ac.ox.cs.pagoda.multistage.treatement.SimpleComparator;
import uk.ac.ox.cs.pagoda.query.GapByStore4ID;
import uk.ac.ox.cs.pagoda.rules.DatalogProgram;
import uk.ac.ox.cs.pagoda.rules.Program;

public class RestrictedApplication2 extends TwoStageApplication {

	private Normalisation norm;
	private boolean hasDisjunctions;
	private Comparator<Atom> disjunctComparator;
	
	public RestrictedApplication2(TwoStageQueryEngine engine, DatalogProgram program, GapByStore4ID gap) {
		super(engine, program, gap);
		if (hasDisjunctions) {
			addNegativeDatalogRules(); 
			disjunctComparator = new SimpleComparator();
		}
	}

	private void addNegativeDatalogRules() {
		Collection<DLClause> allRules = new LinkedList<DLClause>(rules); 
		allRules.addAll(constraints); 
		for (DLClause clause: allRules) {
			addAddtionalDatalogRules(clause);
		}
		allRules.clear();
	}
	
	private void addAddtionalDatalogRules(DLClause clause) {
		Atom[] headAtoms = clause.getHeadAtoms(); 
		Atom[] bodyAtoms = clause.getBodyAtoms(); 
		int headLength = headAtoms.length; 
		int bodyLength = bodyAtoms.length;
		DLClause tClause; 
		if (m_bottom.isBottomRule(clause)) { 
			if (clause.getBodyLength() == 1) return ; 
			for (int i = 0; i < bodyLength; ++i) 
				if (bodyAtoms[i].getDLPredicate() instanceof AtomicConcept) { 
					Atom[] newBodyAtoms = new Atom[bodyLength - 1];
					for (int j = 0; j < bodyLength - 1; ++j)
						newBodyAtoms[j] = j < i ? bodyAtoms[j] : bodyAtoms[j + 1];
						
					Atom negativeAtom = MultiStageUpperProgram.getNegativeAtom(bodyAtoms[i]);
					tClause = DLClause.create(new Atom[] { negativeAtom }, newBodyAtoms); 
					addDatalogRule(tClause);
				}
		}
		else if (headLength > 1) {
			for (int i = 0; i < headLength; ++i) {
				DLPredicate p = headAtoms[i].getDLPredicate(); 
				if (!(p instanceof AtomicConcept)) {
					return ; 
				}
			}

			for (int i = 0; i < headLength; ++i) {
				Atom[] newBodyAtoms = new Atom[headLength + bodyLength - 1]; 
				for (int j = 0; j < headLength + bodyLength - 1; ++j)
					newBodyAtoms[j] = j < bodyLength ?  bodyAtoms[j] : 
										j < bodyLength + i ? MultiStageUpperProgram.getNegativeAtom(headAtoms[j - bodyLength]) : 
											MultiStageUpperProgram.getNegativeAtom(headAtoms[j - bodyLength + 1]); 

				tClause = DLClause.create(new Atom[] { headAtoms[i] }, newBodyAtoms); 
				addDatalogRule(tClause);
			}
		}
		else if (headLength == 1) {
			DLPredicate p = clause.getHeadAtom(0).getDLPredicate();  
			if (p instanceof AtomicConcept) {
				Atom negativeHeadAtom = MultiStageUpperProgram.getNegativeAtom(clause.getHeadAtom(0)); 
				for (int i = 0; i < bodyLength; ++i) 
					if (bodyAtoms[i].getDLPredicate() instanceof AtomicConcept) { 
						Atom[] newBodyAtoms = new Atom[clause.getBodyLength()];
						newBodyAtoms[0] = negativeHeadAtom; 
						for (int j = 1; j < bodyLength; ++j)
							newBodyAtoms[j] = j <= i ? bodyAtoms[j - 1] : bodyAtoms[j];
							
						tClause = DLClause.create(new Atom[] {MultiStageUpperProgram.getNegativeAtom(bodyAtoms[i])}, newBodyAtoms); 
						addDatalogRule(tClause);
					}
			}
			else if (p instanceof AtLeastConcept && clause.getBodyLength() == 1 && clause.getBodyAtom(0).getDLPredicate() instanceof AtomicConcept) {
				AtLeastConcept alc = (AtLeastConcept) p; 
				AtomicConcept ac = norm.getLeftAuxiliaryConcept(alc, true); 
				if (ac != null) {
					Atom bodyAtom = clause.getBodyAtom(0);  
					addDatalogRule(DLClause.create(new Atom[] {MultiStageUpperProgram.getNegativeAtom(bodyAtom)}, 
							new Atom[] {MultiStageUpperProgram.getNegativeAtom(Atom.create(ac, bodyAtom.getArgument(0)))} )); 
				}
			}
		}
	}

	@Override
	protected void addAuxiliaryRules() {
		for (DLClause constraint: constraints) 
			if (constraint.getHeadLength() <= 1) 
				processExistentialRule(constraint); 
			else 
				processDisjunctiveRule(constraint); 
	}

	private static final Atom[] empty = new Atom[0];
	
	private void processDisjunctiveRule(DLClause constraint) {
		int headLength = constraint.getHeadLength(); 
		Atom[] orderedAtoms = new Atom[headLength]; 
		for (int i = 0; i < headLength; ++i)
			orderedAtoms[i] = constraint.getHeadAtom(i); 
				
		Arrays.sort(orderedAtoms, disjunctComparator);
		
		Collection<Atom> bodyAtoms = new LinkedList<Atom>();
		for (int i = 0; i < headLength; ++i) {
			bodyAtoms.add(getNAFAtom(orderedAtoms[i])); 
		}
		for (Atom atom: constraint.getBodyAtoms())
			bodyAtoms.add(atom); 
		
		Atom negAtom; 
		for (Atom atom: constraint.getHeadAtoms()) {
			negAtom = MultiStageUpperProgram.getNegativeAtom(atom); 
			bodyAtoms.add(getNAFAtom(negAtom));
			addDatalogRule(DLClause.create(new Atom[] {atom}, bodyAtoms.toArray(empty)));
		}
	}

	private void processExistentialRule(DLClause constraint) {
		Atom[] bodyAtoms = new Atom[constraint.getBodyLength() + 1]; 
		bodyAtoms[0] = getNAFAtom(constraint.getHeadAtom(0)); 
		int i = 0; 
		for (Atom atom: constraint.getBodyAtoms())
			bodyAtoms[++i] = atom; 
		
		Collection<DLClause> overClauses = overExist.convert(constraint, getOriginalClause(constraint)); 
		for (DLClause clause: overClauses) 
			if (DLClauseHelper.hasSubsetBodyAtoms(clause, constraint))
				addDatalogRule(DLClause.create(new Atom[] {clause.getHeadAtom(0)}, bodyAtoms));
	}

	@Override
	protected Collection<DLClause> getInitialClauses(Program program) {
		Collection<DLClause> clauses = program.getClauses();
		hasDisjunctions = false; 
		for (DLClause clause: clauses)
			if (clause.getHeadLength() > 1) {
				hasDisjunctions = true; 
				break; 
			}
		
		if (hasDisjunctions) {
			norm = new Normalisation(clauses, program.getOntology(), m_bottom);
			norm.process();
			clauses = norm.m_normClauses; 
		}
		return clauses;
	}
	
	protected DLClause getOriginalClause(DLClause clause) {
		DLClause original = super.getOriginalClause(clause); 
		return norm.getOriginalClause(original); 
	}
}
