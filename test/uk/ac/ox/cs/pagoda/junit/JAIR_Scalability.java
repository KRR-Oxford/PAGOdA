package uk.ac.ox.cs.pagoda.junit;

import org.junit.Test;

import uk.ac.ox.cs.pagoda.tester.PagodaTester;

public class JAIR_Scalability {
	
	private static final String date = "_0123"; 

	@Test
	public void reactome() {
		testReactome(10, false);
	}
	
	@Test
	public void chembl() {
		testChEMBL(1, false);
	}

	@Test
	public void uniprot() {
		testUniProt(1, false);
	}

	public void testReactome(int percentage, boolean save) {
		String[] args = new String[] {
				PagodaTester.onto_dir + "bio2rdf/reactome/biopax-level3-processed.owl", 
				PagodaTester.onto_dir + "bio2rdf/reactome/graph sampling/simplifed_sample_" + percentage + ".ttl", 
				PagodaTester.onto_dir + "bio2rdf/reactome/queries/test.sparql"
				, "reactome.ans"
		}; 
		if (percentage == 10)
			args[1] = args[1].replace("simplifed", "reactome");
		
		PagodaTester.main(args); 
		if (save)
			AllTests.copy("log4j.log", "/home/yzhou/java-workspace/test-share/results_new/reactome/pagoda_" + percentage + "p" + date); 
	}
	
	public void testChEMBL(int percentage, boolean save) {
		String[] args = new String[] {
				PagodaTester.onto_dir + "bio2rdf/chembl/cco-noDPR.ttl", 
				PagodaTester.onto_dir + "bio2rdf/chembl/sample_" + percentage + ".nt",
//				PagodaTester.onto_dir + "bio2rdf/chembl/queries/atomic_ground.sparql"
				PagodaTester.onto_dir + "bio2rdf/chembl/queries/test.sparql"
				, "chembl.ans"
		}; 
		if (percentage == 1 || percentage == 10 || percentage == 50)
			args[1] = args[1].replace("chembl", "chembl/graph sampling");
		else 
			if (percentage == 100)
				args[1] = "/home/yzhou/RDFData/ChEMBL/facts/ChEMBL.ttl"; 
		
		PagodaTester.main(args);
		if (save)
		AllTests.copy("log4j.log", "/home/yzhou/java-workspace/test-share/results_new/chembl/pagoda_" + percentage + "p" + date); 
	}
	
	public void testUniProt(int percentage, boolean save) {
		String[] args = new String[] {
				PagodaTester.onto_dir + "bio2rdf/uniprot/core-sat-processed.owl", 
				PagodaTester.onto_dir + "bio2rdf/uniprot/sample_" + percentage + ".nt",
//				PagodaTester.onto_dir + "bio2rdf/uniprot/queries/atomic_ground.sparql"
				PagodaTester.onto_dir + "bio2rdf/uniprot/queries/test.sparql"
				, "uniprot.ans"
		}; 
		
		if (percentage == 1 || percentage == 10 || percentage == 50)
			args[1] = args[1].replace("uniprot", "uniprot/graph sampling");
		else 
			if (percentage == 100)
				args[1] = "/home/yzhou/krr-nas-share/Yujiao/ontologies/bio2rdf/uniprot/data/uniprot_cleaned.nt"; 
		
		PagodaTester.main(args); 
		if (save)
			AllTests.copy("log4j.log", "/home/yzhou/java-workspace/test-share/results_new/uniprot/pagoda_" + percentage + "p" + date); 
	}
	
	public static void main(String... args) {
		PagodaTester.ShellMode = true; 
		new JAIR_Scalability().testUniProt(50, false);
	}

}
