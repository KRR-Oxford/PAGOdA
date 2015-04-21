package uk.ac.ox.cs.pagoda.tester;

import org.semanticweb.owlapi.model.OWLOntology;

import uk.ac.ox.cs.pagoda.owl.OWLHelper;
import uk.ac.ox.cs.pagoda.reasoner.QueryReasoner;

public class ORETester {

	public static void main(String... args) {
//		args = new String[] { "/home/yzhou/krr-nas-share/Yujiao/ontologies/ORE2014/DL/00a1118a-5420-46f0-b4b2-a2585165b28a_ePizza.owl" };
//		args = new String[] { "/home/yzhou/krr-nas-share/Yujiao/ontologies/ORE2014/DL/77de15c6-cc39-4960-a38a-e35e487d52b0_owl%2Fcoma" };
//		args = new String[] { "/home/yzhou/krr-nas-share/Yujiao/ontologies/ORE2014/DL/wine_nodatatype.owl" };
		
//		args = new String[] { "/home/yzhou/krr-nas-share/Yujiao/ontologies/ORE2014/EL/b7700fe1-103b-4b32-a21c-f6604a763ba5_t-cell.owl" };
		args = new String[] { "/home/yzhou/krr-nas-share/Yujiao/ontologies/ORE2014/EL/baa29363-f93c-4285-827e-0e2380c82efc_cations.n3" };
		
		
		OWLOntology ontology = OWLHelper.loadOntology(args[0]);
		QueryReasoner pagoda = QueryReasoner.getInstance(ontology);
		System.out.println(pagoda); 
		pagoda.loadOntology(ontology);
		if (pagoda.preprocess())
			System.out.println("The ontology is consistent!"); 
		else 
			System.out.println("The ontology is inconsistent!"); 
	}
	
}
