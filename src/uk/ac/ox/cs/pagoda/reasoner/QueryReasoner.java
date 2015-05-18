package uk.ac.ox.cs.pagoda.reasoner;

import com.google.gson.Gson;
import org.semanticweb.owlapi.model.OWLOntology;
import uk.ac.ox.cs.pagoda.owl.OWLHelper;
import uk.ac.ox.cs.pagoda.query.AnswerTuples;
import uk.ac.ox.cs.pagoda.query.QueryManager;
import uk.ac.ox.cs.pagoda.query.QueryRecord;
import uk.ac.ox.cs.pagoda.util.Properties;
import uk.ac.ox.cs.pagoda.util.Timer;
import uk.ac.ox.cs.pagoda.util.Utility;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Collection;

// TODO clean APIs
public abstract class QueryReasoner {
	
//	protected boolean forSemFacet = false;
	Properties properties;
	
	private static boolean defaultMultiStages = true; 
	private static boolean defaultEqualities = true; 

	public enum Type { Full, RLU, ELHOU }

	public static QueryReasoner getInstance(Properties p) {
		OWLOntology ontology = OWLHelper.loadOntology(p.getOntologyPath());
		QueryReasoner pagoda = getInstance(ontology, p);
		pagoda.properties = p; 
		pagoda.loadOntology(ontology);
		pagoda.importData(p.getDataPath());
		if (pagoda.preprocess()) {
			Utility.logInfo("The ontology is consistent!");
			return pagoda; 
		}
		else {
			System.out.println("The ontology is inconsistent!");
			pagoda.dispose();
			return null;
		}
	}
	
	public static QueryReasoner getInstance(OWLOntology o) {
		QueryReasoner pagoda = getInstance(Type.Full, o, defaultMultiStages, defaultEqualities);
		pagoda.properties = new Properties(); 
		return pagoda; 
	}
	
	public void setToClassify(boolean flag) {
		properties.setToClassify(flag);
	}
	
	public void setToCallHermiT(boolean flag) {
		properties.setToCallHermiT(flag);
	}
	
	private static QueryReasoner getInstance(OWLOntology o, Properties p) {
		return getInstance(Type.Full, o, defaultMultiStages, defaultEqualities); 
	}
	
	public static QueryReasoner getInstance(Type type, OWLOntology o, boolean performMultiStages, boolean considerEqualities) {
//		Utility.initialise();
		QueryReasoner reasoner; 
		if (OWLHelper.isInOWL2RL(o)) reasoner = new RLQueryReasoner();
		else if (OWLHelper.isInELHO(o)) reasoner = new ELHOQueryReasoner();
		else 
		switch (type) {
			case RLU: 
				reasoner = new RLUQueryReasoner(performMultiStages, considerEqualities); break;   
			case ELHOU: 
				reasoner = new ELHOUQueryReasoner(performMultiStages, considerEqualities); break;  
			default: 
				reasoner = new MyQueryReasoner(performMultiStages, considerEqualities); 
			}
		return reasoner; 
	}
	
	public static final String ImportDataFileSeparator = ";"; 
	protected StringBuilder importedData = new StringBuilder(); 
	
	public void importData(String datafile) {
		if (datafile != null && !datafile.equalsIgnoreCase("null"))
			importData(datafile.split(ImportDataFileSeparator)); 
	}

	public void importData(String[] datafiles) {
		if (datafiles != null) {
			for (String datafile: datafiles) {
				File file = new File(datafile); 
				if (file.exists()) {
					if (file.isFile()) importDataFile(file);
					else importDataDirectory(file);
				}
				else {
					Utility.logError("warning: file " + datafile + " doesn't exists."); 
				}
			}
		}
	}
	
	private void importDataDirectory(File file) {
		for (File child: file.listFiles())
			if (child.isFile()) importDataFile(child);
			else importDataDirectory(child);
	}
	
	private void importDataFile(File file) {
		String datafile;
		try {
			datafile = file.getCanonicalPath();
		} catch (IOException e) {
			e.printStackTrace();
			return ; 
		} 
		importDataFile(datafile); 
	}
	
