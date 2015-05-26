package uk.ac.ox.cs.pagoda.tracking;

import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Set;

import org.semanticweb.HermiT.model.DLClause;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLDataProperty;
import org.semanticweb.owlapi.model.OWLLiteral;
import org.semanticweb.owlapi.model.OWLIndividual;
import org.semanticweb.owlapi.model.OWLObject;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;

import uk.ac.ox.cs.pagoda.MyPrefixes;
import uk.ac.ox.cs.pagoda.hermit.DLClauseHelper;
import uk.ac.ox.cs.pagoda.owl.OWLHelper;
import uk.ac.ox.cs.pagoda.query.AnswerTuple;
import uk.ac.ox.cs.pagoda.query.QueryRecord;
import uk.ac.ox.cs.pagoda.reasoner.light.BasicQueryEngine;
import uk.ac.ox.cs.pagoda.reasoner.light.RDFoxTripleManager;
import uk.ac.ox.cs.pagoda.util.Namespace;
import uk.ac.ox.cs.pagoda.util.Timer;
import uk.ac.ox.cs.pagoda.util.UFS;
import uk.ac.ox.cs.pagoda.util.Utility;
import uk.ac.ox.cs.JRDFox.JRDFStoreException;
import uk.ac.ox.cs.JRDFox.model.Datatype;
import uk.ac.ox.cs.JRDFox.store.DataStore;
import uk.ac.ox.cs.JRDFox.store.Resource;
import uk.ac.ox.cs.JRDFox.store.TupleIterator;
import uk.ac.ox.cs.JRDFox.store.DataStore.UpdateType;

public class QueryTracker {

	QueryRecord m_record;
	BasicQueryEngine m_dataStore;
	TrackingRuleEncoder m_encoder;

	OWLOntologyManager m_manager;
	Set<OWLAxiom> m_tBoxAxioms;
	
	UFS<String> equalityGroups; 

	public QueryTracker(TrackingRuleEncoder encoder, BasicQueryEngine lowerStore, QueryRecord queryRecord) {
		m_encoder = encoder;
		m_dataStore = lowerStore;
		m_record = queryRecord;

		m_manager = m_encoder.getOntology().getOWLOntologyManager();
		equalityGroups = m_dataStore.getEqualityGroups(true); 

	}

	public OWLOntology extract(BasicQueryEngine trackingStore, QueryRecord[] botQuerRecords, boolean incrementally) {
		try {
			if (m_record.getRelevantOntology() != null) {
				m_manager.removeOntology(m_record.getRelevantOntology());
				m_record.setRelevantOntology(null);
				m_record.clearClauses();
			}
			m_record.setRelevantOntology(m_manager.createOntology());
		} catch (OWLOntologyCreationException e) {
			e.printStackTrace();
		}

		m_encoder.setCurrentQuery(m_record);

//		m_encoder.encodingQuery(botQuerRecords);
//		m_encoder.saveTrackingRules("tracking_query" + m_record.getQueryID() + ".dlog");

		DataStore store = trackingStore.getDataStore();
		long oldTripleCount, tripleCount;
		try {
			Timer t1 = new Timer();
			oldTripleCount = store.getTriplesCount();
			// store.addRules(new String[] {m_encoder.getTrackingProgram()});
			store.importRules(m_encoder.getTrackingProgram(), UpdateType.ScheduleForAddition);
			store.applyReasoning(incrementally);
			tripleCount = store.getTriplesCount();

			Utility.logDebug("tracking store after materialising tracking program: "
					+ tripleCount
					+ " ("
					+ (tripleCount - oldTripleCount)
					+ " new)");
			Utility.logDebug("tracking store finished the materialisation of tracking program in "
					+ t1.duration() + " seconds.");
		} catch (JRDFStoreException e) {
			e.printStackTrace();
		} 
		extractAxioms(trackingStore);

		trackingStore.clearRulesAndIDBFacts(m_encoder.getAddedData()); 

		if (!m_record.isBottom()
				&& !(m_encoder instanceof TrackingRuleEncoderDisj2))
			addRelatedAxiomsAndClauses(botQuerRecords);
		
		return m_record.getRelevantOntology();
	}

