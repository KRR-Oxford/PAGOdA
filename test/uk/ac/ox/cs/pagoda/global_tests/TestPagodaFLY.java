package uk.ac.ox.cs.pagoda.global_tests;

import org.testng.annotations.Test;
import uk.ac.ox.cs.pagoda.Pagoda;
import uk.ac.ox.cs.pagoda.query.CheckAnswers;
import uk.ac.ox.cs.pagoda.util.TestUtil;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

public class TestPagodaFLY {

	@Test(groups = {"light"})
	public void answersCorrectness_withGJFC() throws IOException {
		String ontoDir = TestUtil.getConfig().getProperty("ontoDir");
		Path answers = Paths.get(File.createTempFile("answers", ".json").getAbsolutePath());
		new File(answers.toString()).deleteOnExit();
		Path givenAnswers = TestUtil.getAnswersFilePath("answers/pagoda-fly-with-GJ-FC-individuals.json");

		Pagoda pagoda = Pagoda.builder()
							  .ontology(TestUtil.combinePaths(ontoDir, "fly/fly_anatomy_XP_with_GJ_FC_individuals.owl"))
				              .query(TestUtil.combinePaths(ontoDir, "fly/queries/fly.sparql"))
							  .answer(answers)
							  .classify(false)
							  .hermit(true)
							  .build();

		pagoda.run();
		CheckAnswers.assertSameAnswers(answers, givenAnswers);
	}

	@Test(groups = {"light"})
	public void answersCorrectness_rolledUp() throws IOException {
		String ontoDir = TestUtil.getConfig().getProperty("ontoDir");
		Path answers = Paths.get(File.createTempFile("answers", ".json").getAbsolutePath());
		new File(answers.toString()).deleteOnExit();
		Path givenAnswers = TestUtil.getAnswersFilePath("answers/pagoda-fly-rolledup.json");

		Pagoda pagoda = Pagoda.builder()
							  .ontology(TestUtil.combinePaths(ontoDir, "fly/fly_rolledUp.owl"))
							  .query(TestUtil.combinePaths(ontoDir, "fly/queries/fly_rolledUp.sparql"))
							  .answer(answers)
							  .classify(false)
							  .hermit(true)
							  .build();

		pagoda.run();
		CheckAnswers.assertSameAnswers(answers, givenAnswers);
	}
}
