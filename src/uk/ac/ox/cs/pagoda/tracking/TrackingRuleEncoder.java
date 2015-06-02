package uk.ac.ox.cs.pagoda.tracking;

import org.semanticweb.HermiT.model.*;
import org.semanticweb.owlapi.model.OWLOntology;
import uk.ac.ox.cs.JRDFox.model.Datatype;
import uk.ac.ox.cs.JRDFox.model.GroundTerm;
import uk.ac.ox.cs.JRDFox.model.Literal;
import uk.ac.ox.cs.pagoda.MyPrefixes;
import uk.ac.ox.cs.pagoda.hermit.DLClauseHelper;
import uk.ac.ox.cs.pagoda.query.AnswerTuple;
import uk.ac.ox.cs.pagoda.query.AnswerTuples;
import uk.ac.ox.cs.pagoda.query.GapTupleIterator;
import uk.ac.ox.cs.pagoda.query.QueryRecord;
import uk.ac.ox.cs.pagoda.reasoner.light.BasicQueryEngine;
import uk.ac.ox.cs.pagoda.reasoner.light.RDFoxTripleManager;
import uk.ac.ox.cs.pagoda.rules.UpperDatalogProgram;
import uk.ac.ox.cs.pagoda.util.Namespace;
import uk.ac.ox.cs.pagoda.util.Utility;
import uk.ac.ox.cs.pagoda.util.disposable.Disposable;
import uk.ac.ox.cs.pagoda.util.disposable.DisposedException;

import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.*;

public abstract class TrackingRuleEncoder extends Disposable {
    public static final String trackingPredicateRelation = Namespace.PAGODA_AUX + "isTrackingPredicateFor";
    public static final String QueryPredicate = Namespace.PAGODA_AUX + "Query";
    protected BasicQueryEngine store;
    protected QueryRecord currentQuery;
    protected Set<String> unaryPredicates = new HashSet<String>();
    protected Set<String> binaryPredicates = new HashSet<String>();
    UpperDatalogProgram program;
    Collection<DLClause> trackingClauses = new HashSet<DLClause>();
    Collection<DLClause> queryClauses = new LinkedList<DLClause>();
    Map<Integer, DLClause> index2clause = new HashMap<Integer, DLClause>();
    Map<DLClause, Integer> clause2index = new HashMap<DLClause, Integer>();
    String equalityRelatedRuleText = null;
    boolean ruleEncoded = false;
    DLPredicate selected;
    private boolean queryEncoded = false;
    private LinkedList<int[]> addedData = new LinkedList<int[]>();
    private String trackingSuffix;

    public TrackingRuleEncoder(UpperDatalogProgram program, BasicQueryEngine store) {
        this.program = program;
        this.store = store;
    }

    public static String getRawTerm(GroundTerm r) {
        if(r instanceof uk.ac.ox.cs.JRDFox.model.Individual)
            return ((uk.ac.ox.cs.JRDFox.model.Individual) r).getIRI();
        else {
            Literal l = (Literal) r;
            if(l.getDatatype().equals(Datatype.XSD_STRING) && l.getDatatype().equals(Datatype.RDF_PLAIN_LITERAL))
                return "\"" + l.getLexicalForm() + "\"";
            else
                return "\"" + l.getLexicalForm() + "\"^^<" + l.getDatatype().getIRI() + ">";
        }
    }

    protected static String getTrackingSuffix(String queryID) {
        return "_AUXt" + queryID;
    }

    public boolean encodingRules() {
        if(isDisposed()) throw new DisposedException();
        if(ruleEncoded) return false;
        ruleEncoded = true;

//		for (DLClause clause: program.getClauses(currentQuery.getClause())) {
        for(DLClause clause : program.getClauses()) {
            encodingRule(clause);
        }
        return true;
    }

    public Collection<int[]> getAddedData() {
        if(isDisposed()) throw new DisposedException();
        return addedData;
    }

