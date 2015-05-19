package uk.ac.ox.cs.pagoda.global_tests;

import org.testng.annotations.Test;

import java.io.IOException;

@Deprecated
public class LightEvaluation {

	@Test
	public void evaluation() throws IOException {
		new TestPagodaUOBM().answersCorrectness(1);
		new TestPagodaLUBM().answersCorrecntess(100);
		new TestPagodaFLY().test();
		new TestPagodaDBPedia().test();
		new TestPagodaNPD().testNPDwithoutDataType();
	}
}
