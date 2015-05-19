package uk.ac.ox.cs.pagoda.global_tests;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.testng.Assert;
import uk.ac.ox.cs.pagoda.query.QueryRecord;

import java.io.BufferedReader;
import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Set;

/**
 * It provides auxiliary methods for checking answers.
 * */
public class CheckAnswers {

    private CheckAnswers() {
    }

    public static void assertSameAnswers(Path computedAnswersFile, Path givenAnswersFile) throws IOException {
        BufferedReader computedReader = Files.newBufferedReader(computedAnswersFile);
        BufferedReader givenReader = Files.newBufferedReader(givenAnswersFile);

        Gson gson = QueryRecord.GsonCreator.getInstance();

        Type cqType = new TypeToken<Set<QueryRecord>>() {}.getType();
        Set<QueryRecord> computedAnswers = gson.fromJson(computedReader, cqType);
        Set<QueryRecord> givenAnswers = gson.fromJson(givenReader, cqType);

        Assert.assertEquals(computedAnswers, givenAnswers);
    }
}
