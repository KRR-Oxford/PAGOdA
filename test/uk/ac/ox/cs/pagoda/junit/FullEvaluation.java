package uk.ac.ox.cs.pagoda.junit;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(Suite.class)
@SuiteClasses({ LightEvaluation.class, 
	CostEvaluation.class
	})

public class FullEvaluation {
	
}
