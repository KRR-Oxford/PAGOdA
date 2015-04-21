package uk.ac.ox.cs.pagoda.junit;

import static org.junit.Assert.fail;

import org.junit.Test;

import uk.ac.ox.cs.pagoda.tester.PagodaTester;
import uk.ac.ox.cs.pagoda.tester.Statistics;

public class PagodaNPD_bench {
	
	@Test
	public void test() {
		PagodaTester.main(
				PagodaTester.onto_dir + "npd-benchmark/npd-v2-ql_a.owl", 
				PagodaTester.onto_dir + "npd-benchmark/npd-v2-ql_a.ttl", 
				PagodaTester.onto_dir + "npd-benchmark/queries/all.sparql"
		); 
		
		Statistics stat = new Statistics("output/log4j.log");
		String diff = stat.diff("results-backup/benchmark/npd-bench.out"); 
		AllTests.copy("output/log4j.log", "results-backup/current/npd-bench.out"); 
		if (!diff.isEmpty())
			fail(diff);
	}
	
}
