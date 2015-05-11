package uk.ac.ox.cs.pagoda.multistage;

import org.semanticweb.HermiT.model.DLClause;

import java.util.Collection;

/**
 * Approximates existential rules by a limited form of Skolemisation.
 * <p>
 * Given a rule and a grounding substitution,
 * it Skolemises the rule if
 * all the terms in the substitution have depth less than a given depth,
 * otherwise it approximates using an alternative <tt>ExistApproximator</tt>.
 *
 * */
public class LimitedSkolemisationApproximator implements ExistApproximator {

    private final int maxTermDepth;
    private final ExistApproximator alternativeApproximator;

    public LimitedSkolemisationApproximator(int maxTermDepth) {
        this(maxTermDepth, new ExistConstantApproximator());
    }

    public LimitedSkolemisationApproximator(int maxTermDepth, ExistApproximator alternativeApproximator) {
        this.maxTermDepth = maxTermDepth;
        this.alternativeApproximator = alternativeApproximator;
    }

    @Override
    public Collection<DLClause> convert(DLClause clause,
                                        DLClause originalClause,
                                        Collection<AnswerTupleID> violationTuples) {
        return null;
    }
}
