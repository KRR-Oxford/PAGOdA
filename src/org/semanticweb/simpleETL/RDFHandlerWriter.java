package org.semanticweb.simpleETL;
import org.openrdf.model.Statement;
import org.openrdf.rio.RDFHandler;
import org.openrdf.rio.RDFHandlerException;
import org.openrdf.rio.RDFWriter;


public class RDFHandlerWriter implements RDFHandler {
	protected RDFWriter m_writer;
	protected boolean m_started;
	
	public RDFHandlerWriter(RDFWriter writer){
		m_writer = writer;
		m_started = false;
	}

	@Override
	public void endRDF() throws RDFHandlerException {
		// Do not end
	}

	@Override
	public void handleComment(String arg0) throws RDFHandlerException {
		m_writer.handleComment(arg0);
		
	}

	@Override
	public void handleNamespace(String arg0, String arg1) throws RDFHandlerException {
		m_writer.handleNamespace(arg0, arg1);		
	}

	@Override
	public void handleStatement(Statement arg0) throws RDFHandlerException {
		m_writer.handleStatement(arg0);		
	}

	@Override
	public void startRDF() throws RDFHandlerException {
		if(!m_started) {
			m_started = true;
			m_writer.startRDF();
		}		
	}	
}
