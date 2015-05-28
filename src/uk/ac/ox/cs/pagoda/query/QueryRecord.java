package uk.ac.ox.cs.pagoda.query;

import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
import org.semanticweb.HermiT.model.*;
import org.semanticweb.owlapi.model.*;
import uk.ac.ox.cs.pagoda.hermit.DLClauseHelper;
import uk.ac.ox.cs.pagoda.reasoner.light.RDFoxAnswerTuples;
import uk.ac.ox.cs.pagoda.rules.GeneralProgram;
import uk.ac.ox.cs.pagoda.util.ConjunctiveQueryHelper;
import uk.ac.ox.cs.pagoda.util.Namespace;
import uk.ac.ox.cs.pagoda.util.PagodaProperties;
import uk.ac.ox.cs.pagoda.util.Utility;
import uk.ac.ox.cs.pagoda.util.tuples.Tuple;
import uk.ac.ox.cs.pagoda.util.tuples.TupleBuilder;

import java.io.*;
import java.lang.reflect.Type;
import java.util.*;

public class QueryRecord {
	
	public static final String botQueryText = "SELECT ?X WHERE { ?X <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.w3.org/2002/07/owl#Nothing> }";
	public static final String SEPARATOR = "----------------------------------------";
	private static final String RDF_TYPE = "a"; //"rdf:type"; //RDF.type.toString();
	boolean processed = false;
	String stringQueryID = null;
	OWLOntology relevantOntology = null;
	Set<DLClause> relevantClauses = new HashSet<DLClause>();
	double[] timer;
	int subID;
	DLClause queryClause = null;
	private Step difficulty;
	private String queryText;
	private int queryID = -1;
	private String[][] answerVariables = null;
	private Set<AnswerTuple> soundAnswerTuples = new HashSet<AnswerTuple>();
	private Set<AnswerTuple> gapAnswerTuples = null;
	private QueryManager m_manager;

	private QueryRecord() {
	}

//	private boolean containsAuxPredicate(String str) {
//		return  str.contains(Namespace.PAGODA_AUX) || str.contains("_AUX") || str.contains("owl#Nothing") ||
//				str.contains("internal:def"); 
//	}

	public QueryRecord(QueryManager manager, String text, int id, int subID) {
		m_manager = manager;
		resetInfo(text, id, subID);
		resetTimer();
	}

	public static Collection<String> collectQueryTexts(Collection<QueryRecord> queryRecords) {
		Collection<String> texts = new LinkedList<String>();
		for(QueryRecord record : queryRecords)
			texts.add(record.queryText);
		return texts;
	}

	public void resetInfo(String text, int id, int subid) {
		queryID = id;
		subID = subid;
		stringQueryID = id + (subID == 0 ? "" : "_" + subID);
		m_manager.remove(queryText);
		m_manager.put(text, this);
		queryClause = null;
		answerVariables = ConjunctiveQueryHelper.getAnswerVariables(text);
		queryText = text; // .replace("_:", "?");
	}

	public void resetTimer() {
		int length = Step.values().length;
		timer = new double[length];
		for(int i = 0; i < length; ++i) timer[i] = 0;
	}

	public AnswerTuples getAnswers() {
		if(isProcessed())
			return getLowerBoundAnswers();

		return getUpperBoundAnswers();
	}

	public AnswerTuples getLowerBoundAnswers() {
		return new AnswerTuplesImp(answerVariables[0], soundAnswerTuples);
	}

	public AnswerTuples getUpperBoundAnswers() {
		return new AnswerTuplesImp(answerVariables[0], soundAnswerTuples, gapAnswerTuples);
	}

	public boolean updateLowerBoundAnswers(AnswerTuples answerTuples) {
		if(answerTuples == null) return false;
		boolean update = false;
		for(AnswerTuple tuple; answerTuples.isValid(); answerTuples.moveNext()) {
			tuple = answerTuples.getTuple();
			if(!soundAnswerTuples.contains(tuple) && (gapAnswerTuples == null || gapAnswerTuples.contains(tuple))) {
				soundAnswerTuples.add(tuple);
				if(gapAnswerTuples != null)
					gapAnswerTuples.remove(tuple);
				update = true;
			}
		}

		if(soundAnswerTuples.isEmpty())
			Utility.logInfo("Lower bound answers empty");
		else if(update)
			Utility.logInfo("Lower bound answers updated: " + soundAnswerTuples.size());
		else
			Utility.logInfo("Lower bound answers unchanged");

		return update;
	}

