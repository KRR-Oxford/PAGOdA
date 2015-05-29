package uk.ac.ox.cs.pagoda.reasoner;

import com.google.gson.Gson;
import org.semanticweb.owlapi.model.OWLOntology;
import uk.ac.ox.cs.pagoda.owl.OWLHelper;
import uk.ac.ox.cs.pagoda.query.AnswerTuples;
import uk.ac.ox.cs.pagoda.query.QueryManager;
import uk.ac.ox.cs.pagoda.query.QueryRecord;
import uk.ac.ox.cs.pagoda.util.PagodaProperties;
import uk.ac.ox.cs.pagoda.util.Timer;
import uk.ac.ox.cs.pagoda.util.Utility;
import uk.ac.ox.cs.pagoda.util.disposable.Disposable;
import uk.ac.ox.cs.pagoda.util.disposable.DisposedException;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Collection;

// TODO clean APIs
public abstract class QueryReasoner extends Disposable {

    public static final String ImportDataFileSeparator = ";";
    private static final boolean DEFAULT_MULTI_STAGES = true;
    private static final boolean DEFAULT_EQUALITIES = true;
    public boolean fullReasoner = this instanceof MyQueryReasoner;
    protected StringBuilder importedData = new StringBuilder();
    //	protected boolean forSemFacet = false;
    PagodaProperties properties;
    BufferedWriter answerWriter = null;
    private QueryManager m_queryManager = new QueryManager();

