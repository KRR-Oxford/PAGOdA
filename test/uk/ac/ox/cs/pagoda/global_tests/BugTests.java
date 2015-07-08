package uk.ac.ox.cs.pagoda.global_tests;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.*;
import org.testng.Assert;
import org.testng.annotations.Test;
import uk.ac.ox.cs.pagoda.query.AnswerTuple;
import uk.ac.ox.cs.pagoda.query.AnswerTuples;
import uk.ac.ox.cs.pagoda.query.QueryRecord;
import uk.ac.ox.cs.pagoda.reasoner.QueryReasoner;
import uk.ac.ox.cs.pagoda.util.TestUtil;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class BugTests {

    public static final String NS = "http://example.org/test#%s";

    private IRI getEntityIRI(String name) {
        return IRI.create(String.format(NS, name));
    }

    @Test
    public void minimumCardinalityAxiom2() throws OWLOntologyCreationException, IOException, OWLOntologyStorageException {

        /*
         * Build test ontology
         * */

        OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
        OWLDataFactory factory = manager.getOWLDataFactory();
        OWLOntology ontology = manager.createOntology();

//        OWLClass student = factory.getOWLClass(getEntityIRI("Student"));
//        manager.addAxiom(ontology, factory.getOWLDeclarationAxiom(student));
//        OWLClass course = factory.getOWLClass(getEntityIRI("Course"));
//        manager.addAxiom(ontology, factory.getOWLDeclarationAxiom(course));
        OWLClass hardWorkingStudent = factory.getOWLClass(getEntityIRI("HardWorkingStudent"));
        manager.addAxiom(ontology, factory.getOWLDeclarationAxiom(hardWorkingStudent));
        OWLNamedIndividual a = factory.getOWLNamedIndividual(getEntityIRI("a"));
        OWLNamedIndividual b = factory.getOWLNamedIndividual(getEntityIRI("b"));
        OWLObjectProperty takesCourse = factory.getOWLObjectProperty(IRI.create(String.format(NS, "takesCourse")));
        manager.addAxiom(ontology, factory.getOWLDeclarationAxiom(takesCourse));

        // Class assertions
        manager.addAxiom(ontology, factory.getOWLClassAssertionAxiom(hardWorkingStudent, a));	// HardWorkingStudent(a)
        manager.addAxiom(ontology, factory.getOWLClassAssertionAxiom(hardWorkingStudent, b));	// HardWorkingStudent(b)

        // Minimum cardinality axiom
        manager.addAxiom(ontology,
                factory.getOWLEquivalentClassesAxiom(hardWorkingStudent,
                        factory.getOWLObjectMinCardinality(3,
                                takesCourse)));

//        manager.saveOntology(ontology, Files.newOutputStream(Paths.get("/home/alessandro/Desktop/test-ontology.owl")));

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

//    @Test
    public void minimumCardinalityAxiom() throws OWLOntologyCreationException, IOException, OWLOntologyStorageException {

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

    /**
     * Bug: the relevant ontology is not a subset of the original one.
     *
     * @throws OWLOntologyCreationException
     * @throws IOException
     * @throws OWLOntologyStorageException
     */
//    @Test
    public void rTest() throws OWLOntologyCreationException, IOException, OWLOntologyStorageException {

        /*
         * Build test ontology
         * */

        OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
        OWLDataFactory factory = manager.getOWLDataFactory();
        OWLOntology ontology = manager.createOntology();

        OWLClass classA = factory.getOWLClass(getEntityIRI("A"));
        manager.addAxiom(ontology, factory.getOWLDeclarationAxiom(classA));
        OWLClass classB = factory.getOWLClass(getEntityIRI("B"));
        manager.addAxiom(ontology, factory.getOWLDeclarationAxiom(classB));
        OWLNamedIndividual a = factory.getOWLNamedIndividual(getEntityIRI("a"));
        OWLNamedIndividual b = factory.getOWLNamedIndividual(getEntityIRI("b"));
        OWLNamedIndividual c = factory.getOWLNamedIndividual(getEntityIRI("c"));
        OWLObjectProperty roleR = factory.getOWLObjectProperty(IRI.create(String.format(NS, "R")));
        manager.addAxiom(ontology, factory.getOWLDeclarationAxiom(roleR));
        OWLObjectProperty roleP = factory.getOWLObjectProperty(IRI.create(String.format(NS, "P")));
        manager.addAxiom(ontology, factory.getOWLDeclarationAxiom(roleP));

        // Class assertions
        manager.addAxiom(ontology, factory.getOWLClassAssertionAxiom(classA, a));    // A(a)
        manager.addAxiom(ontology, factory.getOWLClassAssertionAxiom(classA, b));    // A(b)
        manager.addAxiom(ontology, factory.getOWLObjectPropertyAssertionAxiom(roleP, c, a)); // P(c,a)

        // Axioms
        // subsetOf(A someValuesFrom(R owl:Thing))
        manager.addAxiom(ontology,
                         factory.getOWLSubClassOfAxiom(classA,
                                                       factory.getOWLObjectSomeValuesFrom(roleR,
                                                                                          factory.getOWLThing())));

        // inverseFunctional(R)
        manager.addAxiom(ontology,
                         factory.getOWLInverseFunctionalObjectPropertyAxiom(roleR));

        // subsetOf(someValuesFrom(inverseOf(P) owl:thing) B)
        manager.addAxiom(ontology,
                         factory.getOWLSubClassOfAxiom(factory.getOWLObjectSomeValuesFrom(roleP.getInverseProperty(),
                                                                                          factory.getOWLThing()),
                                                       classB));
        /*
         * Save the ontology
         * */

//        manager.saveOntology(ontology, Files.newOutputStream(Paths.get("/home/alessandro/Desktop/test-ontology.owl")));

        /*
         * Test one query
         * */

        QueryReasoner pagoda = QueryReasoner.getInstance(ontology);
        pagoda.loadOntology(ontology);
        if(pagoda.preprocess()) {
            String queryStr = "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> " +
                    "select distinct ?x" +
                    " where { "
                    + " ?x rdf:type " + classB +
                    " }";
            QueryRecord queryRecord = pagoda.getQueryManager().create(queryStr);
            System.out.println(queryRecord);
            pagoda.evaluate(queryRecord);
            AnswerTuples answers = queryRecord.getAnswers();
            System.out.println("Difficulty: " + queryRecord.getDifficulty());
            for(AnswerTuple ans; answers.isValid(); answers.moveNext()) {
                ans = answers.getTuple();
                TestUtil.logInfo(ans);
            }
        }
        pagoda.dispose();
    }
}
