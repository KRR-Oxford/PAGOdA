package uk.ac.ox.cs.pagoda;

import java.util.Map;

import uk.ac.ox.cs.pagoda.owl.OWLHelper;
import uk.ac.ox.cs.pagoda.util.Namespace;
import uk.ac.ox.cs.pagoda.util.Utility;

public class MyPrefixes {

	public final static MyPrefixes PAGOdAPrefixes = new MyPrefixes();
	
	private org.semanticweb.HermiT.Prefixes hermit = new org.semanticweb.HermiT.Prefixes(); 
	private uk.ac.ox.cs.JRDFox.Prefixes rdfox = new uk.ac.ox.cs.JRDFox.Prefixes();
	
	public MyPrefixes() {
		hermit.declareSemanticWebPrefixes(); 
		hermit.declarePrefix("anony:", Namespace.PAGODA_ANONY);
		hermit.declarePrefix("aux:", Namespace.PAGODA_AUX);
		
		rdfox.declareSemanticWebPrefixes(); 
		rdfox.declarePrefix("anony:", Namespace.PAGODA_ANONY);
		rdfox.declarePrefix("aux:", Namespace.PAGODA_AUX);
	}
	
	public org.semanticweb.HermiT.Prefixes getHermiTPrefixes() {
		return hermit; 
	}
	
	public uk.ac.ox.cs.JRDFox.Prefixes getRDFoxPrefixes() {
		return rdfox; 
	}
	
	public void declarePrefix(String prefixName, String prefixIRI) {
		Utility.logDebug("Prefix declared: " + prefixName + "=" + prefixIRI);
		hermit.declarePrefix(prefixName, prefixIRI); 
		rdfox.declarePrefix(prefixName, prefixIRI); 
	}
	
	public static boolean isColon4Prefixes(String text, int index) {
		if (index >= text.length() - 1) return false; 
		if (index > 0 && text.charAt(index - 1) == '_') return false; // _: blank node
		char nextCharacter = text.charAt(index + 1);
		if (nextCharacter == '-') return false; // :-
		if (nextCharacter == '/') return false; // :/
		return true;
	}

	public String expandText(String text) {
		for (int index = 0; ;) {
			index = text.indexOf(':', index);
			if (!isColon4Prefixes(text, index)) {
				++index; 
				continue;
			}
			if (index == -1) return text;
			int start = index - 1, ends = index + 1;
			char ch;
			while (start >= 0 && (ch = text.charAt(start)) != ',' && ch != '(' && ch != ' ') --start;
			while (ends < text.length() && (ch = text.charAt(ends)) != '(' && ch != ',' && ch != ')') ++ends;

			String sub = text.substring(start + 1, ends);
			String newSub = OWLHelper.addAngles(hermit.expandAbbreviatedIRI(sub)); 
			
			text = text.replaceAll(sub, newSub);
			index = ends + newSub.length() - sub.length();
		}
	}
	
	public void declareNewPrefix(String iri) {
		if (!iri.startsWith("<")) return ;
		int index = splitPoint(iri);
		if (index >= 0) {
			String prefixIRI = iri.substring(1, index + 1), prefixName;
			if ((prefixName = getPrefixName(prefixIRI)) == null) {
				prefixName = getNewPrefixName();
				declarePrefix(prefixName, prefixIRI);
			}
		}
	}
	
	/**
	 * detect the split point of prefix and name for an <url>
	 * 
	 * @param uri
	 * @param index
	 * @return
	 */
	private int splitPoint(String uri) {
		int index = uri.lastIndexOf("#"); 
		if (index != -1) return index; 
		index = uri.lastIndexOf("/"); 
		if (index > 0 && uri.charAt(index - 1) != '/')
			return index;
		return -1; 
	}


	private int prefixNumber = 0; 
	
	public String getNewPrefixName() {
		return "prefix" + (prefixNumber ++) + ":"; 
	}

	public String abbreviateIRI(String iri) {
		declareNewPrefix(iri);
		
		if (iri.startsWith("<"))
			return hermit.abbreviateIRI(OWLHelper.removeAngles(iri));
		else if (iri.contains("^^")) {
			int index = iri.indexOf("^^"); 
			if (iri.charAt(index + 2) == '<')
				return iri.substring(0, index + 2) + abbreviateIRI(iri.substring(index + 2, iri.lastIndexOf('>') + 1)); 
		}
		return iri; 
	}
	
	/**
	 * expanded iri without <>
	 * 
	 * @param iri
	 * @return
	 */
	public String expandIRI(String iri) {		
		if (hermit.canBeExpanded(iri))
			return hermit.expandAbbreviatedIRI(iri); 
		else if (iri.startsWith("<"))
			return iri.substring(1, iri.length() - 1);
		else 
			return iri; 
	}

	/**
	 * expanded iri with <>
	 * 
	 * @param iri
	 * @return
	 */
	public String getQuotedIRI(String iri) {
		if (iri.startsWith("<")) return iri; 
		if (hermit.canBeExpanded(iri))
			return OWLHelper.addAngles(hermit.expandAbbreviatedIRI(iri));
		return iri; 
	}
	
	public String getPrefixName(String prefixIRI) {
		return hermit.getPrefixName(prefixIRI);
	}

	public Map<String, String> getPrefixIRIsByPrefixName() {
		return hermit.getPrefixIRIsByPrefixName();
	}

	public String prefixesText() {
		StringBuffer buf = new StringBuffer();
		for (Map.Entry<String, String> entry: getPrefixIRIsByPrefixName().entrySet())
			buf.append("PREFIX ").append(entry.getKey()).append(" <").append(entry.getValue()).append(">").append(Utility.LINE_SEPARATOR);
		return buf.toString();
	}


}
