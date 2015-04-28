package uk.ac.ox.cs.pagoda.reasoner.full;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.semanticweb.HermiT.Reasoner;
import org.semanticweb.HermiT.model.DLClause;
import org.semanticweb.HermiT.model.Term;
import org.semanticweb.HermiT.model.Variable;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLClassAssertionAxiom;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLDataPropertyAssertionAxiom;
import org.semanticweb.owlapi.model.OWLIndividual;
import org.semanticweb.owlapi.model.OWLObjectPropertyAssertionAxiom;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyManager;

import uk.ac.ox.cs.pagoda.endomorph.Clique;
import uk.ac.ox.cs.pagoda.endomorph.DependencyGraph;
import uk.ac.ox.cs.pagoda.hermit.DLClauseHelper;
import uk.ac.ox.cs.pagoda.query.AnswerTuple;
import uk.ac.ox.cs.pagoda.query.AnswerTuples;
import uk.ac.ox.cs.pagoda.query.QueryRecord;
import uk.ac.ox.cs.pagoda.query.rollup.QueryGraph;
import uk.ac.ox.cs.pagoda.util.ConjunctiveQueryHelper;
import uk.ac.ox.cs.pagoda.util.Namespace;
import uk.ac.ox.cs.pagoda.util.Timer;
import uk.ac.ox.cs.pagoda.util.Utility;

public class HermitChecker implements Checker {
	
	protected OWLDataFactory factory;

	private String queryText; 
	private DLClause queryClause; 
	
	private Reasoner hermit;
	protected String[][] answerVariable;
	protected OWLOntology ontology;
	protected QueryRecord record; 
	protected QueryGraph qGraph = null; 
	boolean toCheck = true;
	
	public HermitChecker(Checker checker) {
		if (checker instanceof HermitChecker) {
			HermitChecker other = (HermitChecker) checker;
			factory = other.factory; 
			queryText = other.queryText;
			queryClause = other.queryClause; 
			answerVariable = other.answerVariable;
			ontology = other.ontology;
//			record = other.record; 
		}
		
		hermit = new Reasoner(ontology);
	}
	
	public HermitChecker(OWLOntology ontology, QueryRecord record, boolean toCheck) {
		this.ontology = ontology;
		queryText = record.getQueryText(); 
		answerVariable = record.getVariables();
		queryClause = record.getClause(); 
//		this.record = record;  
		this.toCheck = toCheck; 
	}
	
	public HermitChecker(OWLOntology ontology, String queryText) {
		this.ontology = ontology;
		this.queryText = queryText; 
		answerVariable = queryText == null ? null : ConjunctiveQueryHelper.getAnswerVariables(queryText);
		queryClause = DLClauseHelper.getQuery(queryText, null); 
//		this.record = null; 
	}
	
	private int tag = 0;
//	AnswerTuple topAnswerTuple = null; 
	AnswerTuple botAnswerTuple = null; 
	
	private void initialiseReasoner() {
		qGraph = new QueryGraph(queryClause.getBodyAtoms(), answerVariable[1], ontology); 
		OWLOntologyManager manager = ontology.getOWLOntologyManager(); 
		factory = manager.getOWLDataFactory();
		
		if (hermit != null)	hermit.dispose();
		
		if (dGraph != null && answerVariable[1].length == 1 && (dGraph.getExits().size() > 1 || dGraph.getEntrances().size() > 1)) {
			Set<OWLAxiom> axioms = new HashSet<OWLAxiom>(); 
			addTopAndBotTuple(axioms);
			manager.addAxioms(ontology, axioms); 
			hermit = new Reasoner(ontology);
			if (!hermit.isConsistent()) {
				hermit.dispose();
				manager.removeAxioms(ontology, axioms); 
				hermit = new Reasoner(ontology); 
			} else {
//				if (topAnswerTuple != null && !check(topAnswerTuple)) tag = -1; 
//				else 
					if (botAnswerTuple != null && check(botAnswerTuple)) tag = 1; 
			}
		}
		else 
			hermit = new Reasoner(ontology);
	}
	
