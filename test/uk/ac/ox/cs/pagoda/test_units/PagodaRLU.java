package uk.ac.ox.cs.pagoda.test_units;

import org.junit.Test;
import uk.ac.ox.cs.pagoda.tester.PagodaTester;

public class PagodaRLU {

	@Test 
	public void testRL() {
		int number = 1; 
		PagodaTester.main(
				PagodaTester.onto_dir + "uobm/univ-bench-dl.owl",
				PagodaTester.onto_dir + "uobm/data/uobm" + number + ".ttl",
				PagodaTester.onto_dir + "uobm/queries/standard.sparql" 
		); 
	}

}
