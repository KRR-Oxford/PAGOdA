package uk.ac.ox.cs.pagoda.global_tests;

import org.testng.annotations.Test;

import java.io.IOException;

public class LightEvaluation {

	@Test
	public void evaluation() throws IOException {
		new TestPagodaUOBM().test(1);
		new TestPagodaLUBM().test(100);
		new TestPagodaFLY().test();
		new TestPagodaDBPedia().test();
		new TestPagodaNPD().testNPDwithoutDataType();
	}
}
