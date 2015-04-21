package org.semanticweb.karma2;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.jgrapht.DirectedGraph;
import org.jgrapht.alg.CycleDetector;
import org.jgrapht.graph.DefaultDirectedGraph;
import org.jgrapht.graph.DefaultEdge;
import org.semanticweb.HermiT.model.Atom;
import org.semanticweb.HermiT.model.Individual;
import org.semanticweb.HermiT.model.Term;
import org.semanticweb.karma2.exception.ConstraintException;
import org.semanticweb.karma2.model.ConjunctiveQuery;
import org.semanticweb.karma2.model.ExtendedConjunctiveQuery;

import uk.ac.ox.cs.JRDFox.model.GroundTerm;
import uk.ac.ox.cs.JRDFox.store.DataStore;
import uk.ac.ox.cs.JRDFox.store.Parameters;
import uk.ac.ox.cs.JRDFox.Prefixes;
import uk.ac.ox.cs.JRDFox.JRDFStoreException;
import uk.ac.ox.cs.JRDFox.store.TupleIterator;
import uk.ac.ox.cs.pagoda.MyPrefixes;
import uk.ac.ox.cs.pagoda.query.AnswerTuple;
import uk.ac.ox.cs.pagoda.query.AnswerTuples;
import uk.ac.ox.cs.pagoda.reasoner.light.RDFoxQueryEngine;
import uk.ac.ox.cs.pagoda.util.Namespace;
import uk.ac.ox.cs.pagoda.util.Timer;
import uk.ac.ox.cs.pagoda.util.UFS;
import uk.ac.ox.cs.pagoda.util.Utility;

public class MyKarma {
	
	private DataStore store;
	
	private Prefixes prefixes = MyPrefixes.PAGOdAPrefixes.getRDFoxPrefixes();
	private Parameters parameters = new Parameters(); 

	public MyKarma() {
		store = RDFoxQueryEngine.createDataStore();
		parameters.m_allAnswersInRoot = true; 
		parameters.m_useBushy = true;
	}
	
	private UFS<String> equalityGroups = null; 
	
	public void computeEqualityGroups() {
		if (equalityGroups != null) return ; 
		equalityGroups = new UFS<String>(); 
		TupleIterator answers = null; 
		try {
			Timer t = new Timer(); 
			answers = store.compileQuery("select ?x ?z  where {?x " + Namespace.EQUALITY_QUOTED + "?z . }", prefixes, parameters);
			for (long multi = answers.open(); multi != 0; multi = answers.getNext()) {
				if (answers.getResourceID(0) != answers.getResourceID(1))
					equalityGroups.merge(answers.getResource(0).m_lexicalForm, answers.getResource(1).m_lexicalForm); 
			}
			Utility.logInfo("@Time to group individuals by equality: " + t.duration());
		} catch (JRDFStoreException e) {
			e.printStackTrace();
		} finally {
			if (answers != null) answers.dispose(); 
		}
	}
	
	public DataStore getStore() {
		return store;
	}
	
	public long getNumberOfFacts() throws JRDFStoreException {
		return store.getTriplesCount();
	}

	public void initializeData(File dataFile) throws JRDFStoreException,
			FileNotFoundException {
		store.importTurtleFile(dataFile, prefixes);
	}

	public void materialise(File ruleFile) throws JRDFStoreException, FileNotFoundException {
		Timer t = new Timer(); 
		Scanner scanner = new Scanner(ruleFile);
		String datalogProgram = scanner.useDelimiter("\\Z").next();
		scanner.close();
		store.clearRulesAndMakeFactsExplicit();
//		store.addRules(new String[] {datalogProgram}); 
		store.importRules(datalogProgram); 
		store.applyReasoning();
		Utility.logDebug("elho-lower-store finished its own materialisation in " + t.duration() + " seconds."); 
	}

	public Collection<AnswerTuple> answerCQ(ConjunctiveQuery q, boolean isGround) {
		return answerCQ(q, null, isGround); 
	}
	
	boolean m_multiThread = false;  
	
	public void setConcurrence(boolean multiThread) {
		this.m_multiThread = multiThread; 
	}
	
	public Set<AnswerTuple> answerCQ(ConjunctiveQuery q, AnswerTuples soundAnswerTuples, boolean isGround) {
		computeEqualityGroups(); 
		if (m_multiThread) 
			return answerCQ_multiThread(q, soundAnswerTuples, isGround);
		else 
			return answerCQ_singleThread(q, soundAnswerTuples, isGround); 
	}
	
