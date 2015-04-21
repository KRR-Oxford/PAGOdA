package uk.ac.ox.cs.pagoda.multistage;

import java.util.Collection;
import java.util.LinkedList;

import org.semanticweb.HermiT.model.DLClause;
import uk.ac.ox.cs.pagoda.constraints.BottomStrategy;
import uk.ac.ox.cs.pagoda.rules.OverApproxDisj;
import uk.ac.ox.cs.pagoda.rules.Program;
import uk.ac.ox.cs.pagoda.util.Timer;

public class FoldedApplication extends MultiStageUpperProgram {

	public FoldedApplication(Program program, BottomStrategy upperBottom) {
		super(program, upperBottom); 
	}

	@Override
	public Collection<Violation> isIntegrated(MultiStageQueryEngine engine, boolean incrementally) {
		if (incrementally) addDerivedPredicate(engine); 
		
		Collection<Violation> ret = new LinkedList<Violation>(); 
		Violation v; 
		
		Timer t = new Timer(); 
		for (DLClause clause: constraints) {
			t.reset(); 
//			if (clause.getHeadLength() > 1)
//				for (DLClause overClause: approxDisj.convert(clause, clause)) {
// 					if ((v = violate(engine, overClause, incrementally)) != null) {
//						v.setClause(getOriginalClause(clause)); 
//						ret.add(v);
//					}
//				}
//			else 
				if ((v = violate(engine, clause, incrementally)) != null) {
					v.setClause(getOriginalClause(clause));
					ret.add(v); 
				}
		}
		
		updatedPredicates.clear();
		
		if (ret.isEmpty()) return null; 
		return ret; 
	}

	@Override
	protected Collection<DLClause> getInitialClauses(Program program) {
		OverApproxDisj approxDisj = new OverApproxDisj(); 
		Collection<DLClause> initialClauses = new LinkedList<DLClause>(); 
		for (DLClause originalClause: program.getClauses())
			for (DLClause c: approxDisj.convert(originalClause, originalClause)) {
				initialClauses.add(c);
				map.put(c, originalClause);
			}
		return initialClauses; 
	}

}
