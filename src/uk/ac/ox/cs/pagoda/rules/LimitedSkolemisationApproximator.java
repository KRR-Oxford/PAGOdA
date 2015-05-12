package uk.ac.ox.cs.pagoda.rules;

import org.semanticweb.HermiT.model.DLClause;
import uk.ac.ox.cs.pagoda.multistage.AnswerTupleID;

import java.util.*;

/**
 * Approximates existential rules by a limited form of Skolemisation.
 * <p>
 * Given a rule and a grounding substitution,
 * it Skolemises the rule if
 * all the terms in the substitution have depth less than a given depth,
 * otherwise it approximates using an alternative <tt>TupleDependentApproximator</tt>.
 *
 * */
public class LimitedSkolemisationApproximator implements TupleDependentApproximator {

    private final int maxTermDepth;
    private final TupleDependentApproximator alternativeApproximator;

    private Map<AnswerTupleID, Integer> mapIndividualsToDepth;

    public LimitedSkolemisationApproximator(int maxTermDepth) {
        this(maxTermDepth, new ExistConstantApproximator());
    }

    public LimitedSkolemisationApproximator(int maxTermDepth, TupleDependentApproximator alternativeApproximator) {
        this.maxTermDepth = maxTermDepth;
        this.alternativeApproximator = alternativeApproximator;
        this.mapIndividualsToDepth = new HashMap<>();
    }

    @Override
    public Collection<DLClause> convert(DLClause clause, DLClause originalClause, Collection<AnswerTupleID> violationTuples) {
        switch (clause.getHeadLength()) {
            case 1:
                return overApprox(clause, originalClause, violationTuples);
            case 0:
                return Arrays.asList(clause);
            default:
                ArrayList<DLClause> result = new ArrayList<>();
                // TODO implement
                return result;
        }


    }

    private Collection<DLClause> overApprox(DLClause clause, DLClause originalClause, Collection<AnswerTupleID> violationTuples) {
        ArrayList<DLClause> result = new ArrayList<>();

        for (AnswerTupleID violationTuple : violationTuples)
            if (getDepth(violationTuple) > maxTermDepth)
                result.addAll(alternativeApproximator.convert(clause, originalClause, null));
            else
                result.add(getInstantiatedSkolemisation(clause, originalClause, violationTuple));

        return result;
    }


    private DLClause getInstantiatedSkolemisation(DLClause clause, DLClause originalClause, AnswerTupleID violationTuple) {
        // TODO implement
        return null;
    }

    private int getDepth(AnswerTupleID violationTuple) {
        if (!mapIndividualsToDepth.containsKey(violationTuple)) return 0;
        return mapIndividualsToDepth.get(violationTuple);
    }
}
