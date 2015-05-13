package uk.ac.ox.cs.pagoda.rules.approximators;

import org.semanticweb.HermiT.model.AtLeast;
import org.semanticweb.HermiT.model.Atom;
import org.semanticweb.HermiT.model.DLClause;
import org.semanticweb.HermiT.model.Individual;
import uk.ac.ox.cs.pagoda.multistage.AnswerTupleID;
import uk.ac.ox.cs.pagoda.util.Namespace;

import java.util.HashMap;
import java.util.Map;

/**
 * If you need a Skolem term (i.e. fresh individual), ask this class.
 */
public class SkolemTermsDispenser {

    public static final String skolemisedIndividualPrefix = Namespace.PAGODA_ANONY + "individual";
    private static SkolemTermsDispenser skolemTermsDispenser;
    private int individualCounter = 0;
    private Map<DLClause, Integer> termNumber = new HashMap<>();
    private Map<SkolemTermId, Integer> mapTermToDepth = new HashMap<>();
    private int dependenciesCounter = 0;
    private Map<AnswerTupleID, Integer> mapDependencyToId = new HashMap<>();

    private SkolemTermsDispenser() {
    }

    public static SkolemTermsDispenser getInstance() {
        if (skolemTermsDispenser == null) skolemTermsDispenser = new SkolemTermsDispenser();
        return skolemTermsDispenser;
    }

    private int getDependencyId(AnswerTupleID answerTupleID) {
        if (mapDependencyToId.containsKey(answerTupleID)) return mapDependencyToId.get(answerTupleID);
        else return mapDependencyToId.put(answerTupleID, dependenciesCounter++);
    }

    public Individual getNewIndividual(DLClause originalClause, int offset, AnswerTupleID dependency) {
        if (!termNumber.containsKey(originalClause)) {
            termNumber.put(originalClause, individualCounter);
            individualCounter += noOfExistential(originalClause);
        }
        if (!mapDependencyToId.containsKey(dependency)) {
            mapDependencyToId.put(dependency, dependenciesCounter++);
        }

        SkolemTermId termId = new SkolemTermId(termNumber.get(originalClause) + offset, getDependencyId(dependency));
        return Individual.create(skolemisedIndividualPrefix + termId);
    }

    private int noOfExistential(DLClause originalClause) {
        int no = 0;
        for (Atom atom : originalClause.getHeadAtoms())
            if (atom.getDLPredicate() instanceof AtLeast)
                no += ((AtLeast) atom.getDLPredicate()).getNumber();
        return no;
    }

    private class SkolemTermId {
        private final int idBase;
        private final int idOffset;

        private SkolemTermId(int idBase, int idOffset) {
            this.idBase = idBase;
            this.idOffset = idOffset;
        }

        @Override
        public String toString() {
            return idBase + "_" + idOffset;
        }
    }
}
