package uk.ac.ox.cs.pagoda.rules.approximators;

import org.semanticweb.HermiT.model.*;
import uk.ac.ox.cs.pagoda.hermit.RuleHelper;
import uk.ac.ox.cs.pagoda.model.BinaryPredicate;
import uk.ac.ox.cs.pagoda.model.UnaryPredicate;
import uk.ac.ox.cs.pagoda.multistage.MultiStageUpperProgram;
import uk.ac.ox.cs.pagoda.util.tuples.Tuple;
import uk.ac.ox.cs.pagoda.util.tuples.TupleBuilder;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Set;

/***
 * Approximates existential rules through a limited form of Skolemisation.
 * <p>
 * Given a rule and a ground substitution,
 * it Skolemises the rule
 * if all the terms in the substitution have depth less than a given depth,
 * otherwise it approximates using an alternative <tt>TupleDependentApproximator</tt>.
 */
public class LimitedSkolemisationApproximator implements TupleDependentApproximator {

    private static final Atom[] EMPTY_BODY = new Atom[0];
    private static final Variable X = Variable.create("X");
    private final int maxTermDepth;
    private final SkolemTermsManager skolemTermsManager;

    public LimitedSkolemisationApproximator(int maxTermDepth) {
        this.maxTermDepth = maxTermDepth;
        this.skolemTermsManager = SkolemTermsManager.getInstance();
    }

    @Override
    public Collection<DLClause> convert(DLClause clause,
                                        DLClause originalClause,
                                        Collection<Tuple<Individual>> violationTuples) {
        switch(clause.getHeadLength()) {
            case 1:
                return overApprox(clause, originalClause, violationTuples);
            case 0:
                return Collections.singletonList(clause);
            default:
                throw new IllegalArgumentException(
                        "Expected clause with head length < 1, but it is " + clause.getHeadLength());
        }


    }

    public int getMaxDepth(Tuple<Individual> violationTuple) {
        int maxDepth = 0;
        for(Individual individual : violationTuple)
            maxDepth = Integer.max(maxDepth, skolemTermsManager.getDepthOf(individual));

        return maxDepth;
    }

    private Collection<DLClause> overApprox(DLClause clause, DLClause originalClause, Collection<Tuple<Individual>> violationTuples) {
        ArrayList<DLClause> result = new ArrayList<>();
        for(Tuple<Individual> violationTuple : violationTuples) {
                result.addAll(getGroundSkolemisation(clause,
                        originalClause, violationTuple, getMaxDepth(violationTuple) >= maxTermDepth));
        }

        return result;
    }

