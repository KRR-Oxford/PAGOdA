package uk.ac.ox.cs.hermit;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.semanticweb.HermiT.Reasoner;
import org.semanticweb.HermiT.model.Atom;
import org.semanticweb.HermiT.model.AtomicRole;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLDatatype;
import org.semanticweb.owlapi.model.OWLIndividual;
import org.semanticweb.owlapi.model.OWLNamedIndividual;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.model.OWLOntologyStorageException;
import org.semanticweb.owlapi.reasoner.Node;

import uk.ac.ox.cs.pagoda.owl.OWLHelper;
import uk.ac.ox.cs.pagoda.owl.QueryRoller;
import uk.ac.ox.cs.pagoda.query.QueryManager;
import uk.ac.ox.cs.pagoda.query.QueryRecord;
import uk.ac.ox.cs.pagoda.util.Timer;

public class HermitQueryReasoner {

	public static void main(String... args) throws FileNotFoundException, OWLOntologyCreationException, OWLOntologyStorageException {
		if (args.length == 0) {
//			args = new String[] {"/media/krr-nas-share/Yujiao/ontologies/lubm/lubm1_merged.owl", null, PagodaTester.lubm_query};
//			args = new String[] {"/users/yzhou/ontologies/uobm/uobm1_merged.owl", null, "/users/yzhou/ontologies/uobm/queries/standard.sparql"};
//			args = new String[] {"/users/yzhou/ontologies/fly/fly_anatomy_XP_with_GJ_FC_individuals.owl", null, "/users/yzhou/ontologies/fly/queries/fly.sparql"};
//			args = new String[] {"/media/krr-nas-share/Yujiao/ontologies/npd/npd-all-minus-datatype.owl", "/media/krr-nas-share/Yujiao/ontologies/npd/data/npd-data-dump-minus-datatype-new.ttl", "/users/yzhou/ontologies/npd/queries/atomic.sparql"};
//			args = new String[] {"/media/krr-nas-share/Yujiao/ontologies/npd/npd-all.owl", "/media/krr-nas-share/Yujiao/ontologies/npd/data/npd-data-dump-processed.ttl", "/users/yzhou/ontologies/npd/queries/atomic.sparql"};
//			args = new String[] {PagodaTester.dbpedia_tbox, PagodaTester.dbpedia_abox, PagodaTester.dbpedia_query};
//			args = new String[] {"/users/yzhou/ontologies/test/unsatisfiable.owl", null, "/users/yzhou/ontologies/test/unsatisfiable_queries.sparql"}; 

//			args = new String[] {"/media/krr-nas-share/Yujiao/ontologies/bio2rdf/chembl/cco-processed-noDPR-noDPD.ttl", "/media/krr-nas-share/Yujiao/ontologies/bio2rdf/chembl/graph sampling/sample_100.nt", "/media/krr-nas-share/Yujiao/ontologies/bio2rdf/chembl/queries/atomic_one_filtered.sparql", "../test-share/results/chembl/hermit_1p"};
			args = new String[] {"/users/yzhou/temp/uniprot_debug/core-processed-noDis.owl", "/users/yzhou/temp/uniprot_debug/sample_1_removed.nt", "/media/krr-nas-share/Yujiao/ontologies/bio2rdf/uniprot/queries/atomic_one.sparql", "../test-share/results/uniprot/hermit_1p"}; 		}
//			args = new String[] {"imported.owl", "", "/media/krr-nas-share/Yujiao/ontologies/bio2rdf/uniprot/queries/atomic_one.sparql", "../test-share/results/uniprot/hermit_1p"}; 		}
		
		
		PrintStream ps = args.length < 4 ? null : new PrintStream(new File(args[3])); 
		for (int i = 0; i < args.length; ++i) {
			if (args[i] == null || args[i].equalsIgnoreCase("null")) args[i] = ""; 
			System.out.println("Argument " + i + ": " + args[i]); 
		}
		
//		PrintStream ps = null; // new PrintStream(new File("../test-share/results/reactome/ "));
		if (ps != null) System.setOut(ps);
		
		Timer t = new Timer(); 
		OWLOntology onto = OWLHelper.loadOntology(args[0]);
		OWLOntologyManager man = onto.getOWLOntologyManager();
		
		OWLDatatype date = man.getOWLDataFactory().getOWLDatatype(IRI.create("http://www.w3.org/2001/XMLSchema#date"));
		
		if (onto.containsEntityInSignature(date)) {
			for (OWLOntology o: onto.getImportsClosure())
				for (OWLAxiom axiom: o.getAxioms())
					if (axiom.getDatatypesInSignature().contains(date)) {
						System.out.println("The axiom: " + axiom + " is being ingored. ");
						man.removeAxiom(onto, axiom); 
					}
			man.saveOntology(onto, new FileOutputStream(args[0] = "tbox_hermit.owl"));
			man.removeOntology(onto);
			onto = OWLHelper.loadOntology(man, args[0]); 
			System.out.println("TBox processed in " + t.duration());
		}
		
		try {
			onto = OWLHelper.getImportedOntology(onto, args[1]); 
		} catch (IOException e) {
			e.printStackTrace();
		}
		System.out.println("Ontology loaded in " + t.duration()); 
		System.out.println("ABox axioms:" + onto.getABoxAxioms(true).size()); 
//		for (OWLOntology o: onto.getImportsClosure())
//			for (OWLAxiom axiom: o.getAxioms())
//				System.out.println(axiom); 
		
		Reasoner hermit = new Reasoner(onto);
		if (!hermit.isConsistent()) {
			System.out.println("The ontology is inconsistent."); 
			return ; 
		}
		System.out.println("Preprocessing DONE in " + t.duration()); 

//		System.out.println(hermit.isConsistent()); 
		
		QueryManager queryManager = new QueryManager(); 
		QueryRoller roller = new QueryRoller(onto.getOWLOntologyManager().getOWLDataFactory());
		int failedCounter = 0; 
		Timer total = new Timer(); 
		for (QueryRecord record: queryManager.collectQueryRecords(args[2])) {
			if (Integer.parseInt(record.getQueryID()) < 10) continue; 
			if (total.duration() > 18000) {
				System.out.println("Time out 5h."); 
				return ; 
			}
			System.out.println("--------------------- Query " + record.getQueryID() + " -----------------------");
			System.out.println(record.getQueryText());
			ExecutorService exec = Executors.newSingleThreadExecutor();
			try {
			Future<Boolean> succ = exec.submit(new QueryThread(record, onto, hermit, roller));
			try {
				try {
					if (record.getQueryID().equals("1"))
						System.out.println(succ.get(60, TimeUnit.MINUTES));
					else 
						System.out.println(succ.get(20, TimeUnit.MINUTES));
				} catch (InterruptedException e) {
//					e.printStackTrace();
				} catch (ExecutionException e) {
//					e.printStackTrace();
				} catch (TimeoutException e) {
//					e.printStackTrace();
				} 
			} finally {
				if (succ.cancel(true)) {
					System.out.println("Trying to cancel the current query thread " + (++failedCounter));
				}
			} 
			} finally {
				exec.shutdownNow(); 
			}
		}
		
		if (ps != null) ps.close();
		System.exit(0);
	}

}

