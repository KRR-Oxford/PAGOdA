package uk.ac.ox.cs.pagoda.rules;

import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;

import org.semanticweb.HermiT.model.AtLeastConcept;
import org.semanticweb.HermiT.model.Atom;
import org.semanticweb.HermiT.model.AtomicRole;
import org.semanticweb.HermiT.model.DLClause;
import org.semanticweb.HermiT.model.DLPredicate;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.model.OWLOntology;
import uk.ac.ox.cs.pagoda.constraints.BottomStrategy;

public class ExistentialToDisjunctive extends UpperProgram {
	
	Set<String> inverseFuncProperties = new HashSet<String>(); 
	
	@Override
	public void load(OWLOntology o, BottomStrategy bottomStrategy) {
		super.load(o, bottomStrategy);
		for (OWLObjectProperty prop: ontology.getObjectPropertiesInSignature(true))
			if (!(ontology.getInverseFunctionalObjectPropertyAxioms(prop).isEmpty())) 
				inverseFuncProperties.add(prop.getIRI().toString());
		((RefinedOverApproxExist) m_approx).setInverseFuncProps(inverseFuncProperties);
	}
	
	@Override
	protected void initApproximator() {
		m_approx = new RefinedOverApproxExist(); 
	}

}

class RefinedOverApproxExist implements Approximator {
	
	Approximator approxExist = new OverApproxExist();
	Set<String> inverseFuncProperties; 
	
	public void setInverseFuncProps(Set<String> set) {
		inverseFuncProperties = set; 
	}
	
	@Override
	public Collection<DLClause> convert(DLClause clause, DLClause originalClause) {
		DLPredicate p;
		Collection<Atom> newHeadAtoms = new LinkedList<Atom>();
		for (Atom headAtom: clause.getHeadAtoms()) 
			newHeadAtoms.add(headAtom);
		
		for (Atom headAtom: clause.getHeadAtoms()) {
			p = headAtom.getDLPredicate(); 
			if (isAtLeastOneOnInverseFuncProperties(p)) 
				newHeadAtoms.add(headAtom); 
		}
		
		if (newHeadAtoms.size() > clause.getHeadLength()) 
			clause = DLClause.create(newHeadAtoms.toArray(new Atom[0]), clause.getBodyAtoms());
			
		return approxExist.convert(clause, clause); 
	}
	
	private boolean isAtLeastOneOnInverseFuncProperties(DLPredicate predicate) {
		if (!(predicate instanceof AtLeastConcept))
			return false;
		AtLeastConcept atLeast = (AtLeastConcept) predicate;
		if (!(atLeast.getOnRole() instanceof AtomicRole))
			return false; 
			
		return atLeast.getNumber() == 1 && inverseFuncProperties.contains(((AtomicRole) atLeast.getOnRole()).getIRI()); 
	}
	

}

