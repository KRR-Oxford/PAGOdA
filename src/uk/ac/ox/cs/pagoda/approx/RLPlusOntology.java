package uk.ac.ox.cs.pagoda.approx;

import org.semanticweb.HermiT.Configuration;
import org.semanticweb.HermiT.model.DLClause;
import org.semanticweb.HermiT.model.DLOntology;
import org.semanticweb.HermiT.structural.OWLClausification;
import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.profiles.OWL2RLProfile;
import org.semanticweb.owlapi.profiles.OWLProfileReport;
import org.semanticweb.owlapi.profiles.OWLProfileViolation;
import uk.ac.ox.cs.pagoda.constraints.NullaryBottom;
import uk.ac.ox.cs.pagoda.constraints.UnaryBottom;
import uk.ac.ox.cs.pagoda.owl.OWLHelper;
import uk.ac.ox.cs.pagoda.util.Namespace;
import uk.ac.ox.cs.pagoda.util.Utility;

import java.io.*;
import java.nio.file.Paths;
import java.util.*;

public class RLPlusOntology implements KnowledgeBase {
	
	OWLOntologyManager manager;
	OWLDataFactory factory;
	String ontologyIRI;
	String corrFileName = null;
	String outputPath, aBoxPath;
	
	OWLOntology inputOntology = null;
	OWLOntology tBox = null;
	OWLOntology aBox = null;
	OWLOntology restOntology = null;
	OWLOntology outputOntology = null; //RL ontology
	
	DLOntology dlOntology = null;
	int rlCounter = 0;
	
	LinkedList<Clause> clauses; 
	Map<OWLAxiom, OWLAxiom> correspondence;
	
	BottomStrategy botStrategy;
	Random random = new Random(19900114);
	private Map<OWLClassExpression, Integer> subCounter = null;
	private Map<OWLClass, OWLClass> atomic2negation = new HashMap<OWLClass, OWLClass>();

