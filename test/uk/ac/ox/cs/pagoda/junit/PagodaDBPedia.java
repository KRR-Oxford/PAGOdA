package uk.ac.ox.cs.pagoda.junit;

import static org.junit.Assert.fail;

import org.junit.Test;

import uk.ac.ox.cs.pagoda.tester.PagodaTester;
import uk.ac.ox.cs.pagoda.tester.Statistics;

public class PagodaDBPedia {

	@Test 
	public void test() {
		PagodaTester.main(
				PagodaTester.onto_dir + "dbpedia/integratedOntology-all-in-one-minus-datatype.owl", 
				PagodaTester.onto_dir + "dbpedia/data/dbpedia-minus-datatype-new.ttl",
				PagodaTester.onto_dir + "dbpedia/atomic.sparql"
		);
		
		Statistics stat = new Statistics("output/log4j.log");
		String diff = stat.diff("results-backup/benchmark/dbpedia.out"); 
		AllTests.copy("output/log4j.log", "results-backup/current/dbpedia.out"); 
		if (!diff.isEmpty())
			fail(diff);
	}

}
