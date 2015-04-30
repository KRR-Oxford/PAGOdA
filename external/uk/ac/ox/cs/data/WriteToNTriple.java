package uk.ac.ox.cs.data;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import org.openrdf.model.Statement;
import org.openrdf.rio.RDFHandler;
import org.openrdf.rio.RDFHandlerException;
import org.openrdf.rio.RDFParseException;
import org.openrdf.rio.RDFParser;
import org.openrdf.rio.RDFWriter;
import org.openrdf.rio.ntriples.NTriplesWriter;
import org.openrdf.rio.turtle.TurtleParser;


public class WriteToNTriple {

	public static void main(String... args) throws RDFParseException, RDFHandlerException, IOException {
		if (args.length == 0)
			args = new String[] {"/media/krr-nas-share/Yujiao/ontologies/bio2rdf/reactome/data/data.ttl", 
							"http://www.biopax.org/release/biopax-level3.owl#"}; 
		
		RDFParser parser = new TurtleParser();
		final RDFWriter writer = new NTriplesWriter(new FileOutputStream(args[0].replace(".ttl", ".nt"))); 
		
		parser.setRDFHandler(new RDFHandler() {
			
			@Override
			public void startRDF() throws RDFHandlerException {
				writer.startRDF();				
			}
			
			@Override
			public void handleStatement(Statement arg0) throws RDFHandlerException {
				writer.handleStatement(arg0);				
			}
			
			@Override
			public void handleNamespace(String arg0, String arg1) throws RDFHandlerException {
				writer.handleNamespace(arg0, arg1);				
			}
			
			@Override
			public void handleComment(String arg0) throws RDFHandlerException {
				writer.handleComment(arg0);
			}
			
			@Override
			public void endRDF() throws RDFHandlerException {
				writer.endRDF();				
			}
		});
		
		parser.parse(new FileInputStream(args[0]), args[1]);
	}
}
