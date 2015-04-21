package uk.ac.ox.cs.pagoda.query;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Scanner;

import uk.ac.ox.cs.pagoda.util.Utility;

public class QueryManager {

	public Collection<QueryRecord> collectQueryRecords(String queryfile) {
		Collection<QueryRecord> ret = new LinkedList<QueryRecord>();
		for (String queryText: collectQueryTexts(queryfile))
			ret.add(create(queryText));
		return ret; 
	}
	
	public static Collection<String> collectQueryTexts(String queryfile) {
		Scanner scanner = null;
		try {
			scanner = new Scanner(new File(queryfile));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			return null; 
		} 
		Collection<String> ret = new LinkedList<String>(); 
		
		StringBuilder sb = new StringBuilder(); 
		int leftToMatch; 
		String text; 
		while (scanner.hasNextLine()) {
			leftToMatch = -1; 
			for (String line; scanner.hasNextLine(); ) {
				line = scanner.nextLine();
				if (line.length() > 6 && line.substring(0, 6).equalsIgnoreCase("SELECT")) {
					String next = line.split(" ")[1]; 
					if (!next.equalsIgnoreCase("distinct"))
						line = line.substring(0, 6) + " distinct" + line.substring(6); 
					}
				for (int i = 0; i < line.length(); ++i)
					if (line.charAt(i) == '{') 
						if (leftToMatch == -1) leftToMatch = 1; 
						else ++leftToMatch; 
					else if (line.charAt(i) == '}') --leftToMatch; 

//				if (line.isEmpty()) break;

				if (!line.isEmpty())
					sb.append(line).append(Utility.LINE_SEPARATOR);
				
				if (leftToMatch == 0) break; 
			}
			
			text = preprocess(sb.toString()); 
			if (!text.isEmpty())
				ret.add(text);
			sb.setLength(0);
		}
		
		scanner.close();
		return ret; 
	}
	
	private static String preprocess(String text) {
		int index; 
		text = text.trim();
		while (text.startsWith("^") || text.startsWith("#") || text.startsWith("//") || text.startsWith("@"))
			if ((index = text.indexOf("\n")) != -1)
				text = text.substring(index + 1);
			else {
				text = "";
				break; 
			}
		return text; // text.replace(" a ", " <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> ");
	}

	private Map<String, QueryRecord> allRecords = new HashMap<String, QueryRecord>(); 
	private int queryCounter = 0; 
	
	public QueryRecord create(String text, int i, int j) {
//		StringBuilder queryText = new StringBuilder();
//		for (String seq : text.split("\s")) {
//			if (seq.length() == 0) continue; 
//			if (queryText.length() != 0) queryText.append(" "); 
//			queryText.append(seq); 
//		}
//		text = queryText.toString(); 
		text = text.replaceAll("\\s+", " ").trim(); 
		QueryRecord ret = allRecords.get(text);  
		if (ret != null) return ret; 
		else {
			if (i == -1) {
				i = ++queryCounter;
			}
			
			ret = new QueryRecord(this, text, i, j); 
			allRecords.put(text, ret); 
			return ret; 
		}
	}
	
	public QueryRecord create(String text, int i) {
		return create(text, i, 0); 
	}
	

	public void remove(String queryText) {
		allRecords.remove(queryText); 		
	}

	public void put(String text, QueryRecord queryRecord) {
		allRecords.put(text, queryRecord); 		
	}

	public QueryRecord create(String queryText) {
		return create(queryText, -1, 0);
	}

}
