package uk.ac.ox.cs.pagoda.rules.approximators;

import org.semanticweb.HermiT.model.DLClause;
import org.semanticweb.HermiT.model.Individual;
import uk.ac.ox.cs.pagoda.util.tuples.Tuple;

import java.util.Collection;

/**
 * It can approximate clauses according to a collection of tuples of individuals.
 * <p>
 * In particular it can be used to approximate rules given some body instantiations.
 */
public interface TupleDependentApproximator {

    Collection<DLClause> convert(DLClause clause,
                                 DLClause originalClause,
                                 Collection<Tuple<Individual>> individualsTuples);
}
