package uk.ac.ox.cs.pagoda.owl;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.semanticweb.HermiT.model.Atom;
import org.semanticweb.HermiT.model.AtomicConcept;
import org.semanticweb.HermiT.model.AtomicRole;
import org.semanticweb.HermiT.model.DLClause;
import org.semanticweb.HermiT.model.Individual;
import org.semanticweb.karma2.profile.ELHOProfile;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.AddImport;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAnnotationAssertionAxiom;
import org.semanticweb.owlapi.model.OWLAnnotationSubject;
import org.semanticweb.owlapi.model.OWLAnnotationValue;
import org.semanticweb.owlapi.model.OWLAnonymousIndividual;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLDataProperty;
import org.semanticweb.owlapi.model.OWLDatatype;
import org.semanticweb.owlapi.model.OWLFunctionalObjectPropertyAxiom;
import org.semanticweb.owlapi.model.OWLIndividual;
import org.semanticweb.owlapi.model.OWLInverseFunctionalObjectPropertyAxiom;
import org.semanticweb.owlapi.model.OWLInverseObjectPropertiesAxiom;
import org.semanticweb.owlapi.model.OWLObjectMaxCardinality;
import org.semanticweb.owlapi.model.OWLObjectMinCardinality;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.model.OWLObjectPropertyAssertionAxiom;
import org.semanticweb.owlapi.model.OWLObjectPropertyDomainAxiom;
import org.semanticweb.owlapi.model.OWLObjectPropertyExpression;
import org.semanticweb.owlapi.model.OWLObjectPropertyRangeAxiom;
import org.semanticweb.owlapi.model.OWLObjectSomeValuesFrom;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.model.OWLOntologyStorageException;
import org.semanticweb.owlapi.model.OWLSubClassOfAxiom;
import org.semanticweb.owlapi.model.OWLSubObjectPropertyOfAxiom;
import org.semanticweb.owlapi.model.UnknownOWLOntologyException;
import org.semanticweb.owlapi.profiles.OWL2RLProfile;
import org.semanticweb.owlapi.profiles.OWLProfileReport;
import org.semanticweb.owlapi.profiles.OWLProfileViolation;
import org.semanticweb.owlapi.profiles.UseOfUndeclaredClass;
import org.semanticweb.owlapi.profiles.UseOfUndeclaredDataProperty;
import org.semanticweb.owlapi.profiles.UseOfUndeclaredObjectProperty;
import org.semanticweb.owlapi.util.OWLOntologyMerger;

import uk.ac.ox.cs.pagoda.approx.Clause;
import uk.ac.ox.cs.pagoda.approx.Clausifier;
import uk.ac.ox.cs.pagoda.hermit.DLClauseHelper;
import uk.ac.ox.cs.pagoda.util.Namespace;
import uk.ac.ox.cs.pagoda.util.Utility;

public class OWLHelper {
	
	public static OWLOntology loadOntology(OWLOntologyManager manager, String fileName) {
		// TODO to be uncommented to set silent missing imports ...
//		manager.setSilentMissingImportsHandling(true);
		OWLOntology ontology = null;
		File file = new File(fileName); 
		try {
			ontology = manager.loadOntologyFromOntologyDocument(file);
		} catch (OWLOntologyCreationException e) {
			e.printStackTrace();
		} catch (UnknownOWLOntologyException e) {
			e.printStackTrace();
		}
		return ontology;
	}
	
	public static OWLOntology loadOntology(String fileName) {
		return loadOntology(OWLManager.createOWLOntologyManager(), fileName);
		
	}
	
