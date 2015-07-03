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

public class SkolemisationTests {

    public static final String NS = "http://example.org/test#%s";

    private IRI getEntityIRI(String name) {
        return IRI.create(String.format(NS, name));
    }

//    @Test
    public void commonSuccessorTest() throws OWLOntologyCreationException, IOException, OWLOntologyStorageException {

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
        OWLClass classC = factory.getOWLClass(getEntityIRI("C"));
        manager.addAxiom(ontology, factory.getOWLDeclarationAxiom(classC));
        OWLNamedIndividual c = factory.getOWLNamedIndividual(getEntityIRI("c"));
        OWLNamedIndividual d = factory.getOWLNamedIndividual(getEntityIRI("d"));
        OWLObjectProperty roleR = factory.getOWLObjectProperty(IRI.create(String.format(NS, "R")));
        manager.addAxiom(ontology, factory.getOWLDeclarationAxiom(roleR));

        // Class assertions
        manager.addAxiom(ontology, factory.getOWLClassAssertionAxiom(classA, c));    // A(c)
        manager.addAxiom(ontology, factory.getOWLClassAssertionAxiom(classA, d));    // A(d)

        // Minimum cardinality axiom
        manager.addAxiom(ontology,
                         factory.getOWLSubClassOfAxiom(classA,
                                                       factory.getOWLObjectUnionOf(
                                                               factory.getOWLObjectSomeValuesFrom(roleR, classB),
                                                               factory.getOWLObjectSomeValuesFrom(roleR, classC))));

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
            String queryStr = "select distinct ?x ?y " +
                    " where { "
                    + " ?x <" + roleR.toStringID() + "> _:z . "
                    + " ?y <" + roleR.toStringID() + "> _:z " +
                    " }";
            QueryRecord queryRecord = pagoda.getQueryManager().create(queryStr);
            pagoda.evaluate(queryRecord);
            AnswerTuples answers = queryRecord.getAnswers();
            System.out.println("Difficulty: " + queryRecord.getDifficulty());
            int count = 0;
            for(AnswerTuple ans; answers.isValid(); answers.moveNext()) {
                ans = answers.getTuple();
                TestUtil.logInfo(ans);
                count++;
            }
            Assert.assertEquals(count, 2);
        }
        pagoda.dispose();
    }

