package uk.ac.ox.cs.pagoda.reasoner.light;

import org.semanticweb.HermiT.model.DLClause;
import uk.ac.ox.cs.JRDFox.JRDFStoreException;
import uk.ac.ox.cs.JRDFox.store.DataStore;
import uk.ac.ox.cs.JRDFox.store.DataStore.UpdateType;
import uk.ac.ox.cs.JRDFox.store.Parameters;
import uk.ac.ox.cs.JRDFox.store.TripleStatus;
import uk.ac.ox.cs.JRDFox.store.TupleIterator;
import uk.ac.ox.cs.pagoda.hermit.DLClauseHelper;
import uk.ac.ox.cs.pagoda.query.AnswerTuples;
import uk.ac.ox.cs.pagoda.query.GapByStore4ID;
import uk.ac.ox.cs.pagoda.rules.DatalogProgram;
import uk.ac.ox.cs.pagoda.rules.Program;
import uk.ac.ox.cs.pagoda.util.*;
import uk.ac.ox.cs.pagoda.util.Timer;
import uk.ac.ox.cs.pagoda.util.disposable.DisposedException;

import java.util.*;

public class BasicQueryEngine extends RDFoxQueryEngine {

    protected DataStore store;
    protected Parameters parameters = new Parameters();
    Set<DLClause> materialisedRules = new HashSet<DLClause>();
    private UFS<String> equalityGroups = null;

    public BasicQueryEngine(String name) {
        super(name);
        store = RDFoxQueryEngine.createDataStore();
        parameters.m_allAnswersInRoot = true;
        parameters.m_useBushy = true;
    }

    public void materialiseFoldedly(DatalogProgram dProgram, GapByStore4ID gap) {
        if(isDisposed()) throw new DisposedException();
        if(gap != null) {
            materialise("lower program", dProgram.getLower().toString());
            String program = dProgram.getUpper().toString();
            try {
                gap.compile(program);
                gap.addBackTo();
                getDataStore().clearRulesAndMakeFactsExplicit();
            } catch(JRDFStoreException e) {
                e.printStackTrace();
            } finally {
                gap.clear();
            }
        }
        else
            materialise("upper program", dProgram.getUpper().toString());
    }

    public int materialiseRestrictedly(DatalogProgram dProgram, GapByStore4ID gap) {
        if(isDisposed()) throw new DisposedException();
        if(gap != null) {
            materialise("lower program", dProgram.getLower().toString());
            String program = dProgram.getUpper().toString();
            try {
                gap.compile(program);
                gap.addBackTo();
                getDataStore().clearRulesAndMakeFactsExplicit();
            } catch(JRDFStoreException e) {
                e.printStackTrace();
            } finally {
                gap.clear();
            }
        }
        else
            materialise("upper program", dProgram.getUpper().toString());

        return 1;
    }

    @Override
    public AnswerTuples evaluate(String queryText) {
        if(isDisposed()) throw new DisposedException();
        return evaluate(queryText, ConjunctiveQueryHelper.getAnswerVariables(queryText)[0]);
    }

    @Override
    public AnswerTuples evaluate(String queryText, String[] answerVars) {
        if(isDisposed()) throw new DisposedException();
        TupleIterator tupleIterator;
        try {
            tupleIterator = store.compileQuery(queryText.replace("_:", "?"), prefixes, parameters);
        } catch(JRDFStoreException e) {
            e.printStackTrace();
            return null;
        }
        return new RDFoxAnswerTuples(answerVars, tupleIterator);
    }

    @Override
    public DataStore getDataStore() {
        if(isDisposed()) throw new DisposedException();
        return store;
    }

    @Override
    public void dispose() {
        super.dispose();
        store.dispose();
    }

    public void outputInstance4BinaryPredicate(String iri, String filename) {
        if(isDisposed()) throw new DisposedException();

        Utility.redirectCurrentOut(filename);
        outputInstance4BinaryPredicate(iri);
        Utility.closeCurrentOut();
    }

    public void outputInstance4BinaryPredicate(String iri) {
        if(isDisposed()) throw new DisposedException();

        outputAnswers("select ?x ?y where { ?x <" + iri + "> ?y . }");
    }

