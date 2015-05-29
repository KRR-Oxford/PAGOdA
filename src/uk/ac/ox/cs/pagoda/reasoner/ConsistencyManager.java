package uk.ac.ox.cs.pagoda.reasoner;

import org.semanticweb.HermiT.model.Atom;
import org.semanticweb.HermiT.model.AtomicConcept;
import org.semanticweb.HermiT.model.DLClause;
import org.semanticweb.HermiT.model.Variable;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import uk.ac.ox.cs.JRDFox.JRDFStoreException;
import uk.ac.ox.cs.JRDFox.store.DataStore;
import uk.ac.ox.cs.JRDFox.store.DataStore.UpdateType;
import uk.ac.ox.cs.pagoda.hermit.DLClauseHelper;
import uk.ac.ox.cs.pagoda.query.AnswerTuples;
import uk.ac.ox.cs.pagoda.query.QueryManager;
import uk.ac.ox.cs.pagoda.query.QueryRecord;
import uk.ac.ox.cs.pagoda.reasoner.full.Checker;
import uk.ac.ox.cs.pagoda.reasoner.light.BasicQueryEngine;
import uk.ac.ox.cs.pagoda.rules.UpperDatalogProgram;
import uk.ac.ox.cs.pagoda.summary.HermitSummaryFilter;
import uk.ac.ox.cs.pagoda.tracking.QueryTracker;
import uk.ac.ox.cs.pagoda.tracking.TrackingRuleEncoder;
import uk.ac.ox.cs.pagoda.util.Timer;
import uk.ac.ox.cs.pagoda.util.Utility;
import uk.ac.ox.cs.pagoda.util.disposable.Disposable;

import java.util.LinkedList;

public class ConsistencyManager extends Disposable {

	protected MyQueryReasoner m_reasoner;
	protected QueryManager m_queryManager;  
	
	Timer t = new Timer();
	QueryRecord fullQueryRecord;
	QueryRecord[] botQueryRecords;
	LinkedList<DLClause> toAddClauses;
	boolean fragmentExtracted = false;
	
	public ConsistencyManager(MyQueryReasoner reasoner) {
		m_reasoner = reasoner;
		m_queryManager = reasoner.getQueryManager();
	}

	@Override
	public void dispose() {
		super.dispose();
		fullQueryRecord.dispose();
	}

