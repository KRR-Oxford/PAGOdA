package uk.ac.ox.cs.pagoda.rules;

import org.semanticweb.owlapi.model.OWLOntology;
import uk.ac.ox.cs.pagoda.constraints.BottomStrategy;
import uk.ac.ox.cs.pagoda.constraints.PredicateDependency;
import uk.ac.ox.cs.pagoda.constraints.UpperUnaryBottom;
import uk.ac.ox.cs.pagoda.util.disposable.Disposable;
import uk.ac.ox.cs.pagoda.util.disposable.DisposedException;

public class DatalogProgram extends Disposable {

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

    public LowerDatalogProgram getLower() {
        if(isDisposed()) throw new DisposedException();
        return lowerProgram;
    }

    public UpperDatalogProgram getUpper() {
        if(isDisposed()) throw new DisposedException();
        return upperProgram;
    }

    public GeneralProgram getGeneral() {
        if(isDisposed()) throw new DisposedException();
        return program;
    }

    public String getAdditionalDataFile() {
        if(isDisposed()) throw new DisposedException();
        return upperProgram.getAdditionalDataFile();
    }

    public BottomStrategy getUpperBottomStrategy() {
        if(isDisposed()) throw new DisposedException();
        return upperBottom;
    }

    @Override
    public void dispose() {
        super.dispose();
        if(upperProgram != null) upperProgram.deleteABoxTurtleFile();
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
}
