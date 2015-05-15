package uk.ac.ox.cs.pagoda.global_tests;

import org.junit.Test;
import uk.ac.ox.cs.pagoda.tester.PagodaTester;
import uk.ac.ox.cs.pagoda.tester.Statistics;
import uk.ac.ox.cs.pagoda.util.TestUtil;

import java.io.IOException;

import static org.junit.Assert.fail;

public class PagodaNPD {

	@Test
	public void testNPDwithoutDataType() throws IOException {
		String ontoDir = TestUtil.getConfig().getProperty("ontoDir");
		PagodaTester.main(
				TestUtil.combinePaths(ontoDir, "npd/npd-all-minus-datatype.owl"),
				TestUtil.combinePaths(ontoDir, "npd/data/npd-data-dump-minus-datatype-new.ttl"),
				TestUtil.combinePaths(ontoDir, "npd/queries/atomic.sparql")
		); 
		
		Statistics stat = new Statistics("output/log4j.log");
		String diff = stat.diff("results-backup/benchmark/npd_minus.out");
		TestUtil.copyFile("output/log4j.log", "results-backup/current/npd_minus.out");
		if (!diff.isEmpty())
			fail(diff);
	}
	
	@Test
	public void testNPD() throws IOException {
		String ontoDir = TestUtil.getConfig().getProperty("ontoDir");
		PagodaTester.main(
				TestUtil.combinePaths(ontoDir, "npd/npd-all.owl"),
				TestUtil.combinePaths(ontoDir, "npd/data/npd-data-dump-processed.ttl"),
				TestUtil.combinePaths(ontoDir, "npd/queries/atomic.sparql")
		);
		
		Statistics stat = new Statistics("output/log4j.log");
		String diff = stat.diff("results-backup/benchmark/npd.out");
		TestUtil.copyFile("output/log4j.log", "results-backup/current/npd.out");
		if (!diff.isEmpty())
			fail(diff);
	}
	
}
