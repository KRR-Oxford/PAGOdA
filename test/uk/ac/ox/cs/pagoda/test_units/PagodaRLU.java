package uk.ac.ox.cs.pagoda.test_units;

import org.junit.Test;
import uk.ac.ox.cs.pagoda.tester.PagodaTester;
import uk.ac.ox.cs.pagoda.util.TestUtil;

public class PagodaRLU {

	@Test 
	public void testRL() {
		int number = 1;
		String ontoDir = TestUtil.getConfig().getProperty("ontoDir");
		PagodaTester.main(
				TestUtil.combinePaths(ontoDir, "uobm/univ-bench-dl.owl"),
				TestUtil.combinePaths(ontoDir, "uobm/data/uobm" + number + ".ttl"),
				TestUtil.combinePaths(ontoDir, "uobm/queries/standard.sparql")
		);
	}

}