	public void extractAxioms(BasicQueryEngine trackingStore) {
		Set<String> unaryPredicates = new HashSet<String>();
		Set<String> binaryPredicates = new HashSet<String>();

		getDerivedPredicates(trackingStore, unaryPredicates, binaryPredicates);

		/**
		 * To add all the relavant ABox assertions to the fragment
		 */

		int aboxAxiomCounter = extractUnaryTuples(trackingStore,
				m_manager.getOWLDataFactory(), unaryPredicates)
				+ extractBinaryTuples(trackingStore,
						m_manager.getOWLDataFactory(), binaryPredicates);

		/**
		 * To all all the relavant TBox rules to the fragment
		 */
		String queryText = m_encoder.getSelectedSPARQLQuery();

		DLClause clause;
		m_tBoxAxioms = new HashSet<OWLAxiom>();
		OWLAxiom tBoxAxiom;
		TupleIterator answers = null;
		String answer;
		try {
			answers = trackingStore.internal_evaluate(queryText);
			for (long multi = answers.open(); multi != 0; multi = answers.getNext()) {
				answer = answers.getResource(0).m_lexicalForm;
				clause = m_encoder.getSelectedClause(MyPrefixes.PAGOdAPrefixes.expandIRI(answer));
				if (DLClauseHelper.isTautologyAboutDifferentFrom(clause))
					continue;
				tBoxAxiom = m_encoder.getProgram().getEquivalentAxiom(clause);
				m_record.addRelevantClauses(clause);
				m_tBoxAxioms.add(tBoxAxiom);
			}
		} catch (JRDFStoreException e) {
			e.printStackTrace();
		} finally {
			if (answers != null) answers.dispose();
		}

		Utility.logTrace("Extracted TBox axioms: ");
		for (OWLAxiom axiom : m_tBoxAxioms) {
			Utility.logTrace(axiom);
			m_manager.addAxiom(m_record.getRelevantOntology(), axiom);
		}
		Utility.logDebug("TBox extraction Done");

		Utility.logDebug("Before adding bottom fragment:\nABoxAxiomsCount = "
				+ aboxAxiomCounter + ", TBoxAxiomsCount = "
				+ m_tBoxAxioms.size());

	}

