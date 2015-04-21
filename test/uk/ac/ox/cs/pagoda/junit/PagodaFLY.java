package uk.ac.ox.cs.pagoda.junit;

import org.junit.Test;

import uk.ac.ox.cs.pagoda.tester.PagodaTester;

public class PagodaFLY {

	@Test
	public void test() {
		PagodaTester.main(
				PagodaTester.onto_dir + "fly/fly_anatomy_XP_with_GJ_FC_individuals.owl",
				PagodaTester.onto_dir + "fly/queries/fly_pellet.sparql" 
		); 
		
//		Statistics stat = new Statistics("output/log4j.log");
//		String diff = stat.diff("results-backup/benchmark/fly.out"); 
//		AllTests.copy("output/log4j.log", "results-backup/current/fly.out"); 
//		if (!diff.isEmpty())
//			fail(diff);
	}
	

}
