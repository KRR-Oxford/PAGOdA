package uk.ac.ox.cs.data;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.Random;

import org.openrdf.model.Statement;
import org.openrdf.rio.RDFHandler;
import org.openrdf.rio.RDFHandlerException;
import org.openrdf.rio.turtle.TurtleParser;
import org.openrdf.rio.turtle.TurtleWriter;

import uk.ac.ox.cs.pagoda.util.Utility;

public class Fragment {
	
	private TurtleWriter m_writer;
	private FragmentRDFHandler m_handler; 

	public Fragment(int fragment, String outFile) {
		try {
			m_writer = new TurtleWriter(new FileOutputStream(outFile));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		m_handler = new FragmentRDFHandler(fragment, m_writer); 
	}
	
	public void process(String prefix, String fileName) {
		FileInputStream istream; 
		try {
			TurtleParser parser = new TurtleParser();
			parser.setRDFHandler(m_handler);
			
			File f = new File(fileName);
			if (f.isDirectory())
				for (String tFileName: f.list()) {
					if (tFileName.endsWith(".ttl")) {
						parser.parse(istream = new FileInputStream(fileName + Utility.FILE_SEPARATOR + tFileName), prefix);
						istream.close(); 
					}
				}
			else {
				parser.parse(istream = new FileInputStream(fileName), prefix);
				istream.close(); 
			}
		} catch (Exception e) {
			e.printStackTrace();
			Utility.logError("aoaoaoao ~~~~~");
			return ;
		}
		Utility.logInfo("DONE");
	}

	public void dispose() {
		try {
			m_writer.endRDF();
		} catch (RDFHandlerException e) {
			e.printStackTrace();
		} 
	}
	
	public static void main(String[] args) {
		/**
		 * for ChEMBL
		 */
		Fragment f = new Fragment(100, "data_01.ttl");
		f.process("http://rdf.ebi.ac.uk/terms/chembl#", "/media/krr-nas-share/Yujiao/ontologies/bio2rdf/chembl/data");
		
		/**
		 * for Reactome
		 * 		"http://www.biopax.org/release/biopax-level3.owl#", 
				"/home/scratch/yzhou/ontologies/bio2rdf/reactome"
				"/home/scratch/yzhou/ontologies/bio2rdf/reactome/biopaxrdf", 
 		 */
		
//		Fragment f = new Fragment(1000, "data_001.ttl");
//		f.process("http://www.biopax.org/release/biopax-level3.owl#", "/media/krr-nas-share/Yujiao/ontologies/bio2rdf/reactome/data.ttl");
		
		f.dispose();
	}
	
}


class FragmentRDFHandler implements RDFHandler {
	
	int m_mod;
	TurtleWriter m_writer; 
	Random m_rand = new Random(); 
	
	public FragmentRDFHandler(int mod, TurtleWriter writer) {
		m_mod = mod;  
		m_writer = writer; 
	}

	@Override
	public void endRDF() throws RDFHandlerException {
	}

	@Override
	public void handleComment(String arg0) throws RDFHandlerException {
		m_writer.handleComment(arg0);
		Utility.logDebug("handling comment: " + arg0);
	}

	@Override
	public void handleNamespace(String arg0, String arg1) throws RDFHandlerException {
		m_writer.handleNamespace(arg0, arg1);
	}

	@Override
	public void handleStatement(Statement arg0) throws RDFHandlerException {
		if (m_rand.nextInt() % m_mod == 0)
			m_writer.handleStatement(arg0);
	}
	
	boolean m_started = false; 

	@Override
	public void startRDF() throws RDFHandlerException {
		if (m_started) return ; 
		m_started = true; 
		m_writer.startRDF();
	}
	
}