	private void addTopAndBotTuple(Set<OWLAxiom> axioms) {
//		String top_str = Namespace.PAGODA_ANONY + "top";
		String bot_str = Namespace.PAGODA_ANONY + "bot";
//		topAnswerTuple = new AnswerTuple(new uk.ac.ox.cs.JRDFox.model.Individual[] { uk.ac.ox.cs.JRDFox.model.Individual.create(top_str) } ); 
		botAnswerTuple = new AnswerTuple(new uk.ac.ox.cs.JRDFox.model.Individual[] { uk.ac.ox.cs.JRDFox.model.Individual.create(bot_str) } );  
//		OWLIndividual top_ind = factory.getOWLNamedIndividual(IRI.create(top_str));
		OWLIndividual bot_ind = factory.getOWLNamedIndividual(IRI.create(bot_str));
		Map<OWLAxiom, Integer> counter = new HashMap<OWLAxiom, Integer>(); 
		
//		Set<String> topAnswers = new HashSet<String>();
		Set<String> botAnswers = new HashSet<String>();
		OWLIndividual sub, obj; 
//		if (dGraph.getExits().size() > 1) {
//			for (Clique answerClique: dGraph.getExits())  
//				topAnswers.add(((uk.ac.ox.cs.JRDFox.model.Individual) answerClique.getRepresentative().getAnswerTuple().getGroundTerm(0)).getIRI());
//		}
//		else topAnswerTuple = null; 
		
		if (dGraph.getEntrances().size() > 1) {
			for (Clique answerClique: dGraph.getEntrances()) 
				botAnswers.add(((uk.ac.ox.cs.JRDFox.model.Individual) answerClique.getRepresentative().getAnswerTuple().getGroundTerm(0)).getIRI()); 
		}
		else botAnswerTuple = null; 
		
		for (OWLAxiom axiom: ontology.getABoxAxioms(true)) 
			if (axiom instanceof OWLClassAssertionAxiom) {
				OWLClassAssertionAxiom ca = (OWLClassAssertionAxiom) axiom;
				sub = ca.getIndividual();
//				if (topAnswers.contains(sub.toStringID())) 
//					axioms.add(factory.getOWLClassAssertionAxiom(ca.getClassExpression(), top_ind));
				if (botAnswers.contains(sub.toStringID())) 
					inc(counter, factory.getOWLClassAssertionAxiom(ca.getClassExpression(), bot_ind));
			}
			else if (axiom instanceof OWLObjectPropertyAssertionAxiom) {
				OWLObjectPropertyAssertionAxiom oa = (OWLObjectPropertyAssertionAxiom) axiom; 
				sub = oa.getSubject(); obj = oa.getObject(); 
////				if (topAnswers.contains(sub.toStringID()))
////					if (topAnswers.contains(obj.toStringID()))
////						axioms.add(factory.getOWLObjectPropertyAssertionAxiom(oa.getProperty(), top_ind, top_ind));
////					else 
////						axioms.add(factory.getOWLObjectPropertyAssertionAxiom(oa.getProperty(), top_ind, obj));
////				else {
////					if (topAnswers.contains(obj.toStringID()))
////						axioms.add(factory.getOWLObjectPropertyAssertionAxiom(oa.getProperty(), sub, top_ind));
////				}
				
				if (botAnswers.contains(sub.toStringID()))
					if (botAnswers.contains(obj.toStringID()))
						inc(counter, factory.getOWLObjectPropertyAssertionAxiom(oa.getProperty(), bot_ind, bot_ind));
					else 
						inc(counter, factory.getOWLObjectPropertyAssertionAxiom(oa.getProperty(), bot_ind, obj));
				else {
					if (botAnswers.contains(obj.toStringID()))
						inc(counter, factory.getOWLObjectPropertyAssertionAxiom(oa.getProperty(), sub, bot_ind));
				}
				
			}
			else if (axiom instanceof OWLDataPropertyAssertionAxiom) {
				OWLDataPropertyAssertionAxiom da = (OWLDataPropertyAssertionAxiom) axiom; 
				sub = da.getSubject(); 
//				if (topAnswers.contains(sub.toStringID())) 
//					axioms.add(factory.getOWLDataPropertyAssertionAxiom(da.getProperty(), top_ind, da.getObject()));
				
				if (botAnswers.contains(sub.toStringID())) 
					inc(counter, factory.getOWLDataPropertyAssertionAxiom(da.getProperty(), bot_ind, da.getObject()));
			}
		
		int number = botAnswers.size(); 
		for (Map.Entry<OWLAxiom, Integer> entry: counter.entrySet()) {
			if (entry.getValue() == number) 
				axioms.add(entry.getKey());
		}
	}

	private void inc(Map<OWLAxiom, Integer> counter, OWLAxiom newAxiom) {
		Integer number = counter.get(newAxiom);  
		if (number == null) counter.put(newAxiom, 1);
		else counter.put(newAxiom, number + 1); 
	}

	@Override
	public int check(AnswerTuples answers) {
		if (hermit == null) initialiseReasoner();
		int answerCounter = 0, counter = 0; 
		for (; answers.isValid(); answers.moveNext()) {
			++counter; 
			if (check(answers.getTuple())) ++answerCounter;
		}
		answers.dispose();
		
		Utility.logDebug("The number of individuals to be checked by HermiT: " + counter, 
				"The number of correct answers: " + answerCounter);
		return answerCounter; 
	}
	
	private int counter = 0;
	
	@Override
	public boolean check(AnswerTuple answerTuple) {
		if (!toCheck) return false; 
		
		if (hermit == null) initialiseReasoner();
		if (tag != 0) return tag == 1; 
		++counter; 
		Timer t = new Timer(); 
		Map<Variable, Term> sub = answerTuple.getAssignment(answerVariable[1]); 
		Set<OWLAxiom> toCheckAxioms = qGraph.getAssertions(sub);
		
//		for (OWLAxiom axiom: toCheckAxioms)	System.out.println(axiom.toString());
		
		if (hermit.isEntailed(toCheckAxioms)) {
			Utility.logDebug("@TIME to check one tuple: " + t.duration()); 
			return true; 
		}
		Utility.logDebug("@TIME to check one tuple: " + t.duration()); 
		return false; 
	}

	@Override
	public boolean isConsistent() {
		if (hermit == null) initialiseReasoner(); 			
		return hermit.isConsistent();
	}
	

	public void dispose() {
		Utility.logInfo("Hermit was called " + counter + " times.");
		if (hermit != null) hermit.dispose(); 
		hermit = null; 
	}

	private DependencyGraph dGraph = null; 
	
	public void setDependencyGraph(DependencyGraph dGraph) {
		this.dGraph = dGraph; 
	}

}
