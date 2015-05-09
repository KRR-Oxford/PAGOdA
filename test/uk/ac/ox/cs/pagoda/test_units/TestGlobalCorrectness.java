package uk.ac.ox.cs.pagoda.test_units;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.apache.log4j.Level;
import org.testng.Assert;
import uk.ac.ox.cs.pagoda.query.QueryRecord;
import uk.ac.ox.cs.pagoda.tester.PagodaTester;
import uk.ac.ox.cs.pagoda.util.Utility;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Set;

/**
 * This is a unit test for TestNG.
 * <p>
 * It tests the correctness on the final output.
 * */
public class TestGlobalCorrectness {

    public static void test(Path ontology, Path data, Path queries, Path givenAnswers) {
        try {
            Utility.setLogLevel(Level.DEBUG);
            Path computedAnswers = Paths.get(File.createTempFile("answers", ".json").getAbsolutePath());
            PagodaTester.main(ontology.toString(), data.toString(), queries.toString(), computedAnswers.toString());
            assertSameContent(computedAnswers, givenAnswers);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void assertSameContent(Path computedAnswersFile, Path givenAnswersFile) throws IOException {
        BufferedReader computedReader = Files.newBufferedReader(computedAnswersFile);
        BufferedReader givenReader = Files.newBufferedReader(givenAnswersFile);

        Gson gson = QueryRecord.GsonCreator.getInstance();

        Type cqType = new TypeToken<Set<QueryRecord>>() {}.getType();
        Set<QueryRecord> computedAnswers = gson.fromJson(computedReader, cqType);
        Set<QueryRecord> givenAnswers = gson.fromJson(givenReader, cqType);

        Assert.assertEquals(computedAnswers, givenAnswers);
    }


}
