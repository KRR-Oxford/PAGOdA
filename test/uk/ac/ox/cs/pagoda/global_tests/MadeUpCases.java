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

import static uk.ac.ox.cs.pagoda.util.TestUtil.getEntityIRI;

public class MadeUpCases {

    @Test(groups = {"existential"})
    public void someTest() throws OWLOntologyCreationException, IOException, OWLOntologyStorageException {

        /*
         * Build test ontology
         * */

        OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
        OWLDataFactory factory = manager.getOWLDataFactory();
        OWLOntology ontology = manager.createOntology();

        OWLClass A1 = factory.getOWLClass(getEntityIRI("A1"));
        manager.addAxiom(ontology, factory.getOWLDeclarationAxiom(A1));
        OWLClass A2 = factory.getOWLClass(getEntityIRI("A2"));
        manager.addAxiom(ontology, factory.getOWLDeclarationAxiom(A2));
        OWLClass A3 = factory.getOWLClass(getEntityIRI("A3"));
        manager.addAxiom(ontology, factory.getOWLDeclarationAxiom(A3));
        OWLClass A4 = factory.getOWLClass(getEntityIRI("A4"));
        manager.addAxiom(ontology, factory.getOWLDeclarationAxiom(A4));
        OWLNamedIndividual a = factory.getOWLNamedIndividual(getEntityIRI("a"));
        OWLNamedIndividual b = factory.getOWLNamedIndividual(getEntityIRI("b"));
        OWLObjectProperty R = factory.getOWLObjectProperty(IRI.create(String.format(TestUtil.NS, "R")));
        manager.addAxiom(ontology, factory.getOWLDeclarationAxiom(R));

        // Class assertions
        manager.addAxiom(ontology, factory.getOWLClassAssertionAxiom(A1, a));
        manager.addAxiom(ontology, factory.getOWLClassAssertionAxiom(A1, b));

        // Minimum cardinality axiom
        manager.addAxiom(ontology, factory.getOWLSubClassOfAxiom(A1, factory.getOWLObjectSomeValuesFrom(R, A2)));
        manager.addAxiom(ontology, factory.getOWLSubClassOfAxiom(A2, factory.getOWLObjectSomeValuesFrom(R, A3)));
        manager.addAxiom(ontology, factory.getOWLSubClassOfAxiom(A3, factory.getOWLObjectSomeValuesFrom(R, A4)));
        manager.addAxiom(ontology, factory.getOWLTransitiveObjectPropertyAxiom(R));

//        manager.saveOntology(ontology, Files.newOutputStream(Paths.get("/home/alessandro/Desktop/test-ontology.owl")));

        /*
         * Test one query
         * */

        QueryReasoner pagoda = QueryReasoner.getInstance(ontology);
        pagoda.loadOntology(ontology);
        if (pagoda.preprocess()) {
            String query = "select distinct ?x ?y " +
                    " where { "
                    + " ?x <" + R.toStringID() + "> _:z . "
                    + " ?y <" + R.toStringID() + "> _:z " +
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
