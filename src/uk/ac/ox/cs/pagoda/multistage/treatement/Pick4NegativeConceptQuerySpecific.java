package uk.ac.ox.cs.pagoda.multistage.treatement;

import org.semanticweb.HermiT.model.*;
import uk.ac.ox.cs.JRDFox.JRDFStoreException;
import uk.ac.ox.cs.pagoda.constraints.PredicateDependency;
import uk.ac.ox.cs.pagoda.hermit.DLClauseHelper;
import uk.ac.ox.cs.pagoda.multistage.MultiStageQueryEngine;
import uk.ac.ox.cs.pagoda.multistage.Normalisation;
import uk.ac.ox.cs.pagoda.multistage.RestrictedApplication;
import uk.ac.ox.cs.pagoda.multistage.Violation;
import uk.ac.ox.cs.pagoda.query.QueryRecord;
import uk.ac.ox.cs.pagoda.rules.approximators.OverApproxExist;
import uk.ac.ox.cs.pagoda.util.Namespace;

import java.util.Comparator;
import java.util.Set;

public class Pick4NegativeConceptQuerySpecific extends Pick4NegativeConcept {

	QueryRecord record;
	Comparator<Atom> comp; 
	Normalisation norm; 
	
	public Pick4NegativeConceptQuerySpecific(MultiStageQueryEngine multiStageQueryEngine, RestrictedApplication program, QueryRecord record) {
		super(multiStageQueryEngine, program);
		this.record = record;
		norm = program.getNormalisation(); 
		dependencyGraph = new PredicateDependency(record.getRelevantClauses()); 
		comp = new DisjunctComparator(new SimpleComparator());
	}

	@Override
	public boolean makeSatisfied(Violation violation) throws JRDFStoreException {
		return makeSatisfied(violation, comp);
	}

	class DisjunctComparator implements Comparator<Atom> {

//		Map<DLPredicate, Integer> dist = new HashMap<DLPredicate, Integer>();
		Comparator<Atom> m_reference;  
		Set<DLPredicate> dsts; 
		
		public DisjunctComparator(Comparator<Atom> referenceComp) {
			m_reference = referenceComp; 
			dsts = dependencyGraph.collectPredicate(DLClauseHelper.getQuery(record.getQueryText(), null).getBodyAtoms()); 
		}
		
		@Override
		public int compare(Atom arg0, Atom arg1) {
			int dist0 = getDistance(arg0.getDLPredicate()); 
			int dist1 = getDistance(arg1.getDLPredicate()); 
			int ret = dist1 - dist0;
			if (ret != 0) return ret; 
			
			return - m_reference.compare(arg0, arg1); 
		}

		private int getDistance(DLPredicate p) {
			if (p instanceof Equality || p instanceof AnnotatedEquality) 
				return -1;
			if (p instanceof Inequality)
				p = AtomicRole.create(Namespace.INEQUALITY); 

			AtLeastConcept alc; 
			if (p instanceof AtomicConcept)
				if ((alc = norm.getRightAtLeastConcept((AtomicConcept) p)) != null) {
					AtomicRole r = alc.getOnRole() instanceof AtomicRole ? 
							(AtomicRole) alc.getOnRole() : 
								((InverseRole) alc.getOnRole()).getInverseOf(); 
					AtomicConcept c = alc.getToConcept() instanceof AtomicConcept ? 
							(AtomicConcept) alc.getToConcept() : 
								OverApproxExist.getNegationConcept(((AtomicNegationConcept) alc.getToConcept()).getNegatedAtomicConcept()); 
					
					if (c.equals(AtomicConcept.THING))
						return dependencyGraph.distance(dsts, r); 
					else 
						return dependencyGraph.distance(dsts, r, c); 
				} else if ((alc = norm.getLeftAtLeastConcept((AtomicConcept) p)) != null) {
					return 2; 
				}
			
			return dependencyGraph.distance(dsts, p);
		}
		
	}

}
