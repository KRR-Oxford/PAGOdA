package uk.ac.ox.cs.pagoda.multistage.treatement;

import org.semanticweb.HermiT.model.*;
import uk.ac.ox.cs.JRDFox.JRDFStoreException;
import uk.ac.ox.cs.JRDFox.store.TupleIterator;
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
import uk.ac.ox.cs.pagoda.util.disposable.DisposedException;
import uk.ac.ox.cs.pagoda.util.tuples.Tuple;
import uk.ac.ox.cs.pagoda.util.tuples.TupleBuilder;

import java.util.*;

public abstract class Pick4NegativeConcept extends Treatment {

    public Set<Atom> addedGroundAtoms = new HashSet<Atom>();
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

    @Override
    public void addAdditionalGapTuples() {
        if(isDisposed()) throw new DisposedException();
        addGap = true;
    }

    void addTripleByID(Atom atom, Atom gapAtom, Map<Variable, Integer> assignment) {
        if(isDisposed()) throw new DisposedException();
        int[] newTuple = tripleManager.getInstance(atom, assignment);
        tripleManager.addTripleByID(newTuple);
        if(addGap)
            tripleManager.addTripleByID(tripleManager.getInstance(gapAtom, assignment));
    }

    protected boolean makeSatisfied(Violation violation, Comparator<Atom> comp) {
        LinkedList<AnswerTupleID> tuples = violation.getTuples();
        DLClause constraint = violation.getConstraint();
        Map<Variable, Integer> assignment = new HashMap<Variable, Integer>();

        if(constraint.getHeadLength() > 1) {
            Atom[] orderedAtoms = Arrays.copyOf(constraint.getHeadAtoms(), constraint.getHeadLength());
            Arrays.sort(orderedAtoms, comp);

            Set<AnswerTupleID> negTuples = new HashSet<AnswerTupleID>();
            String negativeQuery;
            String[] subVars;
            for(Atom headAtom : orderedAtoms) {
                Atom negativeAtom = MultiStageUpperProgram.getNegativeAtom(headAtom);
                if(negativeAtom == null) continue;
                negativeQuery = SparqlHelper.getSPARQLQuery(new Atom[]{negativeAtom},
                                                            subVars =
                                                                    MultiStageUpperProgram.getVarSubset(violation.getVariables(), headAtom));
                negTuples.clear();
                Atom gapHeadAtom = addGap ? getGapAtom(headAtom) : null;
                TupleIterator negAnswers = null;
                try {
                    negAnswers = engine.internal_evaluateNotExpanded(negativeQuery);
                    for(long multi = negAnswers.open(); multi != 0; multi = negAnswers.getNext())
                        negTuples.add(new AnswerTupleID(negAnswers));
                } catch(JRDFStoreException e) {
                    e.printStackTrace();
                } finally {
                    if(negAnswers != null) negAnswers.dispose();
                }

                if(!tuples.isEmpty())
//					program.addUpdatedPredicates(dependencyGraph.getDependence(headAtom.getDLPredicate()));
                    program.addUpdatedPredicate(headAtom.getDLPredicate());

                Comparator<AnswerTupleID> tComp = new TupleComparator(subVars);
                Collections.sort(tuples, tComp);

                AnswerTupleID lastAdded = null;

                for(Iterator<AnswerTupleID> iter = tuples.iterator(); iter.hasNext(); ) {

                    AnswerTupleID tuple = iter.next();
                    if(!negTuples.contains(MultiStageUpperProgram.project(tuple, violation.getVariables(), subVars))) {
                        if(lastAdded == null || tComp.compare(lastAdded, tuple) != 0) {
                            lastAdded = tuple;
                            tuple.getAssignment(violation.getVariables(), assignment);
                            addTripleByID(headAtom, gapHeadAtom, assignment);
                        }
                        iter.remove();
                    }
                }
//				tuples.reset();

                if(tuples.isEmpty())
                    return true;
            }
            if(!tuples.isEmpty()) return false;
        }
        else {
            Set<Atom> headAtoms = new HashSet<Atom>();

            ArrayList<Tuple<Individual>> violationTuples = new ArrayList<>(violation.getTuples().size());
            for(int i = 0; i < violation.getTuples().size(); i++) {
                AnswerTupleID answerTupleID = violation.getTuples().get(i);
                TupleBuilder<Individual> tupleBuilder = new TupleBuilder<>();
                for(int j = 0; j < answerTupleID.getArity(); j++) {
                    String rawTerm = tripleManager.getRawTerm(answerTupleID.getTerm(j));
                    Individual individual = Individual.create(rawTerm.substring(1, rawTerm.length() - 1));
                    tupleBuilder.append(individual);
                }
                violationTuples.add(tupleBuilder.build());
            }

            for(DLClause clause : program.convertExist(constraint, violation.getClause(), violationTuples)) {

                if(!DLClauseHelper.hasSubsetBodyAtoms(clause, constraint)) {
                    Utility.logError("There might be an error here... Cannot happen!!!");
                    throw new Error("This condition should not happen!!!");
                }

                Atom tHeadAtom = clause.getHeadAtom(0);
                Atom tGapHeadAtom = addGap ? getGapAtom(tHeadAtom) : null;
                if(DLClauseHelper.isGround(tHeadAtom)) {
                    if(!addedGroundAtoms.contains(tHeadAtom)) {
                        program.addUpdatedPredicate(tHeadAtom.getDLPredicate());
                        addTripleByID(tHeadAtom, tGapHeadAtom, null);
                        addedGroundAtoms.add(tHeadAtom);
                    }
                }
                else headAtoms.add(tHeadAtom);
            }
            if(!tuples.isEmpty())
                for(Atom atom : headAtoms)
                    program.addUpdatedPredicate(atom.getDLPredicate());

            for(AnswerTupleID tuple : tuples) {
                tuple.getAssignment(violation.getVariables(), assignment);
                for(Atom atom : headAtoms) {
                    addTripleByID(atom, getGapAtom(atom), assignment);
                }
            }
        }

        assignment.clear();
        return true;
    }

