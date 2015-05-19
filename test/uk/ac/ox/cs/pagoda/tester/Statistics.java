package uk.ac.ox.cs.pagoda.tester;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Scanner;

@Deprecated
public class Statistics {
	
	double satCheckTime; 
	double preprocessTime;
	LinkedList<Integer> number = new LinkedList<Integer>(); 
	LinkedList<Double> time = new LinkedList<Double>(); 
	
	public Statistics(String file) {
		Scanner scanner = null; 
		try {
			scanner = new Scanner(new File(file));
			for (String line; scanner.hasNextLine(); ) {
				line = scanner.nextLine(); 
				if (line.contains("time for satisfiability checking"))
					satCheckTime = Double.parseDouble(line.substring(line.indexOf(": ") + 2));
				else if (line.contains("Preprocessing Done in"))
					preprocessTime = Double.parseDouble(line.substring(line.indexOf("in ") + 3, line.indexOf(" second")));
				else if (line.contains("The number of answer tuples:"))
					number.add(Integer.parseInt(line.substring(line.indexOf(": ") + 2))); 
				else if (line.contains("Total time to answer this query:")) 
					time.add(Double.parseDouble(line.substring(line.indexOf(": ") + 2))); 
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} finally {
			if (scanner != null)
				scanner.close();
		}
	}
	
	public String diff(String other) {
		return diff(new Statistics(other)); 
	}
	
	public String diff(Statistics other) {
		if (other.number.size() != number.size()) 
			return "The number of query is different! " + this.number.size() + " v.s. " + other.number.size();  
		int i = 0; 
		Iterator<Integer> iter1 = number.iterator(), iter2 = other.number.iterator();
		StringBuilder diff = new StringBuilder(); 
		int a, b; 
		while (iter1.hasNext()) {
			++i; 
			if ((a = iter1.next()) != (b = iter2.next())) {
				diff.append("Query ").append(i).append(": ").append(a).append(", reference ").append(b).append("\n"); 
			}
		}
		return diff.toString(); 
	}
	
}
