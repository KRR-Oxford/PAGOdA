package uk.ac.ox.cs.pagoda.test_units;

import org.junit.Test;
import uk.ac.ox.cs.pagoda.tester.PagodaTester;
import uk.ac.ox.cs.pagoda.util.TestUtil;

public class PagodaFLY {

	@Test
	public void test() {
		String ontoDir = TestUtil.getConfig().getProperty("ontoDir");
		PagodaTester.main(
				TestUtil.combinePaths(ontoDir, "fly/fly_anatomy_XP_with_GJ_FC_individuals.owl"),
				TestUtil.combinePaths(ontoDir, "fly/queries/fly_pellet.sparql")
		); 
		
//		Statistics stat = new Statistics("output/log4j.log");
//		String diff = stat.diff("results-backup/benchmark/fly.out"); 
//		AllTests.copy("output/log4j.log", "results-backup/current/fly.out"); 
//		if (!diff.isEmpty())
//			fail(diff);
	}
	

}
