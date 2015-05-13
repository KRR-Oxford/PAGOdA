package uk.ac.ox.cs.pagoda.rules;

import org.semanticweb.HermiT.model.DLClause;
import uk.ac.ox.cs.pagoda.multistage.AnswerTupleID;
import uk.ac.ox.cs.pagoda.rules.approximators.OverApproxExist;
import uk.ac.ox.cs.pagoda.rules.approximators.TupleDependentApproximator;

import java.util.Collection;

/**
 * A wrapper for <tt>OverApproxExist</tt>.
 * */
public class ExistConstantApproximator implements TupleDependentApproximator {

    private final OverApproxExist overApproxExist;

    public ExistConstantApproximator() {
        overApproxExist = new OverApproxExist();
    }

    @Override
    public Collection<DLClause> convert(DLClause clause, DLClause originalClause, Collection<AnswerTupleID> violationTuples) {
        return overApproxExist.convert(clause, originalClause);
    }
}
