package uk.ac.ox.cs.pagoda.rules;

public abstract class UpperProgram extends ApproxProgram {

	@Override
	public String getOutputPath() {
		return getDirectory() + "upper.dlog";
	}

}
