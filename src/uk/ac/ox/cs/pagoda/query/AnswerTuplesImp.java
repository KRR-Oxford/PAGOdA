package uk.ac.ox.cs.pagoda.query;

import java.util.Iterator;
import java.util.Set;

public class AnswerTuplesImp implements AnswerTuples {

	int m_index; 
	Iterator<AnswerTuple> m_iter;
	Set<AnswerTuple> m_answers1, m_answers2; 
	String[] m_answerVars; 
	AnswerTuple m_tuple; 

	public AnswerTuplesImp(String[] answerVars, Set<AnswerTuple> answers) {
		m_answers1 = answers;
		m_answers2 = null; 
		m_answerVars = answerVars; 
		reset(); 
	}
	
	public AnswerTuplesImp(String[] answerVars, Set<AnswerTuple> answers1, Set<AnswerTuple> answers2) {
		m_answers1 = answers1; 
		m_answers2 = answers2; 
		m_answerVars = answerVars; 
		reset(); 
	}
	
	@Override
	public boolean isValid() {
		return m_tuple != null; 
	}

	@Override
	public int getArity() {
		return m_answerVars.length;
	}

	@Override
	public void moveNext() {
		if (m_iter != null && m_iter.hasNext()) {
			m_tuple = m_iter.next();
			return ; 
		}
		else if (m_answers2 != null && m_index == 1){
			++m_index; 
			m_iter = m_answers2.iterator(); 
			if (m_iter.hasNext()) {
				m_tuple = m_iter.next();
				return ; 
			}
		}
		else
			m_tuple = null; 
	}

	@Override
	public void dispose() {}

	@Override
	public void reset() {
		if (m_answers1 == null || m_answers1.isEmpty()) {
			m_index = 2;
			m_iter = m_answers2 == null ? null : m_answers2.iterator();
		}
		else {
			m_index = 1; 
			m_iter =  m_answers1.iterator();
		}		
		moveNext();
	}

	@Override
	public boolean contains(AnswerTuple t) {
		return m_answers1.contains(t) || (m_answers2 != null && m_answers2.contains(t));
	}

	@Override
	public AnswerTuple getTuple() {
		return m_tuple; 
	}

	@Override
	public String[] getAnswerVariables() {
		return m_answerVars;
	}

	@Override
	public void remove() {
		m_iter.remove();
	}

}
