package uk.ac.ox.cs.pagoda.test_units;

import org.testng.annotations.Test;
import uk.ac.ox.cs.pagoda.tester.PagodaTester;
import uk.ac.ox.cs.pagoda.util.Utility;

import java.io.IOException;

public class JAIR_PAGOdA {

	@Test
	public void lubm1() throws IOException {
		String[] args = new String[] {
				PagodaTester.onto_dir + "lubm/univ-bench.owl",
				PagodaTester.onto_dir + "lubm/data/lubm1.ttl",
				PagodaTester.onto_dir + "lubm/queries/test.sparql" 
		};
		PagodaTester.main(args);
		Utility.copyFile("output/log4j.log", "/home/yzhou/java-workspace/test-share/results_new/lubm1/pagoda");
	}

	@Test
	public void lubm1_conj() throws IOException {
		String[] args = new String[] {
				PagodaTester.onto_dir + "lubm/univ-bench.owl",
				PagodaTester.onto_dir + "lubm/data/lubm1.ttl",
				PagodaTester.onto_dir + "lubm/queries/test_pellet.sparql"
		};
		PagodaTester.main(args);
		Utility.copyFile("output/log4j.log", "/home/yzhou/java-workspace/test-share/results_new/lubm1/pagoda_conj");
	}

	@Test
	public void lubm1_rolledUp() throws IOException {
		String[] args = new String[] {
				"/home/yzhou/backup/20141212/univ-bench-queries.owl",
				PagodaTester.onto_dir + "lubm/data/lubm1.ttl",
				PagodaTester.onto_dir + "lubm/queries/atomic_lubm.sparql"
		};
		PagodaTester.main(args);
		Utility.copyFile("output/log4j.log", "/home/yzhou/java-workspace/test-share/results_new/lubm1/pagoda_rolledUp");
	}

	@Test
	public void uobm1() throws IOException {
		String[] args = new String[] {
				PagodaTester.onto_dir + "uobm/univ-bench-dl.owl",
				PagodaTester.onto_dir + "uobm/data/uobm1.ttl",
				PagodaTester.onto_dir + "uobm/queries/standard.sparql"
		};
		PagodaTester.main(args);
		Utility.copyFile("output/log4j.log", "/home/yzhou/java-workspace/test-share/results_new/uobm1/pagoda");
	}

	@Test
	public void uobm1_conj() throws IOException {
		String[] args = new String[] {
				PagodaTester.onto_dir + "uobm/univ-bench-dl.owl",
				PagodaTester.onto_dir + "uobm/data/uobm1.ttl",
				PagodaTester.onto_dir + "uobm/queries/standard_pellet.sparql"
		};
		PagodaTester.main(args);
		Utility.copyFile("output/log4j.log", "/home/yzhou/java-workspace/test-share/results_new/uobm1/pagoda_conj");
	}

	@Test
	public void uobm1_rolledUp() {
		String[] args = new String[] {
				"/home/yzhou/backup/20141212/univ-bench-dl-queries.owl",
				PagodaTester.onto_dir + "uobm/data/uobm1.ttl",
				PagodaTester.onto_dir + "uobm/queries/atomic_uobm.sparql"
		};
		PagodaTester.main(args);
//		AllTests.copy("output/log4j.log", "/home/yzhou/java-workspace/test-share/results_new/uobm1/pagoda_rolledUp");
	}

	@Test
	public void fly() {
		String[] args = new String[] {
				PagodaTester.onto_dir + "fly/fly_anatomy_XP_with_GJ_FC_individuals.owl",
				null,
				PagodaTester.onto_dir + "fly/queries/fly_pellet.sparql"
		};
		PagodaTester.main(args);
//		AllTests.copy("output/log4j.log", "/home/yzhou/java-workspace/test-share/results_new/fly/pagoda");
	}

	@Test
	public void fly_conj() throws IOException {
		String[] args = new String[] {
				PagodaTester.onto_dir + "fly/fly_anatomy_XP_with_GJ_FC_individuals.owl",
				null,
				PagodaTester.onto_dir + "fly/queries/fly_pellet.sparql"
		};
		PagodaTester.main(args);
		Utility.copyFile("output/log4j.log", "/home/yzhou/java-workspace/test-share/results_new/fly/pagoda_conj");
	}


