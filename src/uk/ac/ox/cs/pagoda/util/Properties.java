package uk.ac.ox.cs.pagoda.util;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;

public class Properties {

	public static final String FILE_SEPARATOR = ";";
	
//	switches
//	public static final String reuseGapFile = "REUSE_GAP";
	public static final String toTrackProofs = "TO_TRACK";
	public static final String checkAnswers = "TO_CHECK_ANSWERS";
	public static final String redirectSysOut = "TO_REDIRECT_SYS_OUT";
	public static final String considerEqualities = "TO_CONSIDER_EQUALITIES";  

//	parameters
	public static final String testcase = "TEST_CASE";
	public static final String typeOfLowerBounds = "TYPE_LOWER_BOUNDS";
	public static final String FULL_REASONER = "OWLREASONER"; 

//	file locations
	public static final String ontologyFile = "LOWER_T_FILE";
	public static final String importedData = "IMPORT";
	public static final String queryFile = "QUERY_FILE";

//	auxiliary files
//	public static final String auxiliaryDirectory = "AUXILIARY_DIRECTORY";
//	public static final String queryAnswerGapFile = "GAP_FILE";
//	public static final String lowerAnswerFile = "LOWER_ANSWER_FILE";
//	public static final String upperAnswerFile = "UPPER_ANSWER_FILE";
//	public static final String boundsGapFile = "BOUNDS_GAP_FILE";
//	public static final String fragmentFile = "FRAGMENT_FILE";
	
	public static final String correspondence = "CORRESPONDENCE";

	private HashMap<String, String> param = new HashMap<String, String>();

	public static final String on = String.valueOf(true); 
	public static final String off = String.valueOf(false);

		public void reset() {
		param.clear(); 
//		param.put(reuseGapFile, on); 
		param.put(toTrackProofs, on);
		param.put(checkAnswers, on);
		param.put(redirectSysOut, off); 
		param.put(considerEqualities, off); 
	}

	public Properties() {
		reset(); 
	}

	public void addImportedFile(String additionalDataFile) {
		if (additionalDataFile == null) return ;
		String files = param.get(importedData); 
		StringBuilder sb = new StringBuilder();
		if (files != null)
			sb.append(files).append(FILE_SEPARATOR);
		sb.append(additionalDataFile); 
		param.put(importedData, sb.toString()); 
	}
	
	public void load(String file) throws IOException {
		BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(file)));
		String line;
		String tokens[];
		while ((line = Utility.readLine(reader)) != null) {
			if (line.isEmpty() || line.startsWith("#"))
				continue;
			
			tokens = line.split("="); 
			if (tokens[1].equals("on"))
				set(tokens[0], String.valueOf(true));
			else if (tokens[1].equals("off"))
				set(tokens[0], String.valueOf(false));
			else 
				set(tokens[0], tokens[1]);
		}
		reader.close();
	}

	public String get(String key) {
		return param.get(key);
	}

	public void set(String key, String value) {
		param.put(key, value);
	}

}
