package uk.ac.ox.cs.pagoda.global_tests;

import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import uk.ac.ox.cs.pagoda.Pagoda;
import uk.ac.ox.cs.pagoda.tester.PagodaTester;
import uk.ac.ox.cs.pagoda.util.TestUtil;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

import static uk.ac.ox.cs.pagoda.util.TestUtil.combinePaths;

public class TestPagodaUOBM {


	private static final int N_1 = 1;
	private static final int N_2 = 10;

	@DataProvider(name = "uobmNumbers")
	public static Object[][] uobmNumbers() {
		Integer[][] integers = new Integer[N_2 - N_1 + 1][1];
		for(int i = 0; i < N_2 - N_1 + 1; i++)
			integers[i][0] = N_1 + i;
		return integers;
	}

	public void answersCorrectness(int number) throws IOException {
		String ontoDir = TestUtil.getConfig().getProperty("ontoDir");
		Path computedAnswers = Paths.get(File.createTempFile("answers", ".json").getAbsolutePath());
		new File(computedAnswers.toString()).deleteOnExit();

		Pagoda pagoda = Pagoda.builder()
							  .ontology(Paths.get(ontoDir, "uobm/univ-bench-dl.owl"))
							  .data(Paths.get(ontoDir, "uobm/data/uobm" + number + ".ttl"))
							  .query(Paths.get(ontoDir, "uobm/queries/test.sparql"))
							  .answer(computedAnswers)
							  .classify(true)
							  .hermit(true)
							  .build();
		pagoda.run();

		String given_answers = "uobm/uobm" + number + ".json";
		CheckAnswers.assertSameAnswers(computedAnswers, Paths.get(ontoDir, given_answers));
	}

	@Test(groups = {"light"})
	public void answersCorrectness_1() throws IOException {
		answersCorrectness(1);
	}

	@Test(groups = {"heavy"}, dataProvider = "uobmNumbers")
	public void justExecute(int number) {
		String ontoDir = TestUtil.getConfig().getProperty("ontoDir");
		PagodaTester.main(combinePaths(ontoDir, "uobm/univ-bench-dl.owl"),
						  combinePaths(ontoDir, "uobm/data/uobm" + number + ".ttl"),
						  combinePaths(ontoDir, "uobm/queries/answersCorrectness.sparql"));
	}
	
}