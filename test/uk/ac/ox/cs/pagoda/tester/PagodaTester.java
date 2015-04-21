package uk.ac.ox.cs.pagoda.tester;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Scanner;

import org.semanticweb.owlapi.model.OWLOntology;

import uk.ac.ox.cs.pagoda.owl.OWLHelper;
import uk.ac.ox.cs.pagoda.query.AnswerTuple;
import uk.ac.ox.cs.pagoda.query.AnswerTuples;
import uk.ac.ox.cs.pagoda.reasoner.*; 
import uk.ac.ox.cs.pagoda.reasoner.QueryReasoner.Type; 
import uk.ac.ox.cs.pagoda.util.Timer;
import uk.ac.ox.cs.pagoda.util.Utility;

public class PagodaTester {

//	public static final String onto_dir = "/media/RDFData/yzhou/";
//	public static final String onto_dir = "/users/yzhou/ontologies/";
//	public static final String onto_dir = "/home/scratch/yzhou/ontologies/";
	public static final String onto_dir = "/home/yzhou/krr-nas-share/Yujiao/ontologies/"; 

	public static final String fly = onto_dir + "fly/fly_anatomy_XP_with_GJ_FC_individuals.owl";
	public static final String fly_query = onto_dir + "fly/queries/fly.sparql";

	public static final String test_tbox = onto_dir + "smallExampleFromAna/dummy.owl";
	public static final String test_abox = onto_dir + "smallExampleFromAna/initialABox.ttl";
	public static final String test_query = onto_dir + "smallExampleFromAna/queries.dlog";

	public static final int lubm_number = 1;
	public static final String lubm_tbox = onto_dir + "lubm/univ-bench.owl";
	public static final String lubm_abox = onto_dir + "lubm/data/lubm" + lubm_number + ".ttl";
	public static final String lubm_abox_copy = onto_dir + "lubm/data/lubm" + lubm_number + " (copy).ttl";
	public static final String lubm_query = onto_dir + "lubm/queries/test.sparql";
	public static final String lubm_query6 = onto_dir + "lubm/queries/test_q6.sparql";
	public static final String lubm_query20 = onto_dir + "lubm/queries/test_q16.sparql";

	public static final int uobm_number = 1;
	public static final String uobm_tbox = onto_dir + "uobm/univ-bench-dl.owl";
	public static final String uobm_abox = onto_dir + "uobm/data/uobm" + uobm_number + ".ttl";
	public static final String uobm_query = onto_dir + "uobm/queries/standard.sparql";
	public static final String uobm_query_temp = onto_dir + "uobm/queries/temp.sparql";
	public static final String uobm_query2 = onto_dir + "uobm/queries/standard_q2.sparql";
	public static final String uobm_query9 = onto_dir + "uobm/queries/standard_q9.sparql";
	public static final String uobm_query11 = onto_dir + "uobm/queries/standard_q11.sparql";
	public static final String uobm_query12 = onto_dir + "uobm/queries/standard_q12.sparql";
	public static final String uobm_query14 = onto_dir + "uobm/queries/standard_q14.sparql";
	public static final String uobm_query15 = onto_dir + "uobm/queries/standard_q15.sparql";
	public static final String uobm_query_multi = onto_dir + "uobm/queries/standard_multi.sparql";
	public static final String uobm_generated_query1 = onto_dir	+ "uobm/queries/generated_q1.sparql";
	public static final String uobm_query_group3 = onto_dir	+ "uobm/queries/standard_group3.sparql";

	public static final String npd_tbox = onto_dir + "npd/npd-all-minus-datatype.owl"; 
												// "npd/npd-all.owl";
												// "npd-all-minus-datatype.owl";
	public static final String npd_abox = onto_dir + "npd/data/npd-data-dump-minus-datatype-new.ttl"; 
																// "npd/data/npd-data-dump-processed.ttl";
																// "npd-data-dump-minus-datatype-old.ttl";
	public static final String npd_query = onto_dir	+ "npd/queries/atomic.sparql";

