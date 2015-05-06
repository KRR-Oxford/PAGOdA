package uk.ac.ox.cs.pagoda.test_units;

import org.testng.annotations.Test;
import uk.ac.ox.cs.pagoda.tester.PagodaTester;
import uk.ac.ox.cs.pagoda.tester.Statistics;
import uk.ac.ox.cs.pagoda.util.Utility;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

public class PagodaUOBM {

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

	private void test_all(int number ) {
		init();

		PagodaTester.main(
				Utility.combinePaths(ontoDir, "uobm/univ-bench-dl.owl"),
				Utility.combinePaths(ontoDir, "uobm/data/uobm" + number + ".ttl"),
				Utility.combinePaths(ontoDir, "uobm/queries/test.sparql")
//				+ ";" +
// 				Utility.combinePaths(ontoDir, "uobm/queries/standard_group3_all_less.sparql") + ";" +
//				Utility.combinePaths(ontoDir, "uobm/queries/G3.sparql") + ";" +
//				Utility.combinePaths(ontoDir, "uobm/queries/last.sparql")
		);

//		AllTests.copy("log4j.log", "output/jair/newuobm/uobm" + number + ".out");
	}
	
	private void test_upToSum(int number) {
		init();

		PagodaTester.main(
				PagodaTester.onto_dir + "uobm/univ-bench-dl.owl",
				PagodaTester.onto_dir + "uobm/data/uobm" + number + ".ttl",
				PagodaTester.onto_dir + "uobm/queries/standard_group3_all.sparql" 
		); 
		
//		AllTests.copy("log4j.log", "output/jair/uobm" + number + ".out"); 
	}

	@Test
	public void test1() { test_all(1); }

//	@Test
	public void test500() { test_upToSum(500); }
	
//	public static void main(String... args) {
//		new PagodaUOBM().test_all(1);
//	}
	
	private void check() {
		Statistics stat = new Statistics("results-backup/current/uobm1.out"); 
		String diff = stat.diff("results-backup/benchmark/uobm1.out"); 
		System.out.println(diff); 
	}
	
}
