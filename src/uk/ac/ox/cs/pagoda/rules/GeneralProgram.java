package uk.ac.ox.cs.pagoda.rules;

import org.apache.commons.io.FilenameUtils;
import org.semanticweb.HermiT.model.DLClause;
import org.semanticweb.owlapi.model.OWLOntology;
import uk.ac.ox.cs.pagoda.constraints.UnaryBottom;

import java.util.Collection;
import java.util.Collections;
import java.util.Set;

public class GeneralProgram extends Program {
	
	public GeneralProgram(Set<DLClause> relevantClauses, OWLOntology relevantOntology) {
		ontology = relevantOntology;
		
		ontologyDirectory = null; 
		dlOntology = null; 
		botStrategy = new UnaryBottom(); 
		
		clauses = botStrategy.process(relevantClauses); 
	}

	public GeneralProgram() {}

	public Collection<DLClause> convert2Clauses(DLClause clause) {
		return botStrategy.process(Collections.singleton(clause));
	}

	@Override
	public String getOutputPath() {
		return FilenameUtils.concat(getDirectory(), "rules.dlog");
	}

//	@Override
//	public String getDirectory() {
//		File dir = new File(ontologyDirectory + Utility.FILE_SEPARATOR + "general");
//		if (!dir.exists())
//			dir.mkdirs();
//		return dir.getPath();
//	}

	public boolean isHorn() {
		for (DLClause clause: clauses)
			if (clause.getHeadLength() > 1)
				return false;
		return true; 
	}

}
