package uk.ac.ox.cs.data;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

import uk.ac.ox.cs.pagoda.query.QueryManager;

public class QueryFilter {

	public static void main(String[] args) throws FileNotFoundException {
		args = new String[] {"/media/krr-nas-share/Yujiao/ontologies/npd/queries/atomic.sparql", 
				"/home/yzhou/java-workspace/test-share/results_new/npd/pagoda"}; 
		Scanner answerReader = new Scanner(new File(args[1]));
		int totalNumberOfQueries = 0; 
		String line, prefix = "The number of answer tuples: "; 
		int index = 0, length = prefix.length();
		for (String query: QueryManager.collectQueryTexts(args[0])) {
			while (!(line = answerReader.nextLine()).startsWith(prefix));
			++totalNumberOfQueries; 
//			if (query.contains("?X ?Y")) continue; 
			if (line.charAt(length) == '0') continue; 
			System.out.println("^[Query" + ++index + "]"); 
			System.out.println(query); 
		}
		answerReader.close();
		System.out.println("Total number of queries: " + totalNumberOfQueries); 
	}
	
}
