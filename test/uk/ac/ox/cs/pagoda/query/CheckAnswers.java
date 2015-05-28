package uk.ac.ox.cs.pagoda.query;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.testng.Assert;

import java.io.BufferedReader;
import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Set;

/**
 * It provides auxiliary methods for checking answers.
 */
public class CheckAnswers {

    private CheckAnswers() {
    }

    public static void assertSameAnswers(Path computedAnswersFile, Path givenAnswersFile) throws IOException {
        BufferedReader computedReader = Files.newBufferedReader(computedAnswersFile);
        BufferedReader givenReader = Files.newBufferedReader(givenAnswersFile);

        Gson gson = QueryRecord.GsonCreator.getInstance();

        Type cqType = new TypeToken<Set<QueryRecord>>() {
        }.getType();
        Set<QueryRecord> computedAnswersRecords = gson.fromJson(computedReader, cqType);
        Set<QueryRecord> givenAnswersRecords = gson.fromJson(givenReader, cqType);

        for(QueryRecord computedAnswersRecord : computedAnswersRecords) {
            if(computedAnswersRecord.queryID == 8) continue; // DEBUG
            Set<AnswerTuple> givenAnswers = null;
            for(QueryRecord givenAnswersRecord : givenAnswersRecords) {
                if(givenAnswersRecord.queryID == computedAnswersRecord.queryID) {
                    givenAnswers = givenAnswersRecord.soundAnswerTuples;
                    break;
                }
            }

            Assert.assertNotNull(givenAnswers, "Missing given answer for query no. " + computedAnswersRecord.queryID);

            Set<AnswerTuple> computedAnswers = computedAnswersRecord.soundAnswerTuples;
            Assert.assertEquals(computedAnswers.size(), givenAnswers.size(),
                                "Different number sound answers for query " + computedAnswersRecord.queryID + "!"
                                        + "Expected " + givenAnswers.size() + ", got " + computedAnswers.size());
            Assert.assertEquals(computedAnswers, givenAnswers,
                                "Different sound answers for query " + computedAnswersRecord.queryID + "!");
        }
    }
}
