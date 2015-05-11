package uk.ac.ox.cs.pagoda.multistage;

import org.semanticweb.HermiT.model.DLClause;

import java.util.Collection;

/**
 * An approximator for existential rules.
 * */
public interface ExistApproximator {

    Collection<DLClause> convert(DLClause clause,
                                 DLClause originalClause,
                                 Collection<AnswerTupleID> violationTuples);
}
