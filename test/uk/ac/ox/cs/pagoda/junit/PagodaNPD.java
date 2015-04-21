package uk.ac.ox.cs.pagoda.junit;

import static org.junit.Assert.fail;

import org.junit.Test;

import uk.ac.ox.cs.pagoda.tester.PagodaTester;
import uk.ac.ox.cs.pagoda.tester.Statistics;

public class PagodaNPD {

	@Test
	public void testNPDwithoutDataType() {
		PagodaTester.main(
				PagodaTester.onto_dir + "npd/npd-all-minus-datatype.owl", 
				PagodaTester.onto_dir + "npd/data/npd-data-dump-minus-datatype-new.ttl", 
				PagodaTester.onto_dir + "npd/queries/atomic.sparql"
		); 
		
		Statistics stat = new Statistics("output/log4j.log");
		String diff = stat.diff("results-backup/benchmark/npd_minus.out"); 
		AllTests.copy("output/log4j.log", "results-backup/current/npd_minus.out"); 
		if (!diff.isEmpty())
			fail(diff);
	}
	
	@Test
	public void testNPD() {
		PagodaTester.main(
				PagodaTester.onto_dir + "npd/npd-all.owl", 
				PagodaTester.onto_dir + "npd/data/npd-data-dump-processed.ttl", 
				PagodaTester.onto_dir + "npd/queries/atomic.sparql"
		);
		
		Statistics stat = new Statistics("output/log4j.log");
		String diff = stat.diff("results-backup/benchmark/npd.out"); 
		AllTests.copy("output/log4j.log", "results-backup/current/npd.out"); 
		if (!diff.isEmpty())
			fail(diff);
	}
	
}
