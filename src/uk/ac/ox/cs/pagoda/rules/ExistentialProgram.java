package uk.ac.ox.cs.pagoda.rules;

import uk.ac.ox.cs.pagoda.rules.approximators.OverApproxDisj;

public class ExistentialProgram extends UpperProgram {

//	@Override
//	public String getDirectory() {
//		File dir = new File(ontologyDirectory + Utility.FILE_SEPARATOR + "existential");
//		if (!dir.exists())
//			dir.mkdirs();
//		return dir.getPath(); 
//	}

	@Override
	protected void initApproximator() {
		m_approx = new OverApproxDisj();
	}

}
