package uk.ac.ox.cs.pagoda.global_tests;

import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import uk.ac.ox.cs.pagoda.Pagoda;
import uk.ac.ox.cs.pagoda.query.CheckAnswers;
import uk.ac.ox.cs.pagoda.util.TestUtil;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

public class TestPagodaUOBM {

	private static final int N_1 = 1;
	private static final int N_2 = 4;

	@DataProvider(name = "UOBMNumbers")
	private static Object[][] UOBMNumbers() {
		Integer[][] integers = new Integer[N_2 - N_1 + 1][1];
		for(int i = 0; i < N_2 - N_1 + 1; i++)
			integers[i][0] = N_1 + i;
		return integers;
	}

	@Test(groups = {"light"})
	public void answersCorrectness_1() throws IOException {
		answersCorrectness(1);
	}

	@Test(groups = {"heavy"}, dataProvider = "UOBMNumbers")
	public void answersCorrectness(int number) throws IOException {
		String ontoDir = TestUtil.getConfig().getProperty("ontoDir");
		Path answers = Paths.get(File.createTempFile("answers", ".json").getAbsolutePath());
		new File(answers.toString()).deleteOnExit();
		Path givenAnswers = TestUtil.getAnswersFilePath("answers/pagoda-uobm" + number + ".json");

		Pagoda pagoda = Pagoda.builder()
							  .ontology(Paths.get(ontoDir, "uobm/univ-bench-dl.owl"))
							  .data(Paths.get(ontoDir, "uobm/data/uobm" + number + ".ttl"))
							  .query(Paths.get(ontoDir, "uobm/queries/test.sparql"))
							  .answer(answers)
							  .classify(true)
							  .hermit(true)
							  .build();

		pagoda.run();
		CheckAnswers.assertSameAnswers(answers, givenAnswers);
	}
	
}
