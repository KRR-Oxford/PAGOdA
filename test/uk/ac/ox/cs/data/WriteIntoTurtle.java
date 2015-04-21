package uk.ac.ox.cs.data;

import org.semanticweb.simpleETL.SimpleETL;

public class WriteIntoTurtle {
	
	public void rewriteUOBM(int number) {
		rewrite(
				"http://semantics.crl.ibm.com/univ-bench-dl.owl#", 
				"/home/yzhou/krr-nas-share/Yujiao/ontologies/uobm/data/uobm" + number + "_owl", 
				"/home/yzhou/krr-nas-share/Yujiao/ontologies/uobm/data/uobm" + number + ".ttl" 
				);	
	}
	
	public void rewriteUOBM15() {
		rewriteUOBM(15);
	}
	
	public void rewriteUOBM300() {
		rewriteUOBM(300);
	}
	
	public void testUOBM400() {
		rewriteUOBM(400);
	}

	public void rewriteLUBM(int number) {
		rewrite(
				"http://www.lehigh.edu/~zhp2/2004/0401/univ-bench.owl#",
				"/home/yzhou/krr-nas-share/Yujiao/ontologies/lubm/data/lubm" + number + "_owl",
				"/home/yzhou/krr-nas-share/Yujiao/ontologies/lubm/data/lubm" + number + ".ttl"
				);
	}
	
	public void testLUBM900() {
		rewriteLUBM(900);
	}

	public static void main(String[] args) {
//		"http://identifiers.org/biomodels.vocabulary#", 
//		"/home/yzhou/krr-nas-share/Yujiao/BioModels/sbml2rdfall", 
//		"/users/yzhou/ontologies/biomodels");
		
//		"http://www.biopax.org/release/biopax-level3.owl#", 
//		"/home/scratch/yzhou/ontologies/bio2rdf/reactome/biopaxrdf", 
//		"/home/scratch/yzhou/ontologies/bio2rdf/reactome"

		new WriteIntoTurtle().rewriteUOBM(20);
		
//		args = new String[] {
//				"http://www.lehigh.edu/~zhp2/2004/0401/univ-bench.owl#",
//				"/home/yzhou/krr-nas-share/Yujiao/ontologies/lubm/data/lubm400_owl",
//				"/home/yzhou/krr-nas-share/Yujiao/ontologies/lubm/data/lubm400.ttl"
//		}; 
//		
//		new WriteIntoTurtle().rewrite(args); 
	}
	
	public void rewrite(String... args) {
		SimpleETL rewriter = new SimpleETL(args[0], args[1], args[2]); 
		
		try {
			rewriter.rewrite();
		} catch (Exception e) {
			e.printStackTrace();
		} 
	}
	
}
