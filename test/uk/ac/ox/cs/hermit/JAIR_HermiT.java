package uk.ac.ox.cs.hermit;

import org.junit.Test;

import uk.ac.ox.cs.pagoda.tester.PagodaTester;

public class JAIR_HermiT {

	@Test
	public void lubm1() throws Exception {
		String[] args = new String[] {
				PagodaTester.onto_dir + "lubm/univ-bench.owl",
				PagodaTester.onto_dir + "lubm/data/lubm1_owl",
				PagodaTester.onto_dir + "lubm/queries/test.sparql"
//				, "/home/yzhou/java-workspace/test-share/results_new/lubm1/hermit"
		};
		HermitQueryReasoner.main(args);
	}
	
	@Test
	public void lubm1_rolledUp() throws Exception {
		String[] args = new String[] {
				"/home/yzhou/backup/20141212/univ-bench-queries.owl",
				PagodaTester.onto_dir + "lubm/data/lubm1_owl",
				PagodaTester.onto_dir + "lubm/queries/atomic_lubm.sparql" 
//				, "/home/yzhou/java-workspace/test-share/results_new/lubm1/hermit_rolledUp"
		};
		HermitQueryReasoner.main(args);
	}
	
	@Test
	public void uobm1() throws Exception {
		String[] args = new String[] {
				PagodaTester.onto_dir + "uobm/univ-bench-dl.owl",
				PagodaTester.onto_dir + "uobm/data/uobm1_owl_withDeclaration",
				PagodaTester.onto_dir + "uobm/queries/standard.sparql" 
//				, "hermit_uobm1.out"
//				, "/home/yzhou/java-workspace/test-share/results_new/uobm1/hermit"
		};
		HermitQueryReasoner.main(args);
	}
	
	@Test
	public void uobm1_rolledUp() throws Exception {
		String[] args = new String[] {
				"/home/yzhou/backup/20141212/univ-bench-dl-queries.owl",
				PagodaTester.onto_dir + "uobm/data/uobm1_owl_withDeclaration",
				PagodaTester.onto_dir + "uobm/queries/atomic_uobm.sparql" 
				, "hermit_uobm1_rolledUp.out"
//				, "/home/yzhou/java-workspace/test-share/results_new/uobm1/hermit_rolledUp"
		};
		HermitQueryReasoner.main(args);
	}
	
	@Test
	public void fly_rolledUp() throws Exception {
		HermitQueryReasoner.main(
				PagodaTester.onto_dir + "fly/fly-all-in-one_rolledUp.owl",
//				PagodaTester.onto_dir + "fly/fly_anatomy_XP_with_GJ_FC_individuals.owl",
				null, 
				PagodaTester.onto_dir + "fly/queries/fly_atomic.sparql" 
				, "hermit_fly.out"
		);
	}
	
	@Test 
	public void npd() throws Exception {
		HermitQueryReasoner.main(
				PagodaTester.onto_dir + "npd/npd-all-minus-datatype.owl", 
				PagodaTester.onto_dir + "npd/data/npd-data-dump-minus-datatype-new.ttl", 
				PagodaTester.onto_dir + "npd/queries/atomic_ground.sparql" 
				, "hermit_npd.out"
//				, "/home/yzhou/java-workspace/test-share/results_new/npd/hermit"
		); 
	}

	@Test 
	public void dbpedia() throws Exception {
		HermitQueryReasoner.main(
				PagodaTester.onto_dir + "dbpedia/integratedOntology-all-in-one-minus-datatype.owl", 
				PagodaTester.onto_dir + "dbpedia/data/dbpedia-minus-datatype-new.ttl",
				PagodaTester.onto_dir + "dbpedia/queries/atomic_ground.sparql"
				, "/home/yzhou/java-workspace/test-share/results_new/dbpedia/hermit"
		);
	}

	@Test 
	public void reactome() throws Exception {
		HermitQueryReasoner.main(
				PagodaTester.onto_dir + "bio2rdf/reactome/biopax-level3-processed.owl", 
				PagodaTester.onto_dir + "bio2rdf/reactome/graph sampling/reactome_sample_10.ttl", 
				PagodaTester.onto_dir + "bio2rdf/reactome/queries/atomic_ground.sparql"
				, "/home/yzhou/java-workspace/test-share/results_new/reactome/hermit_10p"
		); 
	}

	@Test 
	public void chembl() throws Exception {
		HermitQueryReasoner.main(
				PagodaTester.onto_dir + "bio2rdf/chembl/cco-noDPR.ttl", 
//				null,
				PagodaTester.onto_dir + "bio2rdf/chembl/graph sampling/sample_1.nt", 
				PagodaTester.onto_dir + "bio2rdf/chembl/queries/atomic_ground.sparql"
				, "hermit_chembl.out"
//				, "/home/yzhou/java-workspace/test-share/results_new/chembl/hermit_1p"
		); 
	}

	@Test 
	public void uniprot() throws Exception {
		HermitQueryReasoner.main(
				PagodaTester.onto_dir + "bio2rdf/uniprot/core-sat.owl", 
				PagodaTester.onto_dir + "bio2rdf/uniprot/graph sampling/sample_1.nt", 
				PagodaTester.onto_dir + "bio2rdf/uniprot/queries/atomic_ground.sparql"
				, "hermit_uniprot.out"
//				, "/home/yzhou/java-workspace/test-share/results_new/uniprot/hermit_1p"
		); 
	}

}
