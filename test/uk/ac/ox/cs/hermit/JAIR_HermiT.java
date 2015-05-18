package uk.ac.ox.cs.hermit;

import org.junit.Test;
import uk.ac.ox.cs.pagoda.util.TestUtil;

public class JAIR_HermiT {

	@Test
	public void lubm1() throws Exception {
		String ontoDir = TestUtil.getConfig().getProperty("ontoDir");
		String[] args = new String[] {
				TestUtil.combinePaths(ontoDir, "lubm/univ-bench.owl"),
				TestUtil.combinePaths(ontoDir, "lubm/data/lubm1_owl"),
			  	TestUtil.combinePaths(ontoDir, "lubm/queries/test.sparql")
//				, "/home/yzhou/java-workspace/test-share/results_new/lubm1/hermit"
		};
		HermitQueryReasoner.main(args);
	}
	
	@Test
	public void lubm1_rolledUp() throws Exception {
		String ontoDir = TestUtil.getConfig().getProperty("ontoDir");
		String[] args = new String[] {
				"/home/yzhou/backup/20141212/univ-bench-queries.owl",
				TestUtil.combinePaths(ontoDir, "lubm/data/lubm1_owl"),
				TestUtil.combinePaths(ontoDir, "lubm/queries/atomic_lubm.sparql")
//				, "/home/yzhou/java-workspace/test-share/results_new/lubm1/hermit_rolledUp"
		};
		HermitQueryReasoner.main(args);
	}
	
	@Test
	public void uobm1() throws Exception {
		String ontoDir = TestUtil.getConfig().getProperty("ontoDir");
		String[] args = new String[] {
				TestUtil.combinePaths(ontoDir, "uobm/univ-bench-dl.owl"),
				TestUtil.combinePaths(ontoDir, "uobm/data/uobm1_owl_withDeclaration"),
				TestUtil.combinePaths(ontoDir, "uobm/queries/standard.sparql")
//				, "hermit_uobm1.out"
//				, "/home/yzhou/java-workspace/test-share/results_new/uobm1/hermit"
		};
		HermitQueryReasoner.main(args);
	}
	
	@Test
	public void uobm1_rolledUp() throws Exception {
		String ontoDir = TestUtil.getConfig().getProperty("ontoDir");
		String[] args = new String[] {
				"/home/yzhou/backup/20141212/univ-bench-dl-queries.owl",
				TestUtil.combinePaths(ontoDir, "uobm/data/uobm1_owl_withDeclaration"),
				TestUtil.combinePaths(ontoDir, "uobm/queries/atomic_uobm.sparql")
				, "hermit_uobm1_rolledUp.out"
//				, "/home/yzhou/java-workspace/test-share/results_new/uobm1/hermit_rolledUp"
		};
		HermitQueryReasoner.main(args);
	}
	
	@Test
	public void fly_rolledUp() throws Exception {
		String ontoDir = TestUtil.getConfig().getProperty("ontoDir");
		HermitQueryReasoner.main(
				TestUtil.combinePaths(ontoDir, "fly/fly-all-in-one_rolledUp.owl"),
//				TestUtil.combinePaths(ontoDir, "fly/fly_anatomy_XP_with_GJ_FC_individuals.owl",
				null, 
				TestUtil.combinePaths(ontoDir, "fly/queries/fly_atomic.sparql")
				, "hermit_fly.out"
		);
	}
	
	@Test 
	public void npd() throws Exception {
		String ontoDir = TestUtil.getConfig().getProperty("ontoDir");
		HermitQueryReasoner.main(
				TestUtil.combinePaths(ontoDir, "npd/npd-all-minus-datatype.owl"),
				TestUtil.combinePaths(ontoDir, "npd/data/npd-data-dump-minus-datatype-new.ttl"),
				TestUtil.combinePaths(ontoDir, "npd/queries/atomic_ground.sparql")
				, "hermit_npd.out"
//				, "/home/yzhou/java-workspace/test-share/results_new/npd/hermit"
		); 
	}

	@Test 
	public void dbpedia() throws Exception {
		String ontoDir = TestUtil.getConfig().getProperty("ontoDir");
		HermitQueryReasoner.main(
				TestUtil.combinePaths(ontoDir, "dbpedia/integratedOntology-all-in-one-minus-datatype.owl"),
				TestUtil.combinePaths(ontoDir, "dbpedia/data/dbpedia-minus-datatype-new.ttl"),
				TestUtil.combinePaths(ontoDir, "dbpedia/queries/atomic_ground.sparql")
				, "/home/yzhou/java-workspace/test-share/results_new/dbpedia/hermit"
		);
	}

	@Test 
	public void reactome() throws Exception {
		String ontoDir = TestUtil.getConfig().getProperty("ontoDir");
		HermitQueryReasoner.main(
				TestUtil.combinePaths(ontoDir, "bio2rdf/reactome/biopax-level3-processed.owl"),
				TestUtil.combinePaths(ontoDir, "bio2rdf/reactome/graph sampling/reactome_sample_10.ttl"),
				TestUtil.combinePaths(ontoDir, "bio2rdf/reactome/queries/atomic_ground.sparql")
				, "/home/yzhou/java-workspace/test-share/results_new/reactome/hermit_10p"
		); 
	}

	@Test 
	public void chembl() throws Exception {
		String ontoDir = TestUtil.getConfig().getProperty("ontoDir");
		HermitQueryReasoner.main(
				TestUtil.combinePaths(ontoDir, "bio2rdf/chembl/cco-noDPR.ttl"),
//				null,
				TestUtil.combinePaths(ontoDir, "bio2rdf/chembl/graph sampling/sample_1.nt"),
				TestUtil.combinePaths(ontoDir, "bio2rdf/chembl/queries/atomic_ground.sparql")
				, "hermit_chembl.out"
//				, "/home/yzhou/java-workspace/test-share/results_new/chembl/hermit_1p"
		); 
	}

	@Test 
	public void uniprot() throws Exception {
		String ontoDir = TestUtil.getConfig().getProperty("ontoDir");
		HermitQueryReasoner.main(
				TestUtil.combinePaths(ontoDir, "bio2rdf/uniprot/core-sat.owl"),
				TestUtil.combinePaths(ontoDir, "bio2rdf/uniprot/graph sampling/sample_1.nt"),
				TestUtil.combinePaths(ontoDir, "bio2rdf/uniprot/queries/atomic_ground.sparql")
				, "hermit_uniprot.out"
//				, "/home/yzhou/java-workspace/test-share/results_new/uniprot/hermit_1p"
		); 
	}

}
