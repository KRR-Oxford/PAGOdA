package uk.ac.ox.cs.pagoda.rules;

import org.apache.commons.io.FilenameUtils;
import org.semanticweb.HermiT.Configuration;
import org.semanticweb.HermiT.model.*;
import org.semanticweb.HermiT.structural.OWLClausification;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.*;
import org.semanticweb.simpleETL.SimpleETL;
import uk.ac.ox.cs.pagoda.MyPrefixes;
import uk.ac.ox.cs.pagoda.approx.KnowledgeBase;
import uk.ac.ox.cs.pagoda.approx.RLPlusOntology;
import uk.ac.ox.cs.pagoda.constraints.BottomStrategy;
import uk.ac.ox.cs.pagoda.constraints.NullaryBottom;
import uk.ac.ox.cs.pagoda.constraints.PredicateDependency;
import uk.ac.ox.cs.pagoda.hermit.DLClauseHelper;
import uk.ac.ox.cs.pagoda.hermit.RuleHelper;
import uk.ac.ox.cs.pagoda.owl.OWLHelper;
import uk.ac.ox.cs.pagoda.util.Utility;

import java.io.*;
import java.util.*;

public abstract class Program implements KnowledgeBase {
	
	protected String ontologyDirectory = null;
	protected OWLOntology ontology; 
//	protected DLOntology dlOntology;
    protected Set<DLClause> dlClauses = new HashSet<>();
    protected Set<Atom> positiveFacts = new HashSet<>();
	protected BottomStrategy botStrategy; 
	protected Collection<DLClause> clauses = new HashSet<DLClause>();
//	protected Set<DLClause> used = new HashSet<DLClause>();
protected PredicateDependency dependencyGraph;
	protected LinkedList<OWLTransitiveObjectPropertyAxiom> transitiveAxioms;
	protected LinkedList<DLClause> transitiveClauses;
	protected LinkedList<OWLSubPropertyChainOfAxiom> subPropChainAxioms;
	protected LinkedList<DLClause> subPropChainClauses;
	private String additionalDataFile = null;

	public static String toString(Collection<DLClause> clauses) {
		StringBuilder sb = new StringBuilder(DLClauseHelper.toString(clauses));
		sb.insert(0, MyPrefixes.PAGOdAPrefixes.prefixesText());
		return sb.toString();
	}

