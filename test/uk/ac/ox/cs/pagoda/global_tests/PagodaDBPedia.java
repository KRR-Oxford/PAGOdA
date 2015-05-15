package uk.ac.ox.cs.pagoda.global_tests;

import org.junit.Test;
import uk.ac.ox.cs.pagoda.tester.PagodaTester;
import uk.ac.ox.cs.pagoda.tester.Statistics;
import uk.ac.ox.cs.pagoda.util.TestUtil;

import java.io.IOException;

import static org.junit.Assert.fail;

public class PagodaDBPedia {

	@Test 
	public void test() throws IOException {
		String ontoDir = TestUtil.getConfig().getProperty("ontoDir");
		PagodaTester.main(
				TestUtil.combinePaths(ontoDir, "dbpedia/integratedOntology-all-in-one-minus-datatype.owl"),
				TestUtil.combinePaths(ontoDir, "dbpedia/data/dbpedia-minus-datatype-new.ttl"),
				TestUtil.combinePaths(ontoDir, "dbpedia/atomic.sparql")
		);
		
		Statistics stat = new Statistics("output/log4j.log");
		String diff = stat.diff("results-backup/benchmark/dbpedia.out");
		TestUtil.copyFile("output/log4j.log", "results-backup/current/dbpedia.out");
		if (!diff.isEmpty())
			fail(diff);
	}

}
