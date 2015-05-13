package uk.ac.ox.cs.pagoda.rules;

import org.semanticweb.HermiT.model.DLClause;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLSubPropertyChainOfAxiom;
import org.semanticweb.owlapi.model.OWLTransitiveObjectPropertyAxiom;
import uk.ac.ox.cs.pagoda.owl.OWLHelper;
import uk.ac.ox.cs.pagoda.rules.approximators.Approximator;

import java.util.*;

public abstract class ApproxProgram extends Program {

	protected Approximator m_approx = null;
	/**
	 * mapping from over-approximated DLClauses to DLClauses from the original ontology
	 */
	Map<DLClause, Object> correspondence = new HashMap<DLClause, Object>();
	
	protected ApproxProgram() { initApproximator(); }
	
	protected abstract void initApproximator(); 

	@Override
	public void transform() {
		super.transform();
		Iterator<DLClause> iterClause = transitiveClauses.iterator();
		for (Iterator<OWLTransitiveObjectPropertyAxiom> iterAxiom = transitiveAxioms.iterator(); iterAxiom.hasNext(); ) 
			addCorrespondence(iterClause.next(), iterAxiom.next()); 

		iterClause = subPropChainClauses.iterator(); 
		for (Iterator<OWLSubPropertyChainOfAxiom> iterAxiom = subPropChainAxioms.iterator(); iterAxiom.hasNext(); )
			addCorrespondence(iterClause.next(), iterAxiom.next()); 
	}

	@Override
	public Collection<DLClause> convert2Clauses(DLClause clause) {
		Collection<DLClause> ret = botStrategy.process(m_approx.convert(clause, clause));
//		OWLAxiom correspondingAxiom = OWLHelper.getOWLAxiom(ontology, clause); 
		for (DLClause newClause: ret) {
			addCorrespondence(newClause, clause);
//			addCorrespondence(newClause, correspondingAxiom);
		}
		return ret; 
	}
	
	private void addCorrespondence(DLClause newClause, Object corresponding) {
		Object object; 
		if ((object = correspondence.get(newClause)) != null) {
			if (object.equals(corresponding))
				return ; 
			
			if (object instanceof DLClause) {
				DLClause c1 = (DLClause) object;
				if (c1.getHeadLength() == 1) return ; 
				DLClause c2 = (DLClause) corresponding; 
				if (c2.getHeadLength() == 1) {
					correspondence.put(newClause, c2); 
					return ; 
				}
				ClauseSet list = new ClauseSet(c1, c2);
				correspondence.put(newClause, list); 
			}
			else if (object instanceof ClauseSet){
				ClauseSet list = (ClauseSet) object; 
				list.add((DLClause) corresponding); 
			}
		}
		correspondence.put(newClause, corresponding);
	}

	public OWLAxiom getEquivalentAxiom(DLClause clause) {
		Object obj  = correspondence.get(clause);
		while (obj != null && obj instanceof DLClause && !obj.equals(clause) && correspondence.containsKey(obj))
			obj = correspondence.get(clause);
		if (obj instanceof OWLAxiom) 
			return (OWLAxiom) obj; 
		else if (obj != null)
			return OWLHelper.getOWLAxiom(ontology, (DLClause) obj);
		else {
			return OWLHelper.getOWLAxiom(ontology, clause);
		}
	}

	public DLClause getCorrespondingClause(DLClause clause) {
		Object obj = correspondence.get(clause);
		if (obj instanceof DLClause)
			return (DLClause) obj; 
		else 
			return clause; 
	}
}

class ClauseSet extends HashSet<DLClause> {

	/**
	 *
	 */
	private static final long serialVersionUID = 1L;

	public ClauseSet(DLClause first, DLClause second) {
		add(first);
		add(second);
	}

}