package org.semanticweb.simpleETL;

import org.openrdf.rio.RDFParser;
import org.openrdf.rio.RDFWriter;
import org.openrdf.rio.rdfxml.RDFXMLParser;
import org.openrdf.rio.turtle.TurtleWriter;
import uk.ac.ox.cs.pagoda.util.Utility;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.regex.Pattern;

public class SimpleETL {
	
	protected final static String m_prefix_LUBM = "http://www.lehigh.edu/~zhp2/2004/0401/univ-bench.owl#";
	protected final static String m_prefix_UOBM = "http://semantics.crl.ibm.com/univ-bench-dl.owl#";
	protected final static String m_prefix_FLY= "http://www.virtualflybrain.org/ontologies/individual_neurons/FC_neuron_GF_an.owl#";

	String m_prefix; 
	String m_fileToImport;
	String m_fileToExport; 
	
	public SimpleETL(String prefix, String fileToImport) {
		m_prefix = prefix; 
		m_fileToImport = fileToImport; 
		m_fileToExport = m_fileToImport.replace(".owl", ".ttl"); 
	}
	
	public SimpleETL(String prefix, String fileToImport, String outPath) {
		m_prefix = prefix; 
		m_fileToImport = fileToImport;
		File file = new File(outPath); 
		if (file.exists() && file.isDirectory())
			m_fileToExport = outPath + Utility.FILE_SEPARATOR + "data.ttl"; 
		else 
			m_fileToExport = outPath; 
//				+ Utility.FILE_SEPARATOR + m_fileToImport.substring(m_fileToImport.lastIndexOf(Utility.FILE_SEPARATOR), m_fileToImport.lastIndexOf(".")) + ".ttl"; 
	}
	
	public void rewrite() throws Exception {
//		RDFParser parser = new TurtleParser();
		RDFParser parser = new RDFXMLParser(); 
		
		FileOutputStream fos = new FileOutputStream(m_fileToExport); 
		try {
			RDFWriter writer = new TurtleWriter(fos);
		
//			String m_fileToExport = m_fileToImport.replace(".owl", ".ntriple"); 
//			RDFWriter writer = new NTriplesWriter(new FileOutputStream(m_fileToExport));
		
			RDFHandlerWriter multiHandler = new RDFHandlerWriter(writer);
			parser.setRDFHandler(multiHandler);
			File fileToImport = new File(m_fileToImport);
			if(fileToImport.isDirectory()) {
				for(File file : fileToImport.listFiles()) {
					if(file.isFile() && (Pattern.matches(".*.owl", file.getName()) || Pattern.matches(".*.rdf", file.getName()))) {
						Utility.logDebug("Parsing " + file.getName());
						parser.parse(new FileInputStream(file), m_prefix);
					}
				}
			}
			else
				parser.parse(new FileInputStream(fileToImport), m_prefix);
			writer.endRDF();
		}
		finally {
			fos.close();
		}
		Utility.logInfo("SimpleETL rewriting DONE: additional ontology data is saved in " + m_fileToExport + ".");
	}
	
	public String getExportedFile() {
		return m_fileToExport; 
	}
}