	public static final String npd_bench_tbox = onto_dir + "npd-benchmark/npd-v2-ql_a.owl"; // npd-all-minus-datatype.owl";
	public static final String npd_bench_abox = onto_dir + "npd-benchmark/npd-v2-ql_a.ttl"; // npd-data-dump-minus-datatype-old.ttl";
	public static final String npd_bench_query = onto_dir + "npd-benchmark/queries/all.sparql";

	public static final String dbpedia_tbox = onto_dir + "dbpedia/integratedOntology-all-in-one-minus-datatype.owl";
	public static final String dbpedia_abox = onto_dir + "dbpedia/data/dbpedia-minus-datatype-new.ttl";
	public static final String dbpedia_query = onto_dir + "dbpedia/queries/atomic_ground.sparql";
	public static final String dbpedia_query274 = onto_dir + "dbpedia/atomic_q274.sparql";
	
	public static final String dbpedia_latest_tbox = onto_dir + "dbpedia/dbpedia_2014.owl";
	public static final String travel_tbox = onto_dir + "dbpedia/travel.owl";
	public static final String dbpedia_tbox_simple = onto_dir + "dbpedia/dbpedia_simple.owl";
	
	public static final String bioModels_tbox = onto_dir + "biomodels/biomodels-21.owl"; 
	public static final String bioModels_abox = onto_dir + "biomodels/data_processed_1.ttl"; 
	public static final String bioModels_queries = onto_dir + "biomodels/queries/queries.sparql";
	
	public static final String chembl_tbox = onto_dir + "bio2rdf/chembl/cco-processed-noDPR-noDPD.ttl";  
	public static final String chembl_abox = onto_dir + "bio2rdf/chembl/graph sampling old/sample_100.nt";
	public static final String chembl_queries = onto_dir + "bio2rdf/chembl/queries/problematic.sparql"; //"bio2rdf/chembl/queries/atomic_one_filtered.sparql"; // 

	public static final String reactome_tbox = onto_dir + "bio2rdf/reactome/biopax-level3-processed.owl";  
	public static final String reactome_abox = onto_dir + "bio2rdf/reactome/graph sampling old/sample.ttl"; //data/data.ttl"; //graph sampling old/reactome_sample_10.ttl"; //
	public static final String reactome_queries = onto_dir +"bio2rdf/reactome/queries/atomic.sparql";

	public static final String uniprot_tbox = onto_dir + "bio2rdf/uniprot/core-processed.owl"; 
	public static final String uniprot_abox = onto_dir + "bio2rdf/uniprot/graph sampling/sample_1.nt"; 
	public static final String uniprot_queries = onto_dir + "bio2rdf/uniprot/queries/atomic_one.sparql"; 
	
	public static final String atlas_tbox = onto_dir + "bio2rdf/atlas/gxaterms.owl"; 
	public static final String atlas_abox = onto_dir + "bio2rdf/atlas/graph sampling/sample_1.nt"; 
	public static final String atlas_queries = onto_dir + "bio2rdf/atlas/queries/atomic_one.sparql"; 
	
	public static boolean ShellMode = false; 
	
