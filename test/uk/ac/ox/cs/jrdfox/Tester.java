package uk.ac.ox.cs.jrdfox;

import java.io.File;

import uk.ac.ox.cs.JRDFox.JRDFStoreException;
import uk.ac.ox.cs.JRDFox.Prefixes;
import uk.ac.ox.cs.JRDFox.store.DataStore;
import uk.ac.ox.cs.JRDFox.store.Parameters;
import uk.ac.ox.cs.JRDFox.store.TupleIterator;
import uk.ac.ox.cs.pagoda.util.Timer;

public class Tester {
	
	public static void main(String[] args) {
		try {
			(new Tester()).test();;
		} catch (JRDFStoreException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	DataStore store; 
	Prefixes prefixes = new Prefixes(); 
	Parameters parameters; 
	
	public Tester() {
		try {
			store = new DataStore(new File("lazy-upper-bound"));
		} catch (JRDFStoreException e) {
			e.printStackTrace();
		}
		parameters = new Parameters(); 
		parameters.m_allAnswersInRoot = true; 
		parameters.m_useBushy = true;
		
	}
	
	public void dispose() {
		store.dispose();
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
		TupleIterator iter = store.compileQuery(query, prefixes, parameters);
		
		int number = 0; 
		Timer t = new Timer(); 
		try {
			for (long multi = iter.open(); multi != 0; multi = iter.getNext()) 
				++number; 
		} finally {
			iter.dispose();
		}
		System.out.println(number); 
		System.out.println(t.duration());
	}
	
}
