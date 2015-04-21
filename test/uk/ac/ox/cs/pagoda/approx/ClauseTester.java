package uk.ac.ox.cs.pagoda.approx;

import org.semanticweb.HermiT.model.DLClause;
import org.semanticweb.owlapi.model.OWLOntology;

import uk.ac.ox.cs.pagoda.constraints.NullaryBottom;
import uk.ac.ox.cs.pagoda.owl.OWLHelper;
import uk.ac.ox.cs.pagoda.rules.GeneralProgram;

public class ClauseTester {

	public static void main(String... args) {
		args = new String[] {
//				"/home/yzhou/krr-nas-share/Yujiao/ontologies/bio2rdf/chembl/cco-noDPR.ttl", 
				"/home/yzhou/krr-nas-share/Yujiao/ontologies/bio2rdf/reactome/biopax-level3-processed.owl", 
//				"/media/krr-nas-share/Yujiao/ontologies/bio2rdf/atlas/gxaterms.owl", 
//				"/media/krr-nas-share/Yujiao/ontologies/bio2rdf/uniprot/core-sat-processed.owl",
//				PagodaTester.npd_tbox,
//				"/users/yzhou/temp/ontologies/core.RLor.rdf",
//				"datatype.owl" 
		}; 
		
		String ontoFile = args[0];   
		OWLOntology ontology = OWLHelper.loadOntology(ontoFile);  
		GeneralProgram program = new GeneralProgram();; 
		program.load(ontology, new NullaryBottom());
		program.transform();
		program.save();
		if (program instanceof GeneralProgram) {
			GeneralProgram gp = ((GeneralProgram) program); 
			for (DLClause clause: gp.getClauses()) {
				System.out.println(clause);
				System.out.println(OWLHelper.getOWLAxiom(ontology, clause)); 
			}
		}
	}
	
}
