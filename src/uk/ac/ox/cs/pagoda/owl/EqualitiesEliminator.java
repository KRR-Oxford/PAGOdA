package uk.ac.ox.cs.pagoda.owl;

import java.io.File;
import java.io.IOException;

import org.semanticweb.HermiT.Configuration;
import org.semanticweb.HermiT.model.AnnotatedEquality;
import org.semanticweb.HermiT.model.AtLeast;
import org.semanticweb.HermiT.model.Atom;
import org.semanticweb.HermiT.model.DLClause;
import org.semanticweb.HermiT.model.DLOntology;
import org.semanticweb.HermiT.model.DLPredicate;
import org.semanticweb.HermiT.model.Equality;
import org.semanticweb.HermiT.model.Inequality;
import org.semanticweb.HermiT.structural.OWLClausification;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAnnotationAxiom;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLClassAssertionAxiom;
import org.semanticweb.owlapi.model.OWLDataPropertyAssertionAxiom;
import org.semanticweb.owlapi.model.OWLDeclarationAxiom;
import org.semanticweb.owlapi.model.OWLObjectPropertyAssertionAxiom;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.model.OWLOntologyStorageException;
import org.semanticweb.owlapi.model.OWLTransitiveObjectPropertyAxiom;
import org.semanticweb.owlapi.model.UnknownOWLOntologyException;

import uk.ac.ox.cs.pagoda.hermit.TermGraph;
import uk.ac.ox.cs.pagoda.util.Utility;

public class EqualitiesEliminator  {

	String fileName;
	OWLOntologyManager manager; 
	OWLOntology inputOntology, outputOntology = null;
	
	public EqualitiesEliminator(OWLOntology o) {
		this.fileName = OWLHelper.getOntologyPath(o); 
		inputOntology = o; 
		manager = inputOntology.getOWLOntologyManager(); 
	}
	
	public void removeEqualities() throws OWLOntologyCreationException {
		outputOntology = manager.createOntology(IRI.create(inputOntology.getOntologyID().getOntologyIRI().toString().replace(".owl", "-minus.owl")));
		try {
			manager.setOntologyDocumentIRI(outputOntology, IRI.create(Utility.toFileIRI(getOutputFile().getCanonicalPath())));
		} catch (UnknownOWLOntologyException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}  
		
		for (OWLOntology onto: inputOntology.getImportsClosure()) 
			for (OWLAxiom axiom: onto.getAxioms()) {
				if (axiom instanceof OWLAnnotationAxiom
						|| axiom instanceof OWLDeclarationAxiom 
						|| axiom instanceof OWLTransitiveObjectPropertyAxiom 
						|| axiom instanceof OWLClassAssertionAxiom 
						|| axiom instanceof OWLObjectPropertyAssertionAxiom 
						|| axiom instanceof OWLDataPropertyAssertionAxiom
						) {
					manager.removeAxiom(onto, axiom); 
					manager.addAxiom(outputOntology, axiom); 
				}
			}
		
		Configuration conf = new Configuration();
		OWLClausification clausifier = new OWLClausification(conf);
		DLOntology dlOntology = (DLOntology)clausifier.preprocessAndClausify(inputOntology, null)[1];
				
		TermGraph termGraph; 
		for (DLClause dlClause: dlOntology.getDLClauses()) {
			if (!containsEqualities(dlClause)) {
				termGraph = new TermGraph(dlClause); 
				manager.addAxiom(outputOntology, OWLHelper.getOWLAxiom(inputOntology, termGraph.simplify()));
			}
		}
	}
	
	private boolean containsEqualities(DLClause dlClause) {
		DLPredicate predicate; 
		for (Atom headAtom: dlClause.getHeadAtoms()) {
			predicate = headAtom.getDLPredicate(); 
			if (predicate instanceof Equality || predicate instanceof AnnotatedEquality || predicate instanceof Inequality) {
				return true; 
			}
			
			if (predicate instanceof AtLeast) {
				AtLeast atLeast = (AtLeast) predicate; 
				if (atLeast.getNumber() >= 2)
					return true; 
			}
		}
		return false; 
	}

	public void save()  {
		if (outputOntology == null)
			try {
				removeEqualities();
			} catch (OWLOntologyCreationException e) {
				e.printStackTrace();
				return ;
			} 
		try {
			manager.saveOntology(outputOntology, IRI.create(getOutputFile()));
		} catch (OWLOntologyStorageException e) {
			e.printStackTrace();
		}
	}
	
	public File getOutputFile() {
		return new File(fileName.replace(".owl", "-minus.owl"));
	}
	
	public OWLOntology getOutputOntology() {
		if (outputOntology == null)
			try {
				removeEqualities();
			} catch (OWLOntologyCreationException e) {
				e.printStackTrace();
			} 
		return outputOntology; 
	}

	public static void main(String[] args) throws OWLOntologyCreationException, OWLOntologyStorageException {
		args = ("/home/yzhou/ontologies/uobm/univ-bench-dl.owl").split("\\ "); 
		
		EqualitiesEliminator eliminator = new EqualitiesEliminator(OWLHelper.loadOntology(args[0]));
		eliminator.save(); 		
		
	}

}
