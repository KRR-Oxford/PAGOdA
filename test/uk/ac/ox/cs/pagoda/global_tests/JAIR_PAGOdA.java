package uk.ac.ox.cs.pagoda.global_tests;

import org.testng.annotations.Test;
import uk.ac.ox.cs.pagoda.tester.PagodaTester;
import uk.ac.ox.cs.pagoda.util.TestUtil;

import java.io.IOException;

public class JAIR_PAGOdA {

	public static void main(String... args) {
		try {
			new JAIR_PAGOdA().lubm1();
		} catch(IOException e) {
			e.printStackTrace();
		}
	}

	@Test
	public void lubm1() throws IOException {
		String ontoDir = TestUtil.getConfig().getProperty("ontoDir");
		String[] args = new String[] {
				TestUtil.combinePaths(ontoDir, "lubm/univ-bench.owl"),
				TestUtil.combinePaths(ontoDir, "lubm/data/lubm1.ttl"),
				TestUtil.combinePaths(ontoDir, "lubm/queries/answersCorrectness.sparql")
		};
		PagodaTester.main(args);
		TestUtil.copyFile("output/log4j.log", "/home/yzhou/java-workspace/answersCorrectness-share/results_new/lubm1/pagoda");
	}

	@Test
	public void lubm1_conj() throws IOException {
		String ontoDir = TestUtil.getConfig().getProperty("ontoDir");
		String[] args = new String[] {
				TestUtil.combinePaths(ontoDir, "lubm/univ-bench.owl"),
				TestUtil.combinePaths(ontoDir, "lubm/data/lubm1.ttl"),
				TestUtil.combinePaths(ontoDir, "lubm/queries/test_pellet.sparql")
		};
		PagodaTester.main(args);
		TestUtil.copyFile("output/log4j.log", "/home/yzhou/java-workspace/answersCorrectness-share/results_new/lubm1/pagoda_conj");
	}

	@Test
	public void lubm1_rolledUp() throws IOException {
		String ontoDir = TestUtil.getConfig().getProperty("ontoDir");
		PagodaTester.main(
				"/home/yzhou/backup/20141212/univ-bench-queries.owl",
				TestUtil.combinePaths(ontoDir, "lubm/data/lubm1.ttl"),
				TestUtil.combinePaths(ontoDir, "lubm/queries/atomic_lubm.sparql")
				);
		TestUtil.copyFile("output/log4j.log", "/home/yzhou/java-workspace/answersCorrectness-share/results_new/lubm1/pagoda_rolledUp");
	}

	@Test
	public void uobm1() throws IOException {
		String ontoDir = TestUtil.getConfig().getProperty("ontoDir");
		String[] args = new String[] {
				TestUtil.combinePaths(ontoDir, "uobm/univ-bench-dl.owl"),
				TestUtil.combinePaths(ontoDir, "uobm/data/uobm1.ttl"),
				TestUtil.combinePaths(ontoDir, "uobm/queries/standard.sparql")
		};
		PagodaTester.main(args);
		TestUtil.copyFile("output/log4j.log", "/home/yzhou/java-workspace/answersCorrectness-share/results_new/uobm1/pagoda");
	}

	@Test
	public void uobm1_conj() throws IOException {
		String ontoDir = TestUtil.getConfig().getProperty("ontoDir");
		String[] args = new String[] {
				TestUtil.combinePaths(ontoDir, "uobm/univ-bench-dl.owl"),
				TestUtil.combinePaths(ontoDir, "uobm/data/uobm1.ttl"),
				TestUtil.combinePaths(ontoDir, "uobm/queries/standard_pellet.sparql")
		};
		PagodaTester.main(args);
		TestUtil.copyFile("output/log4j.log", "/home/yzhou/java-workspace/answersCorrectness-share/results_new/uobm1/pagoda_conj");
	}

	@Test
	public void uobm1_rolledUp() {
		String ontoDir = TestUtil.getConfig().getProperty("ontoDir");
		String[] args = new String[] {
				"/home/yzhou/backup/20141212/univ-bench-dl-queries.owl",
				TestUtil.combinePaths(ontoDir, "uobm/data/uobm1.ttl"),
				TestUtil.combinePaths(ontoDir, "uobm/queries/atomic_uobm.sparql")
		};
		PagodaTester.main(args);
//		TestUtil.copyFile(("output/log4j.log", "/home/yzhou/java-workspace/answersCorrectness-share/results_new/uobm1/pagoda_rolledUp");
	}

	@Test
	public void fly() {
		String ontoDir = TestUtil.getConfig().getProperty("ontoDir");
		String[] args = new String[] {
				TestUtil.combinePaths(ontoDir, "fly/fly_anatomy_XP_with_GJ_FC_individuals.owl"),
				null,
				TestUtil.combinePaths(ontoDir, "fly/queries/fly_pellet.sparql")
		};
		PagodaTester.main(args);
//		TestUtil.copyFile(("output/log4j.log", "/home/yzhou/java-workspace/answersCorrectness-share/results_new/fly/pagoda");
	}

