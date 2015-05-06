package uk.ac.ox.cs.pagoda.test_units;

import org.junit.Test;
import uk.ac.ox.cs.pagoda.tester.PagodaTester;
import uk.ac.ox.cs.pagoda.util.Utility;

import java.io.IOException;

public class LightEvaluation {

	@Test
	public void uobm1() throws IOException {
		int number = 1;
		PagodaTester.main(
				PagodaTester.onto_dir + "uobm/univ-bench-dl.owl",
				PagodaTester.onto_dir + "uobm/data/uobm" + number + ".ttl",
				PagodaTester.onto_dir + "uobm/queries/standard.sparql"
		);
		Utility.copyFile("log4j.log", "output/jair/uobm1.out");
	}
	
	@Test
	public void lubm100() throws IOException {
		int number = 100;
		PagodaTester.main(
				PagodaTester.onto_dir + "lubm/univ-bench.owl",
				PagodaTester.onto_dir + "lubm/data/lubm" + number + ".ttl",
				PagodaTester.onto_dir + "lubm/queries/test.sparql"
		);
		Utility.copyFile("log4j.log", "results-backup/current/lubm100.out");
	}
	
	@Test
	public void fly() throws IOException {
		PagodaTester.main(
				PagodaTester.onto_dir + "fly/fly_anatomy_XP_with_GJ_FC_individuals.owl",
				PagodaTester.onto_dir + "fly/queries/fly.sparql" 
		);
		Utility.copyFile("log4j.log", "results-backup/current/fly.out");
	}
	
	@Test 
	public void dbpedia() throws IOException {
		PagodaTester.main(
				PagodaTester.onto_dir + "dbpedia/integratedOntology-all-in-one-minus-datatype.owl", 
				PagodaTester.onto_dir + "dbpedia/data/dbpedia-minus-datatype-new.ttl",
				PagodaTester.onto_dir + "dbpedia/atomic.sparql"
		);
		Utility.copyFile("log4j.log", "results-backup/current/dbpedia.out");
	}
	
	@Test
	public void npdWithoutDataType() throws IOException {
		PagodaTester.main(
				PagodaTester.onto_dir + "npd/npd-all-minus-datatype.owl", 
				PagodaTester.onto_dir + "npd/data/npd-data-dump-minus-datatype-new.ttl", 
				PagodaTester.onto_dir + "npd/queries/atomic.sparql"
		);
		Utility.copyFile("log4j.log", "results-backup/current/npd_minus.out");
	}
	
}
