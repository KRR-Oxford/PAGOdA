package uk.ac.ox.cs.pagoda.summary;

import java.io.File;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

import org.semanticweb.HermiT.model.Atom;
import org.semanticweb.HermiT.model.DLClause;
import org.semanticweb.HermiT.model.Individual;
import org.semanticweb.HermiT.model.Term;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLClassAssertionAxiom;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLDataPropertyAssertionAxiom;
import org.semanticweb.owlapi.model.OWLLiteral;
import org.semanticweb.owlapi.model.OWLNamedIndividual;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.model.OWLObjectPropertyAssertionAxiom;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.model.OWLOntologyStorageException;

import uk.ac.ox.cs.JRDFox.model.GroundTerm;
import uk.ac.ox.cs.JRDFox.model.Literal;
import uk.ac.ox.cs.pagoda.owl.OWLHelper;
import uk.ac.ox.cs.pagoda.query.AnswerTuple;
import uk.ac.ox.cs.pagoda.query.QueryRecord;
import uk.ac.ox.cs.pagoda.util.Namespace;
import uk.ac.ox.cs.pagoda.util.SparqlHelper;
import uk.ac.ox.cs.pagoda.util.Utility;

public class Summary {
	
	OWLOntologyManager manager; 
	OWLDataFactory factory; 
	OWLOntology ontology, summarisedOntology;  
	Graph graph;

	public Summary(OWLOntology ontology) {
		OWLHelper.identifyAndChangeAnnotationAssertions(ontology);
		this.ontology = ontology;
		graph = new Graph(ontology);
		factory = (manager = ontology.getOWLOntologyManager()).getOWLDataFactory(); 
	}
	
	public Summary(OWLOntology ontology, Graph graph) {
		this.ontology = ontology; 
		this.graph = graph; 
		factory = (manager = ontology.getOWLOntologyManager()).getOWLDataFactory(); 
	}
	
	Map<String, String> label2representative = new HashMap<String, String>(); 
	Map<String, String> representatives = new HashMap<String, String>();
	
	public Collection<String> getRepresentatives() {
		return representatives.values(); 
	}
	
	Map<String, LinkedList<String>> groups = null;
	
	public Collection<String> getGroup(String representative) {
		if (groups == null) {
			groups = new HashMap<String, LinkedList<String>>();
			LinkedList<String> group; 
			for (Map.Entry<String, String> entry: representatives.entrySet()) {
				if ((group = groups.get(entry.getValue())) == null)
					group = new LinkedList<String>(); 
				group.add(entry.getKey()); 
			}
		}
		return groups.get(representative); 
	}
	
	private void process(OWLOntology ontology, OWLOntology abstractOntology) {
		OWLOntologyManager manager = ontology.getOWLOntologyManager(); 
		groupIndividualsByConcepts();

		manager.addAxioms(abstractOntology, ontology.getRBoxAxioms(true)); 
		manager.addAxioms(abstractOntology, ontology.getTBoxAxioms(true));

		OWLAxiom newAxiom; 
		for (OWLAxiom axiom: ontology.getABoxAxioms(true)) {
			newAxiom = summeriseAxiom(axiom); 
			manager.addAxiom(abstractOntology, newAxiom);
		}
		
		OWLObjectProperty sameAs = factory.getOWLObjectProperty(IRI.create(Namespace.EQUALITY)); 
		for (Map.Entry<String, String> entry: representatives.entrySet()) 
			if (!entry.getKey().equals(entry.getValue())) 
				manager.addAxiom(abstractOntology, 
						factory.getOWLObjectPropertyAssertionAxiom(
								sameAs, 
								factory.getOWLNamedIndividual(IRI.create(entry.getKey())), 
								factory.getOWLNamedIndividual(IRI.create(entry.getValue())))); 
	}
	
	private void groupIndividualsByConcepts() {
		String name, label, representative;
		Utility.logDebug("grouping individuals by its concepts"); 
		
		for (Node node: graph.getNodes()) {
			name = node.getName();
			label = node.getLabel();

			if ((representative = label2representative.get(label)) == null) {
				representative = name; 
				label2representative.put(label, name); 
			}
			
			representatives.put(name, representative);
		}
	}