	public static void main(String... args) {
		if (args.length == 0) {
//			args = new String[] {test_tbox, test_abox, test_query};
//			args = new String[] {lubm_tbox, lubm_abox, lubm_query.replace(".sparql", "_all_pagoda.sparql")}; 
//			args = new String[] {uobm_tbox, uobm_abox, uobm_query.replace(".sparql", "_all_pagoda.sparql")};
//			args = new String[] {fly, "null", fly_query.replace(".sparql", "_pellet.sparql") };
//			args = new String[] {dbpedia_tbox, dbpedia_abox, dbpedia_query};
//			args = new String[] {travel_tbox, null, dbpedia_query274};
//			args = new String[] {fly, null, fly_query};
//			args = new String[] {npd_tbox, npd_abox, npd_query};
//			args = new String[] {npd_bench_tbox, npd_bench_abox, npd_bench_query};
//			args = new String[] {"../SemFacet/WebContent/WEB-INF/data/dbpedia.owl", "../SemFacet/WebContent/WEB-INF/data/dbpediaA.nt", null}; 
//			args = new String[] {"../SemFacet/WebContent/WEB-INF/data/isg.owl", "../SemFacet/WebContent/WEB-INF/data/isg.nt", null}; 
			args = new String[] {"..\\core\\WebContent\\WEB-INF\\data\\fly.owl", "..\\core\\WebContent\\WEB-INF\\data\\fly-data.nt", null}; 
//			args = new String[] {bioModels_tbox, bioModels_abox, bioModels_queries}; 
//			args = new String[] {chembl_tbox, chembl_abox, chembl_queries};
//			args = new String[] {reactome_tbox, reactome_abox, reactome_queries};
//			args = new String[] {reactome_tbox, "/users/yzhou/temp/reactome_debug.ttl", onto_dir +"bio2rdf/reactome/queries/atomic_one_q65.sparql"};
//			args = new String[] {uniprot_tbox.replace(".owl", "-noDis.owl"), "/users/yzhou/temp/uniprot_debug/sample_1_string.nt", uniprot_queries};
//			args = new String[] {uniprot_tbox.replace(".owl", "-noDis.owl"), uniprot_abox, uniprot_queries};
//			args = new String[] {atlas_tbox, atlas_abox, atlas_queries};
//			args = new String[] {onto_dir + "test/unsatisfiable.owl", null, onto_dir + "test/unsatisfiable_queries.sparql"}; 
//			args = new String[] {onto_dir + "test/jair-example.owl", null, onto_dir + "test/jair-example_query.sparql"}; 
//			args[2] = args[2].replace(".sparql", "_all_pagoda.sparql");
//			args[2] = args[2].replace(".sparql", "_pellet.sparql");
		}

		int ontoIndex = 0, dataIndex = 1, queryIndex = 2;

		if (args.length > dataIndex && args[dataIndex] != null && args[dataIndex].endsWith(".sparql")) {
			String[] inputArgs = args;
			args = new String[inputArgs.length + 1];
			for (int i = 0; i < dataIndex; ++i)
				args[i] = inputArgs[i];
			args[dataIndex] = null;
			args[queryIndex] = inputArgs[dataIndex];
			for (int i = dataIndex + 1; i < inputArgs.length; ++i)
				args[i + 1] = inputArgs[i]; 
		}

		StringBuilder info = new StringBuilder(); 
		info.append("System started with \n");
		for (int i = 0; i < args.length; ++i)
			info.append("Arg " + (i + 1) + ": " + args[i] + "\n"); 
		Utility.logInfo(info);

//		Utility.redirectCurrentOut("temp.out");

		OWLOntology ontology = OWLHelper.loadOntology(args[ontoIndex]);
		
		QueryReasoner pagoda = QueryReasoner.getInstance(Type.Full, ontology, true, true); 
//		QueryReasoner pagoda = QueryReasoner.getInstance(Type.ELHOU, ontology, true, true);
		
//		QueryReasoner pagoda = QueryReasoner.getInstanceForSemFacet(ontology);
//		QueryReasoner pagoda = QueryReasoner.getHermiTReasoner(false); 
		
//		PagodaTester tester = new PagodaTester(pagoda);
		String ansFile = args.length > 3 ? args[3] : null; 
		try {
			Timer t = new Timer();
			pagoda.loadOntology(ontology);
			pagoda.importData(args[dataIndex]);
			if (!pagoda.preprocess())
				return;
			Utility.logInfo("Preprocessing Done in " + t.duration()	+ " seconds.");
//			tester.printPredicatesWithGap(); 
//			tester.testSemFacetQueries();
//			tester.testSomeFlyQueries();
//			tester.testISGQueries();
//			tester.testReactomeQueries(); 
			if (args[queryIndex] != null)
				for (String queryFile: args[queryIndex].split(";"))
					pagoda.evaluate(pagoda.getQueryManager().collectQueryRecords(queryFile), ansFile);

			if (ShellMode)
				try {
					evaluateConsoleQuery(pagoda);
				} catch (IOException e) {
					e.printStackTrace();
				} 
 		} finally {
			pagoda.dispose();
		}

		Utility.closeCurrentOut();
		
		if (ShellMode) System.exit(0);
	}
	
//	private void printPredicatesWithGap() {
//		for (String p: ((MyQueryReasoner) pagoda).getPredicatesWithGap()) {
//			System.out.println(p); 
//		}
//	}

