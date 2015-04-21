package uk.ac.ox.cs.pagoda.rules;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.semanticweb.HermiT.model.DLClause;
import org.semanticweb.HermiT.model.DLPredicate;


public class UpperDatalogProgram extends UpperProgram {
	
	public UpperDatalogProgram() {}

//	@Override
//	public String getDirectory() {
//		File dir = new File(ontologyDirectory + Utility.FILE_SEPARATOR + "datalog");
//		if (!dir.exists())
//			dir.mkdirs();
//		return dir.getPath();
//	}

	@Override
	protected void initApproximator() {
		m_approx = new OverApproxBoth(); 		
	}

	public int getBottomNumber() {
		return botStrategy.getBottomNumber();
	}

	public void updateDependencyGraph(Collection<DLClause> delta) {
		Map<DLPredicate, DLClause> map = new HashMap<DLPredicate, DLClause>(); 
		for (DLClause clause: clauses) 
			if (botStrategy.isBottomRule(clause))
				map.put(clause.getHeadAtom(0).getDLPredicate(), getCorrespondingClause(clause)); 
		
		for (DLClause clause: delta) {
			clauses.add(clause); 
			correspondence.put(clause, map.get(clause.getBodyAtom(0).getDLPredicate())); 
		}
			
		dependencyGraph.update(delta); 
	}

}
