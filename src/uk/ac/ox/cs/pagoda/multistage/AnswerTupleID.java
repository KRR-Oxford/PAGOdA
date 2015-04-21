package uk.ac.ox.cs.pagoda.multistage;

import java.util.Map;

import org.semanticweb.HermiT.model.Variable;

import uk.ac.ox.cs.JRDFox.JRDFStoreException;
import uk.ac.ox.cs.JRDFox.store.TupleIterator;

public class AnswerTupleID {
	
	public static final String SEPARATOR = "\t";
	
	String m_str = null; 
	int[] m_tuple; 

	public AnswerTupleID(TupleIterator iter) throws JRDFStoreException {
		m_tuple = new int[iter.getArity()]; 
		for (int i = 0; i < iter.getArity(); ++i) 
			m_tuple[i] = (int) iter.getResourceID(i); 
	}
	
	public AnswerTupleID(int arity) {
		m_tuple = new int[arity];
	}
	
	public void setTerm(int index, int term) {
		m_tuple[index] = term; 
	}

	public int getArity() {
		return m_tuple.length; 
	}

	public int getTerm(int index) {
		return m_tuple[index]; 
	}
	
	@Override	
	public String toString() {
		if (m_str != null) return m_str; 
		StringBuilder sb = new StringBuilder();  
		for (int i = 0; i < m_tuple.length; ++i) {
			if (sb.length() != 0) sb.append(SEPARATOR);
			sb.append(m_tuple[i]); 
		}
		return sb.toString();
	}
	
	@Override 
	public int hashCode() {
		int code = 0; 
		for (int i = 0; i < m_tuple.length; ++i)
			code = code * 1997 + m_tuple[i];
		return code; 
//		return toString().hashCode(); 
	}
	
	@Override 
	public boolean equals(Object that) {
		if (!(that instanceof AnswerTupleID)) return false;
		AnswerTupleID thatTuple = (AnswerTupleID) that; 
		if (m_tuple.length != thatTuple.m_tuple.length) return false; 
		for (int i = 0; i < m_tuple.length; ++i)
			if (m_tuple[i] != thatTuple.m_tuple[i])
				return false; 
		return true; 
//		return toString().equals(that.toString()); 
	}

	public Map<Variable, Integer> getAssignment(String[] variables, Map<Variable, Integer> assignment) {
		for (int i = 0; i < m_tuple.length; ++i)
			assignment.put(Variable.create(variables[i]), m_tuple[i]); 
		return assignment; 
	}

}
