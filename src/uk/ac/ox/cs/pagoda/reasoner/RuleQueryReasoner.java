package uk.ac.ox.cs.pagoda.reasoner;

import uk.ac.ox.cs.pagoda.multistage.MultiStageQueryEngine;
import uk.ac.ox.cs.pagoda.query.GapByStore4ID;
import uk.ac.ox.cs.pagoda.query.GapByStore4ID2;
import uk.ac.ox.cs.pagoda.query.QueryRecord;
import uk.ac.ox.cs.pagoda.reasoner.light.BasicQueryEngine;
import uk.ac.ox.cs.pagoda.rules.DatalogProgram;
import uk.ac.ox.cs.pagoda.tracking.TrackingRuleEncoderDisjVar1;
import uk.ac.ox.cs.pagoda.tracking.TrackingRuleEncoderWithGap;
import uk.ac.ox.cs.pagoda.util.PagodaProperties;
import uk.ac.ox.cs.pagoda.util.Timer;
import uk.ac.ox.cs.pagoda.util.Utility;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;

/***
 * A reasoner for answering queries over rule ontologies.
 */
public class RuleQueryReasoner extends MyQueryReasoner {

    private final Path ruleOntologyPath;

    public RuleQueryReasoner(PagodaProperties properties) {
        this.properties = properties;
        this.ruleOntologyPath = properties.getRuleOntologyPath();
    }

    /***
     * Just for testing.
     *
     * @param properties
     * @param ruleOntologyPath
     */
    protected RuleQueryReasoner(PagodaProperties properties, Path ruleOntologyPath) {
        this.properties = properties;
        this.ruleOntologyPath = properties.getRuleOntologyPath();
    }

    @Override
    public boolean preprocess() {
        try(InputStream ontologyInputStream = Files.newInputStream(ruleOntologyPath)){
            program = new DatalogProgram(ontologyInputStream);
//    		program.getLower().save();
//    		program.getUpper().save();
//    		program.getGeneral().save();

            if (!program.getGeneral().isHorn())
                lazyUpperStore = new MultiStageQueryEngine("lazy-upper-bound", true);

            importData(program.getAdditionalDataFile());
            isConsistent = ConsistencyStatus.UNCHECKED;
            rlLowerStore = new BasicQueryEngine("rl-lower-bound");
            trackingStore = new MultiStageQueryEngine("tracking", false);
            // todo create interface in consistency manager and add it here
//            ruleQueryReasoner.consistency = new ConsistencyManager(ruleQueryReasoner);

        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }

        timer = new Timer();

        Utility.logInfo("Preprocessing (and checking satisfiability)...");

        String name = "data", datafile = getImportedData();
        rlLowerStore.importRDFData(name, datafile);
        rlLowerStore.materialise("lower program", program.getLower().toString());
//		program.getLower().save();
        if(!consistency.checkRLLowerBound()) {
            Utility.logDebug("time for satisfiability checking: " + timer.duration());
            isConsistent = ConsistencyStatus.INCONSISTENT;
            return false;
        }
        Utility.logDebug("The number of sameAs assertions in RL lower store: " + rlLowerStore.getSameAsNumber());

        // todo mark original individuals (use Top)
//        String originalMarkProgram = OWLHelper.getOriginalMarkProgram(ontology);
        String originalMarkProgram = "";

        if(lazyUpperStore != null) {
            lazyUpperStore.importRDFData(name, datafile);
            lazyUpperStore.materialise("saturate named individuals", originalMarkProgram);
            int tag = lazyUpperStore.materialiseRestrictedly(program, null);
            if(tag == -1) {
                Utility.logDebug("time for satisfiability checking: " + timer.duration());
                isConsistent = ConsistencyStatus.INCONSISTENT;
                return false;
            }
            else if(tag != 1) {
                lazyUpperStore.dispose();
                lazyUpperStore = null;
            }
        }
        if(consistency.checkUpper(lazyUpperStore)) {
            isConsistent = ConsistencyStatus.CONSISTENT;
            Utility.logDebug("time for satisfiability checking: " + timer.duration());
        }

        trackingStore.importRDFData(name, datafile);
        trackingStore.materialise("saturate named individuals", originalMarkProgram);

        GapByStore4ID gap = new GapByStore4ID2(trackingStore, rlLowerStore);
        trackingStore.materialiseFoldedly(program, gap);
        predicatesWithGap = gap.getPredicatesWithGap();
        gap.clear();


        if(program.getGeneral().isHorn())
            encoder = new TrackingRuleEncoderWithGap(program.getUpper(), trackingStore);
        else
            encoder = new TrackingRuleEncoderDisjVar1(program.getUpper(), trackingStore);

        if(!isConsistent())
            return false;

        consistency.extractBottomFragment();

        return true;
    }

    @Override
    public void evaluate(QueryRecord queryRecord) {
        queryLowerAndUpperBounds(queryRecord);

        // TODO possibly add relevant rules subset extraction
        // TODO add delta-chase
    }
}