	private Set<AnswerTuple> answerCQ_multiThread(ConjunctiveQuery q, AnswerTuples soundAnswerTuples, boolean isGround) {
		Set<Future<AnswerTuple>> set = new HashSet<Future<AnswerTuple>>();
		ExtendedConjunctiveQuery qext = ExtendedConjunctiveQuery.computeExtension(q);
		TupleIterator tupleIterator;
		try {
			tupleIterator = store.compileQuery(qext.toString(), prefixes, parameters);
		} catch (JRDFStoreException e) {
			e.printStackTrace();
			return null; 
		} 
		ExecutorService es = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
		AnswerTuple tuple; 
		try {
			try {
				for (long multi = tupleIterator.open(); multi != 0; multi = tupleIterator.getNext()) {
					Map<Term, GroundTerm> match = new HashMap<Term, GroundTerm>();
					for (int i = 0; i < qext.getNumberOfAnswerTerms(); i++) {
						match.put(qext.getAnswerTerm(i), tupleIterator.getGroundTerm(i)); 
					}
					if ((tuple = contains(qext, soundAnswerTuples, match)) != null) 
							set.add(es.submit(new Spurious(qext, match, tuple, isGround)));
				}
			} catch (JRDFStoreException e) {
				e.printStackTrace();
				return null; 
			} finally {
				tupleIterator.dispose();
			}
			Set<AnswerTuple> result = new HashSet<AnswerTuple>(set.size());
			while(!set.isEmpty()) {
				Iterator<Future<AnswerTuple>> it = set.iterator();
				while(it.hasNext()) {
					Future<AnswerTuple> isReady = it.next();
					if (isReady.isDone()) {
						try {
							tuple = isReady.get();
							if (tuple != null)
								result.add(tuple);
							it.remove();
						} catch (InterruptedException e) {
							e.printStackTrace();
						} catch (ExecutionException e) {
							e.printStackTrace();
						}
					}
				}
			}
			return result;
		} finally {
			es.shutdown();
		}
	}
	
	private Set<AnswerTuple> answerCQ_singleThread(ConjunctiveQuery q, AnswerTuples soundAnswerTuples, boolean isGround) {
		ExtendedConjunctiveQuery qext = ExtendedConjunctiveQuery.computeExtension(q);
		TupleIterator tupleIterator;
		try {
			tupleIterator = store.compileQuery(qext.toString(), prefixes, parameters);
		} catch (JRDFStoreException e) {
			e.printStackTrace();
			return null; 
		}
		
		boolean useBushyValue = parameters.m_useBushy, allAnswersInRootValue = parameters.m_allAnswersInRoot;
		parameters.m_useBushy = false; 
		parameters.m_allAnswersInRoot = false; 
		try {
		Set<AnswerTuple> result = new HashSet<AnswerTuple>();
		AnswerTuple tuple; 
		try {
			for (long multi = tupleIterator.open(); multi != 0; multi = tupleIterator.getNext()) {
				Map<Term, GroundTerm> match = new HashMap<Term, GroundTerm>();
				for (int i = 0; i < qext.getNumberOfAnswerTerms(); i++) {
					match.put(qext.getAnswerTerm(i), tupleIterator.getGroundTerm(i));
				}
				if (((tuple = contains(qext, soundAnswerTuples, match)) != null) && (new Spurious(qext, match, tuple, isGround).call()) != null)
					result.add(tuple); 
			}
		} catch (JRDFStoreException e) {
			e.printStackTrace();
			return null; 
		} finally {
			tupleIterator.dispose();
		}
		return result;
		} finally {
			parameters.m_useBushy = useBushyValue;
			parameters.m_allAnswersInRoot = allAnswersInRootValue; 
		}

	}
	
	private AnswerTuple contains(ExtendedConjunctiveQuery qext, AnswerTuples answerTuples, Map<Term, GroundTerm> match) {
		GroundTerm[] terms = new GroundTerm[qext.getNumberOfRealAnswerTerms()];
		int index = 0; 
		for (Term t : qext.getRealAnswerTerms()) 
			terms[index++] = match.get(t);			
		AnswerTuple tuple = new AnswerTuple(terms); 
		if (answerTuples != null && answerTuples.contains(tuple)) return null; 
		return tuple; 
	}


	class Spurious implements Callable<AnswerTuple> {
		private ExtendedConjunctiveQuery query;
		private Map<Term, GroundTerm> match;
		private AnswerTuple tuple; 
		private boolean isGround;  

		public Spurious(ExtendedConjunctiveQuery query, Map<Term, GroundTerm> m, AnswerTuple t, boolean isGround) {
			this.query = query;
			this.match = m;
			this.tuple = t; 
			this.isGround = isGround; 
		}

