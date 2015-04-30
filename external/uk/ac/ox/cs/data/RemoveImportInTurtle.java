package uk.ac.ox.cs.data;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import org.openrdf.model.Statement;
import org.openrdf.rio.RDFHandler;
import org.openrdf.rio.RDFHandlerException;
import org.openrdf.rio.RDFParseException;
import org.openrdf.rio.turtle.TurtleParser;
import org.openrdf.rio.turtle.TurtleWriter;

public class RemoveImportInTurtle {

	public static void main(String[] args) throws RDFParseException, RDFHandlerException, IOException {
		if (args.length == 0) 
			args = new String[] { 
//				"/media/krr-nas-share/Yujiao/ontologies/lubm/data/lubm1.ttl", 
//				"../trowl/lubm_trowl/lubm1.ttl", 
//				"http://www.lehigh.edu/~zhp2/2004/0401/univ-bench.owl#"
				"/media/krr-nas-share/Yujiao/ontologies/npd/data/npd-data-dump-minus-datatype-new.ttl", 
				"/users/yzhou/temp/npd.ttl",
				"http://sws.ifi.uio.no/data/npd-v2/#"
				};
		TurtleParser parser = new TurtleParser();
		TurtleWriter writer = new TurtleWriter(new FileOutputStream(new File(args[1]))); 
		parser.setRDFHandler(new LocalRDFHandler(writer));
		parser.parse(new FileInputStream(new File(args[0])), args[2]);
	}
	
}

class LocalRDFHandler implements RDFHandler {

	TurtleWriter m_writer; 
	
	public LocalRDFHandler(TurtleWriter writer) {
		m_writer = writer; 
	}

	@Override
	public void startRDF() throws RDFHandlerException {
		m_writer.startRDF();
		
	}

	@Override
	public void endRDF() throws RDFHandlerException {
		m_writer.endRDF();		
	}

	@Override
	public void handleNamespace(String prefix, String uri)
			throws RDFHandlerException {
		m_writer.handleNamespace(prefix, uri);
		
	}

	@Override
	public void handleStatement(Statement st) throws RDFHandlerException {
		if (st.getObject().toString().equals("http://www.w3.org/2002/07/owl#Ontology"))
			return ;
		if (st.getPredicate().toString().equals("http://www.w3.org/2002/07/owl#imports")) 
			return ; 
		m_writer.handleStatement(st);
		
	}

	@Override
	public void handleComment(String comment) throws RDFHandlerException {
		m_writer.handleComment(comment);
		
	}
	
}