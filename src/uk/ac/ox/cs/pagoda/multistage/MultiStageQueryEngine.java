package uk.ac.ox.cs.pagoda.multistage;

import org.semanticweb.HermiT.model.Atom;
import org.semanticweb.HermiT.model.DLClause;
import org.semanticweb.HermiT.model.Individual;
import uk.ac.ox.cs.JRDFox.JRDFStoreException;
import uk.ac.ox.cs.JRDFox.store.DataStore;
import uk.ac.ox.cs.pagoda.constraints.BottomStrategy;
import uk.ac.ox.cs.pagoda.multistage.treatement.Pick4NegativeConceptNaive;
import uk.ac.ox.cs.pagoda.multistage.treatement.Pick4NegativeConceptQuerySpecific;
import uk.ac.ox.cs.pagoda.multistage.treatement.Treatment;
import uk.ac.ox.cs.pagoda.query.GapByStore4ID;
import uk.ac.ox.cs.pagoda.query.QueryRecord;
import uk.ac.ox.cs.pagoda.reasoner.light.RDFoxTripleManager;
import uk.ac.ox.cs.pagoda.rules.DatalogProgram;
import uk.ac.ox.cs.pagoda.rules.Program;
import uk.ac.ox.cs.pagoda.rules.approximators.SkolemTermsManager;
import uk.ac.ox.cs.pagoda.util.PagodaProperties;
import uk.ac.ox.cs.pagoda.util.Timer;
import uk.ac.ox.cs.pagoda.util.Utility;
import uk.ac.ox.cs.pagoda.util.disposable.DisposedException;
import uk.ac.ox.cs.pagoda.util.tuples.Tuple;
import uk.ac.ox.cs.pagoda.util.tuples.TupleBuilder;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

public class MultiStageQueryEngine extends StageQueryEngine {

    private HashMap<String, List> statistics = new HashMap<>();
    private Set<Tuple<Integer>> oversizedSkolemisedFacts;
    private RDFoxTripleManager rdFoxTripleManager;

    private int lastMaxTermDepth = -1;
    private boolean firstCall = true;

    public MultiStageQueryEngine(String name, boolean checkValidity) {
        super(name, checkValidity);
    }

    /**
     * c-chase
     */
    @Override
    public void materialiseFoldedly(DatalogProgram dProgram, GapByStore4ID gap) {
        if(isDisposed()) throw new DisposedException();

        materialise("lower program", dProgram.getLower().toString());
        Program generalProgram = dProgram.getGeneral();
        FoldedApplication program = new FoldedApplication(generalProgram, dProgram.getUpperBottomStrategy());
        Treatment treatment = new Pick4NegativeConceptNaive(this, program);
        materialise(program, treatment, gap);
    }

    /**
     * c-chase^f
     */
    @Override
    public int materialiseRestrictedly(DatalogProgram dProgram, GapByStore4ID gap) {
        if(isDisposed()) throw new DisposedException();

        if(gap != null)
            materialise("lower program", dProgram.getLower().toString());

        Program generalProgram = dProgram.getGeneral();
        RestrictedApplication program = new RestrictedApplication(generalProgram, dProgram.getUpperBottomStrategy());
        Treatment treatment = new Pick4NegativeConceptNaive(this, program);
        int ret = materialise(program, treatment, gap);
        treatment.dispose(); // FIXME does nothing
        return ret;
    }

    /**
     * delta-chase
     */
    public int materialiseSkolemly(DatalogProgram dProgram, GapByStore4ID gap, int maxTermDepth) {
        if(isDisposed()) throw new DisposedException();

        if(!firstCall && maxTermDepth <= lastMaxTermDepth)
            throw new IllegalArgumentException("maxTermDepth must be greater than " + lastMaxTermDepth);
        if(firstCall)
            materialise("lower program", dProgram.getLower().toString());
        lastMaxTermDepth = maxTermDepth;

        Program generalProgram = dProgram.getGeneral();
        LimitedSkolemisationApplication program =
                new LimitedSkolemisationApplication(generalProgram,
                                                    dProgram.getUpperBottomStrategy(),
                                                    maxTermDepth);
        rdFoxTripleManager = new RDFoxTripleManager(store, true);
        Treatment treatment = new Pick4NegativeConceptNaive(this, program, rdFoxTripleManager);
        int result = materialise(program, treatment, gap, maxTermDepth);
        firstCall = false;
        return result;
    }