	private int extractBinaryTuples(BasicQueryEngine trackingStore, OWLDataFactory factory, Set<String> binaryPredicates) {
		OWLOntology fragment = m_record.getRelevantOntology();  
		int count;  
		int aboxAxiomCounter = 0; 
		Resource sub, obj; 
		OWLAxiom aboxAxiom;
		String trackingIRI; 
		Set<Integer> trackedIDEqualities = new HashSet<Integer>(); 
		Set<String> trackedEntityEqualities = new HashSet<String>(); 
		TupleIterator trackingAnswers, lowerAnswers;
		
		for (Iterator<String> iter = binaryPredicates.iterator(); iter.hasNext(); ) {
			trackingIRI = iter.next();  
			String propIRI = m_encoder.getOriginalPredicate(trackingIRI);
			if (propIRI == null) continue; 
			if (!propIRI.equals(Namespace.EQUALITY_QUOTED)) continue;
			trackingAnswers = null; 
			try {
				trackingAnswers = trackingStore.internal_evaluateAgainstIDBs(getSPARQLQuery4Binary(trackingIRI));
				for (long multi = trackingAnswers.open(); multi != 0; multi = trackingAnswers.getNext()) {
					if (trackingAnswers.getResourceID(0) != trackingAnswers.getResourceID(1)) {
						for (int i = 0; i < 2; ++i) 
							if (trackedIDEqualities.add(trackingAnswers.getResourceID(i))) {
								trackedEntityEqualities.add(trackingAnswers.getResource(i).m_lexicalForm);
							}
					}
				}
			} catch (JRDFStoreException e) {
				e.printStackTrace();
			} finally {
				if (trackingAnswers != null) trackingAnswers.dispose(); 
			}
			iter.remove();
			break; 
		}
		
		String sub_rep, obj_rep; 
		
		for (Iterator<String> iter = binaryPredicates.iterator(); iter.hasNext(); ) {
			trackingIRI = iter.next(); 
			count = 0; 
			String propIRI = m_encoder.getOriginalPredicate(trackingIRI);
			if (propIRI == null) continue; 
			iter.remove(); 
			lowerAnswers = null; trackingAnswers = null; 
			Set<String> lower = new HashSet<String>();
			OWLObject prop = null;
			try {
				trackingAnswers = trackingStore.internal_evaluateAgainstIDBs(getSPARQLQuery4Binary(trackingIRI));
				trackingAnswers.open();
				if (trackingAnswers.getMultiplicity() == 0) continue; 
				
				lowerAnswers = m_dataStore.internal_evaluateNotExpanded(getSPARQLQuery4Binary(propIRI));
				lowerAnswers.open(); 
				if (lowerAnswers.getMultiplicity() == 0) continue; 
				
				StringBuilder builder = new StringBuilder();
				for (long multi = lowerAnswers.getMultiplicity(); multi != 0; multi = lowerAnswers.getNext()) {
					sub = lowerAnswers.getResource(0); 
					obj = lowerAnswers.getResource(1);
					builder.setLength(0);
					builder.append(equalityGroups.find(sub.m_lexicalForm)).append(AnswerTuple.SEPARATOR).append(equalityGroups.find(obj.m_lexicalForm)); 
					lower.add(builder.toString());
				}
				
				for (long multi = trackingAnswers.getMultiplicity(); multi != 0; multi = trackingAnswers.getNext()) {
					sub = trackingAnswers.getResource(0);
					obj = trackingAnswers.getResource(1);
					builder.setLength(0);
					sub_rep = equalityGroups.find(sub.m_lexicalForm);
					obj_rep = equalityGroups.find(obj.m_lexicalForm); 
					if (!sub_rep.equals(sub.m_lexicalForm) || !obj_rep.equals(obj.m_lexicalForm)) continue;
					
					builder.append(sub_rep).append(AnswerTuple.SEPARATOR).append(obj_rep); 
					if (lower.contains(builder.toString())) {
						OWLObject owlObj = getOWLObject(obj, factory); 
						if (owlObj instanceof OWLIndividual) {
							if (prop == null)
								prop = factory.getOWLObjectProperty(IRI.create(propIRI.startsWith("<") ? OWLHelper.removeAngles(propIRI) : propIRI));
							aboxAxiom = factory.getOWLObjectPropertyAssertionAxiom(
									(OWLObjectProperty) prop, 
									factory.getOWLNamedIndividual(IRI.create(sub_rep)), 
									factory.getOWLNamedIndividual(IRI.create(obj_rep)));
						}
						else if (owlObj instanceof OWLLiteral) {
							if (prop == null)
								prop = factory.getOWLDataProperty(IRI.create(propIRI.startsWith("<") ? OWLHelper.removeAngles(propIRI) : propIRI));
							aboxAxiom = factory.getOWLDataPropertyAssertionAxiom(
									(OWLDataProperty) prop, 
									factory.getOWLNamedIndividual(IRI.create(sub_rep)), 
									(OWLLiteral) owlObj);
						}
						else {
							Utility.logError("There might be an error here ... "); 
							continue; 
						}
						if (!fragment.containsAxiom(aboxAxiom)) {
							m_manager.addAxiom(fragment, aboxAxiom);
							++aboxAxiomCounter;
							++count;
						}
					}
				}
			} catch (JRDFStoreException e) {
				e.printStackTrace();
			} finally {
				if (trackingAnswers != null) trackingAnswers.dispose();
				if (lowerAnswers != null) lowerAnswers.dispose();
				lower.clear();
			}
			Utility.logDebug("property: " + propIRI + " " + count); 
		}
		
		count = 0;
		String value; 
		OWLObjectProperty sameAs = factory.getOWLObjectProperty(IRI.create(Namespace.EQUALITY)); 
		for (String key: equalityGroups.keySet()) {
			if (!trackedEntityEqualities.contains(key)) continue; 
			value = equalityGroups.find(key);
			m_manager.addAxiom(fragment, factory.getOWLObjectPropertyAssertionAxiom(
					sameAs, 
					factory.getOWLNamedIndividual(IRI.create(key)), 
					factory.getOWLNamedIndividual(IRI.create(value))));
			++aboxAxiomCounter; 
			++count; 
		}
		Utility.logDebug("property: " + Namespace.EQUALITY_QUOTED + " " + count);
		
		trackedEntityEqualities.clear(); 
		trackedIDEqualities.clear();
		Utility.logTrace(Namespace.EQUALITY_QUOTED + " " + count);
		
		Utility.logDebug("ABox extraction Done"); 
		return aboxAxiomCounter; 
	}

