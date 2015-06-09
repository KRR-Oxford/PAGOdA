package uk.ac.ox.cs.pagoda.global_tests;

import org.testng.annotations.Test;
import uk.ac.ox.cs.pagoda.Pagoda;
import uk.ac.ox.cs.pagoda.query.CheckAnswers;
import uk.ac.ox.cs.pagoda.util.TestUtil;
import uk.ac.ox.cs.pagoda.util.Timer;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

public class TestPagodaFLY {

	//	@Test(groups = {"light"})
	public void answersCorrectness_withGJFC() throws IOException {
		String ontoDir = TestUtil.getConfig().getProperty("ontoDir");
		Path answers = Paths.get(File.createTempFile("answers", ".json").getAbsolutePath());
		new File(answers.toString()).deleteOnExit();
		Path givenAnswers = TestUtil.getAnswersFilePath("answers/pagoda-fly-with-GJ-FC-individuals.json");

		Pagoda pagoda = Pagoda.builder()
							  .ontology(Paths.get(ontoDir, "fly/fly_anatomy_XP_with_GJ_FC_individuals.owl"))
							  .query(Paths.get(ontoDir, "fly/queries/fly.sparql"))
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
							  .ontology(Paths.get(ontoDir, "fly/fly_rolledUp.owl"))
							  .query(Paths.get(ontoDir, "fly/queries/fly_rolledUp.sparql"))
							  .answer(answers)
							  .answer(Paths.get("/home/alessandro/Desktop/answers.json"))
							  .classify(false)
							  .hermit(true)
							  .build();

		pagoda.run();
		CheckAnswers.assertSameAnswers(answers, givenAnswers);
	}

	@Test(groups = {"light", "justExecute"})
	public void justExecute_newQueries() throws IOException {
		String ontoDir = TestUtil.getConfig().getProperty("ontoDir");

		Pagoda pagoda = Pagoda.builder()
				.ontology(Paths.get(ontoDir, "fly/fly_rolledUp.owl"))
//							  .ontology(Paths.get(ontoDir, "fly/fly_anatomy_XP_with_GJ_FC_individuals.owl"))
				.query(Paths.get(ontoDir, "fly/queries/new_queries.sparql"))
//							  .answer(Paths.get("/home/alessandro/Desktop/answers.json"))
				.classify(false)
				.hermit(true)
				.skolem(false)
				.build();

		pagoda.run();
	}

	@Test(groups = {"light", "comparison"})
	public void compare_newQueries() throws IOException {
		String ontoDir = TestUtil.getConfig().getProperty("ontoDir");

		Timer timer = new Timer();
		Pagoda.builder()
			  .ontology(Paths.get(ontoDir, "fly/fly_rolledUp.owl"))
			  .query(Paths.get(ontoDir, "fly/queries/new_queries.sparql"))
			  .classify(false)
			  .hermit(true)
				.skolem(true) // <----<< Skolem upper bound is ENABLED <<<
				.build()
				.run();
		double t1 = timer.duration();

		timer.reset();

		Pagoda.builder()
			  .ontology(Paths.get(ontoDir, "fly/fly_rolledUp.owl"))
			  .query(Paths.get(ontoDir, "fly/queries/new_queries.sparql"))
			  .classify(false)
			  .hermit(true)
				.skolem(false) // <----<< Skolem upper bound is DISABLED <<<
				.build()
				.run();
		double t2 = timer.duration();

		if(t1 < t2)
			TestUtil.logInfo("Overall reasoning with Skolem upper bound was " + (int) (t2 / t1 * 100 - 100) + "x faster!");
		else
			TestUtil.logInfo("Overall reasoning with Skolem upper bound was " + (int) (t1 / t2 * 100 - 100) + "x slower...");
	}
}
