package uk.ac.ox.cs.pagoda.util;

import java.io.*;

public class TurtleHelper {

	public static void simplify(String tempFile, String outputPath) throws IOException {
		BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(tempFile)));
		BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(outputPath)));

		String line, sub = null, pred = null, obj = null;
		char lastSymbol = '.', symbol; 
		String[] seg;
		while ((line = reader.readLine()) != null) {
			if (line.trim().isEmpty() || line.startsWith("#") || line.startsWith("@base")) 
				continue;
			
			if (line.startsWith("@")) {
				writer.write(line);
				writer.newLine();
				continue;
			}
				
			
			symbol = line.charAt(line.length() - 1);
			
			if (lastSymbol == '.') {
				seg = line.split(" ");
				sub = seg[0];
				pred = seg[1];
				obj = seg[2];
			}
			else if (lastSymbol == ';') {
				line = line.substring(sub.length() + 1);
				seg = line.split(" ");
				pred = seg[0];
				obj = seg[1];
			}
			else if (lastSymbol == ',') {
				line = line.substring(sub.length() + pred.length() + 2);
				obj = line.substring(0, line.lastIndexOf(' '));
			}
			else Utility.logError("ERROR");

			lastSymbol = symbol;
			if (pred.equals("rdf:type") && obj.startsWith("owl:"))
				continue;
			
			writer.write(sub + " " + pred + " " + obj + " .\n");
		}
		
		reader.close();
		writer.close();
	}

}