	private static void evaluateConsoleQuery(QueryReasoner pagoda) throws IOException {
		int ending = (int) '$', symbol;
		while (true) {
			Utility.logInfo("Input your query ending with $"); 
			StringBuilder queryBuilder = new StringBuilder(); 
			while ((symbol = System.in.read()) != ending) {
				queryBuilder.append((char) symbol); 
			}
			System.in.read(); 
			if (queryBuilder.length() == 0) return ; 
			pagoda.evaluate_shell(queryBuilder.toString());
		}
	}

	void testReactomeQueries() {
		evaluate("select ?x where { ?x <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.biopax.org/release/biopax-level3.owl#DnaReference> . }");
		evaluate("select ?y ?z where { <http://identifiers.org/ensembl/ENSG00000157557> ?y ?z . }");
		evaluate("select ?y where { <http://identifiers.org/ensembl/ENSG00000157557> <http://www.biopax.org/release/biopax-level3.owl#name> ?y . }", true);  

	}

	void testSemFacetQueries() {
//		try {
//			BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream("query.line")));
//			for (String line; (line = reader.readLine()) != null && !line.isEmpty(); ) 
//				evaluate(line, true);
//			reader.close();
//		} catch (FileNotFoundException e) {
//			e.printStackTrace();
//		} catch (IOException e) {
//			e.printStackTrace();
//		}
		evaluate("select ?x ?z where { ?x <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> ?z }", true);
		evaluate("select distinct ?y where { ?x ?y ?z }", true);
		evaluate("select distinct ?z where { ?x <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> ?z }", true);
		evaluate("select ?y ?z where { <http://www.reactome.org/biopax/46/49633#Protein3885> ?y ?z .}", true); 
	}
	
	void testISGQueries() {
		evaluate("select ?z where {<http://cs.ox.ac.uk/Evgeny_Kharlamov> <http://cs.ox.ac.uk/lat> ?z .}", false);
		evaluate("select ?x where {?x <http://cs.ox.ac.uk/type> <http://cs.ox.ac.uk/person> .}", false);
	}

	void testSomeTravelQueries() {
		evaluate("select ?y ?z where {<http://www.owl-ontologies.com/travel.owl#BlueMountains> ?y ?z. }", true); 
		evaluate("select ?x where {?x <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.owl-ontologies.com/travel.owl#RetireeDestination>. }"); 
		evaluate("select ?x where {?x <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.owl-ontologies.com/travel.owl#BackpackersDestination>. }");
	}