class QueryThread implements Callable<Boolean> {

	QueryRecord record;
	OWLOntology onto; 
	Reasoner hermit; 
	QueryRoller roller; 
	
	public QueryThread(QueryRecord record2, OWLOntology onto2, Reasoner hermit2, QueryRoller roller2) {
		record = record2; onto = onto2; hermit = hermit2; roller = roller2; 
	}

	@Override
	public Boolean call() throws Exception {
		Set<String> answers = new HashSet<String>(); 
		Timer t = new Timer(); 
		if (record.getDistinguishedVariables().length > 1) {
			if (record.getDistinguishedVariables().length == 2 && record.getClause().getBodyLength() == 1) {
				dealWithAtomicBinaryQuery(record.getClause().getBodyAtom(0), answers);
				System.out.println("Query " + record.getQueryID() + " The number of answers: " + answers.size());
				System.out.println("Query " + record.getQueryID() + " Total time: " + t.duration());
			}
			else {
				System.out.println("Query " + record.getQueryID() + " The number of answers: Query cannot be processsed.");
				System.out.println("Query " + record.getQueryID() + " Total time: Query cannot be processsed."); 
			}
			return false;  
		}

		OWLClassExpression exp = null;

		try {
			exp = roller.rollUp(record.getClause(), record.getAnswerVariables()[0]); 
		} catch (Exception e) {
			System.out.println("Query " + record.getQueryID() + " The number of answers: Query cannot be processsed.");
			System.out.println("Query " + record.getQueryID() + " Total time: Query cannot be processsed."); 
			return false;
		}			
		System.out.println(exp); 
		for (Node<OWLNamedIndividual> node: hermit.getInstances(exp, false)) {
			for (OWLIndividual ind: node.getEntities()) {
				answers.add(ind.toStringID()); 
			}
		}
		System.out.println("Query " + record.getQueryID() + " The number of answers: " + answers.size());
		System.out.println("Query " + record.getQueryID() + " Total time: " + t.duration());
		return true; 
	}

	private void dealWithAtomicBinaryQuery(Atom bodyAtom, Set<String> answers) {
		StringBuilder sb = new StringBuilder(); 
		OWLDataFactory f = onto.getOWLOntologyManager().getOWLDataFactory(); 
		OWLObjectProperty p = f.getOWLObjectProperty(IRI.create(((AtomicRole) bodyAtom.getDLPredicate()).getIRI()));
		for (Node<OWLNamedIndividual> sub: hermit.getInstances(f.getOWLObjectMinCardinality(1, p), false)) {
			for (Node<OWLNamedIndividual> obj: hermit.getObjectPropertyValues(sub.getRepresentativeElement(), p)) {
				for (OWLNamedIndividual subInd: sub.getEntities()) {
					sb.setLength(0);
					sb.append(subInd.toString()).append(" ");
					int len = sb.length();
					for (OWLNamedIndividual objInd: obj.getEntities()) {
						sb.setLength(len);
						sb.append(objInd.toString()); 
						answers.add(sb.toString()); 
					}
				}
			}
		}
	}
	
}

