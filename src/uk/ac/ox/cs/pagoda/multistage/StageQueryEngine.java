package uk.ac.ox.cs.pagoda.multistage;

import uk.ac.ox.cs.pagoda.query.AnswerTuples;
import uk.ac.ox.cs.pagoda.query.GapByStore4ID;
import uk.ac.ox.cs.pagoda.query.QueryRecord;
import uk.ac.ox.cs.pagoda.reasoner.light.BasicQueryEngine;
import uk.ac.ox.cs.pagoda.rules.DatalogProgram;
import uk.ac.ox.cs.pagoda.util.Utility;

public abstract class StageQueryEngine extends BasicQueryEngine {

	protected boolean checkValidity;
	Boolean validMaterialisation = null;

	public StageQueryEngine(String name, boolean checkValidity) {
		super(name);
		this.checkValidity = checkValidity;
	}

	public abstract void materialiseFoldedly(DatalogProgram dProgram, GapByStore4ID gap);

	public abstract int materialiseRestrictedly(DatalogProgram dProgram, GapByStore4ID gap);

	public abstract int materialiseSkolemly(DatalogProgram dProgram, GapByStore4ID gap);

	public boolean isValid() {
		if (!checkValidity) return true; 
		if (validMaterialisation != null) return validMaterialisation; 
		
		validMaterialisation = false;
		
		AnswerTuples iter = null;
		try {
			iter = evaluate(QueryRecord.botQueryText);
			validMaterialisation = !iter.isValid();
//			if (!validMaterialisation)
//				outputAnswers(QueryRecord.botQueryText);
		} finally {
			if (iter != null) iter.dispose();
		}

		if (validMaterialisation)
			Utility.logInfo("The " + name + " store is valid.");
		else
			Utility.logInfo("The " + name + " store is not valid.");
		return validMaterialisation;
	}

}
