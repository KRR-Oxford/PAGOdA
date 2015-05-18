package uk.ac.ox.cs.pagoda.tester;

import uk.ac.ox.cs.pagoda.reasoner.QueryReasoner;
import uk.ac.ox.cs.pagoda.util.Properties;
import uk.ac.ox.cs.pagoda.util.Timer;
import uk.ac.ox.cs.pagoda.util.Utility;

// TODO clean it, or code another one
public class PagodaTester {

	public static void main(String... args) {
//		Properties properties = new Properties(PagodaTester.class.
//				getClassLoader().getResource("uobm.properties").getPath());
		Properties properties = new Properties();

		int index = 0; 
		if (args.length > index) properties.setOntologyPath(args[index++]);   
		if (args.length > index && (args[index].endsWith(".ttl") || args[index].endsWith(".nt"))) properties.setDataPath(args[index++]);   
		if (args.length > index && args[index].endsWith(".sparql")) properties.setQueryPath(args[index++]);   
		if (args.length > index && !args[index].startsWith("-")) properties.setAnswerPath(args[index++]);   
		if (args.length > index) properties.setToClassify(Boolean.parseBoolean(args[index++].substring(1)));   
		if (args.length > index) properties.setToCallHermiT(Boolean.parseBoolean(args[index++].substring(1)));   
	
		Utility.logInfo("Ontology file: " + properties.getOntologyPath());
		Utility.logInfo("Data files: " + properties.getDataPath());
		Utility.logInfo("Query files: " + properties.getQueryPath());
		Utility.logInfo("Answer file: " + properties.getAnswerPath());
		
		QueryReasoner pagoda = null; 
		
		try {
			Timer t = new Timer();
			pagoda = QueryReasoner.getInstance(properties); 
			if (pagoda == null) return;
			
			Utility.logInfo("Preprocessing Done in " + t.duration()	+ " seconds.");
			
			if (properties.getQueryPath() != null)
				for (String queryFile: properties.getQueryPath().split(";"))
					pagoda.evaluate(pagoda.getQueryManager().collectQueryRecords(queryFile));
 		} finally {
			if (pagoda != null) pagoda.dispose();
		}
	}

}