    private Collection<DLClause> getGroundSkolemisation(DLClause clause,
                                                        DLClause originalClause,
                                                        Tuple<Individual> violationTuple,
                                                        boolean useClauseUniqueIndividual) {

        String[] commonVars = MultiStageUpperProgram.getCommonVars(clause);

        // TODO check: strong assumption, the first tuples are the common ones
        TupleBuilder<Individual> commonIndividualsBuilder = new TupleBuilder<>();
        for(int i = 0; i < commonVars.length; i++)
            commonIndividualsBuilder.append(violationTuple.get(i));
        Tuple<Individual> commonIndividuals = commonIndividualsBuilder.build();

        Atom headAtom = clause.getHeadAtom(0);
        Atom[] bodyAtoms = clause.getBodyAtoms();

//        Atom[] bodyAtoms = clause.getBodyAtoms();
        int offset = OverApproxExist.indexOfExistential(headAtom, originalClause);

        // BEGIN: copy and paste
        ArrayList<DLClause> ret = new ArrayList<>();
        DLPredicate predicate = headAtom.getDLPredicate();
        Set<Variable> unsafeVars;
        if(predicate instanceof AtLeastConcept) {
            AtLeastConcept atLeastConcept = (AtLeastConcept) predicate;
            LiteralConcept concept = atLeastConcept.getToConcept();
            Role role = atLeastConcept.getOnRole();
            AtomicConcept atomicConcept;

            if(concept instanceof AtomicNegationConcept) {
                Atom atom1 =
                        Atom.create(atomicConcept = ((AtomicNegationConcept) concept).getNegatedAtomicConcept(), X);
                Atom atom2 = Atom.create(atomicConcept = (AtomicConcept) OverApproxExist.getNegationPredicate(atomicConcept), X);
                ret.add(DLClause.create(new Atom[0], new Atom[]{atom1, atom2}));
            }
            else {
                atomicConcept = (AtomicConcept) concept;
                if(atomicConcept.equals(AtomicConcept.THING))
                    atomicConcept = null;
            }

            int card = atLeastConcept.getNumber();
            Individual[] individuals = new Individual[card];
            SkolemTermsManager termsManager = SkolemTermsManager.getInstance();
            for(int i = 0; i < card; ++i)
                if(useClauseUniqueIndividual)
                    individuals[i] = termsManager.getFreshIndividual(originalClause,
                                                                     offset + i,
                                                                     maxTermDepth + 1);
                else
                    individuals[i] = termsManager.getFreshIndividual(originalClause,
                                                                     offset + i,
                                                                     commonIndividuals);

            for(int i = 0; i < card; ++i) {
                if(atomicConcept != null)
                    ret.add(DLClause.create(new Atom[]{Atom.create(atomicConcept, individuals[i])}, EMPTY_BODY));

                Atom atom = role instanceof AtomicRole ?
                        Atom.create((AtomicRole) role, commonIndividuals.get(0), individuals[i]) :
                        Atom.create(((InverseRole) role).getInverseOf(), individuals[i], commonIndividuals.get(0));

                ret.add(DLClause.create(new Atom[]{atom}, EMPTY_BODY));
            }

            for(int i = 0; i < card; ++i)
                for(int j = i + 1; j < card; ++j)
                    // TODO to be checked ... different as
                    ret.add(DLClause.create(new Atom[]{Atom.create(Inequality.INSTANCE, individuals[i], individuals[j])}, EMPTY_BODY));

        }
        else if((headAtom.getDLPredicate() instanceof UnaryPredicate ||
                headAtom.getDLPredicate() instanceof BinaryPredicate) &&
                (unsafeVars = RuleHelper.getUnsafeVariables(DLClause.create(new Atom[]{headAtom}, bodyAtoms))) != null) {
            // TODO check correctness
            SkolemTermsManager termsManager = SkolemTermsManager.getInstance();
            if(headAtom.getDLPredicate() instanceof UnaryPredicate) {
                Individual freshIndividual;
                if(useClauseUniqueIndividual)
                    freshIndividual = termsManager.getFreshIndividual(originalClause,
                                                                      offset,
                                                                      maxTermDepth + 1);
                else
                    freshIndividual = termsManager.getFreshIndividual(originalClause,
                                                                      offset,
                                                                      commonIndividuals);

                Atom newHead = Atom.create(headAtom.getDLPredicate(), freshIndividual);
                ret.add(DLClause.create(new Atom[]{newHead}, bodyAtoms));
            }
            if(headAtom.getDLPredicate() instanceof BinaryPredicate) {
                Variable var0 = headAtom.getArgumentVariable(0);
                Variable var1 = headAtom.getArgumentVariable(1);
                Term term0, term1;
                if(unsafeVars.contains(var0))
                    if(useClauseUniqueIndividual)
                        term0 = termsManager.getFreshIndividual(originalClause, offset, maxTermDepth + 1);
                    else
                        term0 = termsManager.getFreshIndividual(originalClause, offset, commonIndividuals);
                else
                    term0 = var0;
                if(unsafeVars.contains(var1))
                    if(useClauseUniqueIndividual)
                        term1 = termsManager.getFreshIndividual(originalClause, offset + 1, maxTermDepth + 1);
                    else
                        term1 = termsManager.getFreshIndividual(originalClause, offset + 1, commonIndividuals);
                else
                    term1 = var1;
                Atom newHead = Atom.create(headAtom.getDLPredicate(), term0, term1);
                ret.add(DLClause.create(new Atom[]{newHead}, bodyAtoms));
            }

        }
        else if(predicate instanceof AtLeastDataRange) {
            // TODO to be implemented ...
        }
        else
            ret.add(DLClause.create(new Atom[]{headAtom}, EMPTY_BODY));

        return ret;

        // END: copy and paste
    }
}
