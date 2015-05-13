package uk.ac.ox.cs.pagoda.reasoner.light;

import uk.ac.ox.cs.pagoda.multistage.Normalisation;
import uk.ac.ox.cs.pagoda.rules.approximators.OverApproxExist;

import java.util.Comparator;

public class DLPredicateComparator implements Comparator<String> {

	@Override
	public int compare(String arg0, String arg1) {
		int ret = type(arg0) - type(arg1);
		if (ret != 0) return ret;
		
		return arg0.compareTo(arg1); 
	}

	private int type(String p) {
		if (p.contains(OverApproxExist.negativeSuffix)) return 1; 
		if (p.contains(Normalisation.auxiliaryConceptPrefix)) return 2; 
		else return 0; 
	}

}
