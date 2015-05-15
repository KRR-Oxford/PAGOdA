package uk.ac.ox.cs.pagoda.global_tests;

import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import uk.ac.ox.cs.pagoda.tester.PagodaTester;
import uk.ac.ox.cs.pagoda.util.TestUtil;

import java.io.IOException;
import java.nio.file.Paths;

import static uk.ac.ox.cs.pagoda.util.TestUtil.combinePaths;

public class PagodaUOBM {

	private void testN(int number ) throws IOException {
		String ontoDir = TestUtil.getConfig().getProperty("ontoDir");
		TestGlobalCorrectness.test(Paths.get(ontoDir, "uobm/univ-bench-dl.owl"),
								   Paths.get(ontoDir, "uobm/data/uobm" + number + ".ttl"),
								   Paths.get(ontoDir, "uobm/queries/test.sparql"),
								   Paths.get(ontoDir, "uobm/uobm" + number + ".json"));
	}

	@Test
	public void test1() throws IOException {
		testN(1);
	}

	private static final int N_1 = 8;
	private static final int N_2 = 10;


	@DataProvider(name = "uobmNumbers")
	public static Object[][] uobmNumbers() {
		Integer[][] integers = new Integer[N_2 - N_1 + 1][1];
		for (int i = 0; i < N_2 - N_1 + 1; i++)
			integers[i][0]= N_1 + i;
		return integers;
	}

//	@Test
//	public void justExecute3() {
//		justExecute(1);
//	}

	@Test(dataProvider = "uobmNumbers")
	public void justExecute(int number) {
		String ontoDir = TestUtil.getConfig().getProperty("ontoDir");
		PagodaTester.main(combinePaths(ontoDir, "uobm/univ-bench-dl.owl"),
						  combinePaths(ontoDir, "uobm/data/uobm" + number + ".ttl"),
						  combinePaths(ontoDir, "uobm/queries/test.sparql"));
	}
	
}
