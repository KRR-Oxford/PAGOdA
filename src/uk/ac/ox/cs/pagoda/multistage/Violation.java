package uk.ac.ox.cs.pagoda.multistage;

import org.semanticweb.HermiT.model.DLClause;

import java.util.LinkedList;

public class Violation {

	DLClause clause = null; 
	DLClause constraint; 
	LinkedList<AnswerTupleID> tuples;
	String[] vars; 
	
	public Violation(DLClause clause, LinkedList<AnswerTupleID> gap, String[] vars) {
		constraint = clause; 
		tuples = gap;
		this.vars = vars; 
	}
	
	public void setClause(DLClause clause) {
		this.clause = clause; 
	}
	
	public DLClause getClause() {
		if (clause != null) return clause;  
		return constraint; 
	}
	
	public DLClause getConstraint() {
		return constraint;
	}

	public LinkedList<AnswerTupleID> getTuples() {
		return tuples;
	}

	public int size() {
		return tuples.size();
	} 
	
	public String[] getVariables() {
		return vars; 
	}

	@Override
	public String toString() {
		return "Violation{" +
				"constraint=" + constraint +
				", clause=" + clause +
				", tuples=" + tuples +
				'}';
	}
}