    public String getTrackingPredicate(String predicateIRI) {
        if(isDisposed()) throw new DisposedException();
        if(predicateIRI.startsWith("<"))
            return predicateIRI.replace(">", getTrackingSuffix(currentQuery.getQueryID()) + ">");
        else
            return predicateIRI + getTrackingSuffix(currentQuery.getQueryID());
    }

    public void setCurrentQuery(QueryRecord record) {
        if(isDisposed()) throw new DisposedException();
        deprecateTrackingAndQueryRules();
        currentQuery = record;
        selected = AtomicConcept.create(getSelectedPredicate());
        trackingSuffix = "_AUXt" + currentQuery.getQueryID();
    }

    @Override
    public void dispose() {
        super.dispose();
        deprecateTrackingAndQueryRules();
    }

    public String getTrackingProgram() {
        if(isDisposed()) throw new DisposedException();
        StringBuilder sb = getTrackingProgramBody();
        sb.insert(0, MyPrefixes.PAGOdAPrefixes.prefixesText());
        return sb.toString();
    }

    public void saveTrackingRules(String fileName) {
        if(isDisposed()) throw new DisposedException();
        BufferedWriter writer = null;
        try {
            writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(fileName)));
            writer.write(getTrackingProgram());
            writer.close();
        } catch(IOException e) {
            e.printStackTrace();
            return;
        }
        Utility.logDebug("The tracking rules are saved in " + fileName + ".");
    }

    public String getSelectedPredicate() {
        if(isDisposed()) throw new DisposedException();
        return getIRI("_selected" + currentQuery.getQueryID());
    }

    public DLClause getSelectedClause(String iri) {
        if(isDisposed()) throw new DisposedException();
        int index = iri.lastIndexOf("_r") + 2;
        int ruleIndex = Integer.parseInt(iri.substring(index));
        return index2clause.get(ruleIndex);
    }

    /**
     * SELECT ?X
     * WHERE {
     * ?X <http://www.w3.org/1999/02/22-rdf-syntax-ns#:type> :_selected?
     * }
     */
    public String getSelectedSPARQLQuery() {
        if(isDisposed()) throw new DisposedException();
        StringBuilder builder = new StringBuilder();
        builder.append("SELECT ?X\nWHERE {\n?X <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> ");
        builder.append(selected.toString()).append("\n}");
        return builder.toString();
    }

    public OWLOntology getOntology() {
        if(isDisposed()) throw new DisposedException();
        return program.getOntology();
    }

    public UpperDatalogProgram getProgram() {
        if(isDisposed()) throw new DisposedException();
        return program;
    }

    public String getOriginalPredicate(String p) {
        if(isDisposed()) throw new DisposedException();
        if(p.startsWith("<")) {
            if(!p.endsWith(trackingSuffix + ">")) return null;
        }
        else if(!p.endsWith(trackingSuffix)) return null;

        return p.replace(trackingSuffix, "");
    }

    public boolean isAuxPredicate(String p) {
        if(isDisposed()) throw new DisposedException();
        return false;
    }

    protected abstract String getEqualityRelatedRuleText();

    protected String getIRI(String name) {
        return program.getOntology().getOntologyID().getOntologyIRI().toString() + "#" + name;
    }

    protected abstract void encodingRule(DLClause clause);

    protected Individual getIndividual4GeneralRule(DLClause clause) {
        clause = program.getCorrespondingClause(clause);
//		if (clause == null)
//			return Individual.create(getIRI("_r0"));

        int index;
        if(clause2index.containsKey(clause))
            index = clause2index.get(clause);
        else {
            index = clause2index.size() + 1;
            index2clause.put(index, clause);
            clause2index.put(clause, index);
        }

        return Individual.create(getIRI("_r" + index));
    }

    private void encodingQuery(QueryRecord[] botQuerRecords) {
        if(queryEncoded) return;
        queryEncoded = true;

        if(currentQuery.getArity() > 0 && currentQuery.getArity() < 3) {
            encodingAtomicQuery(botQuerRecords);

        }
        else {
            DLClause queryClause = currentQuery.getClause();
            Atom[] bodyAtoms = queryClause.getBodyAtoms();
            for(Atom bodyAtom : bodyAtoms)
                addQueryRule(bodyAtom, bodyAtoms);
        }
    }

    private void addQueryRule(Atom atom, Atom[] atoms) {
        DLClause newClause;
        Atom headAtom;

        headAtom = Atom.create(
                getTrackingDLPredicate(atom.getDLPredicate()),
                DLClauseHelper.getArguments(atom));
        newClause = DLClause.create(new Atom[]{headAtom}, atoms);
        queryClauses.add(newClause);
    }

    protected String getCurrentQueryPredicate() {
        return QueryPredicate + currentQuery.getQueryID();
    }

    protected void encodingAtomicQuery(QueryRecord[] botQuerRecords) {
        encodingAtomicQuery(botQuerRecords, false);
    }

    protected void encodingAtomicQuery(QueryRecord[] botQuerRecords, boolean includingBottom) {
        DLClause queryClause = currentQuery.getClause();
        AnswerTuples answerTuples = currentQuery.getGapAnswers();
        String[] answerVariables = currentQuery.getAnswerVariables();

        String currentQueryPredicate = getCurrentQueryPredicate();
        Atom newAtom;
        if(answerVariables.length == 1) {
            AtomicConcept queryConcept = AtomicConcept.create(currentQueryPredicate);
            newAtom = Atom.create(queryConcept, Variable.create(answerVariables[0]));
        }
        else {
            AtomicRole queryRole = AtomicRole.create(currentQueryPredicate);
            newAtom = Atom.create(queryRole, Variable.create(answerVariables[0]), Variable.create(answerVariables[1]));
        }

        Atom[] bodyAtoms = queryClause.getBodyAtoms();
        Atom[] newBodyAtoms = new Atom[queryClause.getBodyLength() + 1];
        for(int i = 0; i < bodyAtoms.length; ++i)
            newBodyAtoms[i + 1] = bodyAtoms[i];
        newBodyAtoms[0] = newAtom;

        for(Atom bodyAtom : bodyAtoms)
            addQueryRule(bodyAtom, newBodyAtoms);

        RDFoxTripleManager tripleManager = new RDFoxTripleManager(store.getDataStore(), true);
//		MyPrefixes prefixes = MyPrefixes.PAGOdAPrefixes;
        int[] triple;
        int predicate = tripleManager.getResourceID(AtomicConcept.create(currentQueryPredicate));
        int rdftype = tripleManager.getResourceID(AtomicRole.create(Namespace.RDF_TYPE));
        if(answerVariables.length == 1) {
            for(AnswerTuple answer; answerTuples.isValid(); answerTuples.moveNext()) {
                answer = answerTuples.getTuple();
                triple =
                        new int[]{tripleManager.getResourceID(getRawTerm(answer.getGroundTerm(0))), rdftype, predicate};
                addedData.add(triple);
                tripleManager.addTripleByID(triple);
//				System.out.println("To be removed ... \n" + tripleManager.getRawTerm(tripleManager.getResourceID(prefixes.expandIRI(answer.getRawTerm(0)))) + " " + tripleManager.getRawTerm(rdftype) + " " + tripleManager.getRawTerm(predicate));
            }
        }
        else {
            for(AnswerTuple answer; answerTuples.isValid(); answerTuples.moveNext()) {
                answer = answerTuples.getTuple();
                triple =
                        new int[]{tripleManager.getResourceID(getRawTerm(answer.getGroundTerm(0))), predicate, tripleManager
                                .getResourceID(getRawTerm(answer.getGroundTerm(1)))};
                addedData.add(triple);
                tripleManager.addTripleByID(triple);
            }
        }
//        answerTuples.dispose();

        if(includingBottom && botQuerRecords != null) {
            int index = 0;
            GroundTerm t;
            String raw;
            for(QueryRecord botQueryRecord : botQuerRecords) {
                answerTuples = botQueryRecord.getGapAnswers();
                int subID = 0;//botQueryRecord.getSubID();
                String p = subID == 0 ? AtomicConcept.NOTHING.getIRI() : Namespace.OWL_NS + "Nothing_final" + (++index);
                predicate = tripleManager.getResourceID(AtomicConcept.create(p = getTrackingPredicate(p)));
                for(AnswerTuple answer; answerTuples.isValid(); answerTuples.moveNext()) {
                    answer = answerTuples.getTuple();
//					System.out.println("To be removed ... " + answer.getRawTerm(0));
                    raw = ((t =
                            answer.getGroundTerm(0)) instanceof uk.ac.ox.cs.JRDFox.model.Individual) ? ((uk.ac.ox.cs.JRDFox.model.Individual) t)
                            .getIRI() : t.toString();
                    triple = new int[]{tripleManager.getResourceID(raw), rdftype, predicate};
                    addedData.add(triple);
                    tripleManager.addTripleByID(triple);
                }
//                answerTuples.dispose();
            }
        }

        Utility.logDebug(addedData.size() + " triples are added into the store.");
    }

    protected DLPredicate getGapDLPredicate(DLPredicate dlPredicate) {
        return getDLPredicate(dlPredicate, GapTupleIterator.gapPredicateSuffix);
    }

    DLPredicate getDLPredicate(DLPredicate p, String suffix) {
        if(isDisposed()) throw new DisposedException();
        if(p instanceof AtomicConcept)
            return AtomicConcept.create(((AtomicConcept) p).getIRI() + suffix);
        else if(p instanceof DatatypeRestriction) {
            DatatypeRestriction restriction = (DatatypeRestriction) p;
            String newURI = restriction.getDatatypeURI() + suffix;
            return getDatatypeRestriction(restriction, newURI);
        }
        else if(p instanceof AtomicRole)
            return AtomicRole.create(((AtomicRole) p).getIRI() + suffix);
        else if(p instanceof AnnotatedEquality || p instanceof Equality)
            return AtomicRole.create(Namespace.EQUALITY + suffix);
        else if(p instanceof Inequality)
            return AtomicRole.create(Namespace.INEQUALITY + suffix);
        else if(p instanceof DatatypeRestriction)
            return AtomicConcept.create(((DatatypeRestriction) p).getDatatypeURI() + suffix);
        else {
            Utility.logDebug("strange DL predicate appeared ... " + p,
                             "the program paused here in TrackingRuleEncoderDisj.java");
            return null;
        }
    }

    protected DLPredicate getTrackingDLPredicate(DLPredicate dlPredicate) {
        return getDLPredicate(dlPredicate, getTrackingSuffix(currentQuery.getQueryID()));
    }

    protected DLPredicate getDatatypeRestriction(DatatypeRestriction restriction, String newName) {
        int length = restriction.getNumberOfFacetRestrictions();
        String[] facets = new String[length];
        Constant[] values = new Constant[length];
        for(int i = 0; i < length; ++i) {
            facets[i] = restriction.getFacetURI(i);
            values[i] = restriction.getFacetValue(i);
        }
        return DatatypeRestriction.create(newName, facets, values);
    }

    private String getTrackingRuleText() {
        return DLClauseHelper.toString(trackingClauses);
    }

    private String getQueryRuleText() {
        return DLClauseHelper.toString(queryClauses);
    }

    protected StringBuilder getTrackingProgramBody() {
        encodingRules();
        encodingQuery(new QueryRecord[0]);

        StringBuilder sb = new StringBuilder();
        sb.append(getTrackingRuleText());
        sb.append(getEqualityRelatedRuleText());
        sb.append(getQueryRuleText());
        return sb;
    }

    private void deprecateTrackingAndQueryRules() {
        trackingClauses.clear();
        queryClauses.clear();
        addedData.clear();
        ruleEncoded = false;
        queryEncoded = false;
    }

}
