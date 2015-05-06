package uk.ac.ox.cs.pagoda.test_units;

import static org.junit.Assert.fail;

import org.junit.Test;

import uk.ac.ox.cs.pagoda.tester.PagodaTester;
import uk.ac.ox.cs.pagoda.tester.Statistics;
import uk.ac.ox.cs.pagoda.util.Utility;

import java.io.IOException;

public class PagodaNPD_bench {
	
	@Test
	public void test() throws IOException {
		PagodaTester.main(
				PagodaTester.onto_dir + "npd-benchmark/npd-v2-ql_a.owl", 
				PagodaTester.onto_dir + "npd-benchmark/npd-v2-ql_a.ttl", 
				PagodaTester.onto_dir + "npd-benchmark/queries/all.sparql"
		); 
		
		Statistics stat = new Statistics("output/log4j.log");
		String diff = stat.diff("results-backup/benchmark/npd-bench.out");
		Utility.copyFile("output/log4j.log", "results-backup/current/npd-bench.out");
		if (!diff.isEmpty())
			fail(diff);
	}
	
}
