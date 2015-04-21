package uk.ac.ox.cs.pagoda.junit;

import org.junit.Test;

import uk.ac.ox.cs.pagoda.tester.PagodaTester;
import uk.ac.ox.cs.pagoda.tester.Statistics;

public class PagodaUOBM {

	public void test_all(int number ) {
		PagodaTester.main(
				PagodaTester.onto_dir + "uobm/univ-bench-dl.owl",
				PagodaTester.onto_dir + "uobm/data/uobm" + number + ".ttl",
				PagodaTester.onto_dir + "uobm/queries/standard_all_pagoda.sparql" 
//				PagodaTester.onto_dir + "uobm/queries/standard_group3_all_less.sparql;" + 
//				PagodaTester.onto_dir + "uobm/queries/G3.sparql;" +
//				PagodaTester.onto_dir + "uobm/queries/last.sparql" 
		); 
		
		AllTests.copy("log4j.log", "output/jair/newuobm/uobm" + number + ".out"); 
	}
	
	public void test_upToSum(int number) {
		PagodaTester.main(
				PagodaTester.onto_dir + "uobm/univ-bench-dl.owl",
				PagodaTester.onto_dir + "uobm/data/uobm" + number + ".ttl",
				PagodaTester.onto_dir + "uobm/queries/standard_group3_all.sparql" 
		); 
		
//		AllTests.copy("log4j.log", "output/jair/uobm" + number + ".out"); 
	}
	
	@Test
	public void test1() {	test_all(1); }
	
	public void test500() {	test_upToSum(500); }
	
	public static void main(String... args) {
		new PagodaUOBM().test_all(1); 
	}
	
	public void check() {
		Statistics stat = new Statistics("results-backup/current/uobm1.out"); 
		String diff = stat.diff("results-backup/benchmark/uobm1.out"); 
		System.out.println(diff); 
	}
	
}