    public int materialise4SpecificQuery(Program generalProgram, QueryRecord record, BottomStrategy upperBottom) {
        if(isDisposed()) throw new DisposedException();

        RestrictedApplication program = new RestrictedApplication(generalProgram, upperBottom);
        Treatment treatment = new Pick4NegativeConceptQuerySpecific(this, program, record);
        int ret = materialise(program, treatment, null);
        treatment.dispose(); // FIXME does nothing
        return ret;
    }

    @Override
    public void dispose() {
        super.dispose();

        if(PagodaProperties.isDebuggingMode())
            outputStatistics();
    }

    private int materialise(MultiStageUpperProgram program, Treatment treatment, GapByStore4ID gap) {
        return materialise(program, treatment, gap, -1);
    }

    private int materialise(MultiStageUpperProgram program, Treatment treatment, GapByStore4ID gap, int maxTermDepth) {
        if(!firstCall)
            cleanStoreFromOversizedSkolemisedFacts();

        boolean isSkolemising = maxTermDepth > 0;
//        boolean isSkolemising = true;

        if(gap != null)
            treatment.addAdditionalGapTuples();
        String programName = "multi-stage upper program";
        Timer t = new Timer();

        String datalogProgram = program.getDatalogRuleText();
        long tripleCountBeforeMat = 0;

        // TODO to be removed ...
//		if (gap == null)
//			program.save("output/multi.dlog");

        Collection<Violation> violations;
        int iteration = 0;
        Timer subTimer = new Timer();
        boolean incrementally;
        try {
            while(true) {
                long oldTripleCount = store.getTriplesCount();

                subTimer.reset();
                Utility.logInfo(name + " store is materialising " +
                                        programName + "... (iteration " + ++iteration + ")");

                incrementally = (iteration != 1);

                if(!incrementally)
                    tripleCountBeforeMat = oldTripleCount;

                if(gap != null) {
                    try {
                        gap.compile(incrementally ? null : datalogProgram);
                        gap.addBackTo();
                    } finally {
                        gap.clear();
                    }
                }
                else {
                    if(!incrementally && firstCall) {
//						store.addRules(new String[] {datalogProgram});
                        store.importRules(datalogProgram);
                    }
                    store.applyReasoning(incrementally || !firstCall);
                }

//				Utility.logInfo("The number of sameAs assertions in the current store: " + getSameAsNumber());

                if(!isValid()) {
                    if(iteration == 1) {
                        Utility.logDebug("The ontology is inconsistent.");
                        return -1;
                    }
                    Utility.logInfo(name + " store FAILED for multi-stage materialisation in " + t.duration() + " seconds.");
                    return 0;
                }
                else validMaterialisation = null;

                long tripleCount = store.getTriplesCount();
                Utility.logDebug(name + " store after materialising datalog-rules: " + tripleCount + " (" + (tripleCount - oldTripleCount) + " new)");
                Utility.logDebug("Time to materialise datalog-rules: " + subTimer.duration());

                subTimer.reset();
                if((violations = program.isIntegrated(this, !isSkolemising && incrementally)) == null || violations.size() == 0) {
                    if(!isSkolemising)
                        store.clearRulesAndMakeFactsExplicit();
                    Utility.logDebug(name + " store after materialising " + programName + ": " + tripleCount + " (" + (tripleCount - tripleCountBeforeMat) + " new)");
                    Utility.logDebug(name + " store is DONE for multi-stage materialising in " + t.duration() + " seconds.");
                    return isValid() ? 1 : 0;
                }
                Utility.logDebug("Time to detect violations: " + subTimer.duration());

                if(!isSkolemising)
                    store.makeFactsExplicit();
                subTimer.reset();
                oldTripleCount = store.getTriplesCount();

                Utility.logDebug("Number of violations: " + violations.size());

                updateStatistics("violationClauses", violations.stream()
                                                               .map(Violation::getClause)
                                                               .collect(Collectors.toList()));

                for(Violation v : violations) {

                    Utility.logDebug("Dealing with violation: " + v.constraint);
                    Utility.logDebug("Number of violation tuples: " + v.size());

                    Timer localTimer = new Timer();
                    int number = v.size();
                    long vOldCounter = store.getTriplesCount();
                    Set<Treatment.AtomWithIDTriple> satisfiabilityFacts;
                    if((satisfiabilityFacts = treatment.makeSatisfied(v)) == null) {
                        validMaterialisation = false;
                        Utility.logInfo(name + " store FAILED for multi-stage materialisation in " + t.duration() + " seconds.");
                        return 0;
                    }

                    addOversizedSkolemisedFacts(getOversizedSkolemisedFacts(satisfiabilityFacts, maxTermDepth));

                    Utility.logDebug("Time to make the constraint being satisfied: " + localTimer.duration());
                    Utility.logDebug("Triples in the store: before=" + vOldCounter + ", after=" + store.getTriplesCount() + ", new=" + (store
                            .getTriplesCount() - vOldCounter));
                }
                Utility.logDebug(name + " store after adding facts for violations: " + (tripleCount =
                        store.getTriplesCount()) + " (" + (tripleCount - oldTripleCount) + " new)");
                Utility.logDebug("Time to add triples for violations: " + subTimer.duration());

                Utility.logDebug("Number of Skolem individuals: " + SkolemTermsManager.getInstance()
                                                                                      .getSkolemIndividualsCount());
            }
        } catch(JRDFStoreException e) {
            e.printStackTrace();
        }
        return 0;
    }

