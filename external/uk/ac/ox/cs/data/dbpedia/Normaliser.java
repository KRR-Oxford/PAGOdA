package uk.ac.ox.cs.data.dbpedia;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.text.Normalizer;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

public class Normaliser {
	
	public static void main(String[] args) throws IOException {
		if (args.length == 0) {
			args = new String[] { 
					"/home/yzhou/ontologies/npd/npd-data-dump-minus-datatype.ttl", 
					"1"
			}; 
		}
		
		BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(args[0])));
		String fragment = args[0];
		int size = Integer.valueOf(args[1]), index;
		
		if ((index = fragment.lastIndexOf(".")) != -1) {
			fragment = fragment.substring(0, index) + "_new_fragment" + args[1] + fragment.substring(index); 
		}
		else fragment += "_fragment" + args[1]; 
		
		BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(fragment)));
		
//		simpleProcess(reader, writer, size);
		process(reader, writer, size);
		
		writer.close(); 
		reader.close(); 
	}
	
	public static void simpleProcess(BufferedReader reader, BufferedWriter writer, int size) throws IOException {
		String line; 
		int index = 0; 
		while ((line = reader.readLine()) != null) {
			if (++index == size) {
				index = 0; 
				writer.write(line);
				writer.newLine(); 
			}
		}
	}
	
	static final String illegalSymbols = ",()'‘";
	static final String[][] replacedSymbols = new String[][] {
		{"æ", "ae"}, 
		{"ø", "o"}, 
		{"ß", "t"},  
		{"Ł", "L"}, 
		{"ı", "i"}, 
		{"ł", "l"}, 
		{"–", "-"}, 
		{"&", "and"}, 
		{"ð", "o"}, 
		{"ə", "e"}, 
		{"Đ", "D"}, 
		{"ħ", "h"}, 
//		{"%60", "_"},
		{"đ", "d"},  
		{"Þ", "P"}
	};
	
	static Set<Character> symbols2remove;
	static Map<Character, String> symbols2replace; 
	
	static {
		symbols2remove = new HashSet<Character>();
		for (int i = 0; i < illegalSymbols.length(); ++i)
			symbols2remove.add(illegalSymbols.charAt(i));
		
		symbols2replace = new HashMap<Character, String>();
		for (int i = 0; i < replacedSymbols.length; ++i)
			symbols2replace.put(replacedSymbols[i][0].charAt(0), replacedSymbols[i][1]); 
	}
	
	static final String urlSymbols = "http://"; 
	static final int urlSymbolLength = 7;
	
	public static void process(BufferedReader reader, BufferedWriter writer, int size) throws IOException {
		int index = 0;
		String line; 
		
		String newLine; 
		while ((line = reader.readLine()) != null) {
			if (line.contains("@"))	
				continue;

			if (++index == size) {
				newLine = process(line); 
				writer.write(deAccent(newLine.toString()));
				writer.write('.');
				writer.newLine(); 
				index = 0; 
			}
		}
		
		writer.close(); 
		reader.close(); 
	}

	private static String process(String line) {
		line = line.replace("%60", "_");//.replace("__", "_");
		
		int inURL = 0;
		char ch; 
		String str; 
		StringBuilder newLine = new StringBuilder(); 
		for (int i = 0; i < line.length(); ++i) {
			ch = line.charAt(i); 
			
			if (ch == '.') {
				if (inURL == urlSymbolLength) 
					newLine.append('.'); 
				continue; 
			}
		
			if (inURL == urlSymbolLength) {
				if (ch == '/' || ch == '#' || ch == ')' || ch == '>') inURL = 0; 
			}
			else if (ch == urlSymbols.charAt(inURL)) {
				++inURL;
			}
			else inURL = 0; 
			
			if ((str = symbols2replace.get(ch)) != null)
				newLine.append(str); 
			else if (!symbols2remove.contains(ch))
				newLine.append(ch); 
		}
		
		return newLine.toString();
	}

	public static String deAccent(String str) {
	    String nfdNormalizedString = Normalizer.normalize(str, Normalizer.Form.NFD); 
	    Pattern pattern = Pattern.compile("\\p{InCombiningDiacriticalMarks}+");
	    String t = pattern.matcher(nfdNormalizedString).replaceAll("");
	    return t; 
	}
	

}
