package uk.ac.ox.cs.pagoda.multistage;

import org.semanticweb.HermiT.model.*;
import uk.ac.ox.cs.JRDFox.JRDFStoreException;
import uk.ac.ox.cs.JRDFox.store.TupleIterator;
import uk.ac.ox.cs.pagoda.MyPrefixes;
import uk.ac.ox.cs.pagoda.constraints.BottomStrategy;
import uk.ac.ox.cs.pagoda.hermit.RuleHelper;
import uk.ac.ox.cs.pagoda.query.GapByStore4ID;
import uk.ac.ox.cs.pagoda.reasoner.light.RDFoxTripleManager;
import uk.ac.ox.cs.pagoda.rules.DatalogProgram;
import uk.ac.ox.cs.pagoda.rules.Program;
import uk.ac.ox.cs.pagoda.rules.approximators.OverApproxExist;
import uk.ac.ox.cs.pagoda.util.Namespace;
import uk.ac.ox.cs.pagoda.util.SparqlHelper;
import uk.ac.ox.cs.pagoda.util.Utility;

import java.util.*;

abstract class TwoStageApplication {

	private static final String NAF_suffix = "_NAF";
	protected TwoStageQueryEngine engine;
	protected MyPrefixes prefixes = MyPrefixes.PAGOdAPrefixes;
	protected Set<DLClause> rules = new HashSet<DLClause>();
	protected Collection<DLClause> constraints = new LinkedList<DLClause>();
	protected BottomStrategy m_bottom;
	protected Set<Atom> toGenerateNAFFacts = new HashSet<Atom>();
	protected OverApproxExist overExist = new OverApproxExist();
	Program lowerProgram;
	boolean m_incrementally = true;
	Set<Integer> allIndividuals = new HashSet<Integer>();
	RDFoxTripleManager tripleManager;
	private GapByStore4ID gap;
	private StringBuilder datalogRuleText = new StringBuilder();
	private Map<DLClause, DLClause> map = new HashMap<DLClause, DLClause>();

	public TwoStageApplication(TwoStageQueryEngine engine, DatalogProgram program, GapByStore4ID gap) {
		this.engine = engine;
		tripleManager = new RDFoxTripleManager(engine.getDataStore(), m_incrementally);
		this.gap = gap;
		m_bottom = program.getUpperBottomStrategy();
		lowerProgram = program.getLower();

		Variable X = Variable.create("X");
		Collection<DLClause> clauses = getInitialClauses(program.getGeneral());
		Collection<DLClause> introducedConstraints = new LinkedList<DLClause>();
		LinkedList<Atom> newHeadAtoms = new LinkedList<Atom>();
		for (DLClause clause : m_bottom.process(clauses)) {
			if (m_bottom.isBottomRule(clause)
					|| clause.getHeadLength() == 1
					&& !(clause.getHeadAtom(0).getDLPredicate() instanceof AtLeast))
				addDatalogRule(clause);
			else {
				newHeadAtoms.clear();
				boolean changed = false;
				for (Atom atom : clause.getHeadAtoms()) {
					if (atom.getDLPredicate() instanceof AtLeastConcept) {
						AtLeastConcept atLeast = (AtLeastConcept) atom
								.getDLPredicate();
						if (atLeast.getToConcept() instanceof AtomicNegationConcept) {
							AtomicConcept positive = ((AtomicNegationConcept) atLeast
									.getToConcept()).getNegatedAtomicConcept();
							AtomicConcept negative = OverApproxExist
									.getNegationConcept(positive);
							Atom atom1 = Atom.create(positive, X);
							Atom atom2 = Atom.create(negative, X);
							introducedConstraints.add(DLClause.create(
									new Atom[0], new Atom[] { atom1, atom2 }));
							newHeadAtoms.add(Atom.create(AtLeastConcept.create(
									atLeast.getArity(), atLeast.getOnRole(),
									negative), atom.getArgument(0)));
							changed = true;
							continue;
						}
					} else if (atom.getDLPredicate() instanceof AtLeastDataRange)
						changed = true;
					else
						newHeadAtoms.add(atom);

				}
				if (!changed)
					constraints.add(clause);
				else if (!newHeadAtoms.isEmpty()) {
					DLClause newClause = DLClause.create(
							newHeadAtoms.toArray(new Atom[0]),
							clause.getBodyAtoms());
					map.put(newClause, clause);
					constraints.add(newClause);
				}
			}
		}

		for (DLClause clause : m_bottom.process(introducedConstraints))
			addDatalogRule(clause);

	}

