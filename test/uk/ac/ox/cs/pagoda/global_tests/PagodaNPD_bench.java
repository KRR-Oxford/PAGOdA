package uk.ac.ox.cs.pagoda.global_tests;

import org.junit.Test;
import uk.ac.ox.cs.pagoda.tester.PagodaTester;
import uk.ac.ox.cs.pagoda.tester.Statistics;
import uk.ac.ox.cs.pagoda.util.TestUtil;

import java.io.IOException;

import static org.junit.Assert.fail;

public class PagodaNPD_bench {
	
	@Test
	public void test() throws IOException {
		String ontoDir = TestUtil.getConfig().getProperty("ontoDir");
		PagodaTester.main(
				TestUtil.combinePaths(ontoDir, "npd-benchmark/npd-v2-ql_a.owl"),
				TestUtil.combinePaths(ontoDir, "npd-benchmark/npd-v2-ql_a.ttl"),
				TestUtil.combinePaths(ontoDir, "npd-benchmark/queries/all.sparql")
		); 
		
		Statistics stat = new Statistics("output/log4j.log");
		String diff = stat.diff("results-backup/benchmark/npd-bench.out");
		TestUtil.copyFile("output/log4j.log", "results-backup/current/npd-bench.out");
		if (!diff.isEmpty())
			fail(diff);
	}
	
}
