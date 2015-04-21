package uk.ac.ox.cs.pagoda.summary;

import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;

import org.semanticweb.HermiT.model.Constant;
import org.semanticweb.HermiT.model.Individual;
import org.semanticweb.HermiT.model.Term;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassAssertionAxiom;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.model.OWLObjectPropertyAssertionAxiom;
import org.semanticweb.owlapi.model.OWLOntology;

import uk.ac.ox.cs.JRDFox.model.GroundTerm;
import uk.ac.ox.cs.JRDFox.model.Literal;
import uk.ac.ox.cs.pagoda.query.AnswerTuple;

public class Graph {
	
	Set<Node> nodes = new HashSet<Node>(); 
	Map<Node, Edge[]> sortedOutGoingEdges = new HashMap<Node, Edge[]>(); 
	Map<Node, Edge[]> sortedInComingEdges = new HashMap<Node, Edge[]>(); 
	
	public Graph(OWLOntology ontology) {
		Map<Node, Collection<Edge>> outGoingEdges = new HashMap<Node, Collection<Edge>>(); 
		Map<Node, Collection<Edge>> inComingEdges = new HashMap<Node, Collection<Edge>>();
		
		for (OWLAxiom axiom: ontology.getABoxAxioms(true))
			if (axiom instanceof OWLClassAssertionAxiom) 
				addClassAssertion((OWLClassAssertionAxiom) axiom); 
			else if (axiom instanceof OWLObjectPropertyAssertionAxiom)
				addPropertyAssertion((OWLObjectPropertyAssertionAxiom) axiom, inComingEdges, outGoingEdges); 
		
		for (Node node: nodes) {
			sortedOutGoingEdges.put(node, sort(outGoingEdges.get(node)));
			sortedInComingEdges.put(node, sort(inComingEdges.get(node))); 
		}
		
		outGoingEdges.clear();
		inComingEdges.clear(); 
	}
	
	public Collection<Node> getNodes() { return nodes; }

	private void addPropertyAssertion(OWLObjectPropertyAssertionAxiom axiom, 
			Map<Node, Collection<Edge>> inComingEdges, Map<Node, Collection<Edge>> outGoingEdges) {
		
		Node u = getNode(axiom.getSubject().toStringID()), v = getNode(axiom.getObject().toStringID());
			
		nodes.add(u); 
		nodes.add(v); 
		
		Edge e = new Edge(u, v, ((OWLObjectProperty) axiom.getProperty()).toStringID());
		
		Collection<Edge> edges = outGoingEdges.get(u); 
		if (edges == null) {
			edges = new LinkedList<Edge>();
			outGoingEdges.put(u, edges); 
		}
		edges.add(e);
		
		edges = inComingEdges.get(v); 
		if (edges == null) {
			edges = new LinkedList<Edge>(); 
			inComingEdges.put(v, edges); 
		}
		edges.add(e); 
	}

	private void addClassAssertion(OWLClassAssertionAxiom axiom) {
		OWLClass cls = (OWLClass) axiom.getClassExpression();
		Node u; 
//		if (cls.getIRI().toString().startsWith(HermitSummaryFilter.QueryAnswerTermPrefix))
//			u = getNode(axiom.getIndividual().toStringID(), false);
//		else 
			u = getNode(axiom.getIndividual().toStringID());
		
		if (u == null) return ; 
		u.addConcept(cls.toStringID());
		nodes.add(u); 
	}

	public Edge[] getOutGoingEdges(Node u) {
		return sortedOutGoingEdges.get(u); 
	}
	
	public Edge[] getInComingEdges(Node u) {
		return sortedInComingEdges.get(u);
	}
	
	Comparator<Edge> edgeComp = new EdgeComparatorByNodeLabel(); 
	
	public Edge[] sort(Collection<Edge> edges) {
		if (edges == null) return new Edge[0]; 
		Edge[] sortedEdges = new Edge[edges.size()];
		edges.toArray(sortedEdges); 
		Arrays.sort(sortedEdges, edgeComp); 
		return sortedEdges;
	}
	

	private Comparator<Node> coarseNodeComparator = null; 

	public Comparator<Node> getCoarseNodeComparator() {
		if (coarseNodeComparator == null)
			coarseNodeComparator = new EstimatedFeatureComparator(this); 
		return coarseNodeComparator; 
	}
	
	Map<String, Node> allNodes = new HashMap<String, Node>();
	
	private Node getNode(String nodeName) { 
		Node node = null;
		if ((node = allNodes.get(nodeName)) == null) {
			node = new Node(nodeName); 
			allNodes.put(nodeName, node);
		}
		return node; 
	}
	
	private Node getNode(GroundTerm t) {
		if (t instanceof uk.ac.ox.cs.JRDFox.model.Individual)
			return getNode(((uk.ac.ox.cs.JRDFox.model.Individual) t).getIRI());
		else {
			Literal l = (Literal) t;
			return getNode(l.getLexicalForm() + "^^" + l.getDatatype().getIRI());
		}
	}
	
	public Node getNode(Term t) {
		if (t instanceof Individual)
			return getNode(((Individual) t).getIRI()); 
		else if (t instanceof Constant)
			return getNode(((Constant) t).getLexicalForm() + "^^" + ((Constant) t).getDatatypeURI()); 
		return null; 
	}

	public NodeTuple getNodeTuple(AnswerTuple tuple) {
		NodeTuple ret = new NodeTuple(tuple); 
		for (int i = 0; i < tuple.getArity(); ++i) 
			ret.addNode(getNode(tuple.getGroundTerm(i)));
		return ret;
	}

}

