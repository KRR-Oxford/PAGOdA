package uk.ac.ox.cs.pagoda.global_tests;

import org.testng.annotations.Test;

import java.io.IOException;

@Deprecated
public class LightEvaluation {

	@Test
	public void evaluation() throws IOException {
		new TestPagodaUOBM().answersCorrectness(1);
		new TestPagodaLUBM().answersCorrectness(100);
//		new TestPagodaFLY().answersCorrectness();
		new TestPagodaDBPedia().answersCorrectness();
		new TestPagodaNPD().testNPDwithoutDataType();
	}
}
