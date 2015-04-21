package uk.ac.ox.cs.pagoda.multistage.treatement;

import java.util.Comparator;

import org.semanticweb.HermiT.model.AnnotatedEquality;
import org.semanticweb.HermiT.model.Atom;
import org.semanticweb.HermiT.model.AtomicConcept;
import org.semanticweb.HermiT.model.AtomicRole;
import org.semanticweb.HermiT.model.DLPredicate;
import org.semanticweb.HermiT.model.Equality;
import org.semanticweb.HermiT.model.Inequality;

import uk.ac.ox.cs.pagoda.multistage.Normalisation;
import uk.ac.ox.cs.pagoda.util.Utility;

public class SimpleComparator implements Comparator<Atom> {

	@Override
	public int compare(Atom o1, Atom o2) {
		int ret = type(o1) - type(o2);
		if (ret != 0) return ret; 
		
		return o1.getDLPredicate().toString().compareTo(o2.getDLPredicate().toString()); 
//		String name1 = o1.getDLPredicate().toString(); 
//		String name2 = o2.getDLPredicate().toString();
//		boolean value1 = name1.contains(Normalisation.auxiliaryConceptPrefix); 
//		boolean value2 = name2.contains(Normalisation.auxiliaryConceptPrefix);  
//		if (value1 && value2 || !value1 && !value2) 
//			return name1.compareTo(name2); 
//		return value1 ? 1 : -1; 
	}

	private int type(Atom a) {
		DLPredicate p = a.getDLPredicate(); 
		if (p instanceof AtomicConcept) 
			if (!p.toString().contains(Normalisation.auxiliaryConceptPrefix))
				return 0;
			else 
				return 3;
		
		if (p instanceof AtomicRole) return 1;
		if (p instanceof Inequality) return 2; 
		if (p instanceof Equality || p instanceof AnnotatedEquality) return 4;
		Utility.logError("Unknown DLPredicate in an atom: " + a); 
		return -1; 
	}
	
}