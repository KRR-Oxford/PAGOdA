package uk.ac.ox.cs.pagoda.query;

import com.google.gson.*;
import org.semanticweb.HermiT.model.Constant;
import org.semanticweb.HermiT.model.Individual;
import org.semanticweb.HermiT.model.Term;
import org.semanticweb.HermiT.model.Variable;
import uk.ac.ox.cs.JRDFox.JRDFStoreException;
import uk.ac.ox.cs.JRDFox.model.Datatype;
import uk.ac.ox.cs.JRDFox.model.GroundTerm;
import uk.ac.ox.cs.JRDFox.model.Literal;
import uk.ac.ox.cs.JRDFox.store.TupleIterator;
import uk.ac.ox.cs.pagoda.util.Namespace;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class AnswerTuple {
	
	public static final String SEPARATOR = "\t";
	static final Pattern owlLiteralRegex =
			Pattern.compile("^\"(?<lexicalForm>[^@]+(@(?<langTag>.+))?)\"(^^<(?<dataType>.+)>)?$");
	String m_str = null;
	GroundTerm[] m_tuple;
	
	public AnswerTuple(TupleIterator iter, int arity) {
		m_tuple = new GroundTerm[arity];
		try {
			for (int i = 0; i < arity; ++i)
				m_tuple[i] = iter.getGroundTerm(i);
		} catch (JRDFStoreException e) {
			e.printStackTrace();
		}
	}
	
	public AnswerTuple(GroundTerm[] terms) {
		m_tuple = terms;
	}

//	private AnswerTuple(String m_str) {
//		this.m_str = m_str;
//	}

	private AnswerTuple(AnswerTuple sup, int arity) {
		m_tuple = new GroundTerm[arity];
		for(int i = 0; i < arity; ++i) m_tuple[i] = sup.m_tuple[i];
	}

	public static AnswerTuple create(AnswerTuple extendedTuple, int length) {
		if(length == extendedTuple.getArity()) return extendedTuple;
		else return new AnswerTuple(extendedTuple, length);
	}
	
	public int getArity() {
		return m_tuple.length;
	}

	public int hashCode() {
//		return toString().hashCode();
		int code = 0;
		for (int i = 0; i < m_tuple.length; ++i)
			code = code * 1997 + m_tuple[i].hashCode();
		return code;
	}

	public boolean equals(Object obj) {
		if (!(obj instanceof AnswerTuple)) return false;
		AnswerTuple that = (AnswerTuple) obj;
		if (m_tuple.length != that.m_tuple.length) return false;
		for (int i = 0; i < m_tuple.length; ++i)
			if (!m_tuple[i].equals(that.m_tuple[i]))
				return false;
		return true;
//		return toString().equals(obj.toString());
	}
	
	public String toString() {
		if(m_str != null) return m_str;
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < m_tuple.length; ++i) {
			if (sb.length() != 0) sb.append(SEPARATOR);
			if (m_tuple[i] instanceof uk.ac.ox.cs.JRDFox.model.Individual)
				sb.append("<").append(((uk.ac.ox.cs.JRDFox.model.Individual) m_tuple[i]).getIRI()).append(">");
			else if (m_tuple[i] instanceof uk.ac.ox.cs.JRDFox.model.BlankNode) {
				sb.append(m_tuple[i].toString());
			}
			else {
				Literal l = (Literal) m_tuple[i];
				sb.append('"').append(l.getLexicalForm()).append("\"");
				if (!l.getDatatype().equals(Datatype.XSD_STRING) && !l.getDatatype().equals(Datatype.RDF_PLAIN_LITERAL))
					sb.append("^^<").append(l.getDatatype().getIRI()).append(">");
			}
		}
		return m_str = sb.toString();
	}

	public GroundTerm getGroundTerm(int i) {
		return m_tuple[i];
	}
	
	public Map<Variable, Term> getAssignment(String[] vars) {
		Map<Variable, Term> map = new HashMap<Variable, Term>();
		int index = 0;
		Term t;
		for (String var: vars) {
			if(m_tuple[index] instanceof uk.ac.ox.cs.JRDFox.model.Individual)
				t = Individual.create((((uk.ac.ox.cs.JRDFox.model.Individual) m_tuple[index]).getIRI()));
			else {
				uk.ac.ox.cs.JRDFox.model.Literal l = (uk.ac.ox.cs.JRDFox.model.Literal) m_tuple[index];
				t = Constant.create(l.getLexicalForm(), l.getDatatype().getIRI());
			}
			map.put(Variable.create(var), t);
			++index;
		}
		return map;
	}

	public boolean hasAuxPredicate() {
		String iri;
		for (int i = 0; i < m_tuple.length; ++i)
			if ((m_tuple[i] instanceof uk.ac.ox.cs.JRDFox.model.Individual)) {
				iri = ((uk.ac.ox.cs.JRDFox.model.Individual) m_tuple[i]).getIRI();
				if(iri.startsWith(Namespace.PAGODA_AUX) || iri.contains("_AUX") || iri.contains("_neg") || iri.contains("internal:def"))
					return true;
			}
		return false;
	}

	public boolean hasAnonymousIndividual() {
		String iri;
		for(int i = 0; i < m_tuple.length; ++i)
			if((m_tuple[i] instanceof uk.ac.ox.cs.JRDFox.model.Individual)) {
				iri = ((uk.ac.ox.cs.JRDFox.model.Individual) m_tuple[i]).getIRI();
				if(iri.startsWith(Namespace.PAGODA_ANONY) || iri.startsWith(Namespace.KARMA_ANONY))
					return true;
			}
		return false;
	}

	public static class AnswerTupleSerializer implements JsonSerializer<AnswerTuple> {

		public JsonElement serialize(AnswerTuple src, Type typeOfSrc, JsonSerializationContext context) {
			return new JsonPrimitive(src.toString());
		}

	}

	public static class AnswerTupleDeserializer implements JsonDeserializer<AnswerTuple> {
		public AnswerTuple deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
				throws JsonParseException {
			String tuplesString = json.getAsJsonPrimitive().getAsString();
			StringTokenizer tokenizer = new StringTokenizer(SEPARATOR);
			GroundTerm[] terms = new GroundTerm[tokenizer.countTokens()];

			// TODO test parsing
			for (int i = 0; i < tokenizer.countTokens(); i++) {
				String token = tokenizer.nextToken();
				if (token.charAt(0) == '<') {
					terms[i] = uk.ac.ox.cs.JRDFox.model.Individual.create(token.substring(1,token.length()-1));
				}
				else if (token.charAt(0) == '"') {
					Matcher matcher = owlLiteralRegex.matcher(token);
					if(matcher.matches()) {
						String lexicalForm = matcher.group("lexicalForm");
						String dataTypeIRI = matcher.group("dataType");
						Datatype dataType;
						if (dataTypeIRI.isEmpty()) dataType = Datatype.RDF_PLAIN_LITERAL;
						else dataType = uk.ac.ox.cs.JRDFox.model.Datatype.value(dataTypeIRI);
						terms[i] = uk.ac.ox.cs.JRDFox.model.Literal.create(lexicalForm, dataType);
					}
					else {
						throw new IllegalArgumentException("The given json does not represent a valid AnswerTuple");
					}
				}
				else {
					terms[i] = uk.ac.ox.cs.JRDFox.model.BlankNode.create(token);
				}
			}
			return new AnswerTuple(terms);
		}
	}

}