	public void extractBottomFragment() {
		if(fragmentExtracted) return;
		fragmentExtracted = true;

		UpperDatalogProgram upperProgram = m_reasoner.program.getUpper();
		int number = upperProgram.getBottomNumber();

		if(number <= 1) {
			botQueryRecords = new QueryRecord[]{fullQueryRecord};
		}
		else {
			QueryRecord[] tempQueryRecords = new QueryRecord[number - 1];
			QueryRecord record;
			for(int i = 0; i < number - 1; ++i) {
				tempQueryRecords[i] = record =
						m_queryManager.create(QueryRecord.botQueryText.replace("Nothing", "Nothing" + (i + 1)), 0, i + 1);
				AnswerTuples iter = null;
				try {
					iter = m_reasoner.trackingStore.evaluate(record.getQueryText(), record.getAnswerVariables());
					record.updateUpperBoundAnswers(iter);
				} finally {
					if(iter != null) iter.dispose();
					iter = null;
				}
			}

			int bottomNumber = 0;
			int[] group = new int[number - 1];
			for(int i = 0; i < number - 1; ++i) group[i] = i;
			for(int i = 0; i < number - 1; ++i)
				if(tempQueryRecords[i].isProcessed()) tempQueryRecords[i].dispose();
				else if(group[i] == i) {
					++bottomNumber;
					record = tempQueryRecords[i];
					for(int j = i + 1; j < number - 1; ++j)
						if(record.hasSameGapAnswers(tempQueryRecords[j]))
							group[j] = i;
				}

			Utility.logInfo("There are " + bottomNumber + " different bottom fragments.");
			toAddClauses = new LinkedList<DLClause>();
			int bottomCounter = 0;
			botQueryRecords = new QueryRecord[bottomNumber];
			Variable X = Variable.create("X");
			for(int i = 0; i < number - 1; ++i)
				if(!tempQueryRecords[i].isDisposed() && !tempQueryRecords[i].isProcessed())
					if(group[i] == i) {
						botQueryRecords[bottomCounter] = record = tempQueryRecords[i];
						record.resetInfo(QueryRecord.botQueryText.replace("Nothing", "Nothing_final" + (++bottomCounter)), 0,
										 group[i] = bottomCounter);
						toAddClauses.add(
								DLClause.create(
										new Atom[]{Atom.create(AtomicConcept.create(AtomicConcept.NOTHING.getIRI() + "_final" + bottomCounter), X)},
										new Atom[]{Atom.create(AtomicConcept.create(AtomicConcept.NOTHING.getIRI() + (i + 1)), X)}));
					}
					else {
						toAddClauses.add(
								DLClause.create(
										new Atom[]{Atom.create(AtomicConcept.create(AtomicConcept.NOTHING.getIRI() + "_final" + group[group[i]]), X)},
										new Atom[]{Atom.create(AtomicConcept.create(AtomicConcept.NOTHING.getIRI() + (i + 1)), X)}));
						tempQueryRecords[i].dispose();
					}

			upperProgram.updateDependencyGraph(toAddClauses);
		}

		String[] programs = collectTrackingProgramAndImport();
		if(programs.length == 0)
			return;

		DataStore store = m_reasoner.trackingStore.getDataStore();
		long oldTripleCount, tripleCount;
		try {
			Timer t1 = new Timer();
			oldTripleCount = store.getTriplesCount();
			for(String program : programs)
				store.importRules(program, UpdateType.ScheduleForAddition);
			store.applyReasoning(true);
			tripleCount = store.getTriplesCount();

			Utility.logInfo("tracking store after materialising tracking program: " + tripleCount + " (" + (tripleCount - oldTripleCount) + " new)",
							"tracking store finished the materialisation of tracking program in " + t1.duration() + " seconds.");

			extractAxioms();
			store.clearRulesAndMakeFactsExplicit();
		} catch(JRDFStoreException e) {
			e.printStackTrace();
		} catch(OWLOntologyCreationException e) {
			e.printStackTrace();
		}
	}

	public QueryRecord[] getQueryRecords() {
		return botQueryRecords;
	}

	boolean checkRLLowerBound() {
		fullQueryRecord = m_queryManager.create(QueryRecord.botQueryText, 0);
		AnswerTuples iter = null;

		try {
			iter = m_reasoner.rlLowerStore.evaluate(fullQueryRecord.getQueryText(), fullQueryRecord.getAnswerVariables());
			fullQueryRecord.updateLowerBoundAnswers(iter);
		} finally {
			iter.dispose();
		}

		if (fullQueryRecord.getNoOfSoundAnswers() > 0) {
			Utility.logInfo("Answers to bottom in the lower bound: ", fullQueryRecord.outputSoundAnswerTuple());
			return false;
		}
		return true;
	}

//	protected boolean unsatisfiability(double duration) {
//		fullQueryRecord.dispose();
//		Utility.logDebug("The ontology and dataset is unsatisfiable.");
//		return false;
//	}

//	protected boolean satisfiability(double duration) {
//		fullQueryRecord.dispose();
//		Utility.logDebug("The ontology and dataset is satisfiable.");
//		return true;
//	}

	boolean checkELLowerBound() {
		fullQueryRecord.updateLowerBoundAnswers(m_reasoner.elLowerStore.evaluate(fullQueryRecord.getQueryText(), fullQueryRecord
				.getAnswerVariables()));
		if(fullQueryRecord.getNoOfSoundAnswers() > 0) {
			Utility.logInfo("Answers to bottom in the lower bound: ", fullQueryRecord.outputSoundAnswerTuple());
			return true;
		}
		return true;
	}

