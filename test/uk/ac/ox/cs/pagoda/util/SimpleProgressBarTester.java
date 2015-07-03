package uk.ac.ox.cs.pagoda.util;

import org.testng.annotations.Test;

public class SimpleProgressBarTester {

    @Test
    public void test() throws InterruptedException {
        SimpleProgressBar simpleProgressBar = new SimpleProgressBar("TestBar", 1000);
        for(int i = 0; i < 1000; i++) {
            simpleProgressBar.update(i);
            Thread.sleep(10);
        }
        simpleProgressBar.dispose();
    }
}
