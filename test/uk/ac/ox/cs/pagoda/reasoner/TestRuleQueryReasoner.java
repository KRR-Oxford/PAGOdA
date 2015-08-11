package uk.ac.ox.cs.pagoda.reasoner;

import org.testng.annotations.Test;
import uk.ac.ox.cs.pagoda.query.AnswerTuples;
import uk.ac.ox.cs.pagoda.query.QueryManager;
import uk.ac.ox.cs.pagoda.query.QueryRecord;
import uk.ac.ox.cs.pagoda.util.PagodaProperties;

import java.io.File;
import java.nio.file.Path;
import java.util.Collection;

public class TestRuleQueryReasoner {

    private static final String DATA_BASE_PATH = "/home/alessandro/Dropbox/Oxford/Example_ontologies/";

    @Test
    public static void test() {
        PagodaProperties pagodaProperties = new PagodaProperties();
        pagodaProperties.setDataPath(DATA_BASE_PATH + "dataset.ttl");
        Path ruleOntologyPath = new File(DATA_BASE_PATH + "ontology.rl").toPath();
        Collection<QueryRecord> queryRecords = new QueryManager().collectQueryRecords(DATA_BASE_PATH + "queries.sparql");

        RuleQueryReasoner ruleQueryReasoner = new RuleQueryReasoner(pagodaProperties, ruleOntologyPath);
        ruleQueryReasoner.importData(DATA_BASE_PATH + "dataset.ttl");
        if(!ruleQueryReasoner.preprocess()) {
            System.out.println("Preprocessing failed");
            return;
        }

        for (QueryRecord queryRecord : queryRecords) {
            System.out.println("Query: " + queryRecord.getQueryText());

            ruleQueryReasoner.evaluate(queryRecord);

            System.out.println("> Lower bound answers <");
            AnswerTuples lowerBoundAnswers = queryRecord.getLowerBoundAnswers();
            while(lowerBoundAnswers.isValid()) {
                System.out.println(lowerBoundAnswers.getTuple());
                lowerBoundAnswers.moveNext();
            }
            System.out.println();
            System.out.println("> Upper bound answers <");
            AnswerTuples upperBoundAnswers = queryRecord.getUpperBoundAnswers();
            while(upperBoundAnswers.isValid()) {
                System.out.println(upperBoundAnswers.getTuple());
                upperBoundAnswers.moveNext();
            }

            System.out.println((queryRecord.isProcessed()) ? "Completely answered" : "Not completely answered");
            System.out.println();
        }
    }
}
