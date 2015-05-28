package uk.ac.ox.cs.pagoda.endomorph.plan;

import uk.ac.ox.cs.pagoda.endomorph.Clique;
import uk.ac.ox.cs.pagoda.reasoner.full.Checker;
import uk.ac.ox.cs.pagoda.summary.NodeTuple;
import uk.ac.ox.cs.pagoda.util.Utility;

import java.util.Set;

public class PlainPlan implements CheckPlan {
	
	Checker checker; 
	Set<Clique> toCheck; 

	public PlainPlan(Checker checker, Set<Clique> toCheck) {
		this.checker = checker; 
		this.toCheck = toCheck; 
	}

	@Override
	public int check() {
		int count = 0; 
		for (Clique clique: toCheck)
			if (checker.check(clique.getRepresentative().getAnswerTuple())) {
				count += clique.getNodeTuples().size(); 
				for (NodeTuple nodeTuple: clique.getNodeTuples()) 
					Utility.logDebug(nodeTuple.getAnswerTuple().toString());
			}
		return count; 
	}
	
}