	public static OWLOntology getImportedOntology(OWLOntology tbox, String... dataFiles) throws OWLOntologyCreationException, OWLOntologyStorageException, IOException {
		if (dataFiles == null || dataFiles.length == 0 || dataFiles.length == 1 && (dataFiles[0] == null || dataFiles[0].isEmpty())) return tbox; 
		
		OWLOntologyManager manager = tbox.getOWLOntologyManager(); 
		OWLOntology importedOntology = manager.createOntology();
		
		for (String dataFile: dataFiles) {
			File data = new File(dataFile);
			if (!data.exists())	
				Utility.logError(dataFile.toString() + " doesn't exist."); 
			
//			importDeclaration(manager, importedOntology, tbox.getOntologyID().getOntologyIRI().toString());
			importDeclaration(manager, importedOntology, Utility.toFileIRI(getOntologyPath(tbox)));
			if (data.isDirectory()) {
				for (File file: data.listFiles()) {
					importDeclaration(manager, importedOntology, Utility.toFileIRI(file.getCanonicalPath()));
				}
			}
			else {
				importDeclaration(manager, importedOntology, Utility.toFileIRI(new File(dataFile).getCanonicalPath()));
			}
		}

		String path = "./imported.owl";
		FileOutputStream out = new FileOutputStream(path); 
		manager.saveOntology(importedOntology, out);
		out.close();
		
		System.out.println("In the closure: " + importedOntology.getImportsClosure().size());
		for (OWLOntology onto: importedOntology.getImportsClosure()) {
			System.out.println(onto.getOntologyID().toString()); 
		}
		manager.removeOntology(importedOntology);
		manager.removeOntology(tbox);
		System.out.println("Removed imported and tbox ontology.");
		OWLOntologyManager newManager = OWLManager.createOWLOntologyManager(); 
		importedOntology = loadOntology(newManager, path); 
		newManager.setOntologyDocumentIRI(importedOntology, IRI.create(Utility.toFileIRI(new File(path).getCanonicalPath())));
		System.out.println("In the closure: " + importedOntology.getImportsClosure().size());
		for (OWLOntology onto: importedOntology.getImportsClosure()) {
			System.out.println(onto.getOntologyID().toString()); 
		}
		return importedOntology; 		
	}
	
	public static OWLOntology getMergedOntology(String ontologyFile, String dataFile) throws OWLOntologyCreationException, IOException, OWLOntologyStorageException {
		OWLOntology tbox = loadOntology(ontologyFile); 
		OWLOntologyManager manager = tbox.getOWLOntologyManager();
		OWLOntology mergedOntology = new OWLOntologyMerger(manager).createMergedOntology(manager, null);
		for (OWLOntology o: manager.getOntologies())
			if (o != mergedOntology) {
				for (OWLAxiom axiom: o.getAxioms()) 
						manager.addAxiom(mergedOntology, axiom); 
				}
		Utility.logDebug("loaded and merged the ontology and the dataset: ");
		return mergedOntology; 
	}
	
	public static boolean removeAnnotations(OWLOntology inputOntology) {
		OWLOntologyManager manager = inputOntology.getOWLOntologyManager();
		boolean updated = false; 
		for (OWLAxiom axiom: inputOntology.getAxioms()) 
			if (axiom.toString().contains("Annotation")) {
				manager.removeAxiom(inputOntology, axiom); 
				updated = true; 
			}
		return updated; 
	}
	
	public static boolean correctDataTypeRangeAxioms(OWLOntology inputOntology) {
		OWLOntologyManager manager = inputOntology.getOWLOntologyManager(); 
		OWLDataFactory factory = manager.getOWLDataFactory(); 
		boolean updated = false; 
		OWLClass cls; 
		OWLDatatype datatype; 
		for (OWLOntology onto: inputOntology.getImportsClosure())
			for (OWLAxiom axiom: onto.getAxioms()) 
				if (axiom instanceof OWLObjectPropertyRangeAxiom) {
					OWLObjectPropertyRangeAxiom a = (OWLObjectPropertyRangeAxiom) axiom; 
					if (a.getRange() instanceof OWLClass && a.getProperty() instanceof OWLObjectProperty && (cls = (OWLClass) a.getRange()).toString().contains("datatype")) {
						manager.removeAxiom(onto, axiom); 
						manager.removeAxiom(onto, factory.getOWLDeclarationAxiom(cls)); 
						
						datatype = factory.getOWLDatatype(cls.getIRI());
						manager.addAxiom(onto, factory.getOWLDeclarationAxiom(datatype));
						manager.addAxiom(onto, factory.getOWLDataPropertyRangeAxiom(
									factory.getOWLDataProperty(((OWLObjectProperty) a.getProperty()).getIRI()), 
									datatype));
					}
				}
		
		return updated; 
	}
	
