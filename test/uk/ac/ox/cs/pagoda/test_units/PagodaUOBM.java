package uk.ac.ox.cs.pagoda.test_units;

import org.testng.annotations.Test;
import uk.ac.ox.cs.pagoda.util.TestUtil;

import java.io.IOException;
import java.nio.file.Paths;

public class PagodaUOBM {

	private void testN(int number ) throws IOException {
		String ontoDir = TestUtil.getConfig().getProperty("ontoDir");
		TestGlobalCorrectness.test(Paths.get(ontoDir, "uobm/univ-bench-dl.owl"),
							  Paths.get(ontoDir, "uobm/data/uobm" + number + ".ttl"),
							  Paths.get(ontoDir, "uobm/queries/test.sparql"),
							  Paths.get(ontoDir, "uobm/uobm" + number + ".ans"));
	}

	@Test
	public void test1() throws IOException {
		testN(1);
	}
	
}