		public AnswerTuple call() {
			if (isMappingAnswerVariablesToAuxiliary(query, match)); 
			else { 
				if (isGround) return tuple;
					
				EqualityConstraintRelation sim = new EqualityConstraintRelation(query, match);
				try {
					sim.computeRelation();
					if (areEqualityConstraintsSatisfiedByMatch(query, sim, match) 
							&& !isCyclic(query, sim, match)) {
						return tuple; 
					}
				} catch (ConstraintException e) {
					Utility.logError(e.toString()); 
					e.printStackTrace();
					return null; 
				}
			}
			return null;			
		}
		
	}

		private boolean isMappingAnswerVariablesToAuxiliary(
				ExtendedConjunctiveQuery conjunctiveQuery,
				Map<Term, GroundTerm> match) {
			for (Term ansQueryTerm : conjunctiveQuery.getRealAnswerTerms()) {
				if (! (ansQueryTerm instanceof Individual)) {
					GroundTerm datalog_term = match.get(ansQueryTerm);
					if (isSyntacticAnonymous(datalog_term))
						return true;
				}
			}
			return false;
		}

		private boolean isCyclic(ExtendedConjunctiveQuery q,
				EqualityConstraintRelation sim, Map<Term, GroundTerm> match) {
			DirectedGraph<Term, DefaultEdge> auxGraph = new DefaultDirectedGraph<Term, DefaultEdge>(
					DefaultEdge.class);
			for (Term queryTerm : q.getTerms()) {
				if (!(queryTerm instanceof Individual) && isRealAnonymous(match.get(queryTerm)))
					auxGraph.addVertex(sim.getRepresentative(queryTerm));
			}
			for (Atom a : q.getAtoms())
				if (a.getArity() == 2 && !(a.getArgument(0) instanceof Individual) && !(a.getArgument(1) instanceof Individual)) 
					if (isRealAnonymous(match.get(a.getArgument(0))) && isRealAnonymous(match.get(a.getArgument(1))))
						auxGraph.addEdge(sim.getRepresentative(a.getArgument(0)),	sim.getRepresentative(a.getArgument(0)));
			return (new CycleDetector<Term, DefaultEdge>(auxGraph)).detectCycles();

		}

		private boolean isRealAnonymous(GroundTerm datalog_t) {
			if (!(datalog_t instanceof uk.ac.ox.cs.JRDFox.model.Individual)) return false; 
			uk.ac.ox.cs.JRDFox.model.Individual ind = (uk.ac.ox.cs.JRDFox.model.Individual) datalog_t;
			if (!ind.getIRI().startsWith(Namespace.KARMA_ANONY)) return false; 
			
			return equalityGroups.find(ind.getIRI()).contains(Namespace.KARMA_ANONY); 
					
//			String query = "select ?x where { ?x <http://www.w3.org/2002/07/owl#sameAs> <" + ind.getIRI() + ">. } ";
//			TupleIterator tupleIterator;
//			try {
//				tupleIterator = store.compileQuery(query,	prefixes, parameters);
//			} catch (JRDFStoreException e) {
//				e.printStackTrace();
//				return false; 
//			} 
//			
//			try {
//				GroundTerm t; 
//				for (long multi = tupleIterator.open(); multi != 0; multi = tupleIterator.getNext()) {
//					t = tupleIterator.getGroundTerm(0); 
//					if (t instanceof uk.ac.ox.cs.JRDFox.model.Individual && !((uk.ac.ox.cs.JRDFox.model.Individual) t).isAnony)
//						return false; 
//				}
//			} catch (JRDFStoreException e) {
//				e.printStackTrace();
//				return false; 
//			} finally {
//				tupleIterator.dispose();
//			}
//			return true;
		}

		private boolean areEqualityConstraintsSatisfiedByMatch(
				ExtendedConjunctiveQuery q, EqualityConstraintRelation sim,
				Map<Term, GroundTerm> m) throws ConstraintException {
			for (Term s : q.getTerms())
				for (Term t : q.getTerms())
					if (sim.areConstraintToBeEqual(s, t)) {
						if (!areMappedToEqualDatalogTerms(q, m, s, t))
							return false;
					}
			return true;
		}