    public void outputInstanceNumbers(String filename) {
        if(isDisposed()) throw new DisposedException();

        TupleIterator predicateTuples = null;
        TupleIterator instanceTuples;
        Set<String> number = new HashSet<String>();
        String predicate;
        try {
            predicateTuples =
                    getDataStore().compileQuery("SELECT DISTINCT ?Y WHERE { ?X <" + Namespace.RDF_TYPE + "> ?Y }", prefixes, parameters);
            for(long multi = predicateTuples.open(); multi != 0; multi = predicateTuples.getNext()) {
                predicate = RDFoxTripleManager.getQuotedTerm(predicateTuples.getResource(0));
                instanceTuples = null;
                try {
                    instanceTuples =
                            getDataStore().compileQuery("SELECT ?X WHERE { ?X <" + Namespace.RDF_TYPE + "> " + predicate + " }", prefixes, parameters);
                    long totalCount = 0;
                    for(long multi1 = instanceTuples.open(); multi1 != 0; multi1 = instanceTuples.getNext()) {
                        totalCount += instanceTuples.getMultiplicity();
                    }
                    number.add(predicate + " * " + totalCount);
                } finally {
                    if(instanceTuples != null) instanceTuples.dispose();
                }
            }
        } catch(JRDFStoreException e) {
            e.printStackTrace();
        } finally {
            if(predicateTuples != null) predicateTuples.dispose();
            predicateTuples = null;
        }

        try {
            predicateTuples =
                    getDataStore().compileQuery("SELECT DISTINCT ?Y WHERE { ?X ?Y ?Z }", prefixes, parameters);
            for(long multi = predicateTuples.open(); multi != 0; multi = predicateTuples.getNext()) {
                predicate = RDFoxTripleManager.getQuotedTerm(predicateTuples.getResource(0));
                instanceTuples = null;
                try {
                    instanceTuples =
                            getDataStore().compileQuery("SELECT ?X ?Z WHERE { ?X " + predicate + " ?Z }", prefixes, parameters);
                    long totalCount = 0;
                    for(long multi1 = instanceTuples.open(); multi1 != 0; multi1 = instanceTuples.getNext())
                        totalCount += instanceTuples.getMultiplicity();
                    number.add(predicate + " * " + totalCount);
                } finally {
                    if(instanceTuples != null) instanceTuples.dispose();
                }
            }

        } catch(JRDFStoreException e) {
            e.printStackTrace();
        } finally {
            if(predicateTuples != null) predicateTuples.dispose();
            predicateTuples = null;
        }

        Utility.redirectCurrentOut(filename);
        String[] ordered = number.toArray(new String[0]);
        Arrays.sort(ordered, new DLPredicateComparator());
        for(String line : ordered) System.out.println(line);
        Utility.closeCurrentOut();

    }

    public TupleIterator internal_evaluateAgainstIDBs(String queryText) throws JRDFStoreException {
        if(isDisposed()) throw new DisposedException();

        TupleIterator iter =
                store.compileQuery(queryText, prefixes, parameters, TripleStatus.TUPLE_STATUS_IDB.union(TripleStatus.TUPLE_STATUS_EDB), TripleStatus.TUPLE_STATUS_IDB);
//		iter.open();
        return iter;
    }

    public TupleIterator internal_evaluate(String queryText) throws JRDFStoreException {
        if(isDisposed()) throw new DisposedException();

        TupleIterator iter = store.compileQuery(queryText, prefixes, parameters);
//		iter.open();
        return iter;
    }

    public void setExpandEquality(boolean flag) {
        if(isDisposed()) throw new DisposedException();

        parameters.m_expandEquality = flag;
    }

    public TupleIterator internal_evaluateNotExpanded(String queryText) throws JRDFStoreException {
        if(isDisposed()) throw new DisposedException();

        parameters.m_expandEquality = false;
        TupleIterator iter = store.compileQuery(queryText, prefixes, parameters);
//		iter.open();
        parameters.m_expandEquality = true;
        return iter;
    }

    public TupleIterator internal_evaluate(String queryText, boolean incrementally) throws JRDFStoreException {
        if(isDisposed()) throw new DisposedException();

        return incrementally ? internal_evaluateAgainstIDBs(queryText) : internal_evaluate(queryText);
    }

    public String getUnusedRules(Collection<DLClause> clauses, boolean toUpdate) {
        if(isDisposed()) throw new DisposedException();

        DLClause clause;
        for(Iterator<DLClause> iter = clauses.iterator(); iter.hasNext(); ) {
            if(materialisedRules.contains(clause = iter.next()))
                iter.remove();
            else if(toUpdate) materialisedRules.add(clause);
        }

        if(clauses.isEmpty()) return null;

        return Program.toString(clauses);
    }

    public void outputMaterialisedRules() {
        if(isDisposed()) throw new DisposedException();

        System.out.println(DLClauseHelper.toString(materialisedRules));
    }

    public void outputAnswers(String query) {
        if(isDisposed()) throw new DisposedException();

        TupleIterator iter = null;
        try {
            iter = internal_evaluate(query);
            System.out.println(query);
            int arity = iter.getArity();
            for(long multi = iter.open(); multi != 0; multi = iter.getNext()) {
                for(int i = 0; i < arity; ++i)
                    System.out.print(RDFoxTripleManager.getQuotedTerm(iter.getResource(i)) + "\t");
                System.out.println();
            }
        } catch(JRDFStoreException e) {
            e.printStackTrace();
        } finally {
            if(iter != null) iter.dispose();
        }
    }

    public void outputInstance4UnaryPredicate(String iri) {
        if(isDisposed()) throw new DisposedException();

        outputAnswers("select ?x where { ?x "
                              + "<http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <"
                              + iri
                              + "> .}");
    }