	boolean checkUpper(BasicQueryEngine upperStore) {
		if(upperStore != null) {
			AnswerTuples tuples = null;
			try {
				tuples = upperStore.evaluate(fullQueryRecord.getQueryText(), fullQueryRecord.getAnswerVariables());
				if(!tuples.isValid()) {
					Utility.logInfo("There are no contradictions derived in " + upperStore.getName() + " materialisation.");
					Utility.logDebug("The ontology and dataset is satisfiable.");
					return true;
				}
			} finally {
				if(tuples != null) tuples.dispose();
			}
		}
		return false;
	}

	boolean check() {
//		if (!checkRLLowerBound()) return false;
//		if (!checkELLowerBound()) return false;
//		if (checkLazyUpper()) return true;
		AnswerTuples iter = null;

		try {
			iter =
					m_reasoner.trackingStore.evaluate(fullQueryRecord.getQueryText(), fullQueryRecord.getAnswerVariables());
			fullQueryRecord.updateUpperBoundAnswers(iter);
		} finally {
			if(iter != null) iter.dispose();
		}

		if(fullQueryRecord.getNoOfCompleteAnswers() == 0)
			return true;

		extractBottomFragment();

		try {
			extractAxioms4Full();
		} catch(OWLOntologyCreationException e) {
			e.printStackTrace();
		}
//		fullQueryRecord.saveRelevantClause();

		boolean satisfiability;

		Checker checker;
		for(QueryRecord r : getQueryRecords()) {
			// TODO to be removed ...
//			r.saveRelevantOntology("bottom" + r.getQueryID() + ".owl");
			checker = new HermitSummaryFilter(r, true); // m_reasoner.factory.getSummarisedReasoner(r);
			satisfiability = checker.isConsistent();
			checker.dispose();
			if(!satisfiability) return false;
		}

//		Checker checker = m_reasoner.factory.getSummarisedReasoner(fullQueryRecord);
//		boolean satisfiable = checker.isConsistent();
//		checker.dispose();
//		if (!satisfiable) return unsatisfiability(t.duration());

		return true;
	}

	private void extractAxioms4Full() throws OWLOntologyCreationException {
		OWLOntologyManager manager = m_reasoner.encoder.getProgram().getOntology().getOWLOntologyManager();
		OWLOntology fullOntology = manager.createOntology();
		for (QueryRecord record: botQueryRecords) {
			for (DLClause clause: record.getRelevantClauses()) {
				fullQueryRecord.addRelevantClauses(clause);
			}
			manager.addAxioms(fullOntology, record.getRelevantOntology().getAxioms());
		}
		fullQueryRecord.setRelevantOntology(fullOntology);
	}

	private void extractAxioms() throws OWLOntologyCreationException {
		OWLOntologyManager manager = m_reasoner.encoder.getProgram().getOntology().getOWLOntologyManager();
		for (QueryRecord record: botQueryRecords) {
			record.setRelevantOntology(manager.createOntology());
			QueryTracker tracker = new QueryTracker(m_reasoner.encoder, m_reasoner.rlLowerStore, record);
			m_reasoner.encoder.setCurrentQuery(record);
			tracker.extractAxioms(m_reasoner.trackingStore);
//			record.saveRelevantClause();
//			record.saveRelevantOntology("bottom" + record.getQueryID() + ".owl");
			Utility.logInfo("finish extracting axioms for bottom " + record.getQueryID());
		}
	}

	private String[] collectTrackingProgramAndImport() {
		String[] programs = new String[botQueryRecords.length];
		TrackingRuleEncoder encoder = m_reasoner.encoder;

		StringBuilder builder;
		LinkedList<DLClause> currentClauses = new LinkedList<DLClause>();

		for (int i = 0; i < botQueryRecords.length; ++i) {
			encoder.setCurrentQuery(botQueryRecords[i]);
			builder = new StringBuilder(encoder.getTrackingProgram());
//			encoder.saveTrackingRules("tracking_bottom" + (i + 1) + ".dlog");

			for (DLClause clause: toAddClauses)
				if (clause.getHeadAtom(0).getDLPredicate().toString().contains("_final" + (i + 1)))
					currentClauses.add(clause);

			builder.append(DLClauseHelper.toString(currentClauses));
			programs[i] = builder.toString();

			currentClauses.clear();
		}

		return programs;
	}


}
