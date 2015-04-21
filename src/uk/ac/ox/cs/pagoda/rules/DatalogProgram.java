package uk.ac.ox.cs.pagoda.rules;

import org.semanticweb.owlapi.model.OWLOntology;

import uk.ac.ox.cs.pagoda.constraints.*;

public class DatalogProgram {

	UpperDatalogProgram upperProgram = new UpperDatalogProgram(); 
	LowerDatalogProgram lowerProgram; 
	GeneralProgram program = new GeneralProgram();
	
	BottomStrategy upperBottom; 
	
	public DatalogProgram(OWLOntology o) {
		lowerProgram = new LowerDatalogProgram();
		init(o, false); 
	}
	
	public DatalogProgram(OWLOntology o, boolean toClassify) {
		lowerProgram = new LowerDatalogProgram(toClassify);
		init(o, toClassify); 
	}

	private void init(OWLOntology o, boolean forSemFacet) {
		upperProgram.load(o, upperBottom = new UpperUnaryBottom());
//		upperProgram.load(o, upperBottom = new UnaryBottom());
		lowerProgram.clone(upperProgram);
		program.clone(upperProgram);
//		program.botStrategy = new UnaryBottom(); 
		
		upperProgram.transform();
		lowerProgram.transform();
		program.transform();
		
		program.buildDependencyGraph();
		PredicateDependency graph = upperProgram.buildDependencyGraph(); 
		lowerProgram.dependencyGraph = graph; 
	}

	public LowerDatalogProgram getLower() {
		return lowerProgram; 
	}
	
	public UpperDatalogProgram getUpper() {
		return upperProgram; 
	}
	
	public GeneralProgram getGeneral() {
		return program; 
	}
	
	public String getAdditionalDataFile() {
		return upperProgram.getAdditionalDataFile();
	}

	public BottomStrategy getUpperBottomStrategy() {
		return upperBottom;
	}

	public void deleteABoxTurtleFile() {
		if (upperProgram  != null) upperProgram.deleteABoxTurtleFile();  
	}
}
