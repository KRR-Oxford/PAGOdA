package uk.ac.ox.cs.pagoda.reasoner;

import org.semanticweb.HermiT.Reasoner;
import org.semanticweb.owlapi.model.*;
import uk.ac.ox.cs.JRDFox.model.Individual;
import uk.ac.ox.cs.pagoda.multistage.MultiStageQueryEngine;
import uk.ac.ox.cs.pagoda.owl.OWLHelper;
import uk.ac.ox.cs.pagoda.owl.QueryRoller;
import uk.ac.ox.cs.pagoda.query.*;
import uk.ac.ox.cs.pagoda.reasoner.light.BasicQueryEngine;
import uk.ac.ox.cs.pagoda.rules.DatalogProgram;
import uk.ac.ox.cs.pagoda.util.Utility;
import uk.ac.ox.cs.pagoda.util.disposable.DisposedException;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

class HermiTReasoner extends QueryReasoner {

    Reasoner hermit;

    BasicQueryEngine upperStore = null;

    OWLOntology onto;
    OWLDataFactory factory;

    String importedOntologyPath = null;

    QueryRoller roller;
    boolean toCheckSatisfiability;

    public HermiTReasoner(boolean toCheckSatisfiability) {
        this.toCheckSatisfiability = toCheckSatisfiability;
    }

    @Override
    public void loadOntology(OWLOntology ontology) {
        if(isDisposed()) throw new DisposedException();
        onto = ontology;
    }

    @Override
    public boolean preprocess() {
        if(isDisposed()) throw new DisposedException();
        OWLOntology tbox = onto;
        try {
            onto = OWLHelper.getImportedOntology(tbox, importedData.toString().split(ImportDataFileSeparator));
            importedOntologyPath = OWLHelper.getOntologyPath(onto);
        } catch(OWLOntologyCreationException | OWLOntologyStorageException | IOException e) {
            e.printStackTrace();
        }

        DatalogProgram datalogProgram = new DatalogProgram(tbox, false);
        importData(datalogProgram.getAdditionalDataFile());
        upperStore = new MultiStageQueryEngine("rl-upper", false);
        upperStore.importRDFData("data", importedData.toString());
        GapByStore4ID gap = new GapByStore4ID(upperStore);
        upperStore.materialiseFoldedly(datalogProgram, gap);
        gap.clear();

        factory = onto.getOWLOntologyManager().getOWLDataFactory();
        roller = new QueryRoller(factory);

        hermit = new Reasoner(onto);
        return isConsistent();
    }

    @Override
    public boolean isConsistent() {
        if(isDisposed()) throw new DisposedException();
        if(toCheckSatisfiability)
            return hermit.isConsistent();
        return true;
    }

    @Override
    public void evaluate(QueryRecord record) {
        if(isDisposed()) throw new DisposedException();
        String[] disVars = record.getDistinguishedVariables();
        Set<OWLNamedIndividual> individuals = onto.getIndividualsInSignature(true);
        if(disVars.length == 1) {
            OWLClassExpression clsExp = roller.rollUp(record.getClause(), record.getAnswerVariables()[0]);
            Set<AnswerTuple> answers = new HashSet<AnswerTuple>();
            for(OWLNamedIndividual individual : individuals) {
                Utility.logDebug("checking ... " + individual);
                if(hermit.isEntailed(factory.getOWLClassAssertionAxiom(clsExp, individual))) {
                    answers.add(new AnswerTuple(new Individual[]{Individual.create(individual.toStringID())}));
                }
            }
            record.updateLowerBoundAnswers(new AnswerTuplesImp(record.getAnswerVariables(), answers));
            record.markAsProcessed();
        }
        else {
            // FIXME join here
            record.markAsProcessed();
        }
    }

    @Override
    public void evaluateUpper(QueryRecord record) {
        if(isDisposed()) throw new DisposedException();
        AnswerTuples rlAnswer = null;
        try {
            rlAnswer = upperStore.evaluate(record.getQueryText(), record.getAnswerVariables());
            record.updateUpperBoundAnswers(rlAnswer, true);
        } finally {
            if(rlAnswer != null) rlAnswer.dispose();
        }
    }

    @Override
    public void dispose() {
        super.dispose();
        if(importedOntologyPath != null) {
            File tmp = new File(importedOntologyPath);
            if(tmp.exists()) tmp.delete();
        }
    }

}
