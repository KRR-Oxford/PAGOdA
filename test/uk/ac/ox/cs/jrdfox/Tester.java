package uk.ac.ox.cs.jrdfox;

import java.io.File;

import org.semanticweb.owlapi.model.OWLOntology;

import uk.ac.ox.cs.JRDFox.JRDFStoreException;
import uk.ac.ox.cs.JRDFox.Prefixes;
import uk.ac.ox.cs.JRDFox.store.DataStore;
import uk.ac.ox.cs.JRDFox.store.DataStore.UpdateType;
import uk.ac.ox.cs.JRDFox.store.Parameters;
import uk.ac.ox.cs.JRDFox.store.TripleStatus;
import uk.ac.ox.cs.JRDFox.store.TupleIterator;
import uk.ac.ox.cs.JRDFox.store.DataStore.StoreType;
import uk.ac.ox.cs.pagoda.owl.OWLHelper;
import uk.ac.ox.cs.pagoda.reasoner.light.RDFoxQueryEngine;
import uk.ac.ox.cs.pagoda.util.Namespace;
import uk.ac.ox.cs.pagoda.util.Timer;

public class Tester {
	
	public static void main(String[] args) throws JRDFStoreException {
		Tester tester = new Tester();
		tester.testCrash();
	}
	
	private void evaluate_againstIDs(String queryText) throws JRDFStoreException {
		int number = 0; 
		Timer t = new Timer(); 
		TupleIterator iter = null; 
		try {
			iter = store.compileQuery(queryText, prefixes, parameters, TripleStatus.TUPLE_STATUS_IDB.union(TripleStatus.TUPLE_STATUS_EDB), TripleStatus.TUPLE_STATUS_IDB);
			for (long multi = iter.open(); multi != 0; multi = iter.getNext()) 
				++number; 
		} finally {
			if (iter != null) iter.dispose();
		}
		System.out.println(number); 
		System.out.println(t.duration());

	}

	DataStore store; 
	Prefixes prefixes = new Prefixes(); 
	Parameters parameters; 
	
	public Tester() {
		try {
			store = new DataStore(StoreType.NarrowParallelHead);
			store.setNumberOfThreads(RDFoxQueryEngine.matNoOfThreads);
			store.initialize();
			System.out.println("data store created.");
		} catch (JRDFStoreException e) {
			e.printStackTrace();
		}
		parameters = new Parameters();
		parameters.m_allAnswersInRoot = true; 
		parameters.m_useBushy = true; 
	}
	
	public Tester(String path) {
		try {
			store = new DataStore(new File(path));
		} catch (JRDFStoreException e) {
			e.printStackTrace();
		}
		parameters = new Parameters(); 
//		parameters.m_allAnswersInRoot = true; 
//		parameters.m_useBushy = true;
	}
	
	public void applyReasoning(boolean incremental) {
		Timer t = new Timer(); 
		try {
			store.applyReasoning(incremental);
		} catch (JRDFStoreException e) {
			e.printStackTrace();
		}  
		System.out.println("reasoning done: " + t.duration()); 
	}
	
	public void dispose() {
		store.dispose();
	}
	
	public void testCrash() throws JRDFStoreException {
//		DataStore lowerStore = new DataStore(StoreType.NarrowParallelHead);
//		lowerStore.setNumberOfThreads(RDFoxQueryEngine.matNoOfThreads);
//		lowerStore.initialize();
//		System.out.println("lower data store created.");
		OWLOntology ontology = OWLHelper.loadOntology("data/fly/fly_anatomy_XP_with_GJ_FC_individuals.owl");
		System.out.println("ontology loaded ... " + ontology.getAxiomCount());
		
		store.importTurtleFile(new File("testcase/fly.ttl"));
		System.out.println("data loaded. " + store.getTriplesCount());
		
		store.importRules(new File[] {new File("testcase/lower.dlog")});
		System.out.println("rules loaded. " + store.getTriplesCount());
		
		store.applyReasoning();
		System.out.println("materialised. " + store.getTriplesCount());
		
		store.clearRulesAndMakeFactsExplicit();
		
		store.importRules(new File[] {new File("testcase/multi.dlog")});
		System.out.println("rules loaded. " + store.getTriplesCount());
		
		store.applyReasoning();
		System.out.println("materialised. " + store.getTriplesCount());
		
		store.makeFactsExplicit();
		
		store.importTurtleFiles(new File[] {new File("testcase/first.ttl")}, UpdateType.ScheduleForAddition);
		System.out.println("first data loaded. " + store.getTriplesCount());
		
		store.applyReasoning(true);
		System.out.println("incremental reasoning done. " + store.getTriplesCount());
		
		store.clearRulesAndMakeFactsExplicit();
		
		store.importTurtleFiles(new File[] {new File("testcase/second.ttl")}, UpdateType.ScheduleForAddition);
		store.importRules(new File[] {new File("testcase/tracking.dlog")}, UpdateType.ScheduleForAddition);
		store.applyReasoning(true);
		System.out.println("incremental reasoning done. " + store.getTriplesCount());
		
		evaluate_againstIDs("select distinct ?z where { ?x <" + Namespace.RDF_TYPE + "> ?z . }");
		System.out.println("done."); 
//		tester.applyReasoning(true);
//		tester.evaluate_againstIDs("select distinct ?z where { ?x <" + Namespace.RDF_TYPE + "> ?z . }");
//		System.out.println("done."); 
		
		store.dispose();
//		lowerStore.dispose();
	}
	