	private OWLAxiom summeriseAxiom(OWLAxiom axiom) {
		if (axiom instanceof OWLClassAssertionAxiom) {
			OWLClassAssertionAxiom assertion = (OWLClassAssertionAxiom) axiom;
			OWLNamedIndividual a = getRepresentativeIndividual(assertion.getIndividual().toStringID()); 
			return factory.getOWLClassAssertionAxiom(assertion.getClassExpression(), a); 
		}
		else if (axiom instanceof OWLObjectPropertyAssertionAxiom) {
			OWLObjectPropertyAssertionAxiom assertion = (OWLObjectPropertyAssertionAxiom) axiom; 
			OWLNamedIndividual a = getRepresentativeIndividual(assertion.getSubject().toStringID()); 
			OWLNamedIndividual b = getRepresentativeIndividual(assertion.getObject().toStringID());
			return factory.getOWLObjectPropertyAssertionAxiom(assertion.getProperty(), a, b); 
		}
		else if (axiom instanceof OWLDataPropertyAssertionAxiom) {
			OWLDataPropertyAssertionAxiom assertion = (OWLDataPropertyAssertionAxiom) axiom; 
			OWLNamedIndividual a = getRepresentativeIndividual(assertion.getSubject().toStringID()); 
			OWLLiteral b = assertion.getObject(); 
			return factory.getOWLDataPropertyAssertionAxiom(assertion.getProperty(), a, b); 
			
		}
		else {
			Utility.logError("Unknown axiom: " + axiom); 
			return null;
		}
	}

	public OWLNamedIndividual getRepresentativeIndividual(String name) {
		return factory.getOWLNamedIndividual(IRI.create(getRepresentativeName(name))); 
	}
	
	public String getRepresentativeName(String name) {
		String rep = representatives.get(name); 
		if (rep == null) return name;  
		return rep; 
	}
	
	public OWLOntology getSummary() {
		if (summarisedOntology == null) {
			try {
				summarisedOntology = ontology.getOWLOntologyManager().createOntology();
			} catch (OWLOntologyCreationException e) {
				summarisedOntology = null; 
				e.printStackTrace();
			}
			process(ontology, summarisedOntology);
		}
		return summarisedOntology;
	}
	
	public void save(String fileName) {
		try {
			manager.saveOntology(summarisedOntology, IRI.create(new File(fileName)));
		} catch (OWLOntologyStorageException e) {
			e.printStackTrace();
		}
	}
	
	private GroundTerm getSummary(GroundTerm t) {
		if (t instanceof Literal) return t; 
		return uk.ac.ox.cs.JRDFox.model.Individual.create(getSummary(((uk.ac.ox.cs.JRDFox.model.Individual) t).getIRI())); 
	}

	public String getSummary(QueryRecord record) {
		DLClause queryClause = getSummary(record.getClause()); 
		return SparqlHelper.getSPARQLQuery(queryClause.getBodyAtoms(), record.getAnswerVariables());
	}
	
	public DLClause getSummary(DLClause clause) {
		Atom[] newHeadAtoms = new Atom[clause.getHeadLength()], newBodyAtoms = new Atom[clause.getBodyLength()];
		int index = 0; 
		for (Atom atom: clause.getHeadAtoms()) 
			newHeadAtoms[index++] = getSummary(atom); 
		for (Atom atom: clause.getBodyAtoms()) 
			newBodyAtoms[index++] = getSummary(atom); 
		
		return DLClause.create(newHeadAtoms, newBodyAtoms); 
	}
	
	public Atom getSummary(Atom atom) {
		Term[] args = new Term [atom.getArity()]; 
		for (int i = 0; i < atom.getArity(); ++i)
			if ((args[i] = atom.getArgument(i)) instanceof Individual)
				args[i] = Individual.create(getSummary(atom.getArgument(i).toString()));
		return Atom.create(atom.getDLPredicate(), args); 
	}
	
	public String getSummary(String name) {
		return getRepresentativeName(OWLHelper.removeAngles(name));
	}

	public AnswerTuple getSummary(AnswerTuple answer) {
		int arity = answer.getArity();
		GroundTerm[] t = new GroundTerm[arity]; 
		for (int i = 0; i < arity; ++i)
			t[i] = getSummary(answer.getGroundTerm(i)); 
		return new AnswerTuple(t); 
	}

}
