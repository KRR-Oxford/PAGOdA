package uk.ac.ox.cs.pagoda.global_tests;

import org.junit.Test;
import uk.ac.ox.cs.pagoda.tester.PagodaTester;
import uk.ac.ox.cs.pagoda.util.TestUtil;

import java.io.IOException;

public class LightEvaluation {

	@Test
	public void uobm1() throws IOException {
		int number = 1;
		String ontoDir = TestUtil.getConfig().getProperty("ontoDir");
		PagodaTester.main(
				TestUtil.combinePaths(ontoDir, "uobm/univ-bench-dl.owl"),
				TestUtil.combinePaths(ontoDir, "uobm/data/uobm" + number + ".ttl"),
				TestUtil.combinePaths(ontoDir, "uobm/queries/standard.sparql")
		);
		TestUtil.copyFile("log4j.log", "output/jair/uobm1.out");
	}
	
	@Test
	public void lubm100() throws IOException {
		int number = 100;
		String ontoDir = TestUtil.getConfig().getProperty("ontoDir");
		PagodaTester.main(
				TestUtil.combinePaths(ontoDir, "lubm/univ-bench.owl"),
				TestUtil.combinePaths(ontoDir, "lubm/data/lubm" + number + ".ttl"),
				TestUtil.combinePaths(ontoDir, "lubm/queries/test.sparql")
		);
		TestUtil.copyFile("log4j.log", "results-backup/current/lubm100.out");
	}
	
	@Test
	public void fly() throws IOException {
		String ontoDir = TestUtil.getConfig().getProperty("ontoDir");
		PagodaTester.main(
				TestUtil.combinePaths(ontoDir, "fly/fly_anatomy_XP_with_GJ_FC_individuals.owl"),
				TestUtil.combinePaths(ontoDir, "fly/queries/fly.sparql")
		);
		TestUtil.copyFile("log4j.log", "results-backup/current/fly.out");
	}
	
	@Test 
	public void dbpedia() throws IOException {
		String ontoDir = TestUtil.getConfig().getProperty("ontoDir");
		PagodaTester.main(
				TestUtil.combinePaths(ontoDir, "dbpedia/integratedOntology-all-in-one-minus-datatype.owl"),
				TestUtil.combinePaths(ontoDir, "dbpedia/data/dbpedia-minus-datatype-new.ttl"),
				TestUtil.combinePaths(ontoDir, "dbpedia/atomic.sparql")
		);
		TestUtil.copyFile("log4j.log", "results-backup/current/dbpedia.out");
	}
	
	@Test
	public void npdWithoutDataType() throws IOException {
		String ontoDir = TestUtil.getConfig().getProperty("ontoDir");
		PagodaTester.main(
				TestUtil.combinePaths(ontoDir, "npd/npd-all-minus-datatype.owl"),
				TestUtil.combinePaths(ontoDir, "npd/data/npd-data-dump-minus-datatype-new.ttl"),
				TestUtil.combinePaths(ontoDir, "npd/queries/atomic.sparql")
		);
		TestUtil.copyFile("log4j.log", "results-backup/current/npd_minus.out");
	}
	
}
