package uk.ac.ox.cs.pagoda.global_tests;

import org.testng.annotations.Test;
import uk.ac.ox.cs.pagoda.tester.PagodaTester;
import uk.ac.ox.cs.pagoda.util.Properties;
import uk.ac.ox.cs.pagoda.util.TestUtil;

import java.io.IOException;

public class JAIR_Scalability {
	
	private static final String date = "_0123";

	public static void main(String... args) throws IOException {
		Properties.shellModeDefault = true;
		new JAIR_Scalability().testUniProt(50, false);
	}
	
	@Test
	public void reactome() throws IOException {
		testReactome(10, false);
	}

	@Test
	public void chembl() throws IOException {
		testChEMBL(1, false);
	}

	@Test
	public void uniprot() throws IOException {
		testUniProt(1, false);
	}

	public void testReactome(int percentage, boolean save) throws IOException {
		String ontoDir = TestUtil.getConfig().getProperty("ontoDir");
		String[] args = new String[] {
				TestUtil.combinePaths(ontoDir, "bio2rdf/reactome/biopax-level3-processed.owl"),
				TestUtil.combinePaths(ontoDir, "bio2rdf/reactome/graph sampling/simplifed_sample_" + percentage + ".ttl"),
				TestUtil.combinePaths(ontoDir, "bio2rdf/reactome/queries/answersCorrectness.sparql")
				, "reactome.ans"
		};
		if (percentage == 10)
			args[1] = args[1].replace("simplifed", "reactome");

		PagodaTester.main(args);
		if (save)
			TestUtil.copyFile("log4j.log", "/home/yzhou/java-workspace/answersCorrectness-share/results_new/reactome/pagoda_" + percentage + "p" + date);
	}
	
	public void testChEMBL(int percentage, boolean save) throws IOException {
		String ontoDir = TestUtil.getConfig().getProperty("ontoDir");
		String[] args = new String[] {
				TestUtil.combinePaths(ontoDir, "bio2rdf/chembl/cco-noDPR.ttl"),
				TestUtil.combinePaths(ontoDir, "bio2rdf/chembl/sample_" + percentage + ".nt"),
//				TestUtil.combinePaths(ontoDir, "bio2rdf/chembl/queries/atomic_ground.sparql")
				TestUtil.combinePaths(ontoDir, "bio2rdf/chembl/queries/answersCorrectness.sparql")
				, "chembl.ans"
		};
		if (percentage == 1 || percentage == 10 || percentage == 50)
			args[1] = args[1].replace("chembl", "chembl/graph sampling");
		else
			if (percentage == 100)
				args[1] = "/home/yzhou/RDFData/ChEMBL/facts/ChEMBL.ttl";

		PagodaTester.main(args);
		if (save)
			TestUtil.copyFile("log4j.log", "/home/yzhou/java-workspace/answersCorrectness-share/results_new/chembl/pagoda_" + percentage + "p" + date);
	}
	
	public void testUniProt(int percentage, boolean save) throws IOException {
		String ontoDir = TestUtil.getConfig().getProperty("ontoDir");
		String[] args = new String[] {
				TestUtil.combinePaths(ontoDir, "bio2rdf/uniprot/core-sat-processed.owl"),
				TestUtil.combinePaths(ontoDir, "bio2rdf/uniprot/sample_" + percentage + ".nt"),
//				TestUtil.combinePaths(ontoDir, "bio2rdf/uniprot/queries/atomic_ground.sparql")
				TestUtil.combinePaths(ontoDir, "bio2rdf/uniprot/queries/answersCorrectness.sparql")
				, "uniprot.ans"
		};

		if (percentage == 1 || percentage == 10 || percentage == 50)
			args[1] = args[1].replace("uniprot", "uniprot/graph sampling");
		else
			if (percentage == 100)
				args[1] = "/home/yzhou/krr-nas-share/Yujiao/ontologies/bio2rdf/uniprot/data/uniprot_cleaned.nt";

		PagodaTester.main(args);
		if (save)
			TestUtil.copyFile("log4j.log", "/home/yzhou/java-workspace/answersCorrectness-share/results_new/uniprot/pagoda_" + percentage + "p" + date);
	}

}