//    @Test
    public void yTest() throws OWLOntologyCreationException, IOException, OWLOntologyStorageException {

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
        OWLClass classC = factory.getOWLClass(getEntityIRI("C"));
        manager.addAxiom(ontology, factory.getOWLDeclarationAxiom(classC));
        OWLClass classD = factory.getOWLClass(getEntityIRI("D"));
        manager.addAxiom(ontology, factory.getOWLDeclarationAxiom(classD));
        OWLNamedIndividual a = factory.getOWLNamedIndividual(getEntityIRI("a"));
        OWLNamedIndividual b = factory.getOWLNamedIndividual(getEntityIRI("b"));
        OWLNamedIndividual c = factory.getOWLNamedIndividual(getEntityIRI("c"));
        OWLNamedIndividual d = factory.getOWLNamedIndividual(getEntityIRI("d"));
        OWLObjectProperty roleR = factory.getOWLObjectProperty(IRI.create(String.format(NS, "R")));
        manager.addAxiom(ontology, factory.getOWLDeclarationAxiom(roleR));
        OWLObjectProperty roleS = factory.getOWLObjectProperty(IRI.create(String.format(NS, "S")));
        manager.addAxiom(ontology, factory.getOWLDeclarationAxiom(roleS));
        OWLObjectProperty roleP = factory.getOWLObjectProperty(IRI.create(String.format(NS, "P")));
        manager.addAxiom(ontology, factory.getOWLDeclarationAxiom(roleP));

        // Class assertions
        manager.addAxiom(ontology, factory.getOWLClassAssertionAxiom(classD, a));    // D(a)
        manager.addAxiom(ontology, factory.getOWLClassAssertionAxiom(classD, b));    // D(b)
        manager.addAxiom(ontology, factory.getOWLObjectPropertyAssertionAxiom(roleS, c, a)); // S(c,a)
        manager.addAxiom(ontology, factory.getOWLObjectPropertyAssertionAxiom(roleP, d, b)); // P(d,b)

        // Axioms
        // subsetOf(D someValuesFrom(R owl:Thing))
        manager.addAxiom(ontology,
                         factory.getOWLSubClassOfAxiom(classD,
                                                       factory.getOWLObjectSomeValuesFrom(roleR,
                                                                                          factory.getOWLThing())));
        // subsetOf(someValuesFrom(inverseOf(S) owl:Thing) allValuesFrom(R A))
        manager.addAxiom(ontology,
                         factory.getOWLSubClassOfAxiom(factory.getOWLObjectSomeValuesFrom(roleS.getInverseProperty(),
                                                                                          factory.getOWLThing()),
                                                       factory.getOWLObjectAllValuesFrom(roleR, classA)));
        // subsetOf(someValuesFrom(inverseOf(P) owl:Thing) B)
        manager.addAxiom(ontology,
                         factory.getOWLSubClassOfAxiom(factory.getOWLObjectSomeValuesFrom(roleP.getInverseProperty(),
                                                                                          factory.getOWLThing()),
                                                       classB));
        // subsetOf(someValuesFrom(R A) C)
        manager.addAxiom(ontology,
                         factory.getOWLSubClassOfAxiom(factory.getOWLObjectSomeValuesFrom(roleR, classA), classC));

        /*
         * Save the ontology
         * */

        manager.saveOntology(ontology, Files.newOutputStream(Paths.get("/home/alessandro/Desktop/test-ontology.owl")));

        /*
         * Test one query
         * */

        QueryReasoner pagoda = QueryReasoner.getInstance(ontology);
        pagoda.loadOntology(ontology);
        if(pagoda.preprocess()) {
            String queryStr = "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> " +
                    "select distinct ?x" +
                    " where { "
//                    + " ?x rdf:type " + classB + " ."
//                    + " ?x " + roleR + " "+ "_:y . "
                    + " ?x rdf:type " + classC +
                    " }";
            QueryRecord queryRecord = pagoda.getQueryManager().create(queryStr);
            System.out.println(queryRecord);
            pagoda.evaluate(queryRecord);
            AnswerTuples answers = queryRecord.getAnswers();
            System.out.println("Difficulty: " + queryRecord.getDifficulty());
            int count = 0;
            for(AnswerTuple ans; answers.isValid(); answers.moveNext()) {
                ans = answers.getTuple();
                TestUtil.logInfo(ans);
                count++;
            }
//            Assert.assertEquals(count, 1);
        }
        pagoda.dispose();
    }

    @Test
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
//        OWLClass classC = factory.getOWLClass(getEntityIRI("C"));
//        manager.addAxiom(ontology, factory.getOWLDeclarationAxiom(classC));
//        OWLClass classD = factory.getOWLClass(getEntityIRI("D"));
//        manager.addAxiom(ontology, factory.getOWLDeclarationAxiom(classD));
        OWLNamedIndividual a = factory.getOWLNamedIndividual(getEntityIRI("a"));
        OWLNamedIndividual b = factory.getOWLNamedIndividual(getEntityIRI("b"));
        OWLNamedIndividual c = factory.getOWLNamedIndividual(getEntityIRI("c"));
//        OWLNamedIndividual d = factory.getOWLNamedIndividual(getEntityIRI("d"));
        OWLObjectProperty roleR = factory.getOWLObjectProperty(IRI.create(String.format(NS, "R")));
        manager.addAxiom(ontology, factory.getOWLDeclarationAxiom(roleR));
//        OWLObjectProperty roleF = factory.getOWLObjectProperty(IRI.create(String.format(NS, "F")));
//        manager.addAxiom(ontology, factory.getOWLDeclarationAxiom(roleF));
        OWLObjectProperty roleP = factory.getOWLObjectProperty(IRI.create(String.format(NS, "P")));
        manager.addAxiom(ontology, factory.getOWLDeclarationAxiom(roleP));
//        OWLObjectProperty roleL = factory.getOWLObjectProperty(IRI.create(String.format(NS, "L")));
//        manager.addAxiom(ontology, factory.getOWLDeclarationAxiom(roleL));

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

        manager.saveOntology(ontology, Files.newOutputStream(Paths.get("/home/alessandro/Desktop/test-ontology.owl")));

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
            int count = 0;
            for(AnswerTuple ans; answers.isValid(); answers.moveNext()) {
                ans = answers.getTuple();
                TestUtil.logInfo(ans);
                count++;
            }
//            Assert.assertEquals(count, 1);
        }
        pagoda.dispose();
    }
}
