package uk.ac.ox.cs.pagoda.junit;

import static org.junit.Assert.*;

import org.junit.Test;

import uk.ac.ox.cs.pagoda.tester.PagodaTester;
import uk.ac.ox.cs.pagoda.tester.Statistics;

public class PagodaLUBM {

	public void test_all(int number) {
		PagodaTester.main(
				PagodaTester.onto_dir + "lubm/univ-bench.owl",
				PagodaTester.onto_dir + "lubm/data/lubm" + number + ".ttl",
				PagodaTester.onto_dir + "lubm/queries/test_all_pagoda.sparql" 
		); 
		
		AllTests.copy("log4j.log", "output/jair/lubm" + number + ".out"); 
	}
	
	@Test
	public void test1() { test_all(1); }
	
	public void test() {
		int number = 100;
		test_all(number); 
	}
	
	public void check(int number) {
		Statistics stat = new Statistics("output/log4j.log");
		String diff = stat.diff("results-backup/benchmark/lubm" + number + ".out"); 
		AllTests.copy("output/log4j.log", "results-backup/current/lubm" + number + ".out"); 
		if (!diff.isEmpty()) 
			fail(diff);
	}
	
}
