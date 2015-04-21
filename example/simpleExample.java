import org.semanticweb.owlapi.model.OWLOntology;

import uk.ac.ox.cs.pagoda.query.AnswerTuple;
import uk.ac.ox.cs.pagoda.query.AnswerTuples;
import uk.ac.ox.cs.pagoda.reasoner.QueryReasoner;


public class simpleExample {
	
	OWLOntology ontology; 
	String dataPath;		// splited by ; (i.e. path1;path2)
	String[] queryTexts; 

	public boolean test() {
		QueryReasoner r = QueryReasoner.getInstance(ontology); 
		try  {
			r.loadOntology(ontology);
			r.importData(dataPath);
			if (!r.preprocess()) return false;
			AnswerTuples answers; 
			AnswerTuple answer; 
			for (String queryText: queryTexts) {
				answers = r.evaluate(queryText);
				for (int arity = answers.getArity(); answers.isValid(); answers.moveNext()) {
					answer = answers.getTuple();  
					for (int i = 0; i < arity; ++i)
						System.out.println(answer.getGroundTerm(i) + " "); 
					System.out.println();
					answers.dispose();
				}
			}
		} finally {
			r.dispose();
		}
		return true; 
	}
	
}