	private OWLObject getOWLObject(Resource rdfoxTerm, OWLDataFactory factory) {
		if (rdfoxTerm.m_datatype.equals(Datatype.IRI_REFERENCE))
			return factory.getOWLNamedIndividual(IRI.create(rdfoxTerm.m_lexicalForm));
//		if (rdfoxTerm.m_datatype.equals(Datatype.OWL_REAL) || rdfoxTerm.m_datatype.equals(Datatype.OWL_RATIONAL) ||
//				rdfoxTerm.m_datatype.equals(Datatype.XSD_DECIMAL) || rdfoxTerm.m_datatype.equals(Datatype.XSD_INTEGER) ||
//				rdfoxTerm.m_datatype.equals(Datatype.XSD_NON_NEGATIVE_INTEGER) || rdfoxTerm.m_datatype.equals(Datatype.XSD_POSITIVE_INTEGER) ||
//				rdfoxTerm.m_datatype.equals(Datatype.XSD_NEGATIVE_INTEGER) || rdfoxTerm.m_datatype.equals(Datatype.XSD_LONG) ||
//				rdfoxTerm.m_datatype.equals(Datatype.XSD_INT) || rdfoxTerm.m_datatype.equals(Datatype.XSD_SHORT) ||
//				rdfoxTerm.m_datatype.equals(Datatype.XSD_BYTE) || rdfoxTerm.m_datatype.equals(Datatype.XSD_UNSIGNED_LONG) ||
//				rdfoxTerm.m_datatype.equals(Datatype.XSD_UNSIGNED_INT) || rdfoxTerm.m_datatype.equals(Datatype.XSD_UNSIGNED_SHORT) ||
//				rdfoxTerm.m_datatype.equals(Datatype.XSD_UNSIGNED_BYTE))
		if (rdfoxTerm.m_datatype.equals(Datatype.XSD_DATE))
			return factory.getOWLLiteral(rdfoxTerm.m_lexicalForm, factory.getOWLDatatype(IRI.create(Namespace.XSD_STRING)));
		
		else return factory.getOWLLiteral(rdfoxTerm.m_lexicalForm, factory.getOWLDatatype(IRI.create(rdfoxTerm.m_datatype.getIRI()))); 
	}

	private int extractUnaryTuples(BasicQueryEngine trackingStore, OWLDataFactory factory, Set<String> unaryPredicates) {
		OWLOntology fragment = m_record.getRelevantOntology();
		int count;
		int aboxAxiomCounter = 0;
		String answer; 
		OWLAxiom aboxAxiom;
		for (String trackingIRI : unaryPredicates) {
			count = 0;
			String clsIRI = m_encoder.getOriginalPredicate(trackingIRI);
			if (clsIRI == null)
				continue;
			TupleIterator answers = null, lowerAnswers = null;
			Set<String> lower = new HashSet<String>();
			OWLClass cls = factory.getOWLClass(IRI.create(clsIRI.startsWith("<") ? OWLHelper.removeAngles(clsIRI) : clsIRI));
			try {
				answers = trackingStore.internal_evaluateAgainstIDBs(getSPARQLQuery4Unary(trackingIRI));
				answers.open();
				if (answers.getMultiplicity() == 0) continue; 

				lowerAnswers = m_dataStore.internal_evaluateNotExpanded(getSPARQLQuery4Unary(clsIRI));
				lowerAnswers.open(); 
				if (lowerAnswers.getMultiplicity() == 0) continue;
				
				for (long multi = lowerAnswers.getMultiplicity(); multi != 0; multi = lowerAnswers.getNext())
					lower.add(equalityGroups.find(lowerAnswers.getResource(0).m_lexicalForm));

				for (long multi = answers.getMultiplicity(); multi != 0; multi = answers.getNext()) {
					answer = equalityGroups.find(answers.getResource(0).m_lexicalForm);
					if (lower.contains(answer)) {
						OWLIndividual instance = factory.getOWLNamedIndividual(IRI.create(answer));
						aboxAxiom = factory.getOWLClassAssertionAxiom(cls,instance);
						if (!fragment.containsAxiom(aboxAxiom)) {
							m_manager.addAxiom(fragment, aboxAxiom);
							++aboxAxiomCounter;
							++count;
						}
					}
				}
			} catch (JRDFStoreException e) {
				e.printStackTrace();
			} finally {
				if (answers != null) answers.dispose();
				if (lowerAnswers != null) lowerAnswers.dispose();
				lower.clear();
			}
			Utility.logDebug("class: " + clsIRI + " " + count);
		}
		return aboxAxiomCounter;
	}

