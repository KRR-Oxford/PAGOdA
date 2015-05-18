package uk.ac.ox.cs.pagoda.global_tests;

import org.testng.annotations.Test;
import uk.ac.ox.cs.pagoda.Pagoda;
import uk.ac.ox.cs.pagoda.util.TestUtil;

import java.io.IOException;
import java.nio.file.Paths;

public class TestPagodaLUBM {

	public void test(int number) throws IOException {
		String ontoDir = TestUtil.getConfig().getProperty("ontoDir");
		Pagoda pagoda = Pagoda.builder()
							  .ontology(Paths.get(ontoDir, "lubm/univ-bench.owl"))
							  .data(Paths.get(ontoDir, "lubm/data/lubm" + number + ".ttl"))
							  .query(Paths.get(ontoDir, "lubm/queries/test.sparql"))
							  .classify(true)
							  .hermit(true)
							  .build();
		CheckAnswersOverDataset.check(pagoda, Paths.get(ontoDir, "lubm/lubm" + number + ".json"));
	}

	@Test
	public void test_1() throws IOException {
		test(1);
	}

	public void justExecute_100() {
		int number = 100;
		String ontoDir = TestUtil.getConfig().getProperty("ontoDir");
		Pagoda pagoda = Pagoda.builder()
							  .ontology(Paths.get(ontoDir, "lubm/univ-bench.owl"))
							  .data(Paths.get(ontoDir, "lubm/data/lubm" + number + ".ttl"))
							  .query(Paths.get(ontoDir, "lubm/queries/test.sparql"))
							  .classify(true)
							  .hermit(true)
							  .build();
		pagoda.run();
	}

	public static void main(String... args) {
		new TestPagodaLUBM().justExecute_100();
	}
}