	// FIXME multiple anonymous ontologies
	@Override
	public void load(OWLOntology o, uk.ac.ox.cs.pagoda.constraints.BottomStrategy bottomStrategy) {
		if (bottomStrategy instanceof UnaryBottom)
			botStrategy = BottomStrategy.UNARY;
		else if (bottomStrategy instanceof NullaryBottom)
			botStrategy = BottomStrategy.NULLARY;
		else
			botStrategy = BottomStrategy.TOREMOVE;

		if(corrFileName == null)
			corrFileName = "rlplus.crr";
		manager = o.getOWLOntologyManager();
//		manager = OWLManager.createOWLOntologyManager();
		factory = manager.getOWLDataFactory();
		inputOntology = o;

		try {
			String path = OWLHelper.getOntologyPath(inputOntology);
			String name = path.substring(path.lastIndexOf(Utility.JAVA_FILE_SEPARATOR));
			String originalExtension = name.lastIndexOf(".") >= 0 ? name.substring(name.lastIndexOf(".")) : "";

			if (inputOntology.getOntologyID().getOntologyIRI() == null)
				ontologyIRI = "http://www.example.org/anonymous-ontology" + originalExtension;
			else
				ontologyIRI = inputOntology.getOntologyID().getOntologyIRI().toString();

			String tOntoIRI = ontologyIRI;
			if (!tOntoIRI.endsWith(originalExtension)) tOntoIRI += originalExtension;

			String rlOntologyIRI = originalExtension.isEmpty() ? tOntoIRI + "-RL.owl" : tOntoIRI.replaceFirst(originalExtension, "-RL.owl");
			String rlDocumentIRI = (outputPath = Paths.get(Utility.getGlobalTempDirAbsolutePath(), "RL.owl").toString());
			outputOntology = manager.createOntology(IRI.create(rlOntologyIRI));
			manager.setOntologyDocumentIRI(outputOntology, IRI.create(Utility.toFileIRI(rlDocumentIRI)));

			String tBoxOntologyIRI, aBoxOntologyIRI;
			tBoxOntologyIRI =
					originalExtension.isEmpty() ? tOntoIRI + "-TBox.owl" : tOntoIRI.replaceFirst(originalExtension, "-TBox.owl");
			aBoxOntologyIRI = originalExtension.isEmpty() ? tOntoIRI + "-ABox.owl" : tOntoIRI.replaceFirst(originalExtension, "-ABox.owl");

			String tBoxDocumentIRI = Paths.get(Utility.getGlobalTempDirAbsolutePath(), "TBox.owl").toString();
			String aBoxDocumentIRI = (aBoxPath = Paths.get(Utility.getGlobalTempDirAbsolutePath(), "ABox.owl").toString());
			tBox = manager.createOntology(IRI.create(tBoxOntologyIRI));
			aBox = manager.createOntology(IRI.create(aBoxOntologyIRI));
			manager.setOntologyDocumentIRI(tBox, IRI.create(Utility.toFileIRI(tBoxDocumentIRI)));
			manager.setOntologyDocumentIRI(aBox, IRI.create(Utility.toFileIRI(aBoxDocumentIRI)));

			FileOutputStream aBoxOut = new FileOutputStream(aBoxPath);
			manager.saveOntology(aBox, aBoxOut);
			aBoxOut.close();

			restOntology = manager.createOntology();
		}
		catch (OWLOntologyCreationException e) {
			e.printStackTrace();
		} catch (OWLOntologyStorageException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public OWLOntology getTBox() {
		return tBox;
	}
	
	public String getABoxPath() {
		return aBoxPath;
	}
	
	private void add2SubCounter(OWLClassExpression exp) {
		Integer count = subCounter.get(exp);
		if (count == null) count = 0;
		++count;
		subCounter.put(exp, count);
	}
	
	public void simplify() {
		if(simplifyABox()) {
			save(aBox);
//			save(tBox);
		} else
			tBox = inputOntology;
	}

	@Override
	public void transform() {
		simplify();
		filter();
		clausify();

		subCounter = new HashMap<OWLClassExpression, Integer>();
		clauses = new LinkedList<Clause>();
		Clausifier clausifier = Clausifier.getInstance(restOntology);

		for (DLClause c: dlOntology.getDLClauses()) {
			Clause clause = new Clause(clausifier, c);
			clauses.add(clause);

			/*
			 * count the expressions in the left
			 */
			for (OWLClassExpression exp: clause.getSubClasses()) {
				if(exp instanceof OWLClass)
					add2SubCounter(exp);
				else if (exp instanceof OWLObjectSomeValuesFrom) {
					OWLObjectSomeValuesFrom someValue = (OWLObjectSomeValuesFrom)exp;
					add2SubCounter(factory.getOWLObjectSomeValuesFrom(someValue.getProperty(), factory.getOWLThing()));
					add2SubCounter(someValue.getFiller());
				}
				else if (exp instanceof OWLObjectMinCardinality) {
					OWLObjectMinCardinality minCard = (OWLObjectMinCardinality)exp;
					add2SubCounter(factory.getOWLObjectSomeValuesFrom(minCard.getProperty(), factory.getOWLThing()));
					add2SubCounter(minCard.getFiller());
				} else
					Utility.logError("strange class expression: " + exp);

			}
		}

		correspondence = new HashMap<OWLAxiom, OWLAxiom>();
		Set<OWLAxiom> addedAxioms = new HashSet<OWLAxiom>();
		OWLClassExpression subExp;
		for (Clause clause: clauses) {
			subExp = uk.ac.ox.cs.pagoda.owl.OWLHelper.getSimplifiedConjunction(factory, clause.getSubClasses());
			addedAxioms.clear();
			for (OWLClassExpression exp: getDisjunctionApprox0(clause.getSuperClasses())) {
				addedAxioms.add(factory.getOWLSubClassOfAxiom(subExp, transform(exp, addedAxioms)));
				for(OWLAxiom a : addedAxioms)
					addAxiom2output(a, factory.getOWLSubClassOfAxiom(subExp,
							OWLHelper.getSimplifiedDisjunction(factory, clause.getSuperClasses())));
			}
		}

		subCounter.clear();
	}
	
	@Override
	public void save() {
		if (corrFileName != null)
			save(correspondence, corrFileName);
		save(outputOntology);
	}

	private void save(Map<OWLAxiom, OWLAxiom> map, String corrFileName) {
		if (corrFileName == null) return ;
		ObjectOutput output;
		try {
			output = new ObjectOutputStream(new BufferedOutputStream(new FileOutputStream(corrFileName)));
			output.writeObject(map);
			output.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	protected void save(OWLOntology onto) {
		try {
			onto.getOWLOntologyManager().saveOntology(onto);
		} catch (OWLOntologyStorageException e) {
			e.printStackTrace();
		}
	}

	/*
	 * treat disjunction as conjunction
	 */
	private Set<OWLClassExpression> getDisjunctionApprox0(Set<OWLClassExpression> superClasses) {
		return superClasses;
	}
	
	/*
	 * choose one simple class disjunct
	 */
	@SuppressWarnings("unused")
	private Set<OWLClassExpression> getDisjunctionApprox1(Set<OWLClassExpression> superClasses) {
		if(superClasses.isEmpty() || superClasses.size() == 1)
			return superClasses;

		OWLClassExpression rep = null;
		int min = Integer.MAX_VALUE, o;
		for(OWLClassExpression exp : superClasses)
			if(exp instanceof OWLClass && (o = getOccurrence(exp)) < min) {
				min = o;
				rep = exp;
			}

		if(rep == null) rep = superClasses.iterator().next();

		return Collections.singleton(rep);
	}

	/*
	 * randomly choose a class expression to represent this disjunction
	 */
	@SuppressWarnings("unused")
	private Set<OWLClassExpression> getDisjunctionApprox2(Set<OWLClassExpression> superClasses) {
		if(superClasses.isEmpty() || superClasses.size() == 1)
			return superClasses;

		int index = random.nextInt() % superClasses.size();
		if (index < 0) index += superClasses.size();

		int i = 0;
		for(OWLClassExpression exp : superClasses)
			if (i++ == index)
				return Collections.singleton(exp);
		return null;
	}
	
	/*
	 * choose the one that appears least in the l.h.s.
	 */
	@SuppressWarnings("unused")
	private Set<OWLClassExpression> getDisjunctionApprox3(Set<OWLClassExpression> superClasses) {
		if(superClasses.isEmpty() || superClasses.size() == 1)
			return superClasses;

		OWLClassExpression rep = null, exp1;
		int occurrence = Integer.MAX_VALUE, o;
		for (OWLClassExpression exp: superClasses) {
			o = 0;
			exp1 = exp;
			if (exp instanceof OWLObjectMinCardinality) {
				OWLObjectMinCardinality minCard = (OWLObjectMinCardinality)exp;
				if (minCard.getCardinality() == 1)
					exp1 = factory.getOWLObjectSomeValuesFrom(minCard.getProperty(), minCard.getFiller());
			}

			if (!subCounter.containsKey(exp1) || (o = subCounter.get(exp1)) < occurrence) {
				rep = exp;
				occurrence = o;
			}
		}

		return Collections.singleton(rep);
	}

	private int getOccurrence(OWLClassExpression exp) {
		if (!subCounter.containsKey(exp))
			return 0;
		return subCounter.get(exp);
	}

	@SuppressWarnings("unused")
	private Set<OWLClassExpression> getDisjunctionApprox4(Set<OWLClassExpression> superClasses) {
		if(superClasses.isEmpty() || superClasses.size() == 1)
			return superClasses;

		OWLClassExpression rep = null;
		int occurrence = Integer.MAX_VALUE, o;
		for (OWLClassExpression exp: superClasses) {
			o = 0;
			if (exp instanceof OWLObjectMinCardinality) {
				OWLObjectMinCardinality minCard = (OWLObjectMinCardinality)exp;
				if (minCard.getCardinality() == 1) {
					o = getOccurrence((factory.getOWLObjectSomeValuesFrom(minCard.getProperty(), factory.getOWLThing())));
					o += getOccurrence(minCard.getFiller());
//					if (o < o1) o = o1;
				}
			} else
				o = getOccurrence(exp);

			if (o < occurrence || o == occurrence && !(rep instanceof OWLClass)) {
				rep = exp;
				occurrence = o;
			}
		}

		return Collections.singleton(rep);
	}

	private boolean simplifyABox() {
		boolean flag = false;
		Map<OWLClassExpression, OWLClass> complex2atomic= new HashMap<OWLClassExpression, OWLClass>();

		OWLDatatype anyURI = factory.getOWLDatatype(IRI.create(Namespace.XSD_NS + "anyURI"));
		OWLObjectProperty sameAs = factory.getOWLObjectProperty(IRI.create(Namespace.EQUALITY));
		OWLObjectProperty differentFrom = factory.getOWLObjectProperty(IRI.create(Namespace.INEQUALITY));

		for (OWLOntology imported: inputOntology.getImportsClosure())
			for (OWLAxiom axiom: imported.getAxioms()) {
				if (axiom instanceof OWLClassAssertionAxiom) {
					flag = true;
					OWLClassAssertionAxiom assertion = (OWLClassAssertionAxiom)axiom;
					OWLClassExpression clsExp = assertion.getClassExpression();
					OWLClass cls;
					if (clsExp instanceof OWLClass) {
						if (((OWLClass) clsExp).toStringID().startsWith("owl:"))
							manager.addAxiom(tBox, axiom);
						else manager.addAxiom(aBox, axiom);
					}
					else {
						if ((cls = complex2atomic.get(clsExp)) == null) {
							complex2atomic.put(clsExp, cls = getNewConcept(tBox, rlCounter++));
							manager.addAxiom(tBox, factory.getOWLSubClassOfAxiom(cls, clsExp));
						}
						manager.addAxiom(aBox, factory.getOWLClassAssertionAxiom(cls, assertion.getIndividual()));
					}
				}
				else if (axiom instanceof OWLObjectPropertyAssertionAxiom || axiom instanceof OWLDataPropertyAssertionAxiom || axiom instanceof OWLAnnotationAssertionAxiom) {
					if(axiom.getDataPropertiesInSignature().contains(anyURI)) continue;
					flag = true;
					manager.addAxiom(aBox, axiom);
				}
				else if (axiom instanceof OWLSameIndividualAxiom) {
					OWLIndividual firstIndividual = null, previousIndividual = null, lastIndividual = null;
					for (OWLIndividual next: ((OWLSameIndividualAxiom) axiom).getIndividuals()) {
						if(firstIndividual == null) firstIndividual = previousIndividual = next;
						else
							manager.addAxiom(aBox, factory.getOWLObjectPropertyAssertionAxiom(sameAs, previousIndividual, next));
						previousIndividual = lastIndividual = next;
					}
					manager.addAxiom(aBox, factory.getOWLObjectPropertyAssertionAxiom(sameAs, lastIndividual, firstIndividual));
				}
				else if (axiom instanceof OWLDifferentIndividualsAxiom) {
					int index1 = 0, index2;
					for (OWLIndividual individual1: ((OWLDifferentIndividualsAxiom) axiom).getIndividuals()) {
						++index1;
						index2 = 0;
						for (OWLIndividual individual2: ((OWLDifferentIndividualsAxiom) axiom).getIndividuals()) {
							if (index2++ < index1) {
								manager.addAxiom(aBox, factory.getOWLObjectPropertyAssertionAxiom(differentFrom, individual1, individual2));
							} else break;
						}
					}
				} else
					manager.addAxiom(tBox, axiom);
			}

		return flag;
	}

	private void filter() {
		OWL2RLProfile profile = new OWL2RLProfile();
		OWLProfileReport report = profile.checkOntology(tBox);
		Set<OWLAxiom> rlAxioms = tBox.getAxioms();
		OWLAxiom axiom;

		for (OWLProfileViolation violation: report.getViolations()) {
			manager.addAxiom(restOntology, axiom = violation.getAxiom());
			rlAxioms.remove(axiom);
		}

		for (Iterator<OWLAxiom> iter = rlAxioms.iterator(); iter.hasNext(); )
			addAxiom2output(iter.next(), null);
	}
	
	private void clausify() {
		Configuration conf = new Configuration();
		OWLClausification clausifier = new OWLClausification(conf);
		dlOntology = (DLOntology)clausifier.preprocessAndClausify(restOntology, null)[1];
		clausifier = null;
	}
	
	protected void addAxiom2output(OWLAxiom axiom, OWLAxiom correspondingAxiom) {
		manager.addAxiom(outputOntology, axiom);
		if (correspondingAxiom != null)
			correspondence.put(axiom, correspondingAxiom);
	}

	private OWLClassExpression transform(OWLClassExpression exp, Set<OWLAxiom> addedAxioms) {
		if (exp instanceof OWLClass) 
			return exp;
		
		if (exp instanceof OWLObjectHasValue) 
			return exp;
		
		if (exp instanceof OWLObjectSomeValuesFrom) {
			OWLObjectSomeValuesFrom someValueExp = (OWLObjectSomeValuesFrom)exp;
			
			OWLClassExpression tExp = someValueExp.getFiller();
			if (tExp.equals(factory.getOWLThing()))
				exp = factory.getOWLObjectMinCardinality(1, someValueExp.getProperty());
			else 
				exp = factory.getOWLObjectMinCardinality(1, someValueExp.getProperty(), someValueExp.getFiller());
		}
		
		if (exp instanceof OWLObjectMinCardinality) {
			OWLObjectMinCardinality minExp = (OWLObjectMinCardinality)exp;
			OWLObjectPropertyExpression r;

			if (minExp.getFiller().equals(factory.getOWLThing())) {
				r = minExp.getProperty();
			}
			//TODO to be restored ... 
			//else if ((r = exists2role.get(someValueExp)) == null) {
				// deal with r' \subseteq r & range(r') \subseteq C
			else {
				r = getNewRole(outputOntology, rlCounter);
				addedAxioms.add(factory.getOWLSubObjectPropertyOfAxiom(r, minExp.getProperty()));
				OWLClassExpression tExp = minExp.getFiller();
				if (!(tExp instanceof OWLObjectComplementOf)) {
					if (tExp.equals(factory.getOWLThing())); 
					else 
						addedAxioms.add(factory.getOWLObjectPropertyRangeAxiom(r, tExp));
				}
				else if (botStrategy != BottomStrategy.TOREMOVE) {
						OWLClass cls = (OWLClass) tExp.getComplementNNF();
						OWLClass neg;
						if ((neg = atomic2negation.get(cls)) == null) {
							neg = getNewConcept(outputOntology, rlCounter);
							addedAxioms.add(factory.getOWLDisjointClassesAxiom(neg, cls));
							atomic2negation.put(cls, neg);
						}
						addedAxioms.add(factory.getOWLObjectPropertyRangeAxiom(r, neg));
					}
//				exists2role.put(someValueExp, (OWLObjectProperty) r);
			}
			
			// deal with r'(x,c)
			Set<OWLClassExpression> ret = new HashSet<OWLClassExpression>();
			int num = minExp.getCardinality();
			
			Set<OWLNamedIndividual> cs = new HashSet<OWLNamedIndividual>();
			OWLNamedIndividual c;
			for (int i = 0; i < num; ++i) {
				c = getNewIndividual(outputOntology, rlCounter++);
				ret.add(factory.getOWLObjectHasValue(r, c));
				cs.add(c);
			}
			
			if (botStrategy != BottomStrategy.TOREMOVE && cs.size() > 1) {
				addedAxioms.add(factory.getOWLDifferentIndividualsAxiom(cs));
			}
			
			return OWLHelper.getSimplifiedConjunction(factory, ret);
		}
		
		if (exp instanceof OWLObjectMaxCardinality) {
			OWLObjectMaxCardinality maxExp = (OWLObjectMaxCardinality)exp;
			OWLClassExpression tExp = maxExp.getFiller();
			int card = maxExp.getCardinality() >= 1 ? 1 : 0;
			if (!(tExp instanceof OWLObjectComplementOf))
				return factory.getOWLObjectMaxCardinality(card, maxExp.getProperty(), tExp);
			else {
				Utility.logDebug("oh, to be tested ... ");
				OWLClassExpression tExp1 = factory.getOWLObjectAllValuesFrom(maxExp.getProperty(), tExp.getComplementNNF());
				if (card == 0)
					return tExp1;
				else {
					OWLClassExpression tExp2 = factory.getOWLObjectMaxCardinality(1, maxExp.getProperty());
					return factory.getOWLObjectIntersectionOf(tExp1, tExp2);
				}
			}
		}
		
		if (exp instanceof OWLObjectAllValuesFrom) 
			return exp;
		
		if (exp instanceof OWLObjectOneOf) 
			if (((OWLObjectOneOf) exp).getIndividuals().size() == 1)
				return exp;
			else 
				return null;
			
		if (exp instanceof OWLDataHasValue)
			return exp;
		
		//TODO overapproximation - dealing with OWLDataMinCardinality
			
		if (exp instanceof OWLDataSomeValuesFrom) {
			return exp;
		}
		
		if (exp instanceof OWLDataMinCardinality) {
			return exp;
		}
		
		if (exp instanceof OWLDataMaxCardinality) {
			return exp;
		}
			
		
		Set<OWLClassExpression> exps = exp.asConjunctSet();
		if (exps.size() == 1 && exps.iterator().next() == exp) {
			Utility.logError(exp, "error in transform of Ontology~~~~");
		}
		Set<OWLClassExpression> nexps = new HashSet<OWLClassExpression>();
		OWLClassExpression ne;
		boolean changes = false;
		for (OWLClassExpression e: exps) {
			ne = transform(e, addedAxioms);
			if (ne != e) changes = true;
			nexps.add(ne);
		}
		if (changes) 
			return OWLHelper.getSimplifiedConjunction(factory, nexps);
		else 
			return exp;
	}
	
	protected OWLNamedIndividual getNewIndividual(OWLOntology onto, int number) {
		OWLOntologyManager manager = onto.getOWLOntologyManager();
		OWLDataFactory factory = manager.getOWLDataFactory();
		OWLNamedIndividual newIndividual = factory.getOWLNamedIndividual(IRI.create(Namespace.PAGODA_ANONY + "NI" + number));
		manager.addAxiom(onto, factory.getOWLDeclarationAxiom(newIndividual)); 
		return newIndividual; 				
	}

	protected OWLObjectProperty getNewRole(OWLOntology onto, int number) {
		OWLOntologyManager manager = onto.getOWLOntologyManager();
		OWLDataFactory factory = manager.getOWLDataFactory();
		OWLObjectProperty newProperty = factory.getOWLObjectProperty(IRI.create(Namespace.PAGODA_AUX + "NR" + number));
		manager.addAxiom(onto, factory.getOWLDeclarationAxiom(newProperty)); 
		return newProperty; 
	}

	private OWLClass getNewConcept(OWLOntology onto, int number) {
		OWLOntologyManager manager = onto.getOWLOntologyManager();
		OWLDataFactory factory = manager.getOWLDataFactory(); 
		OWLClass newClass = factory.getOWLClass(IRI.create(Namespace.PAGODA_AUX + "NC" + number));
		manager.addAxiom(onto, factory.getOWLDeclarationAxiom(newClass)); 
		return newClass; 
	}

	public OWLOntologyManager getOWLOntologyManager() {
		return inputOntology.getOWLOntologyManager();
	}

	public String getOntologyIRI() {
		return ontologyIRI; 
	}
	
	public OWLOntology getOutputOntology() {
		return outputOntology;
	}

	@Override
	public String getOutputPath() {
		return outputPath;
	}

	@Override
	public String getDirectory() {
		return outputPath.substring(0, outputPath.lastIndexOf(Utility.FILE_SEPARATOR));
	}

	public void setCorrespondenceFileLoc(String path) {
		corrFileName = path; 
	}
	
	private enum BottomStrategy { TOREMOVE, NULLARY, UNARY }
}