	private static void importDeclaration(OWLOntologyManager manager, OWLOntology ontology, String location) {
		AddImport change = new AddImport(ontology, manager.getOWLDataFactory().getOWLImportsDeclaration(IRI.create(location)));
		manager.applyChange(change); 
	}

	public static OWLClassExpression getSimplifiedDisjunction(OWLDataFactory factory, Set<OWLClassExpression> set) {
		if (set.size() == 0)
			return factory.getOWLNothing();
		else if (set.size() == 1)
			return set.iterator().next();
		else return factory.getOWLObjectUnionOf(set);
	}
	
	public static OWLClassExpression getSimplifiedConjunction(OWLDataFactory factory, Set<OWLClassExpression> set) {
		if (set.size() == 0)
			return factory.getOWLThing();
		else if (set.size() == 1)
			return set.iterator().next();
		else return factory.getOWLObjectIntersectionOf(set);
	}
	
	public static OWLAxiom getOWLAxiom(OWLOntology ontology, DLClause dlClause) {
		OWLAxiom owlAxiom; 
		OWLDataFactory factory = ontology.getOWLOntologyManager().getOWLDataFactory();  
		
		dlClause = DLClauseHelper.replaceOtherBottom(dlClause); 
		
		if (dlClause.isAtomicConceptInclusion()) {
			OWLClass subClass = factory.getOWLClass(IRI.create(((AtomicConcept) dlClause.getBodyAtom(0).getDLPredicate()).getIRI()));
			OWLClass superClass = factory.getOWLClass(IRI.create(((AtomicConcept) dlClause.getHeadAtom(0).getDLPredicate()).getIRI()));
			return factory.getOWLSubClassOfAxiom(subClass, superClass); 
		}
		else if (dlClause.isAtomicRoleInclusion()) {
			OWLObjectProperty subProp = factory.getOWLObjectProperty(IRI.create(((AtomicRole) dlClause.getBodyAtom(0).getDLPredicate()).getIRI())); 
			OWLObjectProperty superProp = factory.getOWLObjectProperty(IRI.create(((AtomicRole) dlClause.getHeadAtom(0).getDLPredicate()).getIRI()));
			return factory.getOWLSubObjectPropertyOfAxiom(subProp, superProp);
		}
		else if (dlClause.isAtomicRoleInverseInclusion()) {
			OWLObjectProperty subProp = factory.getOWLObjectProperty(IRI.create(((AtomicRole) dlClause.getBodyAtom(0).getDLPredicate()).getIRI())); 
			OWLObjectProperty superProp = factory.getOWLObjectProperty(IRI.create(((AtomicRole) dlClause.getHeadAtom(0).getDLPredicate()).getIRI()));
			return factory.getOWLSubObjectPropertyOfAxiom(subProp, superProp.getInverseProperty());
		}
		else if (dlClause.isFunctionalityAxiom()) {
			OWLObjectProperty prop = factory.getOWLObjectProperty(IRI.create(((AtomicRole) dlClause.getBodyAtom(0).getDLPredicate()).getIRI()));
			return factory.getOWLFunctionalObjectPropertyAxiom(prop);
		}
		else if (dlClause.isInverseFunctionalityAxiom()) {
			OWLObjectProperty prop = factory.getOWLObjectProperty(IRI.create(((AtomicRole) dlClause.getBodyAtom(0).getDLPredicate()).getIRI())); 
			return factory.getOWLInverseFunctionalObjectPropertyAxiom(prop);
		}
		else if (DLClauseHelper.isFunctionalDataPropertyAxioms(dlClause)) {
			OWLDataProperty prop = factory.getOWLDataProperty(IRI.create(((AtomicRole) dlClause.getBodyAtom(0).getDLPredicate()).getIRI()));
			return factory.getOWLFunctionalDataPropertyAxiom(prop);
		}
		int flag; 
		if ((flag = DLClauseHelper.isRoleCompositionAxioms(dlClause)) >= 0) {
			OWLObjectPropertyExpression R = factory.getOWLObjectProperty(IRI.create(((AtomicRole) dlClause.getBodyAtom(0).getDLPredicate()).getIRI())); 
			OWLObjectPropertyExpression S = factory.getOWLObjectProperty(IRI.create(((AtomicRole) dlClause.getBodyAtom(1).getDLPredicate()).getIRI()));
			OWLObjectPropertyExpression T = factory.getOWLObjectProperty(IRI.create(((AtomicRole) dlClause.getHeadAtom(0).getDLPredicate()).getIRI()));
			
			if (flag % 2 != 0) T = T.getInverseProperty(); flag /= 2;  
			if (flag % 2 != 0) S = S.getInverseProperty(); flag /= 2;  
			if (flag % 2 != 0) R = R.getInverseProperty(); 
			if (R.equals(S) && S.equals(T))
				return factory.getOWLTransitiveObjectPropertyAxiom(R); 
			else {
				List<OWLObjectPropertyExpression> list = new LinkedList<OWLObjectPropertyExpression>(); 
				list.add(R); list.add(S); 
				return factory.getOWLSubPropertyChainOfAxiom(list, T);
			}
		}
		else { 
			Clausifier clausifier = Clausifier.getInstance(ontology); 
//			if (dlClause.isGeneralConceptInclusion()) {
			
			Clause myClause = new Clause(clausifier, dlClause);
			owlAxiom = getSimplifiedGCI(factory, myClause.getSubClasses(), myClause.getSuperClasses());
			

			if (owlAxiom == null)
				Utility.logError("more kinds of DLClause: " + dlClause.toString());
			
			return owlAxiom;
		}
		
	}
	