	protected final void importDataFile(String datafile) {
		if (importedData.length() == 0)
			importedData.append(datafile); 
		else 
			importedData.append(ImportDataFileSeparator).append(datafile);

	}
	
	public abstract void loadOntology(OWLOntology ontology);
	
	public abstract boolean preprocess(); 

	public abstract boolean isConsistent(); 

	public boolean fullReasoner = this instanceof MyQueryReasoner; 

	public abstract void evaluate(QueryRecord record);
	
	public abstract void evaluateUpper(QueryRecord record); 
	
	public AnswerTuples evaluate(String queryText, boolean forFacetGeneration) {
		if (forFacetGeneration) {
			QueryRecord record = m_queryManager.create(queryText);
			Utility.logInfo("---------- start evaluating upper bound for Query " + record.getQueryID() + " ----------", queryText);
			if (!record.processed()) 
				evaluateUpper(record);
//			AnswerTuples tuples = record.getUpperBoundAnswers();
//			for (AnswerTuple tuple; tuples.isValid(); tuples.moveNext()) {
//				tuple = tuples.getTuple(); 
//				if (tuple.toString().contains("NC"))
//					System.out.println(tuple.toString()); 
//			}
			return record.getUpperBoundAnswers(); 
		}
		else 
			return evaluate(queryText); 
	}
	
	public AnswerTuples evaluate(String queryText) {
		QueryRecord record = m_queryManager.create(queryText); 
		Utility.logInfo("---------- start evaluating Query " + record.getQueryID() + " ----------", queryText);
		if (!record.processed())
			evaluate(record);
		AnswerTuples answer = record.getAnswers(); 
		record.dispose();
		return answer;
  
	}
	
	public void evaluate_shell(String queryText) {
		QueryRecord record = m_queryManager.create(queryText); 
		Utility.logInfo("---------- start evaluating Query " + record.getQueryID() + " ----------", queryText);
		if (!record.processed())
			evaluate(record);
		Utility.logInfo("Answers to this query: ", record.outputSoundAnswerTuple());
		record.dispose();
  
	}
	
//	public void evaluate(Collection<QueryRecord> queryRecords) {
//		evaluate(queryRecords);
//	}

	BufferedWriter answerWriter = null;

	public void evaluate(Collection<QueryRecord> queryRecords) {
		if (!isConsistent()) {
			Utility.logDebug("The ontology and dataset is inconsistent."); 
			return ; 
		}

		if(properties.getAnswerPath() != null && answerWriter == null) {
			try {
				answerWriter = Files.newBufferedWriter(Paths.get(properties.getAnswerPath()));
			} catch (IOException e) {
				Utility.logError("The answer path is not valid!");
				e.printStackTrace();
			}
		}
		
		Timer t = new Timer();
		Gson gson = QueryRecord.GsonCreator.getInstance();
		for (QueryRecord record: queryRecords) {
//			if (Integer.parseInt(record.getQueryID()) != 218) continue; 
			Utility.logInfo("---------- start evaluating Query " + record.getQueryID() + " ----------", 
					record.getQueryText());
			if (!record.processed()) {
				t.reset();
				if (!record.processed())
					evaluate(record); 
				Utility.logInfo("Total time to answer this query: " + t.duration()); 
				if (!fullReasoner && !record.processed()) {
					Utility.logInfo("The query has not been fully answered in " + t.duration() + " seconds."); 
					continue; 
				}
			}
			record.outputAnswerStatistics();
			record.outputTimes();
		}
		/* TODO it can handle one call only
		   if you call twice, you will end up with a json file with multiple roots */
		if(answerWriter != null) gson.toJson(queryRecords, answerWriter);
//		queryRecords.stream().forEach(record -> Utility.logDebug(gson.toJson(record)));
		queryRecords.stream().forEach(record -> record.dispose());
	}
	
	public void dispose() {
		if (answerWriter != null) {
			try {
				answerWriter.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
//		Utility.cleanup();
	}  

	private QueryManager m_queryManager = new QueryManager(); 
	
	public QueryManager getQueryManager() {
		return m_queryManager; 
	}


	public static QueryReasoner getHermiTReasoner(boolean toCheckSatisfiability) {
		return new HermiTReasoner(toCheckSatisfiability);
	}

}
