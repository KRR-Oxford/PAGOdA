package uk.ac.ox.cs.pagoda.multistage;


import uk.ac.ox.cs.pagoda.constraints.BottomStrategy;
import uk.ac.ox.cs.pagoda.rules.ExistConstantApproximator;
import uk.ac.ox.cs.pagoda.rules.LimitedSkolemisationApproximator;
import uk.ac.ox.cs.pagoda.rules.Program;

public class LimitedSkolemisationApplication extends RestrictedApplication {

    public static final int MAX_DEPTH = 1;

    public LimitedSkolemisationApplication(Program program, BottomStrategy upperBottom) {
        super(program, upperBottom);
        m_approxExist = new LimitedSkolemisationApproximator(MAX_DEPTH, new ExistConstantApproximator());
    }
}
