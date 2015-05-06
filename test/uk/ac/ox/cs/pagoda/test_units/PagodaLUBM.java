package uk.ac.ox.cs.pagoda.test_units;

import org.testng.Assert;
import org.testng.annotations.Test;
import uk.ac.ox.cs.pagoda.tester.PagodaTester;
import uk.ac.ox.cs.pagoda.tester.Statistics;
import uk.ac.ox.cs.pagoda.util.Utility;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

public class PagodaLUBM {

	public static final String CONFIG_FILE = "config/test.properties";

	private static boolean isInit = false;
	private static String ontoDir;

	private static void init() {
		if(isInit) return;
		isInit = true;

		Properties config = new Properties();

		try(FileInputStream in = new FileInputStream(CONFIG_FILE)) {
			config.load(in);
			in.close();
		} catch (IOException e) {
			e.printStackTrace();
		}

		ontoDir = config.getProperty("ontoDir");
	}

	private void test_all(int number) {
		init();
		PagodaTester.main(
				Utility.combinePaths(ontoDir, "lubm/univ-bench.owl"),
				Utility.combinePaths(ontoDir, "lubm/data/lubm" + number + ".ttl"),
				Utility.combinePaths(ontoDir, "lubm/queries/test.sparql")
		);

//		assertTrue(false);
//		AllTests.copy("log4j.log", "output/jair/lubm" + number + ".out");
	}

	@Test
	public void test1() {
		test_all(1);
	}

//	@Test
//	public void test() {
//		int number = 100;
//		test_all(number);
//	}
	
	private void check(int number) throws IOException {
		Statistics stat = new Statistics("output/log4j.log");
		// TODO insert proper file
		String diff = stat.diff("results-backup/benchmark/lubm" + number + ".out");
		Utility.copyFile("output/log4j.log", "results-backup/current/lubm" + number + ".out");
		if (!diff.isEmpty())
			Assert.fail(diff);
	}
	
}
