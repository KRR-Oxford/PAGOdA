package uk.ac.ox.cs.pagoda.reasoner.light;

import org.semanticweb.HermiT.model.Constant;
import org.semanticweb.HermiT.model.Individual;
import org.semanticweb.HermiT.model.Term;

import uk.ac.ox.cs.pagoda.query.AnswerTuple;
import uk.ac.ox.cs.pagoda.query.AnswerTuples;
import uk.ac.ox.cs.pagoda.util.Utility;
import uk.ac.ox.cs.JRDFox.JRDFStoreException;
import uk.ac.ox.cs.JRDFox.model.GroundTerm;
import uk.ac.ox.cs.JRDFox.store.TupleIterator;

public class RDFoxAnswerTuples implements AnswerTuples {

	long multi; 
	TupleIterator m_iter; 
	String[] m_answerVars; 
	
	public RDFoxAnswerTuples(String[] answerVars, TupleIterator iter) {
		m_answerVars = answerVars;
		m_iter = iter; 
		reset(); 
	}
	
	@Override
	public boolean isValid() {
		return multi != 0;
	}

	@Override
	public int getArity() {
		try {
			return m_iter.getArity();
		} catch (JRDFStoreException e) {
			e.printStackTrace();
			return -1; 
		}
	}

	@Override
	public void moveNext() {
		try {
			multi = m_iter.getNext();
		} catch (JRDFStoreException e) {
			e.printStackTrace();
		}  
	}

	@Override
	public void dispose() {
		m_iter.dispose();
	}
	
	protected void finalize() {
		m_iter.dispose();
	}
	
	@Override
	public AnswerTuple getTuple() {
		return new AnswerTuple(m_iter, m_answerVars.length); 
	}

	@Override
	public void reset() {
		try {
			multi = m_iter.open();
		} catch (JRDFStoreException e) {
			e.printStackTrace();
		} 		
	}

	@Override
	public boolean contains(AnswerTuple t) {
		Utility.logError("Unsupported operation in RDFoxAnswerTuples"); 
		return false;
	}

	@Override
	public void remove() {
		Utility.logError("Unsupported operation in RDFoxAnswerTuples"); 
	}

	@Override
	public String[] getAnswerVariables() {
		return m_answerVars;
	}
	
	public static Term getHermitTerm(GroundTerm t) {
		if (t instanceof uk.ac.ox.cs.JRDFox.model.Individual) {
			uk.ac.ox.cs.JRDFox.model.Individual individual = (uk.ac.ox.cs.JRDFox.model.Individual) t; 
			return Individual.create(individual.getIRI());
		}
		else {
			uk.ac.ox.cs.JRDFox.model.Literal literal = ((uk.ac.ox.cs.JRDFox.model.Literal) t); 
			return Constant.create(literal.getLexicalForm(), literal.getDatatype().getIRI());
		}
	}
	
}