		private boolean areMappedToEqualDatalogTerms(
				ExtendedConjunctiveQuery q, Map<Term, GroundTerm> match,
				Term queryTerm1, Term queryTerm2) {
			GroundTerm datalogTerm1 = (queryTerm1 instanceof Individual) ? toRDFoxIndividual(queryTerm1) : match.get(queryTerm1);
			GroundTerm datalogTerm2 = (queryTerm2 instanceof Individual) ? toRDFoxIndividual(queryTerm2) : match.get(queryTerm2);
			if (datalogTerm1 != null && datalogTerm1.equals(datalogTerm2))
				return true;
			
			return equalityGroups.find(datalogTerm1.toString()).equals(datalogTerm2.toString()); 
//			String query = "prefix owl:	<http://www.w3.org/2002/07/owl#> select where {"
//					+ datalogTerm1
//					+ " owl:sameAs "
//					+ datalogTerm2
//					+ ". } ";
//			TupleIterator tupleIterator;
//			try {
//				tupleIterator = store.compileQuery(query,	prefixes, parameters);
//			} catch (JRDFStoreException e) {
//				e.printStackTrace();
//				return false; 
//			} 
//			boolean res = false;
//			try {
//				res = tupleIterator.open() != 0;
//			} catch (JRDFStoreException e) {
//				e.printStackTrace();
//				return false; 
//			} finally {
//				tupleIterator.dispose();
//			}
//			return res;
		}

		private GroundTerm toRDFoxIndividual(Term t) {
			return uk.ac.ox.cs.JRDFox.model.Individual.create(((Individual) t).getIRI()); 
		}

		private boolean isSyntacticAnonymous(GroundTerm datalog_t) {
			if (datalog_t instanceof uk.ac.ox.cs.JRDFox.model.Individual && ((uk.ac.ox.cs.JRDFox.model.Individual) datalog_t).getIRI().startsWith(Namespace.KARMA_ANONY))
				return true;
			return false;
		}
		
		class EqualityConstraintRelation {

			private ExtendedConjunctiveQuery cq;
			private Map<Term, GroundTerm> match;
			private Map<Term, Set<Term>> sim;

			public EqualityConstraintRelation(ExtendedConjunctiveQuery q,
					Map<Term, GroundTerm> m) {
				cq = q;
				match = m;
				sim = new HashMap<Term, Set<Term>>();
			}

			public void addSingletonClass(Term t) {
				Set<Term> eqclass = new HashSet<Term>();
				eqclass.add(t);
				sim.put(t, eqclass);
			}

			public boolean areConstraintToBeEqual(Term s, Term t)
					throws ConstraintException {
				Term sRepresentative = getRepresentative(s);
				Term tRepresentative = getRepresentative(t);
				if (sRepresentative == null || tRepresentative == null) {
					throw new ConstraintException("Cannot identify terms " + s
							+ " and " + t);
				}
				return sRepresentative.equals(tRepresentative);
			}

			public void constrainToBeEqual(Term s, Term t)
					throws ConstraintException {
				Term sRepresentative = getRepresentative(s);
				Term tRepresentative = getRepresentative(t);
				if (sRepresentative == null || tRepresentative == null) {
					throw new ConstraintException("Cannot identify terms " + s
							+ " and " + t);
				}
				if (!sRepresentative.equals(tRepresentative)) {
					sim.get(sRepresentative).addAll(sim.get(tRepresentative));
					sim.remove(tRepresentative);
				}
			}

			public Term getRepresentative(Term s) {
				if (sim.containsKey(s))
					return s;
				for (Term key : sim.keySet()) {
					if (sim.get(key).contains(s))
						return key;
				}
				return null;
			}

			public Set<Term> getEquivalenceClass(Term s) {
				if (sim.containsKey(s))
					return sim.get(s);
				for (Set<Term> eqClass : sim.values()) {
					if (eqClass.contains(s))
						return eqClass;
				}
				return null;
			}

			public void deriveForkConstraints() throws ConstraintException {
				boolean newDerivedConstraints = true;
				while (newDerivedConstraints) {
					newDerivedConstraints = false;
					for (Atom a1 : cq.getAtoms())
						for (Atom a2 : cq.getAtoms()) {
							if (a1.getArity() == 2 && a2.getArity() == 2) {
								GroundTerm term = a1.getArgument(1) instanceof Individual ? toRDFoxIndividual(a1.getArgument(1)) : match.get(a1.getArgument(1));
								if (areConstraintToBeEqual(a1.getArgument(1), a2.getArgument(1)) && !areConstraintToBeEqual(a1.getArgument(0),a2.getArgument(0))) { 
									if (isRealAnonymous(term)) {
										constrainToBeEqual(a1.getArgument(0), a2.getArgument(0));
										newDerivedConstraints = true;
									}
								}
							}
						}
				}
			}

			public void computeRelation() throws ConstraintException {
				for (Term t : cq.getTerms()) {
					addSingletonClass(t);
				}
				deriveForkConstraints();
			}

			public String toString() {
				String res = "";
				for (Set<Term> terms : this.sim.values()) {
					res += "[ ";
					for (Term t : terms)
						res += t + " ";
					res += "]\n";
				}
				return res;
			}

		}

	public void dispose() {
		store.dispose(); 
	}

}