package uk.ac.ox.cs.pagoda.global_tests;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.*;
import org.testng.Assert;
import org.testng.annotations.Test;
import uk.ac.ox.cs.pagoda.query.AnswerTuple;
import uk.ac.ox.cs.pagoda.query.AnswerTuples;
import uk.ac.ox.cs.pagoda.reasoner.QueryReasoner;
import uk.ac.ox.cs.pagoda.util.TestUtil;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class MinimumCardinalityTest {

    public static final String NS = "http://example.org/test#%s";

    private IRI getEntityIRI(String name) {
        return IRI.create(String.format(NS, name));
    }

    @Test(groups = {"BugTesters"})
    public void test() throws OWLOntologyCreationException, IOException, OWLOntologyStorageException {

        /*
         * Build test ontology
         * */

        OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
        OWLDataFactory factory = manager.getOWLDataFactory();
        OWLOntology ontology = manager.createOntology();

        OWLClass student = factory.getOWLClass(getEntityIRI("Student"));
        manager.addAxiom(ontology, factory.getOWLDeclarationAxiom(student));
        OWLClass course = factory.getOWLClass(getEntityIRI("Course"));
        manager.addAxiom(ontology, factory.getOWLDeclarationAxiom(course));
        OWLClass hardWorkingStudent = factory.getOWLClass(getEntityIRI("HardWorkingStudent"));
        manager.addAxiom(ontology, factory.getOWLDeclarationAxiom(hardWorkingStudent));
        OWLNamedIndividual a = factory.getOWLNamedIndividual(getEntityIRI("a"));
        OWLNamedIndividual b = factory.getOWLNamedIndividual(getEntityIRI("b"));
        OWLNamedIndividual c1 = factory.getOWLNamedIndividual(getEntityIRI("c1"));
        OWLNamedIndividual c2 = factory.getOWLNamedIndividual(getEntityIRI("c2"));
        OWLNamedIndividual c3 = factory.getOWLNamedIndividual(getEntityIRI("c3"));
        OWLNamedIndividual d1 = factory.getOWLNamedIndividual(getEntityIRI("d1"));
        OWLNamedIndividual d2 = factory.getOWLNamedIndividual(getEntityIRI("d2"));
        OWLNamedIndividual d3 = factory.getOWLNamedIndividual(getEntityIRI("d3"));
        OWLObjectProperty takesCourse = factory.getOWLObjectProperty(IRI.create(String.format(NS, "takesCourse")));
        manager.addAxiom(ontology, factory.getOWLDeclarationAxiom(takesCourse));

        // Class assertions
        manager.addAxiom(ontology, factory.getOWLClassAssertionAxiom(student, a));	// Student(a)
        manager.addAxiom(ontology, factory.getOWLClassAssertionAxiom(student, b));	// Student(b)
        manager.addAxiom(ontology, factory.getOWLClassAssertionAxiom(course, c1));	// Course(c1)
        manager.addAxiom(ontology, factory.getOWLClassAssertionAxiom(course, c2));	// Course(c2)
        manager.addAxiom(ontology, factory.getOWLClassAssertionAxiom(course, c3));	// Course(c3)
        manager.addAxiom(ontology, factory.getOWLClassAssertionAxiom(course, d1));	// Course(d1)
        manager.addAxiom(ontology, factory.getOWLClassAssertionAxiom(course, d2));	// Course(d2)
        manager.addAxiom(ontology, factory.getOWLClassAssertionAxiom(course, d3));	// Course(d3)

        // Role assertions
        manager.addAxiom(ontology, factory.getOWLObjectPropertyAssertionAxiom(takesCourse, a, c1));	// takesCourse(a,c1)
        manager.addAxiom(ontology, factory.getOWLObjectPropertyAssertionAxiom(takesCourse, a, c2));	// takesCourse(a,c2)
        manager.addAxiom(ontology, factory.getOWLObjectPropertyAssertionAxiom(takesCourse, a, c3));	// takesCourse(a,c3)
        manager.addAxiom(ontology, factory.getOWLObjectPropertyAssertionAxiom(takesCourse, b, d1));	// takesCourse(b,d1)
        manager.addAxiom(ontology, factory.getOWLObjectPropertyAssertionAxiom(takesCourse, b, d2));	// takesCourse(b,d2)
        manager.addAxiom(ontology, factory.getOWLObjectPropertyAssertionAxiom(takesCourse, b, d3));	// takesCourse(b,d3)

        // Minimum cardinality axiom
        manager.addAxiom(ontology,
                         factory.getOWLEquivalentClassesAxiom(hardWorkingStudent,
                                                              factory.getOWLObjectMinCardinality(3,
                                                                                                 takesCourse)));

        manager.saveOntology(ontology, Files.newOutputStream(Paths.get("/home/alessandro/Desktop/test-ontology.owl")));

        /*
         * Test one query
         * */

        QueryReasoner pagoda = QueryReasoner.getInstance(ontology);
        pagoda.loadOntology(ontology);
        if (pagoda.preprocess()) {
            String query = "select distinct ?x ?y " +
                    " where { "
                    + " ?x <" + takesCourse.toStringID() + "> _:z . "
                    + " ?y <" + takesCourse.toStringID() + "> _:z " +
                    " }";
            AnswerTuples answers = pagoda.evaluate(query);
            int count = 0;
            for (AnswerTuple ans; answers.isValid(); answers.moveNext()) {
                ans = answers.getTuple();
                TestUtil.logInfo(ans);
                count++;
            }
            Assert.assertEquals(count, 2);
        }
        pagoda.dispose();
    }
}