	public boolean updateUpperBoundAnswers(AnswerTuples answerTuples) {
		return updateUpperBoundAnswers(answerTuples, false);
	}

	public boolean updateUpperBoundAnswers(AnswerTuples answerTuples, boolean toCheckAux) {
		if(!(answerTuples instanceof RDFoxAnswerTuples)) {
			String msg = "The upper bound must be computed by RDFox!";
			Utility.logError(msg);
			throw new IllegalArgumentException(msg);
		}

		RDFoxAnswerTuples rdfoxAnswerTuples = (RDFoxAnswerTuples) answerTuples;

		Set<AnswerTuple> candidateGapAnswerTuples = new HashSet<AnswerTuple>();
		AnswerTuple tuple;
		for(; rdfoxAnswerTuples.isValid(); rdfoxAnswerTuples.moveNext()) {
			tuple = rdfoxAnswerTuples.getTuple();
			if(isBottom() || !tuple.hasAnonymousIndividual())
				if((!toCheckAux || !tuple.hasAuxPredicate()) && !soundAnswerTuples.contains(tuple))
					candidateGapAnswerTuples.add(tuple);
		}

		/*** START: debugging ***/
		if(PagodaProperties.isDebuggingMode()) {
			if(rdfoxAnswerTuples.getArity() != getAnswerVariables().length)
				throw new IllegalArgumentException(
						"The arity of answers (" + rdfoxAnswerTuples.getArity() + ") " +
								"is different from the number of answer variables (" +
								getAnswerVariables().length + ")");

			Set<AnswerTuple> namedAnswerTuples = new HashSet<>();
			rdfoxAnswerTuples.reset();
			for(; rdfoxAnswerTuples.isValid(); rdfoxAnswerTuples.moveNext()) {
				tuple = rdfoxAnswerTuples.getTuple();
//				if(isBottom() || !tuple.hasAnonymousIndividual()) {
				namedAnswerTuples.add(tuple);
//				}
			}
			HashSet<AnswerTuple> difference = new HashSet<>(soundAnswerTuples);
			difference.removeAll(namedAnswerTuples);
			if(!difference.isEmpty())
				throw new IllegalArgumentException("The upper bound does not contain the lower bound! Missing answers: " + difference
						.size());
		}
		/*** END: debugging ***/

		boolean update;
		if(gapAnswerTuples == null) {
			gapAnswerTuples = candidateGapAnswerTuples;
			update = true;
		} else {
			update = gapAnswerTuples.retainAll(candidateGapAnswerTuples);
		}

		if(update)
			Utility.logInfo("Upper bound answers updated: " + getNumberOfAnswers());
		else
			Utility.logInfo("Upper bound answers unchanged");

		return update;

//		boolean update = false;
//		for(Iterator<AnswerTuple> iter = gapAnswerTuples.iterator(); iter.hasNext(); ) {
//			tuple = iter.next();
//			if(!candidateGapAnswerTuples.contains(tuple)) {
//				iter.remove();
//				update = true;
//			}
//		}
	}

	public int getNumberOfAnswers() {
		return soundAnswerTuples.size() + gapAnswerTuples.size();
	}

	public void markAsProcessed() {
		processed = true;
	}

	public boolean isProcessed() {
		if(gapAnswerTuples != null && gapAnswerTuples.isEmpty()) processed = true;
		return processed;
	}

	public String[] getDistinguishedVariables() {
		return answerVariables[1];
	}

	public String[] getAnswerVariables() {
		return answerVariables[0];
	}

	public String[][] getVariables() {
		return answerVariables;
	}
	
	public String getQueryText() {
		return queryText;
	}
	
	public String getQueryID() {
		return stringQueryID;
	}

	public AnswerTuples getGapAnswers() {
		return new AnswerTuplesImp(answerVariables[0], gapAnswerTuples);
	}

	public String toString() {
		return queryText;
	}