	private static OWLAxiom getSimplifiedGCI(OWLDataFactory factory, Set<OWLClassExpression> subClasses, Set<OWLClassExpression> superClasses) {
		if (superClasses.size() > 1) {
			Set<OWLClassExpression> toRemoved = new HashSet<OWLClassExpression>(); 
			for (OWLClassExpression cls: new HashSet<OWLClassExpression>(superClasses)) 
				if (cls instanceof OWLObjectMaxCardinality) {
					OWLObjectMaxCardinality maxCard = (OWLObjectMaxCardinality) cls;
					OWLObjectMinCardinality minCard = factory.getOWLObjectMinCardinality(maxCard.getCardinality() + 1, maxCard.getProperty()); 
					for (OWLClassExpression exp: subClasses)
						if (isSubsumedByMinCard(exp, minCard))
							toRemoved.add(exp);  
					subClasses.add(minCard);
					superClasses.remove(maxCard);
				}
			subClasses.removeAll(toRemoved); 
		}
			
		return factory.getOWLSubClassOfAxiom(getSimplifiedConjunction(factory, subClasses), getSimplifiedDisjunction(factory, superClasses)); 
	}
	
	public static OWLAxiom getABoxAssertion(OWLDataFactory factory, Atom atom) {
		if (atom.getDLPredicate().toString().contains("internal:nom#"))
			return null; 
		try {
			if (atom.getArity() == 1)
				return factory.getOWLClassAssertionAxiom(
						factory.getOWLClass(IRI.create(((AtomicConcept) atom.getDLPredicate()).getIRI())), 
						factory.getOWLNamedIndividual(IRI.create(((Individual) atom.getArgument(0)).getIRI()))
						);
			else 
				return factory.getOWLObjectPropertyAssertionAxiom(
						factory.getOWLObjectProperty(IRI.create(((AtomicRole) atom.getDLPredicate()).getIRI())), 
						factory.getOWLNamedIndividual(IRI.create(((Individual) atom.getArgument(0)).getIRI())), 
						factory.getOWLNamedIndividual(IRI.create(((Individual) atom.getArgument(1)).getIRI()))
						);
		} catch (Exception e) {
			return null; 
		}		
	}

