package uk.ac.ox.cs.pagoda.global_tests;

import org.testng.annotations.Test;
import uk.ac.ox.cs.pagoda.Pagoda;
import uk.ac.ox.cs.pagoda.util.TestUtil;

import java.io.IOException;
import java.nio.file.Paths;

public class TestPagodaDBPedia {

	public static final String ANSWER_PATH = "~/TestPagodaDEBPedia.json";

	@Test
	public void just_execute() throws IOException {
		String ontoDir = TestUtil.getConfig().getProperty("ontoDir");
		Pagoda pagoda = Pagoda.builder()
							  .ontology(Paths.get(ontoDir, "dbpedia/integratedOntology-all-in-one-minus-datatype.owl"))
							  .data(Paths.get(ontoDir, "dbpedia/data/dbpedia-minus-datatype-new.ttl"))
							  .query(Paths.get(ontoDir, "dbpedia/atomic.sparql"))
							  .answer(ANSWER_PATH)
							  .classify(true)
							  .hermit(true)
							  .build();
		pagoda.run();
	}

	@Test
	public void answersCorrectness() {
		// TODO implement
	}

}