	public void outputAnswers(BufferedWriter writer) throws IOException {

		int answerCounter = soundAnswerTuples.size();
		if(!isProcessed()) answerCounter += gapAnswerTuples.size();

		Utility.logInfo("The number of answer tuples: " + answerCounter);

		if (writer != null) {
			writer.write("-------------- Query " + queryID + " ---------------------");
			writer.newLine();
			writer.write(queryText);
			writer.newLine();
			StringBuilder space = new StringBuilder();
			int arity = getArity(), varSpace = 0;
			for (int i = 0; i < arity; ++i)
				varSpace += answerVariables[0][i].length();
			for (int i = 0; i < (SEPARATOR.length() - varSpace) / (arity + 1); ++i)
				space.append(" ");
			for (int i = 0; i < getArity(); ++i) {
				writer.write(space.toString());
				writer.write(answerVariables[0][i]);
			}
			writer.newLine();
			writer.write(SEPARATOR);
			writer.newLine();
			for (AnswerTuple tuple: soundAnswerTuples) {
				writer.write(tuple.toString());
				writer.newLine();
			}
			if(!isProcessed())
				for (AnswerTuple tuple: gapAnswerTuples) {
					writer.write("*");
					writer.write(tuple.toString());
					writer.newLine();
				}
//			writer.write(SEPARATOR);
			writer.newLine();
		}

	}

	public void outputAnswerStatistics() {

		int answerCounter = soundAnswerTuples.size();
		if(!isProcessed()) answerCounter += gapAnswerTuples.size();

		Utility.logInfo("The number of answer tuples: " + answerCounter);
//		if (jsonAnswers != null) {
//			JSONObject jsonAnswer = new JSONObject();
//
//			jsonAnswer.put("queryID", queryID);
//			jsonAnswer.put("queryText", queryText);
//
//			JSONArray answerVars = new JSONArray();
//			int arity = getArity(), varSpace = 0;
//			for (int i = 0; i < getArity(); i++)
//				answerVars.add(answerVariables[0][i]);
//			jsonAnswer.put("answerVars", answerVars);
//
//			JSONArray answerTuples = new JSONArray();
//			soundAnswerTuples.stream().forEach(t -> answerTuples.add(t));
//			jsonAnswer.put("answerTuples", answerTuples);
//
//			if (!processed) {
//				JSONArray gapAnswerTuples = new JSONArray();
//				gapAnswerTuples.stream().forEach(t -> gapAnswerTuples.add(t));
//			}
//			jsonAnswer.put("gapAnswerTuples", gapAnswerTuples);
//
//			jsonAnswers.put(Integer.toString(queryID), jsonAnswer);
//		}
	}

	public void outputTimes() {
		for (Step step: Step.values()) {
			Utility.logDebug("time for " + step + ": " + timer[step.ordinal()]);
		}
	}

	public String outputSoundAnswerTuple() {
		StringBuilder builder = new StringBuilder();
		for (AnswerTuple tuple: soundAnswerTuples)
			builder.append(tuple.toString()).append(Utility.LINE_SEPARATOR);
		return builder.toString();
	}

	public String outputGapAnswerTuple() {
		StringBuilder builder = new StringBuilder();
		for(AnswerTuple tuple : gapAnswerTuples)
			builder.append(tuple.toString()).append(Utility.LINE_SEPARATOR);
		return builder.toString();
	}

	public Step getDifficulty() {
		return difficulty;
	}

	public void setDifficulty(Step step) {
		this.difficulty = step;
	}

	public OWLOntology getRelevantOntology() {
		return relevantOntology;
	}

	public void setRelevantOntology(OWLOntology knowledgebase) {
		relevantOntology = knowledgebase;
	}