	@Test
	public void fly_conj() throws IOException {
		String ontoDir = TestUtil.getConfig().getProperty("ontoDir");
		String[] args = new String[] {
				TestUtil.combinePaths(ontoDir, "fly/fly_anatomy_XP_with_GJ_FC_individuals.owl"),
				null,
				TestUtil.combinePaths(ontoDir, "fly/queries/fly_pellet.sparql")
		};
		PagodaTester.main(args);
		TestUtil.copyFile("output/log4j.log", "/home/yzhou/java-workspace/answersCorrectness-share/results_new/fly/pagoda_conj");
	}

	public void fly_rolledUp() {
		String ontoDir = TestUtil.getConfig().getProperty("ontoDir");
		PagodaTester.main(
//				TestUtil.combinePaths(ontoDir, "fly/fly_anatomy_XP_with_GJ_FC_individuals.owl",
				TestUtil.combinePaths(ontoDir, "fly/fly-all-in-one_rolledUp.owl"),
				null,
				TestUtil.combinePaths(ontoDir, "fly/queries/fly_atomic.sparql")
		);
//		TestUtil.copyFile("output/log4j.log", "/home/yzhou/java-workspace/answersCorrectness-share/results_new/fly/pagoda_rolledUp");
	}

	public void dbpedia() {
		String ontoDir = TestUtil.getConfig().getProperty("ontoDir");
		PagodaTester.main(
				TestUtil.combinePaths(ontoDir, "dbpedia/integratedOntology-all-in-one-minus-datatype.owl"),
				TestUtil.combinePaths(ontoDir, "dbpedia/data/dbpedia-minus-datatype-new.ttl"),
				TestUtil.combinePaths(ontoDir, "dbpedia/queries/atomic_ground.sparql"),
				"dbpedia.ans"
		);

//		TestUtil.copyFile("output/log4j.log", "/home/yzhou/java-workspace/answersCorrectness-share/results_new/dbpedia/pagoda");
	}

	public void npd() {
		String ontoDir = TestUtil.getConfig().getProperty("ontoDir");
		PagodaTester.main(
				TestUtil.combinePaths(ontoDir, "npd/npd-all-minus-datatype.owl"),
				TestUtil.combinePaths(ontoDir, "npd/data/npd-data-dump-minus-datatype-new.ttl"),
				TestUtil.combinePaths(ontoDir, "npd/queries/atomic_ground.sparql")
				, "npd.ans"
		);

//		TestUtil.copyFile("output/log4j.log", "/home/yzhou/java-workspace/answersCorrectness-share/results_new/npd/pagoda");
	}

	public void reactome() throws IOException {
		String ontoDir = TestUtil.getConfig().getProperty("ontoDir");
		PagodaTester.main(
				TestUtil.combinePaths(ontoDir, "bio2rdf/reactome/biopax-level3-processed.owl"),
				TestUtil.combinePaths(ontoDir, "bio2rdf/reactome/graph sampling/reactome_sample_10.ttl"),
//				null,
//				TestUtil.combinePaths(ontoDir, "bio2rdf/reactome/queries/atomic_ground.sparql")
				TestUtil.combinePaths(ontoDir, "bio2rdf/reactome/queries/example.sparql")
				, "pagoda_reactome.ans"
		);
		TestUtil.copyFile("log4j.log", "output/jair/pagoda_reactome.example");

//		TestUtil.copyFile(("output/log4j.log", "/home/yzhou/java-workspace/answersCorrectness-share/results_new/reactome/pagoda_10p");
	}

	public void chembl() throws IOException {
		String ontoDir = TestUtil.getConfig().getProperty("ontoDir");
		PagodaTester.main(
				TestUtil.combinePaths(ontoDir, "bio2rdf/chembl/cco-noDPR.ttl"),
				TestUtil.combinePaths(ontoDir, "bio2rdf/chembl/graph sampling/sample_1.nt"),
//				TestUtil.combinePaths(ontoDir, "bio2rdf/chembl/queries/atomic_ground.sparql")
				TestUtil.combinePaths(ontoDir, "bio2rdf/chembl/queries/example.sparql")
				, "pagoda_chembl.ans"
		);
		TestUtil.copyFile("log4j.log", "output/jair/pagoda_chembl.example");
//		TestUtil.copyFile("output/log4j.log", "/home/yzhou/java-workspace/answersCorrectness-share/results_new/chembl/pagoda_1p");
	}

	public void uniprot() throws IOException {
		String ontoDir = TestUtil.getConfig().getProperty("ontoDir");
		PagodaTester.main(
				TestUtil.combinePaths(ontoDir, "bio2rdf/uniprot/core-sat-processed.owl"),
				TestUtil.combinePaths(ontoDir, "bio2rdf/uniprot/graph sampling/sample_1.nt"),
//				null,
//				TestUtil.combinePaths(ontoDir, "bio2rdf/uniprot/queries/atomic_ground.sparql")
				TestUtil.combinePaths(ontoDir, "bio2rdf/uniprot/queries/example.sparql")
				, "pagoda_uniprot.ans"
		);
		TestUtil.copyFile("log4j.log", "output/jair/pagoda_uniprot.example");
//		TestUtil.copyFile("output/log4j.log", "/home/yzhou/java-workspace/answersCorrectness-share/results_new/uniprot/pagoda_1p");
	}

}
