package uk.ac.ox.cs.data.sample;

import org.openrdf.rio.RDFHandlerException;
import org.openrdf.rio.turtle.TurtleWriter;

public abstract class Sampler {

	protected RDFGraph m_graph; 
	protected TurtleWriter m_writer;
	
	public Sampler(RDFGraph graph, TurtleWriter writer) {
		m_graph = graph; 
		m_writer = writer;
	}
	
	public abstract void setLimit(int limit); 
	
	public abstract void sample() throws RDFHandlerException;

	public abstract void dispose();
	

}
