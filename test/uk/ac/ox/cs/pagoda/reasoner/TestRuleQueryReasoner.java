package uk.ac.ox.cs.pagoda.reasoner;

import org.testng.annotations.Test;
import uk.ac.ox.cs.pagoda.query.QueryManager;
import uk.ac.ox.cs.pagoda.query.QueryRecord;
import uk.ac.ox.cs.pagoda.util.PagodaProperties;

import java.io.File;
import java.nio.file.Path;

public class TestRuleQueryReasoner {

    @Test
    public static void test() {
        PagodaProperties pagodaProperties = new PagodaProperties();
        pagodaProperties.setDataPath("TODO_SET_DATA_PATH");
        Path ruleOntologyPath = new File("TODO_SET_ONTOLOGY_PATH").toPath();
        RuleQueryReasoner ruleQueryReasoner = new RuleQueryReasoner(pagodaProperties, ruleOntologyPath);
        ruleQueryReasoner.evaluate(new QueryRecord(new QueryManager(), "TODO_WRITE_A_QUERY", 0, 0));
    }
}
