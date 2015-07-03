package uk.ac.ox.cs.pagoda.hermit;

import org.semanticweb.HermiT.model.*;
import uk.ac.ox.cs.pagoda.MyPrefixes;
import uk.ac.ox.cs.pagoda.util.Namespace;

public class RuleHelper {

//	public static String abbreviateIRI(String text) {
//		String prefixName, prefixIRI; 
//		int start = -1, ends = -1;
//		while (true) {
//			start = text.indexOf('<', ends + 1);
//			if (start == -1) return text;
//			ends = text.indexOf('>', start + 1);
//			if (ends == -1)	return text;
//			String sub = text.substring(start, ends + 1), newSub = text.substring(start + 1, ends);
//			
//			int index = splitPoint(newSub); 
//			if (index >= 0) {
//				prefixIRI = newSub.substring(0, index + 1);
//				if ((prefixName = MyPrefixes.PAGOdAPrefixes.getPrefixName(prefixIRI)) == null) {
//					prefixName = getNewPrefixName();
//					MyPrefixes.PAGOdAPrefixes.declarePrefix(prefixName, prefixIRI);
//				}
//				newSub = newSub.replace(prefixIRI, prefixName); 
//				text = text.replaceAll(sub, newSub);
//				ends -= sub.length() - newSub.length();
//			}
//		}
//	}
	
	public static String getText(DLClause clause) {
		StringBuffer buf = new StringBuffer();
		String atomText; 
		
		boolean lastSpace = true;
		for (Atom headAtom: clause.getHeadAtoms()) {
			if ((atomText = getText(headAtom)) == null) continue; 
			if (!lastSpace)	buf.append(" v ");
			buf.append(atomText);
			lastSpace = false;
		}
		buf.append(" :- ");
		lastSpace = true;
		for (Atom bodyAtom: clause.getBodyAtoms()) {
//		for (String str: strs[1].split(", ")) {
			if ((atomText = getText(bodyAtom)) == null) continue; 
			if (!lastSpace) buf.append(", ");
			buf.append(atomText);
			lastSpace = false;
		}
		buf.append('.');
		return buf.toString();
	}

	
	private static String getText(Atom atom) {
		if (atom.getDLPredicate() instanceof NodeIDsAscendingOrEqual ||
				atom.getDLPredicate() instanceof NodeIDLessEqualThan) 
			return null;
		
		StringBuilder builder = new StringBuilder(); 
		if (atom.getArity() == 1) {
			builder.append(getText(atom.getDLPredicate())); 
			builder.append("("); 
			builder.append(getText(atom.getArgument(0)));
			builder.append(")"); 
		}
		else {
			DLPredicate p = atom.getDLPredicate();
			if (p instanceof Equality || p instanceof AnnotatedEquality) builder.append(Namespace.EQUALITY_ABBR); 
			else if (p instanceof Inequality) builder.append(Namespace.INEQUALITY_ABBR); 
			else builder.append(getText(p));
			builder.append("("); 
			builder.append(getText(atom.getArgument(0))); 
			builder.append(","); 
			builder.append(getText(atom.getArgument(1))); 
			builder.append(")"); 
		}
		return builder.toString(); 
	}

	public static String getText(DLPredicate p) {
		if (p instanceof Equality || p instanceof AnnotatedEquality) return Namespace.EQUALITY_ABBR; 
		if (p instanceof Inequality) return Namespace.INEQUALITY_ABBR;
		if (p instanceof AtomicRole && ((AtomicRole) p).getIRI().startsWith("?"))
			return ((AtomicRole) p).getIRI(); 
		return MyPrefixes.PAGOdAPrefixes.abbreviateIRI(p.toString());
	}

	public static String getText(Term t) {
		if (t instanceof Variable)
			return "?" + ((Variable) t).getName(); 
		return MyPrefixes.PAGOdAPrefixes.abbreviateIRI(t.toString());
	}

}
