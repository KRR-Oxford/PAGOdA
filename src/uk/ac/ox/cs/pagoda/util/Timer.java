package uk.ac.ox.cs.pagoda.util;

public class Timer {
	
	double pastTime = 0;
	boolean active = false; 
	
	long startTime;
	
	public Timer() {
		resume(); 
	}
	
	public void resume() {
		if (active) return;  
		startTime = System.currentTimeMillis();;
		active = true; 
	}
	
	public double duration() {
		double time = pastTime; 
		if (active)
			time += (System.currentTimeMillis() - startTime) / 1000.;
		return time; 
	}
	
	public void pause() {
		if (!active) return ;
		pastTime = duration();
		active = false; 
	}
	
	public double reset() {
		double ret = duration();
		pastTime = 0; 
		active = false; 
		resume(); 
		return ret; 
	}

	double timeout = -1; 

	public boolean timeOut() {
		if (timeout  < 0) return false;
		return duration() > timeout; 
	}
	
	public void setTimeout(double timeout) {
		this.timeout = timeout; 
	}
	
}