    public static QueryReasoner getInstance(PagodaProperties p) {
        OWLOntology ontology = OWLHelper.loadOntology(p.getOntologyPath());
        QueryReasoner pagoda = getInstance(ontology, p);
        pagoda.properties = p;
        pagoda.loadOntology(ontology);
        pagoda.importData(p.getDataPath());
        if(pagoda.preprocess()) {
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
        QueryReasoner pagoda = getInstance(Type.Full, o, DEFAULT_MULTI_STAGES, DEFAULT_EQUALITIES);
        pagoda.properties = new PagodaProperties();
        return pagoda;
    }

    private static QueryReasoner getInstance(OWLOntology o, PagodaProperties p) {
        return getInstance(Type.Full, o, DEFAULT_MULTI_STAGES, DEFAULT_EQUALITIES);
    }

    public static QueryReasoner getInstance(Type type, OWLOntology o, boolean performMultiStages, boolean considerEqualities) {
//		Utility.initialise();
        QueryReasoner reasoner;
        if(OWLHelper.isInOWL2RL(o)) reasoner = new RLQueryReasoner();
        else if(OWLHelper.isInELHO(o)) reasoner = new ELHOQueryReasoner();
        else
            switch(type) {
                case RLU:
                    reasoner = new RLUQueryReasoner(performMultiStages, considerEqualities);
                    break;
                case ELHOU:
                    reasoner = new ELHOUQueryReasoner(performMultiStages, considerEqualities);
                    break;
                default:
                    reasoner = new MyQueryReasoner(performMultiStages, considerEqualities);
            }
        return reasoner;
    }

    public static QueryReasoner getHermiTReasoner(boolean toCheckSatisfiability) {
        return new HermiTReasoner(toCheckSatisfiability);
    }

    public void setToClassify(boolean flag) {
        if(isDisposed()) throw new DisposedException();
        properties.setToClassify(flag);
    }

    public void setToCallHermiT(boolean flag) {
        if(isDisposed()) throw new DisposedException();
        properties.setToCallHermiT(flag);
    }

    public void importData(String datafile) {
        if(isDisposed()) throw new DisposedException();
        if(datafile != null && !datafile.equalsIgnoreCase("null"))
            importData(datafile.split(ImportDataFileSeparator));
    }

    public void importData(String[] datafiles) {
        if(isDisposed()) throw new DisposedException();
        if(datafiles != null) {
            for(String datafile : datafiles) {
                File file = new File(datafile);
                if(file.exists()) {
                    if(file.isFile()) importDataFile(file);
                    else importDataDirectory(file);
                }
                else {
                    Utility.logError("warning: file " + datafile + " doesn't exists.");
                }
            }
        }
    }

    public abstract void loadOntology(OWLOntology ontology);

    public abstract boolean preprocess();

    public abstract boolean isConsistent();

    public abstract void evaluate(QueryRecord record);

    public abstract void evaluateUpper(QueryRecord record);

    public AnswerTuples evaluate(String queryText, boolean forFacetGeneration) {
        if(isDisposed()) throw new DisposedException();
        if(forFacetGeneration) {
            QueryRecord record = m_queryManager.create(queryText);
            Utility.logInfo("---------- start evaluating upper bound for Query " + record.getQueryID() + " ----------", queryText);
            if(!record.isProcessed())
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
        if(isDisposed()) throw new DisposedException();
        QueryRecord record = m_queryManager.create(queryText);
        Utility.logInfo("---------- start evaluating Query " + record.getQueryID() + " ----------", queryText);
        if(!record.isProcessed())
            evaluate(record);
        AnswerTuples answer = record.getAnswers();
        record.dispose();
        return answer;

    }

    public void evaluate_shell(String queryText) {
        if(isDisposed()) throw new DisposedException();
        QueryRecord record = m_queryManager.create(queryText);
        Utility.logInfo("---------- start evaluating Query " + record.getQueryID() + " ----------", queryText);
        if(!record.isProcessed())
            evaluate(record);
        Utility.logInfo("Answers to this query: ", record.outputSoundAnswerTuple());
        record.dispose();

    }

    public void evaluate(Collection<QueryRecord> queryRecords) {
        if(isDisposed()) throw new DisposedException();
        if(!isConsistent()) {
            Utility.logDebug("The ontology and dataset is inconsistent.");
            return;
        }

        if(properties.getAnswerPath() != null && answerWriter == null) {
            try {
                answerWriter = Files.newBufferedWriter(Paths.get(properties.getAnswerPath()));
            } catch(IOException e) {
                Utility.logError("The answer path is not valid!");
                e.printStackTrace();
            }
        }

        Timer t = new Timer();
        Gson gson = QueryRecord.GsonCreator.getInstance();
        for(QueryRecord record : queryRecords) {
//			if (Integer.parseInt(record.getQueryID()) != 218) continue;
            Utility.logInfo("---------- start evaluating Query " + record.getQueryID() + " ----------",
                            record.getQueryText());
            if(!record.isProcessed()) {
                t.reset();
                if(!record.isProcessed())
                    evaluate(record);
                Utility.logInfo("Total time to answer this query: " + t.duration());
                if(!fullReasoner && !record.isProcessed()) {
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

//	public void evaluate(Collection<QueryRecord> queryRecords) {
//		evaluate(queryRecords);
//	}

    @Override
    public void dispose() {
        super.dispose();
        if(answerWriter != null) {
            try {
                answerWriter.close();
            } catch(IOException e) {
                e.printStackTrace();
            }
        }
//		Utility.cleanup();
    }

    public QueryManager getQueryManager() {
        if(isDisposed()) throw new DisposedException();
        return m_queryManager;
    }

    private void importDataDirectory(File file) {
        for(File child : file.listFiles())
            if(child.isFile()) importDataFile(child);
            else importDataDirectory(child);
    }

    private void importDataFile(File file) {
        String datafile;
        try {
            datafile = file.getCanonicalPath();
        } catch(IOException e) {
            e.printStackTrace();
            return;
        }
        importDataFile(datafile);
    }

    protected final void importDataFile(String datafile) {
        if(importedData.length() == 0)
            importedData.append(datafile);
        else
            importedData.append(ImportDataFileSeparator).append(datafile);

    }


    public enum Type {Full, RLU, ELHOU}

}