    private boolean cleanStoreFromOversizedSkolemisedFacts() {
        if(oversizedSkolemisedFacts == null || oversizedSkolemisedFacts.isEmpty())
            return false;

        try {
            for (Tuple<Integer> tuple : oversizedSkolemisedFacts) {
                int[] triple = new int[]{tuple.get(0), tuple.get(1), tuple.get(2)};
                store.addTriplesByResourceIDs(triple, DataStore.UpdateType.ScheduleForDeletion);
            }
        } catch (JRDFStoreException e) {
            e.printStackTrace();
            System.exit(1);
        }
        oversizedSkolemisedFacts = new HashSet<>();

        return true;
    }

    private void addOversizedSkolemisedFacts(Set<Tuple<Integer>> facts) {
        if(oversizedSkolemisedFacts == null)
            oversizedSkolemisedFacts = new HashSet<>();
        oversizedSkolemisedFacts.addAll(facts);
    }

    /**
     * Get triples containing Skolem individuals of depth greater or equal than the maximum.
     *
     * @param satisfiabilityFacts
     * @return
     */
    private Set<Tuple<Integer>> getOversizedSkolemisedFacts(Set<Treatment.AtomWithIDTriple> satisfiabilityFacts, int maxDepth) {
        HashSet<Tuple<Integer>> result = new HashSet<>();
        SkolemTermsManager termsManager = SkolemTermsManager.getInstance();
        for (Treatment.AtomWithIDTriple atomWithIDTriple : satisfiabilityFacts) {
            Atom atom = atomWithIDTriple.getAtom();
            if(atom.getArity() == 1) {
                if(atom.getArgument(0) instanceof Individual && termsManager.getDepthOf((Individual) atom.getArgument(0)) >= maxDepth) {
                    int[] idTriple = atomWithIDTriple.getIDTriple();
                    result.add(new TupleBuilder<Integer>().append(idTriple[0]).append(idTriple[1])
                            .append(idTriple[2]).build());
                }
//                else if(!(atom.getArgument(0) instanceof Individual))
//                    throw new IllegalArgumentException("No individuals: " + atom);
            }
            else {
                if((atom.getArgument(0) instanceof Individual && termsManager.getDepthOf((Individual) atom.getArgument(0)) >= maxDepth)
                    || (atom.getArgument(1) instanceof Individual && termsManager.getDepthOf((Individual) atom.getArgument(1)) >= maxDepth)){
                    int[] idTriple = atomWithIDTriple.getIDTriple();
                    result.add(new TupleBuilder<Integer>().append(idTriple[0]).append(idTriple[1])
                            .append(idTriple[2]).build());
                }
//                else if(!(atom.getArgument(0) instanceof Individual) && !(atom.getArgument(1) instanceof Individual))
//                    throw new IllegalArgumentException("No individuals: " + atom);
            }

        }
        return result;
    }

    private void updateStatistics(String key, List<DLClause> value) {
        if(!statistics.containsKey(key))
            statistics.put(key, new ArrayList<List>());
        statistics.get(key).add(value.stream().map(DLClause::toString).collect(Collectors.toList()));
    }

    private void outputStatistics() {
        Path statisticsPath = PagodaProperties.getDefaultStatisticsDir()
                                              .resolve(Paths.get("MultiStageQueryEngine-ViolationSequence.json"));
        try(BufferedWriter writer = Files.newBufferedWriter(statisticsPath)) {
            QueryRecord.GsonCreator.getInstance().toJson(statistics, writer);
        } catch(IOException e) {
            e.printStackTrace();
        }
    }

}

