package uk.ac.ox.cs.data;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLDataPropertyRangeAxiom;
import org.semanticweb.owlapi.model.OWLException;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyManager;

import uk.ac.ox.cs.pagoda.tester.PagodaTester;
import uk.ac.ox.cs.pagoda.util.Utility;

public class RemoveDataPropertyRange {
	
	public static void process(String file) throws OWLException, IOException {
		OWLOntologyManager originalManager = OWLManager.createOWLOntologyManager(); 
		OWLOntologyManager manager = OWLManager.createOWLOntologyManager(); 
		
		OWLOntology originalOntology = originalManager.loadOntologyFromOntologyDocument(new File(file));
		OWLOntology ontology = manager.createOntology(originalOntology.getOntologyID().getOntologyIRI());
		
		for (OWLOntology onto: originalOntology.getImportsClosure()) 
			for (OWLAxiom axiom: onto.getAxioms()) {
				if (!(axiom instanceof OWLDataPropertyRangeAxiom))
					manager.addAxiom(ontology, axiom); 
			}
		originalManager.removeOntology(originalOntology);
		
		String extension = file.substring(file.lastIndexOf("."));
		String fileName = file.substring(file.lastIndexOf(Utility.FILE_SEPARATOR) + 1); 
		String dest = fileName.replace(extension, "-noDPR.owl"); 
		manager.saveOntology(ontology, new FileOutputStream(dest)); 
		System.out.println("The processed ontology is saved in " + dest + " successfully."); 
		manager.removeOntology(ontology);
	}

	public static void main(String[] args) {
		try {
			process(PagodaTester.chembl_tbox);
		} catch (OWLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

}
