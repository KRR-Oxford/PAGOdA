package uk.ac.ox.cs.pagoda.global_tests;

import org.testng.annotations.Test;
import uk.ac.ox.cs.pagoda.Pagoda;
import uk.ac.ox.cs.pagoda.util.TestUtil;

import java.io.IOException;
import java.nio.file.Paths;

public class TestPagodaNPD {

	public static final String ANSWER_PATH = "~/PagodaNPDWithoutDatatype.json";

	@Test
	public void justExecuteNPDWithoutDataType() {
		String ontoDir = TestUtil.getConfig().getProperty("ontoDir");
		Pagoda pagoda = Pagoda.builder()
							  .ontology(Paths.get(ontoDir, "npd/npd-all-minus-datatype.owl"))
							  .data(Paths.get(ontoDir, "npd/data/npd-data-dump-minus-datatype-new.ttl"))
							  .query(Paths.get(ontoDir, "npd/queries/atomic.sparql"))
							  .answer(ANSWER_PATH)
							  .classify(true)
							  .hermit(true)
							  .build();
		pagoda.run();
	}

	@Test
	public void testNPDwithoutDataType() throws IOException {
		// TODO implement
	}
	
	@Test
	public void testNPD() throws IOException {
		// TODO implement
	}
	
}
