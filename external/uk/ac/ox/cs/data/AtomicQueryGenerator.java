package uk.ac.ox.cs.data;

import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyManager;

import uk.ac.ox.cs.pagoda.owl.OWLHelper;
import uk.ac.ox.cs.pagoda.tester.PagodaTester;
import uk.ac.ox.cs.pagoda.util.Utility;

public class AtomicQueryGenerator {
	
	public static final String template = //"^[query@ID]" + Utility.LINE_SEPARATOR +  
		"PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>" + Utility.LINE_SEPARATOR + 
		"SELECT ?X" + Utility.LINE_SEPARATOR +
		"WHERE {" + Utility.LINE_SEPARATOR + 
		"?X rdf:type <@CLASS>" + Utility.LINE_SEPARATOR + 
		"}"; 
	
	public static String outputFile = "output/atomic_fly.sparql"; 

	public static void main(String[] args) throws Exception {
		if (args.length == 0) {
//			args = new String[] { "/home/yzhou/backup/20141212/univ-bench-dl-queries.owl"}; 
			args = new String[] { PagodaTester.onto_dir + "fly/fly-all-in-one_rolledUp.owl"}; 
//			args = new String[] { PagodaTester.onto_dir + "dbpedia/integratedOntology-all-in-one-minus-datatype.owl" }; 
//			args = new String[] { PagodaTester.onto_dir + "npd/npd-all-minus-datatype.owl" }; 
//			args = new String[] { PagodaTester.onto_dir + "bio2rdf/chembl/cco-noDPR.ttl" }; 
//			args = new String[] { PagodaTester.onto_dir + "bio2rdf/reactome/biopax-level3-processed.owl" }; 
//			args = new String[] { PagodaTester.onto_dir + "bio2rdf/uniprot/core-processed-noDis.owl" }; 
		}
		
//		OWLOntology ontology = OWLHelper.getMergedOntology(args[0], null);
//		OWLHelper.correctDataTypeRangeAxioms(ontology); 
		OWLOntology ontology = OWLHelper.loadOntology(args[0]); 
		
		OWLOntologyManager manager = ontology.getOWLOntologyManager(); 
		OWLDataFactory factory = manager.getOWLDataFactory();
//		manager.saveOntology(ontology, new FileOutputStream(args[0].replace(".owl", "_owlapi.owl")));
		
		if (outputFile != null)
			Utility.redirectCurrentOut(outputFile); 
		
		int queryID = 0; 
		for (OWLClass cls: ontology.getClassesInSignature(true)) {
			if (cls.equals(factory.getOWLThing()) || cls.equals(factory.getOWLNothing()))
				continue; 
			if (!cls.toStringID().contains("Query")) continue; 
			System.out.println("^[Query" + ++queryID + "]"); 
			System.out.println(template.replace("@CLASS", cls.toStringID())); 
			System.out.println(); 
		}
		
		for (OWLOntology onto: ontology.getImportsClosure())
			for (OWLObjectProperty prop: onto.getObjectPropertiesInSignature()) {
//				if (!prop.toStringID().contains("Query")) continue; 
				System.out.println("^[Query" + ++queryID + "]");
				System.out.println("SELECT ?X ?Y"); 
				System.out.println("WHERE {");
				System.out.println("?X <" + prop.toStringID() + "> ?Y ."); 
				System.out.println("}");
				System.out.println();					
			}
		
		String[] answerVars = new String[] {"?X", "?Y"}; 
		
		for (OWLOntology onto: ontology.getImportsClosure())
			for (OWLObjectProperty prop: onto.getObjectPropertiesInSignature()) {
//				if (!prop.toStringID().contains("Query")) continue; 
				for (int i = 0; i < answerVars.length; ++i) {
					System.out.println("^[Query" + ++queryID + "]");
					System.out.println("SELECT " + answerVars[i]); 
					System.out.println("WHERE {");
					System.out.println("?X <" + prop.toStringID() + "> ?Y ."); 
					System.out.println("}");
					System.out.println();					
				}
			}
		
		if (outputFile != null)
			Utility.closeCurrentOut();
	}

}
