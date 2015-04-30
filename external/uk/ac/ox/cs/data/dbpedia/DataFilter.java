package uk.ac.ox.cs.data.dbpedia;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import org.openrdf.rio.RDFHandlerException;
import org.openrdf.rio.RDFParseException;
import org.openrdf.rio.turtle.TurtleParser;
import org.openrdf.rio.turtle.TurtleWriter;
import org.semanticweb.owlapi.model.OWLAnnotationProperty;
import org.semanticweb.owlapi.model.OWLDataProperty;
import org.semanticweb.owlapi.model.OWLOntology;
import uk.ac.ox.cs.pagoda.owl.OWLHelper;

public class DataFilter {

	public static void main(String[] args) throws FileNotFoundException {
		filteringDBPedia(); 
	}
	
	/**
	 * Filter out data property assertions and annotation property assertions in the data set. 
	 * 
	 * @throws FileNotFoundException
	 */
	private static void filteringDBPedia() throws FileNotFoundException {
		String[] args = (
//				"/home/yzhou/ontologies/npd/npd-all.owl "  + 
//				"/home/yzhou/ontologies/npd/data/npd-data-dump-processed.ttl " + 
//				"/home/yzhou/ontologies/npd/data/npd-data-dump-minus-datatype-new.ttl " + 
//				"http://sws.ifi.uio.no/vocab/npd-all.owl#"
				
				"/media/RDFData/yzhou/dbpedia/integratedOntology.owl "  + 
				"/media/RDFData/yzhou/dbpedia/data/dbpedia-processed.ttl " + 
				"/home/yzhou/ontologies/dbpedia/data/dbpedia-minus-datatype-new.ttl " + 
				"http://dbpedia.org/ontology/"
				).split("\\ "); 


		OWLOntology ontology = OWLHelper.loadOntology(args[0]); 
		
		Set<String> properties2ignore = new HashSet<String>(); 
		for (OWLDataProperty prop: ontology.getDataPropertiesInSignature(true))
			properties2ignore.add(prop.toStringID()); 
		for (OWLAnnotationProperty prop: ontology.getAnnotationPropertiesInSignature())
			properties2ignore.add(prop.toStringID()); 
			
		TurtleParser parser = new TurtleParser(); 
		TurtleWriter writer = new TurtleWriter(new FileOutputStream(args[2])); 
		
		parser.setRDFHandler(new DataFilterRDFHandler(writer, properties2ignore));
		try {
			parser.parse(new FileInputStream(args[1]), args[3]);
		} catch (RDFParseException e) {
			e.printStackTrace();
		} catch (RDFHandlerException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} 

	}
	
}
