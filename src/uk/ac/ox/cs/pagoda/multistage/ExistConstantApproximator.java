package uk.ac.ox.cs.pagoda.multistage;

import org.semanticweb.HermiT.model.DLClause;
import uk.ac.ox.cs.pagoda.rules.OverApproxExist;

import java.util.Collection;

/**
 * A wrapper for <tt>OverApproxExist</tt>.
 * */
public class ExistConstantApproximator implements ExistApproximator {

    private final OverApproxExist overApproxExist;

    public ExistConstantApproximator() {
        overApproxExist = new OverApproxExist();
    }

    @Override
    public Collection<DLClause> convert(DLClause clause, DLClause originalClause, Collection<AnswerTupleID> violationTuples) {
        return overApproxExist.convert(clause, originalClause);
    }
}
