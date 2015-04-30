package uk.ac.ox.cs.data.dbpedia;

import java.text.Normalizer;
import java.util.Set;

import org.apache.jena.iri.IRI;
import org.apache.jena.iri.IRIException;
import org.apache.jena.iri.IRIFactory;

import org.openrdf.model.BNode;
import org.openrdf.model.Resource;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.Literal;
import org.openrdf.model.Statement;
import org.openrdf.model.impl.StatementImpl;
import org.openrdf.model.impl.URIImpl;
import org.openrdf.rio.RDFHandler;
import org.openrdf.rio.RDFHandlerException;
import org.openrdf.rio.RDFWriter;

public class DataFilterRDFHandler implements RDFHandler {

	public static IRIFactory iriFactory = IRIFactory.semanticWebImplementation();
	 
	RDFWriter m_writer; 
	Set<String> m_properties; 
	
	public DataFilterRDFHandler(RDFWriter writer, Set<String> properties2ignore) {
		m_writer = writer; 
		m_properties = properties2ignore;
	}

	@Override
	public void endRDF() throws RDFHandlerException {
		m_writer.endRDF(); 
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
		Value newObject = null, oldObject = arg0.getObject();
		
		if (oldObject instanceof Literal) 
			return ;
		else if (oldObject instanceof BNode) {
			newObject = oldObject; 
		}
		else if (oldObject instanceof URI) 
			newObject = new URIImpl(Normalizer.normalize(oldObject.toString(), Normalizer.Form.NFKC)); 
		else {
			System.out.println("Object: " + oldObject.getClass()); 
		}
		
		String predicate = arg0.getPredicate().toString();
		if (m_properties.contains(predicate)) return ;
				
		Resource newSubject = null, oldSubject = arg0.getSubject();
		
		if (oldSubject instanceof BNode) {
			newSubject = oldSubject;
		}
		else if (oldSubject instanceof URI) {
			newSubject = new URIImpl(Normalizer.normalize(oldSubject.toString(), Normalizer.Form.NFKC)); 
		}
		else {
			System.out.println("Subject: " + oldSubject.getClass()); 
		}
		
//		if (newObject.toString().contains("ns#type"))
//			System.out.println(arg0); 
		
		if (newSubject == null || newObject == null) {
			System.out.println(arg0); 
			return ;
		}
		
		IRI subjectIRI, objectIRI;
		try {
			if (newSubject instanceof URI){
				subjectIRI = iriFactory.construct(newSubject.toString());
				if (subjectIRI.hasViolation(true)) {
					System.out.println(arg0); 
					return ;
				}
			}
			if (newObject instanceof URI) {
				objectIRI = iriFactory.construct(newObject.toString()); 
				if (objectIRI.hasViolation(true)) {
					System.out.println(arg0); 
					return ;
				}
			}
			
		} catch (IRIException e) {
			return ; 
		}
		
		m_writer.handleStatement(new StatementImpl(newSubject, arg0.getPredicate(), newObject));
	}

	@Override
	public void startRDF() throws RDFHandlerException {
		m_writer.startRDF();
	}

}