	public void saveRelevantOntology(String filename) {
		if(relevantOntology == null) return;
		OWLOntologyManager manager = relevantOntology.getOWLOntologyManager();
		try {
			FileOutputStream outputStream = new FileOutputStream(filename);
			manager.saveOntology(relevantOntology, outputStream);
			outputStream.close();
		} catch (OWLOntologyStorageException e) {
			e.printStackTrace();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void saveRelevantClause() {
		if(relevantClauses == null) return;
		GeneralProgram p = new GeneralProgram(relevantClauses, relevantOntology);
		p.save();
	}

	public void removeUpperBoundAnswers(Collection<AnswerTuple> answers) {
		for (AnswerTuple answer: answers) {
//			if (soundAnswerTuples.contains(answer))
//				Utility.logError("The answer (" + answer + ") cannot be removed, because it is in the lower bound.");
			if (!gapAnswerTuples.contains(answer))
				Utility.logError("The answer (" + answer + ") cannot be removed, because it is not in the upper bound.");
			gapAnswerTuples.remove(answer);
		}
	}

	public void addLowerBoundAnswers(Collection<AnswerTuple> answers) {
		for (AnswerTuple answer: answers) {
			if (!gapAnswerTuples.contains(answer))
				Utility.logError("The answer (" + answer + ") cannot be added, because it is not in the upper bound.");
			gapAnswerTuples.remove(answer);

			answer = AnswerTuple.getInstance(answer, answerVariables[0].length);
//			if (soundAnswerTuples.contains(answer))
//				Utility.logError("The answer (" + answer + ") cannot be added, because it is in the lower bound.");
			soundAnswerTuples.add(answer);
		}
	}

	public int getNoOfSoundAnswers() {
		return soundAnswerTuples.size();
	}

	public void addProcessingTime(Step step, double time) {
		timer[step.ordinal()] += time;
	}

	public int getArity() {
		return answerVariables[0].length;
	}

	public void addRelevantClauses(DLClause clause) {
		relevantClauses.add(clause);
	}

	public Set<DLClause> getRelevantClauses() {
		return relevantClauses;
	}

	public void clearClauses() {
		relevantClauses.clear();
	}

	public boolean isHorn() {
		for (DLClause clause: relevantClauses)
			if (clause.getHeadLength() > 1)
				return false;
		return true;
	}

	public void saveABoxInTurtle(String filename) {
		try {
			BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(filename)));
			OWLIndividual a, b;
			StringBuilder builder = new StringBuilder();
			for (OWLAxiom axiom: relevantOntology.getABoxAxioms(true)) {
				if (axiom instanceof OWLClassAssertionAxiom) {
					OWLClassAssertionAxiom classAssertion = (OWLClassAssertionAxiom) axiom;
					OWLClass c = (OWLClass) classAssertion.getClassExpression();
					a = classAssertion.getIndividual();
					builder.append(a.toString())
						   .append(" <")
						   .append(Namespace.RDF_TYPE)
						   .append("> ")
						   .append(c.toString());
				}
				else if (axiom instanceof OWLObjectPropertyAssertionAxiom) {
					OWLObjectPropertyAssertionAxiom propertyAssertion = (OWLObjectPropertyAssertionAxiom) axiom;
					OWLObjectProperty p = (OWLObjectProperty) propertyAssertion.getProperty();
					a = propertyAssertion.getSubject();
					b = propertyAssertion.getObject();
					builder.append(a.toString()).append(" ").append(p.toString()).append(" ").append(b.toString());
				}
				else if (axiom instanceof OWLDataPropertyAssertionAxiom) {
					OWLDataPropertyAssertionAxiom propertyAssertion = (OWLDataPropertyAssertionAxiom) axiom;
					OWLDataProperty p = (OWLDataProperty) propertyAssertion.getProperty();
					a = propertyAssertion.getSubject();
					OWLLiteral l = propertyAssertion.getObject();
					builder.append(a.toString()).append(" ").append(p.toString()).append(" ").append(l.toString());
				}

				writer.write(builder.toString());
				writer.write(" .");
				writer.newLine();
				builder.setLength(0);
			}
			writer.close();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {

		}
	}
	
	public void updateSubID() {
		++subID;
		stringQueryID = String.valueOf(queryID) + "_" + subID;
	}

	public DLClause getClause() {
		if (queryClause != null)
			return queryClause;
		return queryClause = DLClauseHelper.getQuery(queryText, null);
	}

	public boolean isBottom() {
		return queryID == 0;
	}

	public int getNoOfCompleteAnswers() {
		return soundAnswerTuples.size() + gapAnswerTuples.size();
	}

	public int getSubID() {
		return subID;
	}

	public boolean hasSameGapAnswers(QueryRecord that) {
		return gapAnswerTuples.containsAll(that.gapAnswerTuples) && that.gapAnswerTuples.containsAll(gapAnswerTuples);
	}

	public void dispose() {
		m_manager.remove(queryText);
		if(gapAnswerTuples != null) gapAnswerTuples = null;
		if(soundAnswerTuples != null) soundAnswerTuples = null;
		if (relevantClauses != null) relevantClauses.clear();
		if (relevantOntology != null)
			relevantOntology.getOWLOntologyManager().removeOntology(relevantOntology);
		answerVariables = null;
	}

	public boolean canBeEncodedIntoAtom() {
		// FIXME
		return true;
//		return false;
	}

	public boolean isPredicate(AnswerTuple a, int i) {
		Atom[] atoms = getClause().getBodyAtoms();
		Variable v = Variable.create(answerVariables[1][i]);
		String iri;
		for(Atom atom : atoms) {
			DLPredicate p = atom.getDLPredicate();
			if (p instanceof AtomicConcept) {
				if(((AtomicConcept) p).getIRI().equals(v.toString())) return true;
			}
			else if (p instanceof AtomicRole) {
				iri = ((AtomicRole) p).getIRI();
				if (iri.equals(v.toString())) return true;
				if(iri.startsWith("?"))
					iri = a.getGroundTerm(i).toString();
				if(iri.equals(Namespace.RDF_TYPE) && atom.getArgument(1).equals(v)) return true;
			}
		}
		return false;
	}

	// TODO remove fully extended query
	public Tuple<String> getExtendedQueryText() {
//		String[] ret = new String[2];s
		int index = queryText.toUpperCase().indexOf(" WHERE");
		String extendedSelect = queryText.substring(0, index);
		String extendedWhere= queryText.substring(index + 1), fullyExtendedWhere = queryText.substring(index + 1);

		String sub, obj;
		Map<String, Set<String>> links = new HashMap<String, Set<String>>();
		Set<String> list;
		for (Atom atom: getClause().getBodyAtoms())
			if (atom.getDLPredicate() instanceof AtomicRole && atom.getArgument(0) instanceof Variable && atom.getArgument(1) instanceof Variable) {
				sub = atom.getArgumentVariable(0).getName();
				obj = atom.getArgumentVariable(1).getName();
				if((list = links.get(sub)) == null)
					links.put(sub, list = new HashSet<String>());
				list.add(obj);
				if((list = links.get(obj)) == null)
					links.put(obj, list = new HashSet<String>());
				list.add(sub);
			}

		StringBuilder extra = new StringBuilder(), fullyExtra = new StringBuilder();
//		if (answerVariables[0] != answerVariables[1]) {
			for (int i = answerVariables[0].length; i < answerVariables[1].length; ++i) {
//			for (int i = 0; i < answerVariables[1].length; ++i) {
				fullyExtra.append(" . ?")
						  .append(answerVariables[1][i])
						  .append(" " + RDF_TYPE + " <")
						  .append(Namespace.PAGODA_ORIGINAL)
						  .append(">");
				if ((list = links.get(answerVariables[1][i])) == null || list.size() < 2) ;
				else {
					extra.append(" . ?")
						 .append(answerVariables[1][i])
						 .append(" " + RDF_TYPE + " <")
						 .append(Namespace.PAGODA_ORIGINAL)
						 .append(">");
				}
			}

		if(extra.length() > 0) {
			extra.append(" }");
				extendedWhere = extendedWhere.replace(" }", extendedWhere.contains(". }") ? extra.substring(2) : extra.toString());
			}

		if(fullyExtra.length() > 0) {
			fullyExtra.append(" }");
				fullyExtendedWhere = fullyExtendedWhere.replace(" }", fullyExtendedWhere.contains(". }") ? fullyExtra.substring(2) : fullyExtra.toString());
			}
//		}

		TupleBuilder result = new TupleBuilder();
		result.append(extendedSelect + " " + fullyExtendedWhere);

		extra.setLength(0);
		if (answerVariables[0] != answerVariables[1]) {
			for (int i = answerVariables[0].length; i < answerVariables[1].length; ++i)
				extra.append(" ?").append(answerVariables[1][i]);
			extendedSelect = extendedSelect + extra.toString();
		}
		result.append(extendedSelect + " " + extendedWhere);

		return result.build();
	}

	public boolean hasNonAnsDistinguishedVariables() {
		return answerVariables[1].length > answerVariables[0].length;
	}

	/**
	 * Two <tt>QueryRecords</tt> are equal iff
	 * they have the same <tt>queryText</tt>,
	 * <tt>soundAnswerTuples</tt>.
	 */
	@Override
	public boolean equals(Object o) {
		if(!o.getClass().equals(getClass())) return false;
		QueryRecord that = (QueryRecord) o;
		return this.queryText.equals(that.queryText)
				&& soundAnswerTuples.equals(that.soundAnswerTuples);
	}

	@Override
	public int hashCode() {
		return Objects.hash(queryText, soundAnswerTuples);
	}

	public enum Step {
		LOWER_BOUND,
		UPPER_BOUND,
		SIMPLE_UPPER_BOUND,
		LAZY_UPPER_BOUND,
		L_SKOLEM_UPPER_BOUND,
		EL_LOWER_BOUND,
		FRAGMENT,
		FRAGMENT_REFINEMENT,
		SUMMARISATION,
		DEPENDENCY,
		FULL_REASONING
	}

	/**
	 * A Json serializer, which considers the main attributes.
	 */
	public static class QueryRecordSerializer implements JsonSerializer<QueryRecord> {
		public JsonElement serialize(QueryRecord src, Type typeOfSrc, JsonSerializationContext context) {
			Gson gson = new GsonBuilder().setPrettyPrinting().create();
			JsonObject object = new JsonObject();
			object.addProperty("queryID", src.queryID);
			object.addProperty("queryText", src.queryText);
//			object.addProperty("difficulty", src.difficulty != null ? src.difficulty.toString() : "");

			object.add("answerVariables", context.serialize(src.getAnswerVariables()));
			object.add("answers", context.serialize(src.soundAnswerTuples));
//			object.add("gapAnswers", context.serialize(src.gapAnswerTuples));

			return object;
		}
	}

	/**
	 * A Json deserializer, compliant to the output of the serializer defined above.
	 */
	public static class QueryRecordDeserializer implements JsonDeserializer<QueryRecord> {
		public QueryRecord deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
				throws JsonParseException {

			QueryRecord record = new QueryRecord();
			JsonObject object = json.getAsJsonObject();
			record.queryID = object.getAsJsonPrimitive("queryID").getAsInt();
			record.queryText = object.getAsJsonPrimitive("queryText").getAsString();
//			record.difficulty = Step.valueOf(object.getAsJsonPrimitive("difficulty").getAsString());

			JsonArray answerVariablesJson = object.getAsJsonArray("answerVariables");
			record.answerVariables = new String[2][];
			record.answerVariables[0] = new String[answerVariablesJson.size()];
			for(int i = 0; i < answerVariablesJson.size(); i++)
				record.answerVariables[0][i] = answerVariablesJson.get(i).getAsString();

			record.soundAnswerTuples = new HashSet<>();
//			record.gapAnswerTuples = new HashSet<>();
			Type type = new TypeToken<AnswerTuple>() { }.getType();
			for (JsonElement answer : object.getAsJsonArray("answers")) {
				record.soundAnswerTuples.add(context.deserialize(answer, type));
			}
//			for (JsonElement answer : object.getAsJsonArray("gapAnswers")) {
//				record.soundAnswerTuples.add(context.deserialize(answer, type));
//			}

			return record;
		}
	}

	/**
	 * Provides an instance (singleton) of Gson, having a specific configuration.
	 * */
	public static class GsonCreator {

		private static Gson gson;

		private GsonCreator() {}

		public static Gson getInstance() {
			if(gson == null) {
				gson = new GsonBuilder()
						.registerTypeAdapter(AnswerTuple.class, new AnswerTuple.AnswerTupleSerializer())
						.registerTypeAdapter(QueryRecord.class, new QueryRecord.QueryRecordSerializer())
						.registerTypeAdapter(QueryRecord.class, new QueryRecord.QueryRecordDeserializer())
						.registerTypeAdapter(AnswerTuple.class, new AnswerTuple.AnswerTupleDeserializer())
						.disableHtmlEscaping()
						.setPrettyPrinting()
						.create();
			}
			return gson;
		}

//		public static void dispose() {
//			gson = null;
//		}

	}
}