	public void fly_rolledUp() {
		PagodaTester.main(new String[] {
//				PagodaTester.onto_dir + "fly/fly_anatomy_XP_with_GJ_FC_individuals.owl",
				PagodaTester.onto_dir + "fly/fly-all-in-one_rolledUp.owl",
				null,
				PagodaTester.onto_dir + "fly/queries/fly_atomic.sparql"
		});
//		Utility.copyFile("output/log4j.log", "/home/yzhou/java-workspace/test-share/results_new/fly/pagoda_rolledUp");
	}

	public void dbpedia() {
		PagodaTester.main(
				PagodaTester.onto_dir + "dbpedia/integratedOntology-all-in-one-minus-datatype.owl",
				PagodaTester.onto_dir + "dbpedia/data/dbpedia-minus-datatype-new.ttl",
				PagodaTester.onto_dir + "dbpedia/queries/atomic_ground.sparql"
				, "dbpedia.ans"
		);

//		Utility.copyFile("output/log4j.log", "/home/yzhou/java-workspace/test-share/results_new/dbpedia/pagoda");
	}

	public void npd() {
		PagodaTester.main(
				PagodaTester.onto_dir + "npd/npd-all-minus-datatype.owl",
				PagodaTester.onto_dir + "npd/data/npd-data-dump-minus-datatype-new.ttl",
				PagodaTester.onto_dir + "npd/queries/atomic_ground.sparql"
				, "npd.ans"
		);

//		Utility.copyFile("output/log4j.log", "/home/yzhou/java-workspace/test-share/results_new/npd/pagoda");
	}

	public void reactome() throws IOException {
		PagodaTester.main(
				PagodaTester.onto_dir + "bio2rdf/reactome/biopax-level3-processed.owl",
				PagodaTester.onto_dir + "bio2rdf/reactome/graph sampling/reactome_sample_10.ttl",
//				null,
//				PagodaTester.onto_dir + "bio2rdf/reactome/queries/atomic_ground.sparql"
				PagodaTester.onto_dir + "bio2rdf/reactome/queries/example.sparql"
				, "pagoda_reactome.ans"
		);
		Utility.copyFile("log4j.log", "output/jair/pagoda_reactome.example");

//		AllTests.copy("output/log4j.log", "/home/yzhou/java-workspace/test-share/results_new/reactome/pagoda_10p");
	}

	public void chembl() throws IOException {
		PagodaTester.main(
				PagodaTester.onto_dir + "bio2rdf/chembl/cco-noDPR.ttl",
				PagodaTester.onto_dir + "bio2rdf/chembl/graph sampling/sample_1.nt",
//				PagodaTester.onto_dir + "bio2rdf/chembl/queries/atomic_ground.sparql"
				PagodaTester.onto_dir + "bio2rdf/chembl/queries/example.sparql"
				, "pagoda_chembl.ans"
		);
		Utility.copyFile("log4j.log", "output/jair/pagoda_chembl.example");
//		Utility.copyFile("output/log4j.log", "/home/yzhou/java-workspace/test-share/results_new/chembl/pagoda_1p");
	}

	public void uniprot() throws IOException {
		PagodaTester.main(
				PagodaTester.onto_dir + "bio2rdf/uniprot/core-sat-processed.owl",
				PagodaTester.onto_dir + "bio2rdf/uniprot/graph sampling/sample_1.nt",
//				null,
//				PagodaTester.onto_dir + "bio2rdf/uniprot/queries/atomic_ground.sparql"
				PagodaTester.onto_dir + "bio2rdf/uniprot/queries/example.sparql"
				, "pagoda_uniprot.ans"
		);
		Utility.copyFile("log4j.log", "output/jair/pagoda_uniprot.example");
//		AllTests.copy("output/log4j.log", "/home/yzhou/java-workspace/test-share/results_new/uniprot/pagoda_1p");
	}

	
	public static void main(String... args) {
		try {
			new JAIR_PAGOdA().lubm1();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