	public void load(InputStream rules, BottomStrategy botStrategy) {
//        this.botStrategy = botStrategy;
//      // fake instantiation
        try {
            load(OWLManager.createOWLOntologyManager().createOntology(), botStrategy);
        } catch (OWLOntologyCreationException e) {
            e.printStackTrace();
        }

        try(BufferedReader br = new BufferedReader(new InputStreamReader(rules))) {
            String line;
            while((line = br.readLine()) != null)
                dlClauses.add(RuleHelper.parseClause(line));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

	public void load(OWLOntology o, BottomStrategy botStrategy) {
		this.botStrategy = botStrategy;
		RLPlusOntology owlOntology = new RLPlusOntology();
		owlOntology.load(o, new NullaryBottom());
		owlOntology.simplify();

		ontology = owlOntology.getTBox();
//		String ontologyPath = OWLHelper.getOntologyPath(ontology);
//		ontologyDirectory = ontologyPath.substring(0, ontologyPath.lastIndexOf(Utility.JAVA_FILE_SEPARATOR));
		String ontologyPath = ontology.getOWLOntologyManager().getOntologyDocumentIRI(ontology).toURI().getPath();
		ontologyDirectory = FilenameUtils.getFullPath(ontologyPath);
		clausify();

		String aboxOWLFile = owlOntology.getABoxPath();
		OWLOntology abox = OWLHelper.loadOntology(aboxOWLFile);
		OWLOntologyManager manager = abox.getOWLOntologyManager();
		OWLAxiom axiom;
		for (Atom atom: positiveFacts) {
			if ((axiom = OWLHelper.getABoxAssertion(manager.getOWLDataFactory(), atom)) != null)
				manager.addAxiom(abox, axiom);
		}

		try {
			FileOutputStream out = new FileOutputStream(aboxOWLFile);
			manager.saveOntology(abox, out);
			out.close();
		} catch(IOException | OWLOntologyStorageException e) {
			e.printStackTrace();
			System.exit(1);
		}

		if (!abox.isEmpty()) {
			SimpleETL rewriter = new SimpleETL(owlOntology.getOntologyIRI(), aboxOWLFile);
			try {
				rewriter.rewrite();
			} catch (Exception e) {
				e.printStackTrace();
			}
			additionalDataFile = rewriter.getExportedFile();
			new File(aboxOWLFile).delete();
		}

	}
	
	public String getAdditionalDataFile() {
		return additionalDataFile;
	}

	@Override
	public void transform() {
		for(DLClause dlClause : dlClauses) {
			DLClause simplifiedDLClause = DLClauseHelper.removeNominalConcept(dlClause);
			simplifiedDLClause = removeAuxiliaryBodyAtoms(simplifiedDLClause);
			simplifiedDLClause = DLClauseHelper.replaceWithDataValue(simplifiedDLClause);
			convert(simplifiedDLClause);
		}

		addingTransitiveAxioms();
		addingSubPropertyChainAxioms();

		Collection<DLClause> botRelated = new LinkedList<DLClause>();
		Variable X = Variable.create("X");
		botRelated.add(DLClause.create(new Atom[0], new Atom[]{Atom.create(Inequality.INSTANCE, X, X)}));
		clauses.addAll(botStrategy.process(botRelated));

		if(this instanceof GeneralProgram)
			Utility.logDebug("The number of rules: " + (clauses.size() - 1));
	}
	
	@Override
	public void save() {
		try {
			BufferedWriter ruleWriter = new BufferedWriter(new OutputStreamWriter(
					new FileOutputStream(getOutputPath())));
			ruleWriter.write(toString());
			ruleWriter.close();
		} catch(IOException e) {
			e.printStackTrace();
		}
		Utility.logInfo("The rules are saved in " + getOutputPath() + ".");
	}

	@Override
	public String toString() {
		return toString(clauses);
	}

	public final void convert(DLClause clause) {
		Collection<DLClause> tempClauses = convert2Clauses(clause);
		clauses.addAll(tempClauses);
	}

	public abstract Collection<DLClause> convert2Clauses(DLClause clause);

	public abstract String getOutputPath();

	public OWLOntology getOntology() {
		return ontology;
	}

	public Collection<DLClause> getClauses() {
		return clauses;
	}

	public Collection<DLClause> getClauses(DLClause queryClause) {
//		if (true) return new HashSet<DLClause>(clauses);
		Set<DLPredicate> predicates = new HashSet<DLPredicate>();
		predicates.addAll(dependencyGraph.collectPredicate(queryClause.getBodyAtoms()));

		Set<DLPredicate> dependence = new HashSet<DLPredicate>();
		for(DLPredicate predicate : predicates)
			dependence.addAll(dependencyGraph.getAncesters(predicate));

		Collection<DLClause> relevantClauses = new LinkedList<DLClause>();
		for(DLClause clause : clauses) {
			if(relevant(clause, dependence))
				relevantClauses.add(clause);

		}
		return relevantClauses;
	}

	public PredicateDependency buildDependencyGraph() {
		if(dependencyGraph == null)
			return dependencyGraph = new PredicateDependency(clauses);
		else
			return dependencyGraph;
	}

	public void getDependencyGraph(PredicateDependency g) {
		dependencyGraph = g;
	}

	public final String getDirectory() {
		return Utility.getGlobalTempDirAbsolutePath();
	}

	public void deleteABoxTurtleFile() {
		if(additionalDataFile != null)
			new File(additionalDataFile).delete();
	}

	/**
	 * clone all information of another program after load()
	 *
	 * @param program
	 */
	void clone(Program program) {
		this.ontologyDirectory = program.ontologyDirectory;
		this.ontology = program.ontology;
//		this.dlOntology = program.dlOntology;
        this.dlClauses = program.dlClauses;
        this.positiveFacts = program.positiveFacts;
		this.botStrategy = program.botStrategy;
		this.additionalDataFile = program.additionalDataFile;
		this.transitiveAxioms = program.transitiveAxioms;
		this.transitiveClauses = program.transitiveClauses;
		this.subPropChainAxioms = program.subPropChainAxioms;
		this.subPropChainClauses = program.subPropChainClauses;
	}

	private void clausify() {
		Configuration conf = new Configuration();
		OWLClausification clausifier = new OWLClausification(conf);
		OWLOntology filteredOntology = null;
		OWLOntologyManager manager = ontology.getOWLOntologyManager();
		try {
			filteredOntology = manager.createOntology();
		} catch(OWLOntologyCreationException e) {
			e.printStackTrace();
		}

		transitiveAxioms = new LinkedList<OWLTransitiveObjectPropertyAxiom>();
		subPropChainAxioms = new LinkedList<OWLSubPropertyChainOfAxiom>();

		OWLDatatype date = ontology.getOWLOntologyManager()
								   .getOWLDataFactory()
								   .getOWLDatatype(IRI.create("http://www.w3.org/2001/XMLSchema#date"));
		int noOfDataPropertyRangeAxioms = 0, noOfAxioms = 0;
		for(OWLOntology onto : ontology.getImportsClosure())
			for(OWLAxiom axiom : onto.getAxioms()) {
				if(axiom instanceof OWLTransitiveObjectPropertyAxiom)
					transitiveAxioms.add((OWLTransitiveObjectPropertyAxiom) axiom);
				else if(axiom instanceof OWLSubPropertyChainOfAxiom)
					subPropChainAxioms.add((OWLSubPropertyChainOfAxiom) axiom);
					// TODO to filter out datatype axioms
				else if(axiom instanceof OWLDataPropertyRangeAxiom) {
					++noOfDataPropertyRangeAxioms;
					Utility.logInfo("The axiom: " + axiom + " is being ignored.");
				}
				else {
					if(axiom.getDatatypesInSignature().contains(date)) {
						Utility.logInfo("The axiom: " + axiom + " is being ignored.");
					}
					else manager.addAxiom(filteredOntology, axiom);
				}

				if(axiom instanceof OWLAnnotationAssertionAxiom ||
						axiom instanceof OWLSubAnnotationPropertyOfAxiom ||
						axiom instanceof OWLDeclarationAxiom ||
						axiom instanceof OWLDataPropertyRangeAxiom) {
				}
				else {
//					System.out.println(axiom);
					++noOfAxioms;
				}

			}
		Utility.logInfo("The number of data property range axioms that are ignored: " + noOfDataPropertyRangeAxioms + "(" + noOfAxioms + ")");

        DLOntology dlOntology = (DLOntology) clausifier.preprocessAndClausify(filteredOntology, null)[1];
        dlClauses = dlOntology.getDLClauses();
        positiveFacts = dlOntology.getPositiveFacts();
	}

	private DLClause removeAuxiliaryBodyAtoms(DLClause dlClause) {
		Collection<Atom> newBodyAtoms = new LinkedList<Atom>();
		DLPredicate p;
		for(Atom bodyAtom : dlClause.getBodyAtoms()) {
			p = bodyAtom.getDLPredicate();
			if(p instanceof AtomicConcept ||
					p instanceof AtomicRole || p instanceof InverseRole ||
					p instanceof Equality || p instanceof AnnotatedEquality || p instanceof Inequality)
				newBodyAtoms.add(bodyAtom);
		}
		LinkedList<Atom> newHeadAtoms = new LinkedList<Atom>();
		Map<Variable, Term> assign = new HashMap<Variable, Term>();
		for(Atom headAtom : dlClause.getHeadAtoms()) {
			p = headAtom.getDLPredicate();
			if(p instanceof AtomicNegationDataRange) {
				AtomicDataRange positive = ((AtomicNegationDataRange) p).getNegatedDataRange();
				if(!(positive instanceof ConstantEnumeration))
					newBodyAtoms.add(Atom.create(positive, headAtom.getArgument(0)));
				else if(((ConstantEnumeration) positive).getNumberOfConstants() == 1) {
					assign.put((Variable) headAtom.getArgument(0), ((ConstantEnumeration) positive).getConstant(0));
//					newBodyAtoms.add(Atom.create(Equality.INSTANCE, headAtom.getArgument(0), ((ConstantEnumeration) positive).getConstant(0)));
				}
				else newHeadAtoms.add(headAtom);
			}
			else
				newHeadAtoms.add(headAtom);
		}

		if(assign.isEmpty() && newHeadAtoms.isEmpty() && newBodyAtoms.size() == dlClause.getBodyLength())
			return dlClause;

		Atom[] headArray =
				newHeadAtoms.size() == dlClause.getHeadLength() ? dlClause.getHeadAtoms() : newHeadAtoms.toArray(new Atom[0]);
		Atom[] bodyArray =
				newBodyAtoms.size() == dlClause.getBodyLength() ? dlClause.getBodyAtoms() : newBodyAtoms.toArray(new Atom[0]);
		if(!assign.isEmpty()) {
			for(int i = 0; i < headArray.length; ++i)
				headArray[i] = DLClauseHelper.getInstance(headArray[i], assign);
			for(int i = 0; i < bodyArray.length; ++i)
				bodyArray[i] = DLClauseHelper.getInstance(bodyArray[i], assign);
		}
		return DLClause.create(headArray, bodyArray);
	}

	private void addingTransitiveAxioms() {
		DLClause transitiveClause;
		Atom headAtom;
		Variable X = Variable.create("X"), Y = Variable.create("Y"), Z = Variable.create("Z");
		transitiveClauses = new LinkedList<DLClause>();
		for(OWLTransitiveObjectPropertyAxiom axiom : transitiveAxioms) {
			OWLObjectPropertyExpression objExp = axiom.getProperty();
			headAtom = getAtom(objExp, X, Z);
			Atom[] bodyAtoms = new Atom[2];
			bodyAtoms[0] = getAtom(objExp, X, Y);
			bodyAtoms[1] = getAtom(objExp, Y, Z);
			transitiveClause = DLClause.create(new Atom[]{headAtom}, bodyAtoms);
			clauses.add(transitiveClause);
			transitiveClauses.add(transitiveClause);
		}
	}

	private Atom getAtom(OWLObjectPropertyExpression exp, Variable x, Variable y) {
		if(exp instanceof OWLObjectProperty)
			return Atom.create(AtomicRole.create(((OWLObjectProperty) exp).toStringID()), x, y);
		// TODO fixed, test it
		OWLObjectPropertyExpression inverseOf;
		if(exp instanceof OWLObjectInverseOf && (inverseOf = (
				(OWLObjectInverseOf) exp).getInverse()) instanceof OWLObjectProperty)
			return Atom.create(AtomicRole.create(((OWLObjectProperty) inverseOf).toStringID()), x, y);
		return null;
	}

	private void addingSubPropertyChainAxioms() {
		DLClause dlClause;
		subPropChainClauses = new LinkedList<DLClause>();
		Atom headAtom;
		Iterator<OWLObjectPropertyExpression> iterExp;
		OWLObjectPropertyExpression objExp;
		for(OWLSubPropertyChainOfAxiom axiom : subPropChainAxioms) {
			objExp = axiom.getSuperProperty();
			List<OWLObjectPropertyExpression> objs = axiom.getPropertyChain();
			headAtom = getAtom(objExp, Variable.create("X"), Variable.create("X" + objs.size()));
			iterExp = objs.iterator();
			int index = 1;
			Atom[] bodyAtoms = new Atom[objs.size()];
			bodyAtoms[0] = getAtom(iterExp.next(), Variable.create("X"), Variable.create("X1"));
			while(index < objs.size()) {
				bodyAtoms[index] =
						getAtom(iterExp.next(), Variable.create("X" + index), Variable.create("X" + (index + 1)));
				++index;
			}
			dlClause = DLClause.create(new Atom[]{headAtom}, bodyAtoms);
			clauses.add(dlClause);
			subPropChainClauses.add(dlClause);
		}
	}

	private boolean relevant(DLClause clause, Set<DLPredicate> set) {
		for(DLPredicate p : dependencyGraph.collectPredicate(clause.getHeadAtoms()))
			if(set.contains(p))
				return true;
		return false;
	}

}
