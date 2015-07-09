package uk.ac.ox.cs.pagoda.endomorph;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.*;
import org.testng.annotations.Test;
import uk.ac.ox.cs.JRDFox.model.GroundTerm;
import uk.ac.ox.cs.JRDFox.model.Individual;
import uk.ac.ox.cs.pagoda.query.AnswerTuple;
import uk.ac.ox.cs.pagoda.summary.Graph;
import uk.ac.ox.cs.pagoda.summary.NodeTuple;
import uk.ac.ox.cs.pagoda.util.TestUtil;

import java.util.HashSet;

import static uk.ac.ox.cs.pagoda.util.TestUtil.getEntityIRI;

public class DependencyGraphTest {

    private OWLOntology getOntology() throws OWLOntologyCreationException {
        OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
        OWLDataFactory factory = manager.getOWLDataFactory();
        OWLOntology ontology = manager.createOntology();

        OWLClass hardWorkingStudent = factory.getOWLClass(getEntityIRI("HardWorkingStudent"));
        manager.addAxiom(ontology, factory.getOWLDeclarationAxiom(hardWorkingStudent));
        OWLNamedIndividual a = factory.getOWLNamedIndividual(getEntityIRI("a"));
        OWLNamedIndividual b = factory.getOWLNamedIndividual(getEntityIRI("b"));
        OWLObjectProperty takesCourse = factory.getOWLObjectProperty(IRI.create(String.format(TestUtil.NS, "takesCourse")));
        manager.addAxiom(ontology, factory.getOWLDeclarationAxiom(takesCourse));

        // Class assertions
        manager.addAxiom(ontology, factory.getOWLClassAssertionAxiom(hardWorkingStudent, a));    // HardWorkingStudent(a)
        manager.addAxiom(ontology, factory.getOWLClassAssertionAxiom(hardWorkingStudent, b));    // HardWorkingStudent(b)

        // Minimum cardinality axiom
        manager.addAxiom(ontology,
                factory.getOWLEquivalentClassesAxiom(hardWorkingStudent,
                        factory.getOWLObjectMinCardinality(3,
                                takesCourse)));
        return ontology;
    }

    @Test
    public void test() throws OWLOntologyCreationException {
        OWLOntology ontology = getOntology();
        Graph graph = new Graph(ontology);
        DependencyGraph dependencyGraph = new DependencyGraph(graph);

        HashSet<NodeTuple> tuples = new HashSet<>();
        tuples.add(graph.getNodeTuple(new AnswerTuple(new GroundTerm[]{Individual.create(String.format(TestUtil.NS, "a")), Individual.create(String.format(TestUtil.NS, "a"))})));
        tuples.add(graph.getNodeTuple(new AnswerTuple(new GroundTerm[]{Individual.create(String.format(TestUtil.NS, "a")), Individual.create(String.format(TestUtil.NS, "b"))})));
        tuples.add(graph.getNodeTuple(new AnswerTuple(new GroundTerm[]{Individual.create(String.format(TestUtil.NS, "b")), Individual.create(String.format(TestUtil.NS, "a"))})));
        tuples.add(graph.getNodeTuple(new AnswerTuple(new GroundTerm[]{Individual.create(String.format(TestUtil.NS, "b")), Individual.create(String.format(TestUtil.NS, "b"))})));

        dependencyGraph.build(tuples);

        System.out.println(dependencyGraph.getTopologicalOrder());
    }
}
