package uk.ac.ox.cs.pagoda.test_units;

import org.semanticweb.owlapi.model.OWLOntology;

import org.testng.annotations.Test;
import uk.ac.ox.cs.pagoda.owl.OWLHelper;
import uk.ac.ox.cs.pagoda.reasoner.QueryReasoner;
import uk.ac.ox.cs.pagoda.reasoner.QueryReasoner.Type;
import uk.ac.ox.cs.pagoda.tester.PagodaTester;
import uk.ac.ox.cs.pagoda.util.Timer;
import uk.ac.ox.cs.pagoda.util.Utility;

public class CostEvaluation {	
	
	@Test
	public void lubm100() {
		int number = 1;
		PagodaTester.main(
				PagodaTester.onto_dir + "lubm/univ-bench.owl",
				PagodaTester.onto_dir + "lubm/data/lubm" + number + ".ttl",
				PagodaTester.onto_dir + "lubm/queries/test_all_pagoda.sparql"
		); 
//		AllTests.copy("output/log4j.log", "results-backup/jair/lubm" + number + ".out"); 
	}
	
	public void lubm1000() {
		int number = 1000;
		String[] args = new String[] {
				PagodaTester.onto_dir + "lubm/univ-bench.owl",
				PagodaTester.onto_dir + "lubm/data/lubm" + number + ".ttl",
				PagodaTester.onto_dir + "lubm/queries/test_all_pagoda.sparql" 
		}; 
		OWLOntology ontology = OWLHelper.loadOntology(args[0]); 
		QueryReasoner reasoner = QueryReasoner.getInstance(Type.ELHOU, ontology, true, true); 
		Timer t = new Timer(); 
		reasoner.loadOntology(ontology);
		reasoner.importData(args[1]);
		if (!reasoner.preprocess())
			return ; 
		Utility.logInfo("Preprocessing Done in " + t.duration()	+ " seconds.");
		
		reasoner.evaluate(reasoner.getQueryManager().collectQueryRecords(args[2]));
//		AllTests.copy("output/log4j.log", "results-backup/jair/lubm" + number + ".out"); 
	}

	@Test
	public void uobm5() {
		int number = 1;
		String[] args = new String[] {
				PagodaTester.onto_dir + "uobm/univ-bench-dl.owl",
				PagodaTester.onto_dir + "uobm/data/uobm" + number + ".ttl",
				PagodaTester.onto_dir + "uobm/queries/standard_all_pagoda.sparql" 
		}; 
		PagodaTester.main(args);
//		AllTests.copy("output/log4j.log", "results-backup/jair/uobm" + number + ".out"); 
	}
	
	public void uobm100() {
		int number = 200;
		String[] args = new String[] {
				PagodaTester.onto_dir + "uobm/univ-bench-dl.owl",
				PagodaTester.onto_dir + "uobm/data/uobm" + number + ".ttl",
				PagodaTester.onto_dir + "uobm/queries/standard_group3_all.sparql" 
		}; 
		PagodaTester.main(args);
//		AllTests.copy("output/log4j.log", "results-backup/jair/uobm" + number + ".out"); 
	}
	
	public void uobm500() {
		int number = 500;
		String[] args = new String[] {
				PagodaTester.onto_dir + "uobm/univ-bench-dl.owl",
				PagodaTester.onto_dir + "uobm/data/uobm" + number + ".ttl",
				PagodaTester.onto_dir + "uobm/queries/standard_all_pagoda.sparql" 
		}; 
		
		OWLOntology ontology = OWLHelper.loadOntology(args[0]); 
		QueryReasoner reasoner = QueryReasoner.getInstance(Type.ELHOU, ontology, true, true);
		Timer t = new Timer(); 
		reasoner.loadOntology(ontology);
		reasoner.importData(args[1]);
		if (!reasoner.preprocess())
			return ; 
		Utility.logInfo("Preprocessing Done in " + t.duration()	+ " seconds.");
		
		reasoner.evaluate(reasoner.getQueryManager().collectQueryRecords(args[2]));
//		AllTests.copy("output/log4j.log", "results-backup/jair/uobm" + number + ".out"); 
	}
	
	
	public static void main(String... args) {
		args = new String[] {
				PagodaTester.onto_dir + "dbpedia/integratedOntology-all-in-one-minus-datatype.owl", 
				PagodaTester.onto_dir + "dbpedia/data/dbpedia-minus-datatype-new.ttl",
				PagodaTester.onto_dir + "dbpedia/queries/atomic_ground.sparql"
		}; 
		
		OWLOntology ontology = OWLHelper.loadOntology(args[0]); 
		QueryReasoner reasoner = QueryReasoner.getInstance(Type.ELHOU, ontology, true, true);
		Timer t = new Timer(); 
		reasoner.loadOntology(ontology);
		reasoner.importData(args[1]);
		if (!reasoner.preprocess())
			return ; 
		Utility.logInfo("Preprocessing Done in " + t.duration()	+ " seconds.");
		
		reasoner.evaluate(reasoner.getQueryManager().collectQueryRecords(args[2]));
	}
}