	public void test() throws JRDFStoreException {
		evaluate("PREFIX benchmark: <http://semantics.crl.ibm.com/univ-bench-dl.owl#> " 
				+ "SELECT distinct ?x WHERE { "
				+ "?x a benchmark:Person . "
				+ "?x benchmark:like ?y . "
				+ "?z a benchmark:Chair . "
				+ "?z benchmark:isHeadOf <http://www.Department0.University0.edu> . "
				+ "?z benchmark:like ?y . "
				+ "?x a <http://www.cs.ox.ac.uk/PAGOdA/auxiliary#Original> . "
				+ "?z a <http://www.cs.ox.ac.uk/PAGOdA/auxiliary#Original> . "
				+ "?y a <http://www.cs.ox.ac.uk/PAGOdA/auxiliary#Original> }");
		
		evaluate("PREFIX benchmark: <http://semantics.crl.ibm.com/univ-bench-dl.owl#> " 
				+ "SELECT distinct ?x WHERE { "
				+ "?x a benchmark:Person . "
				+ "?x benchmark:like ?y . "
				+ "?z a benchmark:Chair . "
				+ "?z benchmark:isHeadOf <http://www.Department0.University0.edu> . "
				+ "?z benchmark:like ?y . "
				+ "?z a <http://www.cs.ox.ac.uk/PAGOdA/auxiliary#Original> . "
				+ "?y a <http://www.cs.ox.ac.uk/PAGOdA/auxiliary#Original> ."
				+ "?x a <http://www.cs.ox.ac.uk/PAGOdA/auxiliary#Original> }"); 

		evaluate("PREFIX benchmark: <http://semantics.crl.ibm.com/univ-bench-dl.owl#> " 
				+ "SELECT distinct ?x WHERE { "
				+ "?x a benchmark:Person . "
				+ "?x benchmark:like ?y . "
				+ "?z a benchmark:Chair . "
				+ "?z benchmark:isHeadOf <http://www.Department0.University0.edu> . "
				+ "?z benchmark:like ?y . "
				+ "?y a <http://www.cs.ox.ac.uk/PAGOdA/auxiliary#Original> . "
				+ "?x a <http://www.cs.ox.ac.uk/PAGOdA/auxiliary#Original> . "
				+ "?z a <http://www.cs.ox.ac.uk/PAGOdA/auxiliary#Original> }"); 
		
		evaluate("PREFIX benchmark: <http://semantics.crl.ibm.com/univ-bench-dl.owl#> " 
				+ "SELECT distinct ?x WHERE { "
				+ "?x a benchmark:Person . "
				+ "?x benchmark:like ?y . "
				+ "?z a benchmark:Chair . "
				+ "?z benchmark:isHeadOf <http://www.Department0.University0.edu> . "
				+ "?z benchmark:like ?y . "
				+ "?y a <http://www.cs.ox.ac.uk/PAGOdA/auxiliary#Original> }"); 
	}
	
	public void evaluate(String query) throws JRDFStoreException {
		int number = 0; 
		Timer t = new Timer(); 
		TupleIterator iter = null; 
		try {
			iter = store.compileQuery(query, prefixes, parameters);
			for (long multi = iter.open(); multi != 0; multi = iter.getNext()) 
				++number; 
		} finally {
			if (iter != null) iter.dispose();
		}
		System.out.println(number); 
		System.out.println(t.duration());
	}
	
}
