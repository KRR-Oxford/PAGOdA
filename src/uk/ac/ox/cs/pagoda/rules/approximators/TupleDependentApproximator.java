package uk.ac.ox.cs.pagoda.rules.approximators;

import org.semanticweb.HermiT.model.DLClause;
import uk.ac.ox.cs.pagoda.multistage.AnswerTupleID;

import java.util.Collection;

/**
 * It approximates rules according to a specific instantiation of the body.
 */
public interface TupleDependentApproximator {

    Collection<DLClause> convert(DLClause clause,
                                 DLClause originalClause,
                                 Collection<AnswerTupleID> violationTuples);
}
