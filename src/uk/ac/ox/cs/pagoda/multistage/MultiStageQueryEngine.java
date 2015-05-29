package uk.ac.ox.cs.pagoda.multistage;

import uk.ac.ox.cs.JRDFox.JRDFStoreException;
import uk.ac.ox.cs.pagoda.constraints.BottomStrategy;
import uk.ac.ox.cs.pagoda.multistage.treatement.Pick4NegativeConceptNaive;
import uk.ac.ox.cs.pagoda.multistage.treatement.Pick4NegativeConceptQuerySpecific;
import uk.ac.ox.cs.pagoda.multistage.treatement.Treatment;
import uk.ac.ox.cs.pagoda.query.GapByStore4ID;
import uk.ac.ox.cs.pagoda.query.QueryRecord;
import uk.ac.ox.cs.pagoda.rules.DatalogProgram;
import uk.ac.ox.cs.pagoda.rules.Program;
import uk.ac.ox.cs.pagoda.util.Timer;
import uk.ac.ox.cs.pagoda.util.Utility;
import uk.ac.ox.cs.pagoda.util.disposable.DisposedException;

import java.util.Collection;

public class MultiStageQueryEngine extends StageQueryEngine {

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
        treatment.dispose(); // does nothing
        return ret;
    }

    public int materialise4SpecificQuery(Program generalProgram, QueryRecord record, BottomStrategy upperBottom) {
        if(isDisposed()) throw new DisposedException();

        RestrictedApplication program = new RestrictedApplication(generalProgram, upperBottom);
        Treatment treatment = new Pick4NegativeConceptQuerySpecific(this, program, record);
        int ret = materialise(program, treatment, null);
        treatment.dispose();
        return ret;
    }

    /**
     * delta-chase
     */
    @Override
    public int materialiseSkolemly(DatalogProgram dProgram, GapByStore4ID gap) {
        if(isDisposed()) throw new DisposedException();

        materialise("lower program", dProgram.getLower().toString());
        Program generalProgram = dProgram.getGeneral();
        LimitedSkolemisationApplication program =
                new LimitedSkolemisationApplication(generalProgram, dProgram.getUpperBottomStrategy());
        Treatment treatment = new Pick4NegativeConceptNaive(this, program);
        return materialise(program, treatment, gap);
    }

    private int materialise(MultiStageUpperProgram program, Treatment treatment, GapByStore4ID gap) {
        if(gap != null)
            treatment.addAdditionalGapTuples();
        String programName = "multi-stage upper program";
        Utility.logInfo(name + " store is materialising " + programName + " ...");
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
                Utility.logInfo("Iteration " + ++iteration + ": ");

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
                    if(!incrementally) {
//						store.addRules(new String[] {datalogProgram});
                        store.importRules(datalogProgram);
                    }
                    store.applyReasoning(incrementally);
                }

//				Utility.logInfo("The number of sameAs assertions in the current store: " + getSameAsNumber());

                if(!isValid()) {
                    if(iteration == 1) {
                        Utility.logInfo("The ontology is inconsistent.");
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
                if((violations = program.isIntegrated(this, incrementally)) == null || violations.size() == 0) {
                    store.clearRulesAndMakeFactsExplicit();
                    Utility.logInfo(name + " store after materialising " + programName + ": " + tripleCount + " (" + (tripleCount - tripleCountBeforeMat) + " new)");
                    Utility.logInfo(name + " store is DONE for multi-stage materialising in " + t.duration() + " seconds.");
                    return isValid() ? 1 : 0;
                }
                Utility.logDebug("Time to detect violations: " + subTimer.duration());

                store.makeFactsExplicit();
                subTimer.reset();
                oldTripleCount = store.getTriplesCount();
                for(Violation v : violations) {

                    Utility.logDebug("Dealing with violation: " + v.constraint);
                    Utility.logDebug("Number of violation tuples: " + v.size());

                    Timer localTimer = new Timer();
                    int number = v.size();
                    long vOldCounter = store.getTriplesCount();
                    if(!treatment.makeSatisfied(v)) {
                        validMaterialisation = false;
                        Utility.logInfo(name + " store FAILED for multi-stage materialisation in " + t.duration() + " seconds.");
                        return 0;
                    }
                    Utility.logDebug("Time to make the constraint being satisfied: " + localTimer.duration());
                    Utility.logDebug("Triples in the store: before=" + vOldCounter + ", after=" + store.getTriplesCount() + ", new=" + (store
                            .getTriplesCount() - vOldCounter));
                }
                Utility.logDebug(name + " store after adding facts for violations: " + (tripleCount =
                        store.getTriplesCount()) + " (" + (tripleCount - oldTripleCount) + " new)");
                Utility.logDebug("Time to add triples for violations: " + subTimer.duration());
            }
        } catch(JRDFStoreException e) {
            e.printStackTrace();
        }
        return 0;
    }

}

