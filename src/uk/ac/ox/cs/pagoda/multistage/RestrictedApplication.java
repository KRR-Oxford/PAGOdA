package uk.ac.ox.cs.pagoda.multistage;

import org.semanticweb.HermiT.model.*;
import uk.ac.ox.cs.pagoda.constraints.BottomStrategy;
import uk.ac.ox.cs.pagoda.rules.Program;
import uk.ac.ox.cs.pagoda.util.Timer;

import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;

public class RestrictedApplication extends MultiStageUpperProgram {

	Normalisation norm;
	boolean hasDisjunction;
	
	public RestrictedApplication(Program program, BottomStrategy upperBottom) {
		super(program, upperBottom); 
		
		if (hasDisjunction)	addNegativeDatalogRules();
		clauses = new HashSet<DLClause>(rules);
		clauses.addAll(constraints); 
	}

	// It should be shifting
	public static Collection<DLClause> addAdditionalDatalogRules(DLClause clause, BottomStrategy bottom, Normalisation norm) {
		LinkedList<DLClause> newClauses = new LinkedList<DLClause>();
		Atom[] headAtoms = clause.getHeadAtoms();
		Atom[] bodyAtoms = clause.getBodyAtoms();
		int headLength = headAtoms.length;
		int bodyLength = bodyAtoms.length;
		DLClause tClause;
		if (bottom.isBottomRule(clause)) {
			if (clause.getBodyLength() == 1) return newClauses;
			for (int i = 0; i < bodyLength; ++i) {
				if (bodyAtoms[i].getDLPredicate() instanceof AtomicConcept) {
					AtomicConcept ac = (AtomicConcept) bodyAtoms[i].getDLPredicate();
					if (!ac.getIRI().endsWith("_exist")) {
						Atom[] newBodyAtoms = new Atom[bodyLength - 1];
						for (int j = 0; j < bodyLength - 1; ++j)
							newBodyAtoms[j] = j < i ? bodyAtoms[j] : bodyAtoms[j + 1];

						Atom negativeAtom = getNegativeAtom(bodyAtoms[i]);
						tClause = DLClause.create(new Atom[] { negativeAtom }, newBodyAtoms);
	//					addDatalogRule(tClause);
						newClauses.add(tClause);
					} else {
						Atom[] newBodyAtoms = new Atom[bodyLength];
						Atom negativeAtom = null;
						org.semanticweb.HermiT.model.Variable E = org.semanticweb.HermiT.model.Variable.create("E");
						AtLeastConcept alc = norm.getLeftAtLeastConcept(ac);

						if (alc.getOnRole() instanceof AtomicRole)
							newBodyAtoms[i] = Atom.create((AtomicRole) alc.getOnRole(), bodyAtoms[i].getArgument(0), E);
						else
							newBodyAtoms[i] = Atom.create(((InverseRole) alc.getOnRole()).getInverseOf(), E, bodyAtoms[i].getArgument(0));
						if (alc.getToConcept() instanceof AtomicConcept)
							negativeAtom = getNegativeAtom(Atom.create((AtomicConcept) alc.getToConcept(), E));
						else
							negativeAtom = getNegativeAtom(Atom.create(((AtomicNegationConcept) alc.getToConcept()).getNegatedAtomicConcept(), E));

						for (int j = 0; j < bodyLength; ++j)
							if (i != j)
								newBodyAtoms[j] = bodyAtoms[j];


						tClause = DLClause.create(new Atom[] { negativeAtom }, newBodyAtoms);
	//					addDatalogRule(tClause);
						newClauses.add(tClause);
					}
				}
			}
		}
		else if (headLength > 1) {
			for (int i = 0; i < headLength; ++i) {
				DLPredicate p = headAtoms[i].getDLPredicate();
				if (!(p instanceof AtomicConcept)) {
					return newClauses;
				}
			}

			for (int i = 0; i < headLength; ++i) {
				Atom[] newBodyAtoms = new Atom[headLength + bodyLength - 1];
				for (int j = 0; j < headLength + bodyLength - 1; ++j)
					newBodyAtoms[j] = j < bodyLength ? bodyAtoms[j] :
							j < bodyLength + i ? getNegativeAtom(headAtoms[j - bodyLength]) :
									getNegativeAtom(headAtoms[j - bodyLength + 1]);

				tClause = DLClause.create(new Atom[]{headAtoms[i]}, newBodyAtoms);
//				addDatalogRule(tClause);
				newClauses.add(tClause);
			}
		}
		else if (headLength == 1) {
			DLPredicate p = clause.getHeadAtom(0).getDLPredicate();
			if (p instanceof AtomicConcept) {
				Atom negativeHeadAtom = getNegativeAtom(clause.getHeadAtom(0));
				for (int i = 0; i < bodyLength; ++i)
					if (bodyAtoms[i].getDLPredicate() instanceof AtomicConcept) {
						Atom[] newBodyAtoms = new Atom[clause.getBodyLength()];
						newBodyAtoms[0] = negativeHeadAtom;
						for (int j = 1; j < bodyLength; ++j)
							newBodyAtoms[j] = j <= i ? bodyAtoms[j - 1] : bodyAtoms[j];

						tClause = DLClause.create(new Atom[]{getNegativeAtom(bodyAtoms[i])}, newBodyAtoms);
//						addDatalogRule(tClause);
						newClauses.add(tClause);
					}
			}
			else if (p instanceof AtLeastConcept && clause.getBodyLength() == 1 && clause.getBodyAtom(0).getDLPredicate() instanceof AtomicConcept) {
				AtLeastConcept alc = (AtLeastConcept) p;
				AtomicConcept ac = norm.getLeftAuxiliaryConcept(alc, true);
				if (ac != null) {
					Atom bodyAtom = clause.getBodyAtom(0);
//					addDatalogRule(DLClause.create(new Atom[] {getNegativeAtom(bodyAtom)},
//							new Atom[] {getNegativeAtom(Atom.create(ac, bodyAtom.getArgument(0)))} ));
					newClauses.add(DLClause.create(new Atom[]{getNegativeAtom(bodyAtom)},
												   new Atom[]{getNegativeAtom(Atom.create(ac, bodyAtom.getArgument(0)))}));
				}
			}
		}
		return newClauses;
	}

