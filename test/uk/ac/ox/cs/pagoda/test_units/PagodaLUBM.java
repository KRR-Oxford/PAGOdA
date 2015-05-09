package uk.ac.ox.cs.pagoda.test_units;

import org.testng.annotations.Test;
import uk.ac.ox.cs.pagoda.util.TestUtil;

import java.io.IOException;
import java.nio.file.Paths;

public class PagodaLUBM {

	private void testN(int number ) throws IOException {
		String ontoDir = TestUtil.getConfig().getProperty("ontoDir");
		TestGlobalCorrectness.test(Paths.get(ontoDir, "lubm/univ-bench.owl"),
								   Paths.get(ontoDir, "lubm/data/lubm" + number + ".ttl"),
								   Paths.get(ontoDir, "lubm/queries/test.sparql"),
								   Paths.get(ontoDir, "lubm/lubm" + number + ".json"));
	}

	@Test
	public void test1() throws IOException {
		testN(1);
	}
	
}
