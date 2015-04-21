package uk.ac.ox.cs.pagoda.reasoner.light;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.LinkedList;
import java.util.Map.Entry;

import org.semanticweb.HermiT.model.DLClause;
import org.semanticweb.karma2.exception.IllegalInputQueryException;
import org.semanticweb.karma2.model.ConjunctiveQuery;
import org.semanticweb.karma2.model.cqparser.ConjunctiveQueryParser;
import uk.ac.ox.cs.pagoda.MyPrefixes;
import uk.ac.ox.cs.pagoda.hermit.DLClauseHelper;
import uk.ac.ox.cs.pagoda.hermit.RuleHelper;
import uk.ac.ox.cs.pagoda.util.Utility;

public class KarmaQuery {
	
	StringBuffer queryBuffer; 

	public KarmaQuery(String queryText) {
		LinkedList<String> answerVariables = new LinkedList<String>(); 
		DLClause clause = DLClauseHelper.getQuery(queryText, answerVariables);
		String clauseText = RuleHelper.getText(clause); 
//		clauseText = RuleHelper.abbreviateIRI(clauseText).replace(":-", "<-");
		clauseText = clauseText.replace(":-", "<-");
		queryBuffer = new StringBuffer();
		
		clauseText = expandIRI4Arguments(clauseText); 
		
		for (Entry<String, String> entry : MyPrefixes.PAGOdAPrefixes.getPrefixIRIsByPrefixName().entrySet()) 
			if (clauseText.contains(entry.getKey())) {
				if (queryBuffer.length() > 0) queryBuffer.append(',').append(Utility.LINE_SEPARATOR);
				queryBuffer.append("prefix ").append(entry.getKey()).append(" <").append(entry.getValue()).append(">"); 
			}
		if (queryBuffer.length() > 0) queryBuffer.append(Utility.LINE_SEPARATOR); 
		
		queryBuffer.append("p(");
		boolean first = true; 
		for (String var: answerVariables) { 
			if (first) first = false; 
			else queryBuffer.append(","); 
				
			queryBuffer.append("?").append(var); 
		}
		queryBuffer.append(")").append(clauseText.substring(0, clauseText.length() - 1));
	}
	
	private String expandIRI4Arguments(String clauseText) {
		int leftIndex = clauseText.indexOf('('), rightIndex = clauseText.indexOf(')', leftIndex + 1);
		String argsText, newArgsText; 
		while (leftIndex != -1) {
			argsText = clauseText.substring(leftIndex + 1, rightIndex);
			newArgsText = MyPrefixes.PAGOdAPrefixes.expandText(argsText); 
			clauseText = clauseText.replace(argsText, newArgsText); 
			
			rightIndex += newArgsText.length() - argsText.length();
			leftIndex = clauseText.indexOf('(', rightIndex + 1); 
			rightIndex = clauseText.indexOf(')', leftIndex + 1); 
		}
		
		return clauseText; 
	}

	public ConjunctiveQuery getConjunctiveQuery() {
		ConjunctiveQuery cq = null;
		try {
			cq = new ConjunctiveQueryParser(toString()).parse();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalInputQueryException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (Exception e) {
			Utility.logDebug("The query cannot be properly handled by KARMA."); 
			return null; 
		}
		return cq; 
	}
	
	@Override
	public String toString() {
		return queryBuffer.toString(); 
	}

	static String sample = "prefix P0: <http://swat.cse.lehigh.edu/onto/univ-bench.owl#>, " +
			"prefix rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>, " +
			"prefix owl: <http://www.w3.org/2002/07/owl#>" +
			"q(?0) <- owl:Thing(?0), P0:Person(?0)";
	
}