	void testSomeFlyQueries() {
		evaluate("select ?x where { ?x <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://purl.obolibrary.org/obo/FBbt_00005106> . }", false);
		
		evaluate("select DISTINCT ?z where { ?x <http://purl.obolibrary.org/obo/FBbt#develops_from> ?any . ?any <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> ?z .  ?x <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://purl.obolibrary.org/obo/FBbt_00067123> . } ", true); 
		
		evaluate("Select ?x where { ?x <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> "
				+ "<http://purl.obolibrary.org/obo/FBbt_00067123>. ?x "
				+ "<http://purl.obolibrary.org/obo/RO_0002131> ?any . ?any "
				+ "<http://www.w3.org/1999/02/22-rdf-syntax-ns#type> "
				+ "<http://purl.obolibrary.org/obo/FBbt_00005140> . }", true);
		
		evaluate("Select ?x where {?x <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> "
				+ "<http://purl.obolibrary.org/obo/FBbt_00067363> . ?x "
				+ "<http://purl.obolibrary.org/obo/RO_0002131> ?any . ?any "
				+ "<http://www.w3.org/1999/02/22-rdf-syntax-ns#type> "
				+ "<http://purl.obolibrary.org/obo/FBbt_00005140> . }", true);

//		evaluate("Select ?x where { "
//				+ "?x <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://purl.obolibrary.org/obo/FBbt_00003660>. "
//				+ "?x <http://purl.obolibrary.org/obo/FBbt#develops_from> ?any . "
//				+ "?any <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://purl.obolibrary.org/obo/FBbt_00001446> . }", true);

		evaluate("select DISTINCT ?z where { ?x <http://purl.obolibrary.org/obo/RO_0002110> ?any . "
				+ "?any <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> ?z . "
				+ "?x <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://purl.obolibrary.org/obo/FBbt_00007016>  . } ", true);

		evaluate("Select * where {"
				+ "<http://www.virtualflybrain.org/ontologies/individuals/VFB_00100607> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://purl.obolibrary.org/obo/FBbt_00007364>. "
				+ "<http://www.virtualflybrain.org/ontologies/individuals/VFB_00100607> <http://www.w3.org/2002/07/owl#sameAs> ?z }", true);

		evaluate("SELECT DISTINCT ?x ?z WHERE {?x <http://www.w3.org/2002/07/owl#sameAs> ?z}", true);
		evaluate("SELECT DISTINCT ?x ?z WHERE {?x <http://purl.obolibrary.org/obo/BFO_0000051> ?z}", true);

		evaluate("select DISTINCT ?y where { ?x ?y ?z . "
				+ "?x <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://purl.obolibrary.org/obo/FBbt_00007364>  }", true);

		evaluateQueriesFromFile("/users/yzhou/Downloads/logs(1).log");
		evaluateQueriesFromFile("/users/yzhou/Downloads/logs.log");

		evaluate("SELECT DISTINCT ?x ?z WHERE {?x <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> ?z}", true);
		evaluate("SELECT DISTINCT ?x ?z WHERE {?x <http://xmlns.com/foaf/0.1/depicts> ?z}", true);

		evaluate("select ?x ?z where { ?x <http://www.w3.org/2002/07/owl#sameAs> ?z } ", true);
		evaluate("select ?x ?z where { ?x <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> ?z } ", true);
	}

	public void evaluateQueriesFromFile(String fileName) {
		Scanner scanner = null;
		try {
			scanner = new Scanner(new File(fileName));
			String line;
			while (scanner.hasNextLine()) {
				line = scanner.nextLine();
				if (line.startsWith("select"))
					evaluate(line, true);
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} finally {
			if (scanner != null)
				scanner.close();
		}
	}

	QueryReasoner pagoda;

	public PagodaTester(QueryReasoner reasoner) {
		pagoda = reasoner;
	}

	Timer timer = new Timer(); 
	
	private void evaluate(String query) {
		evaluate(query, false); 
	}
	
	private void evaluate(String query, boolean tag) {
		timer.reset(); 
		AnswerTuples tuples = pagoda.evaluate(query, tag);
		int arity = tuples.getArity(); 
		int count = 0; 
		for (AnswerTuple tuple; tuples.isValid(); tuples.moveNext()) {
			tuple = tuples.getTuple();
			for (int i = 0; i < arity; ++i)
				tuple.getGroundTerm(i).toString();
//				System.out.print(tuple.getGroundTerm(i).toString() + "\t");
//			System.out.println();
			++count; 
		}
		tuples.dispose();
		Utility.logInfo("The number of answers for this SemFacet query: " +  count);
		Utility.logInfo("Total time for this SemFacet query: " + timer.duration());
	}

}
