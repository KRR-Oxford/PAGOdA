package uk.ac.ox.cs.pagoda.rules;

import org.semanticweb.HermiT.model.DLClause;
import org.semanticweb.HermiT.model.Individual;
import uk.ac.ox.cs.pagoda.rules.approximators.OverApproxExist;
import uk.ac.ox.cs.pagoda.rules.approximators.TupleDependentApproximator;
import uk.ac.ox.cs.pagoda.util.tuples.Tuple;

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
    public Collection<DLClause> convert(DLClause clause, DLClause originalClause, Collection<Tuple<Individual>> violationTuples) {
        return overApproxExist.convert(clause, originalClause);
    }
}
