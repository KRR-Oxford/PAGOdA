package uk.ac.ox.cs.pagoda.multistage;

import org.openrdf.rio.RDFHandlerException;
import org.openrdf.rio.RDFParseException;
import org.openrdf.rio.turtle.TurtleParser;
import uk.ac.ox.cs.JRDFox.JRDFStoreException;
import uk.ac.ox.cs.JRDFox.model.Individual;
import uk.ac.ox.cs.pagoda.query.GapByStore4ID;
import uk.ac.ox.cs.pagoda.reasoner.QueryReasoner;
import uk.ac.ox.cs.pagoda.rules.DatalogProgram;
import uk.ac.ox.cs.pagoda.util.Timer;
import uk.ac.ox.cs.pagoda.util.Utility;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Collection;

public class TwoStageQueryEngine extends StageQueryEngine {
	
	IndividualCollector m_collector = new IndividualCollector();

	public TwoStageQueryEngine(String name, boolean checkValidity) {
		super(name, checkValidity);
	}

	@Override
	public void materialiseFoldedly(DatalogProgram dProgram, GapByStore4ID gap) {
		TwoStageApplication program = new FoldedApplication2(this, dProgram, gap);
		program.materialise();
	}
	
	@Override
	public void importRDFData(String fileName, String importedFile) {
		super.importRDFData(fileName, importedFile);
		TurtleParser parser = new TurtleParser(); 
		parser.setRDFHandler(m_collector);
		for (String file: importedFile.split(QueryReasoner.ImportDataFileSeparator)) {
			FileInputStream inputStream;
			try {
				inputStream = new FileInputStream(file);
				parser.parse(inputStream, "");
				inputStream.close();
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (RDFParseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (RDFHandlerException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} 
		}
	}

	@Override
	public int materialiseRestrictedly(DatalogProgram dProgram, GapByStore4ID gap) {
		TwoStageApplication program = new RestrictedApplication2(this, dProgram, gap);
		return program.materialise();
	}

	@Override
	public int materialiseSkolemly(DatalogProgram dProgram, GapByStore4ID gap) {
		throw new UnsupportedOperationException("This method is not available in " + getClass());
	}

	public void materialise(String programText,	GapByStore4ID gap, boolean incrementally) {
		try {
			if (gap != null) {
				try {
					gap.compile(incrementally ? null : programText);
					gap.addBackTo();
				} finally {
					gap.clear();
				}
			} else {
				long oldTripleCount = store.getTriplesCount();
				Timer t = new Timer(); 

				if (!incrementally)
//					store.addRules(new String[] {programText});
					store.importRules(programText);
				store.applyReasoning(incrementally);
				
				long tripleCount = store.getTriplesCount();
				
				Utility.logDebug("current store after materialising upper related rules: " + tripleCount + " (" + (tripleCount - oldTripleCount) + " new)");
				Utility.logDebug("current store finished the materialisation of upper related rules in " + t.duration() + " seconds.");
			}
			store.clearRulesAndMakeFactsExplicit();
		} catch (JRDFStoreException e) {
			e.printStackTrace();
		}

	}

	public Collection<Individual> getAllIndividuals() {
		return m_collector.getAllIndividuals();
	}

}


