package uk.ac.ox.cs.pagoda.global_tests;

import org.testng.annotations.Test;
import uk.ac.ox.cs.pagoda.Pagoda;
import uk.ac.ox.cs.pagoda.query.CheckAnswers;
import uk.ac.ox.cs.pagoda.util.TestUtil;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

public class TestPagodaLUBM {

	public void answersCorrectness(int number) throws IOException {
		String ontoDir = TestUtil.getConfig().getProperty("ontoDir");
		Path answers = Paths.get(File.createTempFile("answers", ".json").getAbsolutePath());
		new File(answers.toString()).deleteOnExit();
		Path givenAnswers = TestUtil.getAnswersFilePath("answers/pagoda-lubm" + number + ".json");

		Pagoda pagoda = Pagoda.builder()
							  .ontology(Paths.get(ontoDir, "lubm/univ-bench.owl"))
							  .data(Paths.get(ontoDir, "lubm/data/lubm" + number + ".ttl"))
							  .query(Paths.get(ontoDir, "lubm/queries/test.sparql"))
							  .answer(answers)
							  .build();

		pagoda.run();
		CheckAnswers.assertSameAnswers(answers, givenAnswers);
	}

	@Test(groups = {"light", "correctness"})
	public void answersCorrectness_1() throws IOException {
		answersCorrectness(1);
	}

	public void justExecute_sygenia(int number) throws IOException {
		String ontoDir = TestUtil.getConfig().getProperty("ontoDir");
//		Path answers = Paths.get(File.createTempFile("answers", ".json").getAbsolutePath());
//		new File(answers.toString()).deleteOnExit();
//		Path givenAnswers = TestUtil.getAnswersFilePath("answers/pagoda-lubm" + number + ".json");

		Pagoda pagoda = Pagoda.builder()
							  .ontology(Paths.get(ontoDir, "lubm/univ-bench.owl"))
							  .data(Paths.get(ontoDir, "lubm/data/lubm" + number + ".ttl"))
				              .query(Paths.get(ontoDir, "lubm/queries/lubm_sygenia.sparql"))
//							  .answer(answers)
				              .build();

		pagoda.run();
//		CheckAnswers.assertSameAnswers(answers, givenAnswers);
	}

	@Test(groups = {"sygenia"})
	public void justExecute_sygenia_1() throws IOException {
		justExecute_sygenia(1);
	}

	public void justExecute_sygenia_allBlanks(int number) throws IOException {
		String ontoDir = TestUtil.getConfig().getProperty("ontoDir");
//		Path answers = Paths.get(File.createTempFile("answers", ".json").getAbsolutePath());
//		new File(answers.toString()).deleteOnExit();
//		Path givenAnswers = TestUtil.getAnswersFilePath("answers/pagoda-lubm" + number + ".json");

		Pagoda pagoda = Pagoda.builder()
							  .ontology(Paths.get(ontoDir, "lubm/univ-bench.owl"))
							  .data(Paths.get(ontoDir, "lubm/data/lubm" + number + ".ttl"))
				.query(Paths.get(ontoDir, "lubm/queries/lubm_sygenia_all-blanks.sparql"))
//							  .answer(answers)
				.build();

		pagoda.run();
//		CheckAnswers.assertSameAnswers(answers, givenAnswers);
	}

	@Test(groups = {"sygenia"})
	public void justExecute_sygenia_1_allBlanks() throws IOException {
		justExecute_sygenia_allBlanks(1);
	}
}
