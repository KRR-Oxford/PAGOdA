package uk.ac.ox.cs.pagoda.multistage;

import java.util.Collection;

import org.semanticweb.HermiT.model.AtLeastConcept;
import org.semanticweb.HermiT.model.Atom;
import org.semanticweb.HermiT.model.DLClause;

import uk.ac.ox.cs.pagoda.hermit.DLClauseHelper;
import uk.ac.ox.cs.pagoda.query.GapByStore4ID;
import uk.ac.ox.cs.pagoda.rules.DatalogProgram;
import uk.ac.ox.cs.pagoda.rules.Program;

public class FoldedApplication2 extends TwoStageApplication {

	public FoldedApplication2(TwoStageQueryEngine engine, DatalogProgram program, GapByStore4ID gap) {
		super(engine, program, gap);
	}
	
	@Override
	protected void addAuxiliaryRules() {
		Collection<DLClause> overClauses; 
		DLClause disjunct;
		Atom[] bodyAtoms; 
		int i; 
		for (DLClause constraint: constraints) 
			for (Atom headAtom: constraint.getHeadAtoms()) 
				if (headAtom.getDLPredicate() instanceof AtLeastConcept) {
					disjunct = DLClause.create(new Atom[] {headAtom}, constraint.getBodyAtoms());
					overClauses = overExist.convert(disjunct, getOriginalClause(constraint));
					bodyAtoms = new Atom[constraint.getBodyLength() + 1]; 
					bodyAtoms[0] = getNAFAtom(headAtom);
					i = 0; 
					for (Atom bodyAtom: constraint.getBodyAtoms())
						bodyAtoms[++i] = bodyAtom; 
					for (DLClause overClause: overClauses) 
						if (DLClauseHelper.hasSubsetBodyAtoms(disjunct, constraint)) 
							addDatalogRule(DLClause.create(new Atom[] {overClause.getHeadAtom(0)}, bodyAtoms));
				}
				else 
					addDatalogRule(DLClause.create(new Atom[] {headAtom}, constraint.getBodyAtoms())); 
	}

	@Override
	protected Collection<DLClause> getInitialClauses(Program program) {
		return program.getClauses();
	}
	
	

}