    private Atom getGapAtom(Atom atom) {
        if(!addGap) return null;
        String gapPredicate = GapTupleIterator.getGapPredicate(getPredicateIRI(atom.getDLPredicate()));
        Atom gapAtom = atom.getArity() == 1 ? Atom.create(AtomicConcept.create(gapPredicate), atom.getArgument(0)) :
                Atom.create(AtomicRole.create(gapPredicate), atom.getArgument(0), atom.getArgument(1));
        return gapAtom;
    }

    private String getPredicateIRI(DLPredicate dlPredicate) {
        if(dlPredicate instanceof Equality || dlPredicate instanceof AnnotatedEquality)
            return Namespace.EQUALITY;
        if(dlPredicate instanceof Inequality)
            return Namespace.INEQUALITY;
        if(dlPredicate instanceof AtomicConcept)
            return ((AtomicConcept) dlPredicate).getIRI();
        if(dlPredicate instanceof AtomicRole)
            return ((AtomicRole) dlPredicate).getIRI();
        return null;
    }

}

class TupleComparator implements Comparator<AnswerTupleID> {

    int[] validIndexes;

    public TupleComparator(String[] validTerms) {
        int num = 0;
        for(int i = 0; i < validTerms.length; ++i)
            if(validTerms[i] != null)
                ++num;
        validIndexes = new int[num];
        for(int i = 0, j = 0; i < validTerms.length; ++i)
            if(validTerms[i] != null)
                validIndexes[j++] = i;
    }

    @Override
    public int compare(AnswerTupleID o1, AnswerTupleID o2) {
        int delta = 0;
        for(int i = 0; i < validIndexes.length; ++i)
            if((delta = o1.getTerm(validIndexes[i]) - o2.getTerm(validIndexes[i])) != 0)
                return delta;
        return 0;
    }

}