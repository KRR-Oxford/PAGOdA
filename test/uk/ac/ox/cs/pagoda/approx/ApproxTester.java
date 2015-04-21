package uk.ac.ox.cs.pagoda.approx;

import java.io.IOException;

import org.semanticweb.owlapi.model.OWLOntology;

import uk.ac.ox.cs.pagoda.approx.KnowledgeBase;
import uk.ac.ox.cs.pagoda.approx.RLOntology;
import uk.ac.ox.cs.pagoda.approx.RLPlusOntology;
import uk.ac.ox.cs.pagoda.constraints.NullaryBottom;
import uk.ac.ox.cs.pagoda.owl.OWLHelper;
import uk.ac.ox.cs.pagoda.rules.DisjunctiveProgram;
import uk.ac.ox.cs.pagoda.rules.ExistentialProgram;
import uk.ac.ox.cs.pagoda.rules.ExistentialToDisjunctive;
import uk.ac.ox.cs.pagoda.rules.GeneralProgram;
import uk.ac.ox.cs.pagoda.rules.LowerDatalogProgram;
import uk.ac.ox.cs.pagoda.rules.UpperDatalogProgram;
import uk.ac.ox.cs.pagoda.util.Utility;

public class ApproxTester {
	
	private static ApproxType description = ApproxType.DATALOGPMOR; 
	
	private static String ontoFile = null;
	
	public static void main(String[] args) throws IOException
	{
		args = new String[] {
				"-tbox", 
//				"/home/yzhou/krr-nas-share/Yujiao/ontologies/bio2rdf/chembl/cco-noDPR.ttl", 
//				"/home/yzhou/krr-nas-share/Yujiao/ontologies/bio2rdf/reactome/biopax-level3-processed.owl", 
//				"/media/krr-nas-share/Yujiao/ontologies/bio2rdf/atlas/gxaterms.owl", 
//				"/media/krr-nas-share/Yujiao/ontologies/bio2rdf/uniprot/core-sat-processed.owl",
//				PagodaTester.npd_tbox,
//				"/users/yzhou/temp/ontologies/core.RLor.rdf",
				"datatype.owl", 
				"-dest", ApproxType.DATALOGPMOR.toString() 
		}; 

		long startTime = System.currentTimeMillis();
		
		if (args.length > 0) {
			if (args.length % 2 != 0) {
				System.out.println("arguments error..."); 
				return ;
			}
			for (int i = 0; i < args.length ; i = i + 2) 
				if (!setArgument(args[i], args[i + 1])) {
					System.out.println("arguments error..."); 
					return ;
				}
		}
		
//		Utility.redirectSystemOut();
		
		System.setProperty("entityExpansionLimit", String.valueOf(Integer.MAX_VALUE));
		
		String directory = ontoFile.substring(0, ontoFile.lastIndexOf(Utility.FILE_SEPARATOR) + 1); 
		
		KnowledgeBase program = null;
		switch (description) {
		case OWL2RLPLUS: program = new RLPlusOntology(); break; 
		case OWL2RL: program = new RLOntology(); break;
		case DATALOG_UPPER: program = new UpperDatalogProgram(); break; 
		case DATALOG_LOWER: program = new LowerDatalogProgram(); break; 
		case EXISTENTIAL: program = new ExistentialProgram(); break;
		case DISJUNCTIVE: program = new DisjunctiveProgram(); break;  
		case DATALOGPMOR: program = new GeneralProgram(); break;
		case EXIST2DISJ: program = new ExistentialToDisjunctive(); break;
		default: 
			System.exit(0);
		}

		if (program instanceof RLPlusOntology)
			((RLPlusOntology) program).setCorrespondenceFileLoc(directory + "correspondence");
		OWLOntology ontology = OWLHelper.loadOntology(ontoFile);  
		program.load(ontology, new NullaryBottom());

		program.transform();

		program.save();
		
		System.out.println("Time to transform the rules: " + (System.currentTimeMillis() - startTime) / 1000.);
		
		Utility.closeCurrentOut();
	}

	private static boolean setArgument(String key, String value) {
		if (key.equalsIgnoreCase("-dest")) 
			if (value.equalsIgnoreCase("OWL2RL+")) description = ApproxType.OWL2RLPLUS;
			else if (value.equalsIgnoreCase("OWL2RL")) description = ApproxType.OWL2RL; 
			else if (value.equalsIgnoreCase("UPPERDATALOG")) description = ApproxType.DATALOG_UPPER;
			else if (value.equalsIgnoreCase("LOWERDATALOG")) description = ApproxType.DATALOG_LOWER;
			else if (value.equalsIgnoreCase("DATALOGPMOR")) description = ApproxType.DATALOGPMOR;
			else if (value.equalsIgnoreCase("EXISTENTIALRULES")) description = ApproxType.EXISTENTIAL;
			else if (value.equalsIgnoreCase("DISJUNCTIVE")) description = ApproxType.DISJUNCTIVE;
			else if (value.equalsIgnoreCase("EXIST2DISJ")) description = ApproxType.EXIST2DISJ; 
			else {
				System.out.println("illegal destination argument..."); 
				return false;
			}
		else if (key.equalsIgnoreCase("-tbox")) 
			ontoFile = value;
		else {
			System.out.println("unrecognisable type of argument..."); 
			return false;
		}
		
		return true;
	}
	
	public enum ApproxType {
		/**
		 * approx to (RL + self + top being the subClassExp)
		 */
		OWL2RLPLUS, 	
		
		/**
		 * approx to RL
		 */
		OWL2RL, 		
		
		/**
		 * approx to datalog by replacing existential quantified variables 
		 * by fresh constants and replacing disjunctions by conjunctions
		 */
		DATALOG_UPPER, 		
		
		/**
		 * approx to datalog by ignoring existential and disjunctive axiom
		 */
		DATALOG_LOWER, 		
		
		/**
		 * approx to existential rules by replacing disjunctions by 
		 * conjunctions
		 */
		EXISTENTIAL,
		
		/**
		 * approx to disjunctive datalog program by replacing existential 
		 * quantified variables by fresh constants (DNF)
		 */
		DISJUNCTIVE, 
		
		/**
		 * transform into rules, no approximation at all
		 */
		DATALOGPMOR, 	
		
		/**
		 * approx existential quantifiers by disjunctions
		 */
		EXIST2DISJ		

	}; 
	
}
