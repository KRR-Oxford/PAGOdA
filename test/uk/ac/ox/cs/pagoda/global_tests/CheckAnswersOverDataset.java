package uk.ac.ox.cs.pagoda.global_tests;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.testng.Assert;
import uk.ac.ox.cs.pagoda.Pagoda;
import uk.ac.ox.cs.pagoda.query.QueryRecord;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Set;

/**
 * Given an instance of Pagoda, it checks the returned answers.
 * */
public class CheckAnswersOverDataset {

    public static void check(Pagoda pagoda, Path givenAnswers) {
        try {
//            Utility.setLogLevel(Level.DEBUG); // uncomment for outputting partial results
            Path computedAnswers = Paths.get(File.createTempFile("answers", ".json").getAbsolutePath());
            new File(computedAnswers.toString()).deleteOnExit();

            pagoda.run();
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