	int materialise() {
		StringBuilder builder = new StringBuilder(getDatalogRuleText());
		for (DLClause clause: lowerProgram.getClauses())
			if (!rules.contains(clause))
				builder.append(RuleHelper.getText(clause));

		engine.materialise(builder.toString(), null, false);
		addAuxiliaryRules();
		addAuxiliaryNAFFacts();
		engine.materialise(getDatalogRuleText(), gap, m_incrementally);
		return engine.isValid() ? 1 : 0;
	}

	void checkNAFFacts() {
		int counter = 0;
		TupleIterator tuples = null;
		for (Atom atom : toGenerateNAFFacts) {
			try {
				counter = 0;
				atom = getNAFAtom(atom);
				tuples = engine.internal_evaluate(SparqlHelper.getSPARQLQuery(
						new Atom[] { atom }, atom.getArgumentVariable(0)
								.getName()));
				for (long multi = tuples.open(); multi != 0; multi = tuples.getNext()) {
					++counter;
				}
				Utility.logDebug(atom + " " + counter);
			} catch (JRDFStoreException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} finally {
				if (tuples != null)	tuples.dispose();
				tuples = null;
			}
		}
	}

	protected void addDatalogRule(DLClause clause) {
		if (clause.getBodyAtom(0).equals(clause.getHeadAtom(0)))
			return;
		rules.add(clause);
		datalogRuleText.append(RuleHelper.getText(clause)).append('\n');
	}

	public String getDatalogRuleText() {
		StringBuilder program = new StringBuilder();
		program.append(prefixes.prefixesText());
		program.append(datalogRuleText.toString());
		return program.toString();
	}

	protected abstract void addAuxiliaryRules();

	private void addAuxiliaryNAFFacts() {

		for (int id : tripleManager.getResourceIDs(engine.getAllIndividuals()))
			allIndividuals.add(id);

		DLPredicate naf;
		DLPredicate p;
		for (Atom atom: toGenerateNAFFacts) {
			naf = getNAFAtom(atom, false).getDLPredicate();
			p = atom.getDLPredicate();

			int typeID = tripleManager.getResourceID(Namespace.RDF_TYPE);
			int conceptID = tripleManager.getResourceID(((AtomicConcept) naf)
					.getIRI());
			for (int answer : generateNAFFacts(p)) {
				tripleManager.addTripleByID(new int[] { answer, typeID,
						conceptID });
			}
		}
	}

	private Collection<Integer> generateNAFFacts(DLPredicate p) {
		Variable X = Variable.create("X");
		TupleIterator tuples = null;
		Set<Integer> ret = new HashSet<Integer>(allIndividuals);
		try {
			tuples = engine.internal_evaluate(SparqlHelper.getSPARQLQuery(
					new Atom[] { Atom.create(p, X) }, "X"));
			for (long multi = tuples.open(); multi != 0; multi = tuples.getNext()) {
				ret.remove(tuples.getResourceID(0));
			}
		} catch (JRDFStoreException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			if (tuples != null)	tuples.dispose();
		}
		return ret;
	}

	protected abstract Collection<DLClause> getInitialClauses(Program program);

	protected Atom getNAFAtom(Atom atom) {
		return getNAFAtom(atom, true);
	}

	private Atom getNAFAtom(Atom atom, boolean update) {
		DLPredicate p = atom.getDLPredicate();
		if (update) {
			toGenerateNAFFacts.add(atom);
		}
		if (p instanceof AtomicConcept) {
			AtomicConcept nc = AtomicConcept.create(((AtomicConcept) p)
					.getIRI() + "_NAF");
			return Atom.create(nc, atom.getArgument(0));
		}
		if (p instanceof Equality || p instanceof AnnotatedEquality)
			return Atom.create(
					AtomicRole.create(Namespace.EQUALITY + NAF_suffix),
					atom.getArgument(0), atom.getArgument(1));
		if (p instanceof Inequality)
			atom = Atom.create(
					AtomicRole.create(Namespace.INEQUALITY + NAF_suffix),
					atom.getArgument(0), atom.getArgument(1));
		// if (p instanceof AtomicRole) {
		// AtomicRole nr = AtomicRole.create(((AtomicRole) p).getIRI() +
		// NAF_suffix);
		// return Atom.create(nr, atom.getArgument(0), atom.getArgument(1));
		// }
		if (p instanceof AtLeastConcept) {
			AtomicConcept nc = AtomicConcept.create(Normalisation
					.getAuxiliaryConcept4Disjunct((AtLeastConcept) p)
					+ NAF_suffix);
			return Atom.create(nc, atom.getArgument(0));
		}
		Utility.logError("Unknown DLPredicate in an atom: " + atom);
		return null;
	}

	protected DLClause getOriginalClause(DLClause clause) {
		DLClause original = map.get(clause);
		if (original == null)
			return clause;
		return original;
	}

}