package uk.ac.ox.cs.pagoda.multistage;

/***
 * It is like a <tt>MultiStageQueryEngine</tt>, but you can call <tt>materialiseSkolemly</tt>
 * multiple times with increasing values of <tt>maxTermDepth</tt>.
 */
public class IncrementalMultiStageQueryEngine extends MultiStageQueryEngine {

    public IncrementalMultiStageQueryEngine(String name, boolean checkValidity) {
        super(name, checkValidity);
    }
}