	private static boolean isSubsumedByMinCard(OWLClassExpression exp, OWLObjectMinCardinality minCard) {
		if (exp instanceof OWLObjectSomeValuesFrom) {
			OWLObjectSomeValuesFrom someValuesFrom = (OWLObjectSomeValuesFrom) exp; 
			return minCard.getCardinality() > 0 && 
					minCard.getProperty().equals(someValuesFrom.getProperty()) &&
					minCard.getFiller().equals(someValuesFrom.getFiller()); 
		}
		
		if (exp instanceof OWLObjectMinCardinality) {
			OWLObjectMinCardinality minCard2 = (OWLObjectMinCardinality) exp; 
			return minCard.getCardinality() >= minCard2.getCardinality() &&
					minCard.getProperty().equals(minCard2.getProperty()) &&
					minCard.getFiller().equals(minCard2.getFiller()); 
		}
		return false;			
	}

	public static String removeAngles(String quotedString) {
		if (quotedString.startsWith("<") && quotedString.endsWith(">"))
			return quotedString.substring(1, quotedString.length() - 1);
		else if (quotedString.contains(":"))
			return quotedString; 
		else 
			Utility.logError("paused here due to the action to remove Angle in an unquotedString"); 
		return quotedString; 		
	}
	
	public static String addAngles(String name) {
		StringBuilder sb = new StringBuilder(); 
		sb.append("<").append(name).append(">");
		return sb.toString();
	}

	public static boolean isContradiction(OWLDataFactory factory, OWLAxiom axiom) {
 		if (!(axiom instanceof OWLSubClassOfAxiom))
 			return false; 
 		OWLSubClassOfAxiom subClassAxiom = (OWLSubClassOfAxiom) axiom;
 		return subClassAxiom.getSubClass().equals(factory.getOWLThing()) && subClassAxiom.getSuperClass().equals(factory.getOWLNothing()); 
	}

	static MyHornAxiomVisitorEx visitor = new MyHornAxiomVisitorEx(); 
	
	public static boolean isDisjunctiveAxiom(OWLAxiom axiom) {
		boolean isHorn = false; 
		if (axiom instanceof OWLSubClassOfAxiom)
			isHorn = visitor.visit((OWLSubClassOfAxiom) axiom);
		else if (axiom instanceof OWLSubObjectPropertyOfAxiom)
			isHorn = visitor.visit((OWLSubObjectPropertyOfAxiom) axiom); 
		else if (axiom instanceof OWLInverseObjectPropertiesAxiom)
			isHorn = visitor.visit((OWLInverseObjectPropertiesAxiom) axiom);
		else if (axiom instanceof OWLObjectPropertyDomainAxiom)
			isHorn = visitor.visit((OWLObjectPropertyDomainAxiom) axiom);
		else if (axiom instanceof OWLFunctionalObjectPropertyAxiom)
			isHorn = visitor.visit((OWLFunctionalObjectPropertyAxiom) axiom); 
		else if (axiom instanceof OWLInverseFunctionalObjectPropertyAxiom)
			isHorn = visitor.visit((OWLInverseFunctionalObjectPropertyAxiom) axiom); 
		else {
			Utility.logError(axiom);  
		}
		
		if (isHorn)
			Utility.logDebug(axiom.toString() + " is NOT a disjunctive axiom.");
		else
			Utility.logDebug(axiom.toString() + " is a disjunctive axiom.");
		
		return !isHorn; 
	}

	public boolean isELHO(OWLOntology ontology) {
		Utility.logDebug("checking whether ELHO ... ");
		ELHOProfile profile = new ELHOProfile();
		OWLProfileReport report = profile.checkOntology(ontology); 
		for (OWLProfileViolation v: report.getViolations())
			if (v instanceof UseOfUndeclaredClass || 
					v instanceof UseOfUndeclaredObjectProperty || 
					v instanceof UseOfUndeclaredDataProperty); 
			else {
				Utility.logDebug(v.toString(), "not ELHO");
				return false; 
			}
		return true; 
	}

