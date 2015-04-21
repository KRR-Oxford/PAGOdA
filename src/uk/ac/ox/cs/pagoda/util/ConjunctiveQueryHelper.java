package uk.ac.ox.cs.pagoda.util;

import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;

public class ConjunctiveQueryHelper {
	
	public static String[][] getAnswerVariables(String queryText) {
		Collection<String> disVars = new LinkedList<String>(), undisVars = new LinkedList<String>(); 
		for (String var: getAllVariables(queryText))
			if (var.startsWith("?")) disVars.add(var.substring(1)); 
			else undisVars.add(var.substring(2));
		
		String[] distinguishedVariables = disVars.toArray(new String[0]); 
		String[] undistinguishedVariables = undisVars.toArray(new String[0]); 
		String[] answerVariables = null;
		
		String uppercase = queryText.toUpperCase(); 
		int selectIndex = uppercase.indexOf("SELECT");
		int whereIndex = uppercase.indexOf("WHERE"); 
		String selectClause = queryText.substring(selectIndex + 6, whereIndex); 
		if (selectClause.contains("*")) answerVariables = distinguishedVariables;
		else {
			String[] terms = selectClause.split(" ");
			int num = 0; 
			for (int i = 0; i < terms.length; ++i)
				if (terms[i].startsWith("?")) ++num;
			answerVariables = new String[num]; 
			for (int i = 0, j = 0; i < terms.length; ++i)
				if (terms[i].startsWith("?"))
					answerVariables[j++] = terms[i].substring(1);
		}
	
		if (answerVariables != distinguishedVariables) {
			int index = 0; 
			for (; index < answerVariables.length; ++index) {
				distinguishedVariables[index] = answerVariables[index]; 
				disVars.remove(answerVariables[index]); 
			}
			for (String var: disVars)
				distinguishedVariables[index++] = var; 
		}
		
		return new String[][] { answerVariables, distinguishedVariables, undistinguishedVariables };
	}

	private static Collection<String> getAllVariables(String queryText) {
		Collection<String> vars = new HashSet<String>(); 
		int start, end = 0; 
		char ch; 
		while ((start = queryText.indexOf("?", end)) != -1) {
			end = start + 1; 
			while (end + 1 < queryText.length() && (ch = queryText.charAt(end + 1)) != '\n' && ch != ' ') 
				++end;
			vars.add(queryText.substring(start, end + 1)); 
		}
		
		end = 0; 
		while ((start = queryText.indexOf("_:", end)) != -1) {
			end = start + 1; 
			while (end + 1 < queryText.length() && (ch = queryText.charAt(end + 1)) != '\n' && ch != ' ') 
				++end;
			vars.add(queryText.substring(start, end + 1)); 
		}

		return vars; 
	}

}
