package uk.ac.ox.cs.pagoda.global_tests;

import org.testng.annotations.Test;
import uk.ac.ox.cs.pagoda.Pagoda;
import uk.ac.ox.cs.pagoda.util.TestUtil;

public class TestPagodaFLY {

	public static final String ANSWER_PATH = "~/TestPagodaFLY.json";

	@Test(groups = {"light"})
	public void just_execute() {
		String ontoDir = TestUtil.getConfig().getProperty("ontoDir");
		Pagoda pagoda = Pagoda.builder()
							  .ontology(TestUtil.combinePaths(ontoDir, "fly/fly_anatomy_XP_with_GJ_FC_individuals.owl"))
				              .query(TestUtil.combinePaths(ontoDir, "fly/queries/fly.sparql"))
//							  .answer(ANSWER_PATH)
				              .classify(true)
							  .hermit(true)
				              .build();
		pagoda.run();
	}

	@Test
	public void answersCorrectness() {
		// TODO implement
	}
}
