package uk.ac.ox.cs.pagoda.summary;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Scanner;

import org.semanticweb.HermiT.Reasoner;
import org.semanticweb.owlapi.model.AxiomType;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyStorageException;

import uk.ac.ox.cs.pagoda.hermit.DLClauseHelper;
import uk.ac.ox.cs.pagoda.owl.OWLHelper;
import uk.ac.ox.cs.pagoda.owl.QueryRoller;
import uk.ac.ox.cs.pagoda.query.QueryManager;
import uk.ac.ox.cs.pagoda.query.QueryRecord;
import uk.ac.ox.cs.pagoda.summary.Summary;

public class SummaryTester {

	static String FILE_BREAK = System.getProperty("file.separator");
	static String LINE_BREAK = System.getProperty("line.separator");
	
	public static void main(String[] args) throws Exception {
//		String arg = "ontologies/claros/all-in-one-manually.owl"; 
//		String arg = "ontologies/claros/Claros.owl ontologies/claros/data"; 
		String arg =  "../uobmGenerator/univ-bench-dl.owl " + 
				"../uobmGenerator/uobm1 " + //"a " + 
				"ontologies/uobm/queries/uobm_standard_less.sparql"; 
				
		testSummarisedUpperBound(arg.split("\\ ")); 
	}
	
	/**
	 * args[0] ontology file location
	 * args[1] data directory
	 * args[2] sparql query file location
	 * 
	 * @param args 
	 * @throws OWLOntologyCreationException
	 * @throws FileNotFoundException
	 * @throws OWLOntologyStorageException 
	 */
	public static void testSummarisedUpperBound(String[] args) throws OWLOntologyCreationException, FileNotFoundException, OWLOntologyStorageException {
		OWLOntology onto = OWLHelper.loadOntology(args[0]); 
		try {
			onto = OWLHelper.getImportedOntology(onto, args[1]); 
		} catch (IOException e) {
			e.printStackTrace();
		} 
		 
		Summary sum = testSummary(onto);
		System.out.println("Summarisation Done."); 
		
		System.out.println(args[2]); 
		Scanner scanner = new Scanner(new File(args[2])); 
		OWLOntology summary = sum.getSummary();
		OWLDataFactory factory = summary.getOWLOntologyManager().getOWLDataFactory(); 
		QueryRoller r = new QueryRoller(factory);
		OWLClassExpression summarisedQueryExp;
		Reasoner reasoner = new Reasoner(summary); 
		QueryManager queryManager = new QueryManager(); 
		int upperBoundCounter, queryID = 0;
		StringBuilder queryText = new StringBuilder();
		String[] vars; 
		
		for (String line; ; ) {
			queryText.setLength(0);
			while (scanner.hasNextLine() && (line = scanner.nextLine()) != null && !line.startsWith("^[query"));
			if (!scanner.hasNextLine()) break; 
			
			while (scanner.hasNextLine() && (line = scanner.nextLine()) != null && !line.isEmpty())
				queryText.append(line).append(LINE_BREAK);
			if (!scanner.hasNextLine()) break; 
				
			System.out.println("------------ starting computing for Query " + ++queryID + "------------"); 
			
			System.out.println(queryText); 
			
			QueryRecord record = queryManager.create(queryText.toString(), queryID);
			vars = record.getAnswerVariables(); 
			if (vars.length > 1) {
				System.out.println("The query cannot be processed by HermiT ... More than one answer variable"); 
				continue; 
			}
			
			summarisedQueryExp = r.rollUp(DLClauseHelper.getQuery(sum.getSummary(record), null), vars[0]);
			
			upperBoundCounter = 0; 
			for (String representative: sum.getRepresentatives()) 
				if (reasoner.isEntailed(factory.getOWLClassAssertionAxiom(summarisedQueryExp, factory.getOWLNamedIndividual(IRI.create(representative))))) {
					upperBoundCounter += sum.getGroup(representative).size(); 
				}
			
			System.out.println("There are " + upperBoundCounter + " individual(s) in the upper bound computed by summary."); 
		}
		scanner.close();
	}
	
	public static Summary testSummary(OWLOntology ontology) throws OWLOntologyCreationException, FileNotFoundException {
		Summary sum = new Summary(ontology);
		
		System.out.println("original ontology data: "); 
		outputStatistics(ontology); 
		
		OWLOntology summary = sum.getSummary();
		
		System.out.println("summarised ontology data: "); 
		outputStatistics(summary);
		
		try {
			FileOutputStream out = new FileOutputStream("summary.owl"); 
			summary.getOWLOntologyManager().saveOntology(summary, out);
			out.close();
		} catch (OWLOntologyStorageException e) {
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return sum; 
	}

	private static void outputStatistics(OWLOntology onto) {
		System.out.println("TBox: " + onto.getTBoxAxioms(true).size() +
				"\tRBox: " + onto.getRBoxAxioms(true).size() + 
				"\tABox: " + onto.getABoxAxioms(true).size());
		System.out.println("Class Assertions: " + onto.getAxiomCount(AxiomType.CLASS_ASSERTION, true) + 
				"\tObject Property Assertions: " + onto.getAxiomCount(AxiomType.OBJECT_PROPERTY_ASSERTION, true)); 
	}

}
