package uk.ac.ox.cs.pagoda.hermit;

import org.semanticweb.HermiT.model.*;
import uk.ac.ox.cs.pagoda.MyPrefixes;
import uk.ac.ox.cs.pagoda.owl.OWLHelper;
import uk.ac.ox.cs.pagoda.util.Namespace;

import java.security.InvalidParameterException;
import java.util.ArrayList;

public class RuleHelper {
    private static final String OR = "|";
    private static final String IF = ":-";
    private static final String AND = ",";

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
			if (!lastSpace)	buf.append(" ").append(OR).append(" ");
			buf.append(atomText);
			lastSpace = false;
		}
        buf.append(" ").append(IF).append(" ");
		lastSpace = true;
		for (Atom bodyAtom: clause.getBodyAtoms()) {
//		for (String str: strs[1].split(", ")) {
			if ((atomText = getText(bodyAtom)) == null) continue; 
			if (!lastSpace) buf.append(AND).append(" ");
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

	public static Term parseTerm(String s) {
		s = s.trim();
		if(s.startsWith("?")) return Variable.create(s.substring(1));
		return Individual.create(MyPrefixes.PAGOdAPrefixes.expandIRI(s));
	}

    public static Atom parseAtom(String s) {
        s = s.trim();

        String[] split = s.split("\\(");
        String predicateIri = OWLHelper.removeAngles(MyPrefixes.PAGOdAPrefixes.expandText(split[0]));
        String[] predicateArgs = split[1].substring(0, split[1].length() - 1).split(",");
        int numOfargs = predicateArgs.length;
        Term terms[] = new Term[predicateArgs.length];
        for (int i = 0; i < terms.length; i++)
            terms[i] = parseTerm(predicateArgs[i]);
        if(numOfargs == 1) {
            AtomicConcept atomicConcept = AtomicConcept.create(predicateIri);
            return Atom.create(atomicConcept, terms);
        }
        else if(numOfargs == 2) {
            AtomicRole atomicRole = AtomicRole.create(predicateIri);
            return Atom.create(atomicRole, terms);
        }
        else
            throw new InvalidParameterException();
        // TODO? add equality (owl:sameAs)?
    }

    public static DLClause parseClause(String s) {
        s = s.trim();
        if(s.endsWith(".")) s = s.substring(0, s.length()-1).trim();

        String[] headAndBody = s.split(IF);
        String[] headAtomsStr = splitAtoms(headAndBody[0], OR);
        String[] bodyAtomsStr = splitAtoms(headAndBody[1], AND);

        Atom[] headAtoms = new Atom[headAtomsStr.length];
        Atom[] bodyAtoms = new Atom[bodyAtomsStr.length];
        for (int i = 0; i < headAtoms.length; i++)
            headAtoms[i] = parseAtom(headAtomsStr[i]);
        for (int i = 0; i < bodyAtoms.length; i++)
            bodyAtoms[i] = parseAtom(bodyAtomsStr[i]);

        return DLClause.create(headAtoms, bodyAtoms);
    }

    private static String[] splitAtoms(String s, String operator) {
        char op = operator.charAt(0);
        ArrayList<String> result = new ArrayList<>();

        int b = 0;
        boolean betweenParenthesis = false;
        for (int i = 0; i < s.length(); i++) {
            if(s.charAt(i) == '(')
                betweenParenthesis = true;
            else if(s.charAt(i) == ')')
                betweenParenthesis = false;
            else if(s.charAt(i) == op && !betweenParenthesis) {
                result.add(s.substring(b, i));
                b = i + 1;
            }
        }
        if(b < s.length()) result.add(s.substring(b));

        return result.toArray(new String[0]);
    }

}