    public void outputSubjects(String p, String o) {
        if(isDisposed()) throw new DisposedException();

        outputAnswers("select x where { ?x <" + p + "> <" + o + "> . }");
    }

    public void outputObjects(String s, String p) {
        if(isDisposed()) throw new DisposedException();

        outputAnswers("select ?x where { <" + s + "> <" + p + "> ?x . }");
    }

    public void outputIDBFacts() {
        if(isDisposed()) throw new DisposedException();

        TupleIterator iter = null;
        try {
            iter = internal_evaluateAgainstIDBs("select distict ?x ?y ?z where { ?x ?y ?z }");
            for(long multi = iter.open(); multi != 0; multi = iter.getNext()) {
                for(int i = 0; i < 3; ++i)
                    System.out.print(RDFoxTripleManager.getQuotedTerm(iter.getResource(i)) + "\t");
                System.out.println();
            }
        } catch(JRDFStoreException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } finally {
            if(iter != null) iter.dispose();
        }

    }

    public void outputType4Individual(String iri) {
        if(isDisposed()) throw new DisposedException();

        outputAnswers("select ?z where { <" + iri + "> " + Namespace.RDF_TYPE_QUOTED + " ?z }");
    }

    public int getSameAsNumber() {
        if(isDisposed()) throw new DisposedException();

        TupleIterator iter = null;
        int counter = 0;
        try {
            iter = internal_evaluate("select ?x ?y where {?x " + Namespace.EQUALITY_QUOTED + " ?y . }");
            for(long multi = iter.open(); multi != 0; multi = iter.getNext())
                if(iter.getResourceID(0) != iter.getResourceID(1))
                    ++counter;
        } catch(JRDFStoreException e) {
            e.printStackTrace();
        } finally {
            if(iter != null) iter.dispose();
        }
        return counter;
    }

    public UFS<String> getEqualityGroups(boolean reuse) {
        if(isDisposed()) throw new DisposedException();

        if(reuse && equalityGroups != null) return equalityGroups;

        equalityGroups = new UFS<String>();

        TupleIterator answers = null;
        try {
            Timer t = new Timer();
            answers = internal_evaluate("select ?x ?z  where {?x " + Namespace.EQUALITY_QUOTED + "?z . }");
            for(long multi = answers.open(); multi != 0; multi = answers.getNext()) {
                if(answers.getResourceID(0) != answers.getResourceID(1))
                    equalityGroups.merge(answers.getResource(0).m_lexicalForm, answers.getResource(1).m_lexicalForm);
            }
            Utility.logInfo("@Time to group individuals by equality: " + t.duration());
        } catch(JRDFStoreException e) {
            e.printStackTrace();
        } finally {
            if(answers != null) answers.dispose();
        }

        return equalityGroups;
    }

    public void clearRulesAndIDBFacts(Collection<int[]> collection) {
        if(isDisposed()) throw new DisposedException();

//		performDeletion(collection);
        collection.clear();
        try {
            store.clearRulesAndMakeFactsExplicit();
        } catch(JRDFStoreException e) {
            e.printStackTrace();
        }
    }

    protected void outputClassAssertions(String filename) {
        TupleIterator allTuples = null;
        boolean redirect = false;
        try {
            allTuples =
                    getDataStore().compileQuery("SELECT ?X ?Z WHERE { ?X <" + Namespace.RDF_TYPE + "> ?Z }", prefixes, parameters);
            redirect = Utility.redirectCurrentOut(filename);
            for(long multi = allTuples.open(); multi != 0; multi = allTuples.getNext())
                System.out.println(RDFoxTripleManager.getQuotedTerm(allTuples.getResource(0)) + " " + RDFoxTripleManager
                        .getQuotedTerm(allTuples.getResource(1)));
        } catch(JRDFStoreException e) {
            e.printStackTrace();
        } finally {
            if(redirect) Utility.closeCurrentOut();
            if(allTuples != null) allTuples.dispose();
        }
    }

    @SuppressWarnings("unused")
    private void performDeletion(Collection<int[]> collection) {
        Utility.logInfo("Remove all rules, idb facts and added staff...");
        Timer timer = new Timer();
        TupleIterator iter = null;
        try {
            UpdateType ut = UpdateType.ScheduleForDeletion;
            for(int[] t : collection)
                store.addTriplesByResourceIDs(t, ut);

            try {
                iter = internal_evaluateAgainstIDBs("select ?x ?y ?z where { ?x ?y ?z . }");
                for(long multi = iter.open(); multi != 0; multi = iter.getNext()) {
                    int[] triple = new int[3];
                    for(int i = 0; i < 3; ++i)
                        triple[i] = iter.getResourceID(i);
                    store.addTriplesByResourceIDs(triple, ut);
                }
            } finally {
                if(iter != null) iter.dispose();
                iter = null;
            }
            store.applyReasoning(true);
        } catch(JRDFStoreException e) {
            e.printStackTrace();
        }
        Utility.logInfo("Time for deletion: " + timer.duration());
    }


}
