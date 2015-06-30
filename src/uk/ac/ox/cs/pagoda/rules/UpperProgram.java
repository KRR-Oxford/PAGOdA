package uk.ac.ox.cs.pagoda.rules;

import org.apache.commons.io.FilenameUtils;

public abstract class UpperProgram extends ApproxProgram {

	@Override
	public String getOutputPath() {
		return FilenameUtils.concat(getDirectory(), "upper.dlog");
	}

}