	public static void identifyAndChangeAnnotationAssertions(OWLOntology ontology) {
		OWLOntologyManager manager = ontology.getOWLOntologyManager(); 
		OWLDataFactory factory = manager.getOWLDataFactory(); 
		
		Set<String> objectProperties = new HashSet<String>();
		for (OWLOntology onto: ontology.getImportsClosure()) {
			for (OWLObjectProperty prop: onto.getObjectPropertiesInSignature()) {
					objectProperties.add(prop.toStringID()); 
			}
		}
		
		Set<OWLAnnotationAssertionAxiom> toRemove = new HashSet<OWLAnnotationAssertionAxiom>(); 
		Set<OWLObjectPropertyAssertionAxiom> toAdd = new HashSet<OWLObjectPropertyAssertionAxiom>(); 
		OWLAnnotationAssertionAxiom assertion;
		OWLAnnotationSubject anSub; 
		OWLAnnotationValue anValue; 
		
		OWLObjectPropertyAssertionAxiom newAssertion;
		String property; 
		OWLIndividual sub, obj; 
		
		for (OWLOntology onto: ontology.getImportsClosure()) {
			for (OWLAxiom axiom: onto.getAxioms()) 
				if (axiom instanceof OWLAnnotationAssertionAxiom) {
					assertion = (OWLAnnotationAssertionAxiom) axiom; 
					if (objectProperties.contains(property = assertion.getProperty().toStringID())) {
						if ((anSub = assertion.getSubject()) instanceof OWLAnonymousIndividual)
							sub = (OWLAnonymousIndividual) anSub; 
						else 
							sub = factory.getOWLNamedIndividual((IRI) anSub); 
						
						if ((anValue = assertion.getValue()) instanceof OWLAnonymousIndividual)
							obj = (OWLAnonymousIndividual) anValue; 
						else if (anValue instanceof IRI) 
							obj = factory.getOWLNamedIndividual((IRI) anValue);
						else 
							continue; 
						
						newAssertion = factory.getOWLObjectPropertyAssertionAxiom(
								factory.getOWLObjectProperty(IRI.create(property)), sub, obj); 
						toRemove.add(assertion); 
						toAdd.add(newAssertion); 
					}
				}
			manager.removeAxioms(onto, toRemove); 
			manager.addAxioms(onto, toAdd); 
		}
	}

	public static String getOntologyPath(OWLOntology o) {
		return getDocumentIRI(o).substring(5);
	}

	public static String getDocumentIRI(OWLOntology o) {
		return o.getOWLOntologyManager().getOntologyDocumentIRI(o).toString(); 
	}

	public static boolean isInOWL2RL(OWLOntology o) {
		OWL2RLProfile profile = new OWL2RLProfile(); 
		return profile.checkOntology(o).isInProfile(); 
	}

	public static boolean isInELHO(OWLOntology o) {
		ELHOProfile profile = new ELHOProfile(); 
		return profile.checkOntology(o).isInProfile(); 
	}

	public static String getOriginalMarkProgram(OWLOntology ontology) {
		String originalConcept = Namespace.PAGODA_ORIGINAL;  
		StringBuilder sb = new StringBuilder();
		for (OWLDataProperty p: ontology.getDataPropertiesInSignature()) {
			sb.append("<").append(originalConcept).append(">(?x) :- <").append(p.toStringID()).append(">(?x,?y) .\n");
			sb.append("<").append(originalConcept).append(">(?y) :- <").append(p.toStringID()).append(">(?x,?y) .\n");
		}
		for (OWLObjectProperty p: ontology.getObjectPropertiesInSignature()) { 
			sb.append("<").append(originalConcept).append(">(?x) :- <").append(p.toStringID()).append(">(?x,?y) .\n");
			sb.append("<").append(originalConcept).append(">(?y) :- <").append(p.toStringID()).append(">(?x,?y) .\n");
		}
		for (OWLClass c: ontology.getClassesInSignature()) 
			sb.append("<").append(originalConcept).append(">(?x) :- <").append(c.toStringID()).append(">(?x) .\n"); 
		return sb.toString();
	}

}