	private void addNegativeDatalogRules() {
		Collection<DLClause> allRules = new LinkedList<DLClause>(rules);
		allRules.addAll(constraints);
		for (DLClause clause : allRules) {
			for (DLClause newClause : addAdditionalDatalogRules(clause, m_bottom, norm))
				addDatalogRule(newClause);
		}
		allRules.clear();
	}

	public Normalisation getNormalisation() {
		return norm; 
	}
	
	public Collection<Violation> isIntegrated(MultiStageQueryEngine engine, boolean incrementally) {
		if (incrementally) addDerivedPredicate(engine); 
		
		Collection<Violation> ret = new LinkedList<Violation>(); 
		Violation v; 
		
		Timer t = new Timer(); 
		for (DLClause clause: constraints) {
			t.reset(); 
			if ((v = violate(engine, clause, incrementally)) != null) { 
				ret.add(v);
				v.setClause(getOriginalClause(clause));
			}
		}
		
		updatedPredicates.clear();
		
		if (ret.isEmpty()) return null; 
		return ret; 
	}
	
	@Override
	protected DLClause getOriginalClause(DLClause clause) {
		DLClause originalClause = super.getOriginalClause(clause); 
		if (norm == null) return originalClause; 
		return norm.getOriginalClause(originalClause); 
	}

	@Override
	protected Collection<DLClause> getInitialClauses(Program program) {
		Collection<DLClause> clauses = program.getClauses();
		hasDisjunction = false; 
		for (DLClause clause: clauses)
			if (clause.getHeadLength() > 1) {
				hasDisjunction = true; 
				break; 
			}
		
		if (hasDisjunction) {
			norm = new Normalisation(clauses, program.getOntology(), m_bottom);
			norm.process();
			clauses = norm.m_normClauses; 
		}
		return clauses;
	}
	
}
