package uk.ac.ox.cs.pagoda.global_tests;

import org.testng.annotations.Test;
import uk.ac.ox.cs.pagoda.Pagoda;
import uk.ac.ox.cs.pagoda.util.TestUtil;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

public class TestPagodaLUBM {

	/**
	 * Just execute on LUBM 100
	 */
	public static void main(String... args) {
		new TestPagodaLUBM().justExecute_100();
	}

	public void answersCorrecntess(int number) throws IOException {
		String ontoDir = TestUtil.getConfig().getProperty("ontoDir");
		Path computedAnswers = Paths.get(File.createTempFile("answers", ".json").getAbsolutePath());
		new File(computedAnswers.toString()).deleteOnExit();

		Pagoda pagoda = Pagoda.builder()
							  .ontology(Paths.get(ontoDir, "lubm/univ-bench.owl"))
							  .data(Paths.get(ontoDir, "lubm/data/lubm" + number + ".ttl"))
							  .query(Paths.get(ontoDir, "lubm/queries/test.sparql"))
							  .answer(computedAnswers)
							  .classify(true)
							  .hermit(true)
							  .build();
		pagoda.run();

		Path givenAnswers = Paths.get(ontoDir, "lubm/lubm" + number + ".json");
		CheckAnswers.assertSameAnswers(computedAnswers, givenAnswers);
	}

	@Test(groups = {"light"})
	public void answersCorrectness_1() throws IOException {
		answersCorrecntess(1);
	}

	/**
	 * Just execute on LUBM 100
	 * */
	public void justExecute_100() {
		int number = 100;
		String ontoDir = TestUtil.getConfig().getProperty("ontoDir");
		Pagoda pagoda = Pagoda.builder()
							  .ontology(Paths.get(ontoDir, "lubm/univ-bench.owl"))
							  .data(Paths.get(ontoDir, "lubm/data/lubm" + number + ".ttl"))
							  .query(Paths.get(ontoDir, "lubm/queries/answersCorrectness.sparql"))
							  .classify(true)
							  .hermit(true)
							  .build();
		pagoda.run();
	}
}