	private void getDerivedPredicates(BasicQueryEngine trackingStore, Set<String> unaryPredicates, Set<String> binaryPredicates) {
		TupleIterator derivedTuples = null;
		String selectedPredicate = OWLHelper.addAngles(m_encoder.getSelectedPredicate());
		try {
			derivedTuples = trackingStore.internal_evaluateAgainstIDBs("select distinct ?z where { ?x <" + Namespace.RDF_TYPE + "> ?z . }");
			for (long multi = derivedTuples.open(); multi != 0; multi = derivedTuples.getNext()) {
				String p = RDFoxTripleManager.getQuotedTerm(derivedTuples.getResource(0));
				if (p.equals(selectedPredicate)) ;
				else if (m_encoder.isAuxPredicate(p)) ;
				else unaryPredicates.add(p);
			}
		} catch (JRDFStoreException e) {
			e.printStackTrace();
		} finally {
			if (derivedTuples != null) derivedTuples.dispose();
		}

		derivedTuples = null;
		try {
			derivedTuples = trackingStore.internal_evaluateAgainstIDBs("select distinct ?y where { ?x ?y ?z . }");
			for (long multi = derivedTuples.open(); multi != 0; multi = derivedTuples.getNext()) {
				String p = RDFoxTripleManager.getQuotedTerm(derivedTuples.getResource(0));
				if (p.equals(Namespace.RDF_TYPE_ABBR) || p.equals(Namespace.RDF_TYPE_QUOTED)) ;
				else if (p.equals(Namespace.EQUALITY_ABBR) || p.equals(Namespace.EQUALITY_QUOTED)) ;
				else if (m_encoder.isAuxPredicate(p)) ;
				else binaryPredicates.add(p);
			}
		} catch (JRDFStoreException e) {
			e.printStackTrace();
		} finally {
			if (derivedTuples != null) derivedTuples.dispose();
		}
	}

	public void addRelatedAxiomsAndClauses(QueryRecord[] botQueryRecords) {
		LinkedList<QueryRecord> toAddedRecords = new LinkedList<QueryRecord>();

		for (QueryRecord botQueryRecord : botQueryRecords)
			if (overlappingDisjunctiveClauses(botQueryRecord) != null)
				toAddedRecords.add(botQueryRecord);

		for (QueryRecord botQueryRecord : toAddedRecords) {
			m_manager.addAxioms(m_record.getRelevantOntology(), botQueryRecord.getRelevantOntology().getAxioms());
			for (DLClause clause : botQueryRecord.getRelevantClauses())
				m_record.addRelevantClauses(clause);
		}

		if (!toAddedRecords.isEmpty())
			Utility.logDebug("Part of bottom fragments is added for this query.");
		else
			Utility.logDebug("None of bottom fragments is added for this query.");
	}

	private Set<DLClause> overlappingDisjunctiveClauses(
			QueryRecord botQueryRecord) {
		if (m_tBoxAxioms == null)
			return null;

		Set<DLClause> disjunctiveRules = new HashSet<DLClause>();
		Set<DLClause> clauses = m_record.getRelevantClauses();
		for (DLClause clause : botQueryRecord.getRelevantClauses())
			if (clause.getHeadLength() > 1 && clauses.contains(clause))
				disjunctiveRules.add(clause);

		return disjunctiveRules.isEmpty() ? null : disjunctiveRules;
	}

	private String getSPARQLQuery4Unary(String p) {
		StringBuilder builder = new StringBuilder();
		builder.append("select ?x where { ?x <").append(Namespace.RDF_TYPE).append("> ");
		builder.append(p).append(" . }");
		return builder.toString();
	}

	private String getSPARQLQuery4Binary(String p) {
		StringBuilder builder = new StringBuilder();
		builder.append("select ?x ?y where { ?x ").append(p).append(" ?y . }");
		return builder.toString();
	}

}
