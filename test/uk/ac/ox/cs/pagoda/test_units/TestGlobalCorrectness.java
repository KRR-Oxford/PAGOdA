package uk.ac.ox.cs.pagoda.test_units;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import org.apache.log4j.Logger;
import org.testng.Assert;
import uk.ac.ox.cs.pagoda.query.AnswerTuple;
import uk.ac.ox.cs.pagoda.query.QueryRecord;
import uk.ac.ox.cs.pagoda.tester.PagodaTester;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.Set;

/*
 * This is a unit test for TestNG.
 * <p>
 * It tests the correctness on the final output.
 * */
public class TestGlobalCorrectness {

    public static final Logger logger = Logger.getLogger(TestGlobalCorrectness.class);

    public static void test(Path ontology, Path data, Path queries, Path givenAnswers) {
        try {
            Path computedAnswers = Paths.get(File.createTempFile("answers", ".tmp").getAbsolutePath());
            PagodaTester.main(ontology.toString(), data.toString(), queries.toString(), computedAnswers.toString());
            Assert.assertTrue(checkSameContent(computedAnswers, givenAnswers));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static boolean checkSameContent(Path computedAnswersFile, Path givenAnswersFile) throws IOException {
        BufferedReader computedReader = Files.newBufferedReader(computedAnswersFile);
        BufferedReader givenReader = Files.newBufferedReader(givenAnswersFile);

        Gson gson = new GsonBuilder().create();

        Type cqType = new TypeToken<Set<AnswerTuple>>() {}.getType();
        Collection<QueryRecord> computedAnswers = gson.fromJson(computedReader, cqType);
        Collection<QueryRecord> givenAnswers = gson.fromJson(givenReader, cqType);

        return computedAnswers.equals(givenAnswers);
    }


}
