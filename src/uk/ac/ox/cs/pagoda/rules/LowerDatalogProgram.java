package uk.ac.ox.cs.pagoda.rules;

import org.semanticweb.HermiT.Reasoner;
import org.semanticweb.HermiT.model.*;
import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.reasoner.Node;
import uk.ac.ox.cs.pagoda.constraints.BottomStrategy;
import uk.ac.ox.cs.pagoda.constraints.NullaryBottom;
import uk.ac.ox.cs.pagoda.constraints.UnaryBottom;
import uk.ac.ox.cs.pagoda.constraints.UpperUnaryBottom;
import uk.ac.ox.cs.pagoda.multistage.Normalisation;
import uk.ac.ox.cs.pagoda.multistage.RestrictedApplication;
import uk.ac.ox.cs.pagoda.util.Timer;
import uk.ac.ox.cs.pagoda.util.Utility;

import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Set;

public class LowerDatalogProgram extends ApproxProgram implements IncrementalProgram {
	
	boolean m_toClassify; 
	
	public LowerDatalogProgram() {
		m_toClassify = true; 
	}
	
	public LowerDatalogProgram(boolean toClassify) {
		m_toClassify = toClassify; // false; // 
	}
	
	void clone(Program program) {
		super.clone(program); 
		if (botStrategy instanceof UpperUnaryBottom)
			botStrategy = new UnaryBottom(); 
	}


	@Override
	public void transform() {
		if (m_toClassify) {
			ClassifyThread thread = new ClassifyThread(this);
			thread.start();
			super.transform();
			try {
				thread.join(5000);
			} catch (InterruptedException e) {
				return ;
			}
			if (!thread.isAlive()) thread.dispose();
			else thread.interrupt();
		}
		else 
			super.transform();
		
		Normalisation norm = new Normalisation(dlOntology.getDLClauses(), ontology, new NullaryBottom());
		BottomStrategy tBottom = new NullaryBottom(); 
		norm.process();
		for (DLClause nClause: norm.getNormlisedClauses()) {
			if (nClause.getHeadLength() != 1)
				for (DLClause newClause: RestrictedApplication.addAdditionalDatalogRules(nClause, tBottom, norm)) {
//					System.out.println(newClause); 
					if (newClause.getHeadAtom(0).getDLPredicate() instanceof AtomicConcept || newClause.getHeadAtom(0).getDLPredicate() instanceof AtomicRole) {
//						System.out.println(newClause); 
						clauses.add(newClause);
					}
				}
			
			if (nClause.getHeadLength() == 1 && (nClause.getHeadAtom(0).getDLPredicate() instanceof AtomicConcept || nClause.getHeadAtom(0).getDLPredicate() instanceof AtomicRole) && clauses.add(nClause)) {
//				System.out.println(nClause);
			}
		}
	}

	@Override
	public String getOutputPath() {
		return getDirectory() + "lower.dlog";
	}
	
//	@Override
//	public String getDirectory() {
//		File dir = new File(ontologyDirectory + Utility.FILE_SEPARATOR + "datalog");
//		if (!dir.exists())
//			dir.mkdirs();
//		return dir.getPath();
//	}

	@Override
	public void enrich(Collection<DLClause> delta) {
		synchronized (clauses) {
			Iterator<DLClause> iter = delta.iterator();
			while (iter.hasNext()) 
				clauses.add(iter.next()); 
		}
	}
	
	@Override
	public String toString() {
		String text; 
		synchronized (clauses) {
			text = super.toString();
		}
		return text; 
	}
	
	@Override
	protected void initApproximator() {
		m_approx = new IgnoreBoth(); 
	}

}

class ClassifyThread extends Thread {
	
	IncrementalProgram m_program; 
	Collection<DLClause> clauses = new LinkedList<DLClause>(); 
	
	Variable X = Variable.create("X"), Y = Variable.create("Y"); 
	
	ClassifyThread(IncrementalProgram program) {
		m_program = program;
	}
	
	Reasoner hermitReasoner;
	OWLOntology ontology; 
	
	@Override
	public void run() {
		ontology = m_program.getOntology();
		try {
			hermitReasoner = new Reasoner(ontology);
			Timer t = new Timer(); 
			hermitReasoner.classifyClasses();
			Utility.logInfo("HermiT classification done: " + t.duration());
		} catch (Exception e) {
//			e.printStackTrace();
			Utility.logInfo("HermiT cannot classify the ontology.");
			hermitReasoner = null; 
		}
	}
	
	public void dispose() {
		if (hermitReasoner == null)
			return ; 
		Set<OWLClass> classes;
		OWLClass lastClass = null, currentClass; 
		for (OWLClass subClass: ontology.getClassesInSignature()) {
			Node<OWLClass> node = hermitReasoner.getEquivalentClasses(subClass);
			if (!subClass.equals(node.getRepresentativeElement())) continue; 
			
			classes = node.getEntities(); 
			lastClass = subClass; 
			for (Iterator<OWLClass> iter = classes.iterator(); iter.hasNext(); ) {
				currentClass = iter.next();
				if (currentClass.equals(subClass)) continue; 
				addClause(lastClass, currentClass); 
				lastClass = currentClass; 
			}
			addClause(lastClass, subClass); 
			
			for (Node<OWLClass> tNode: hermitReasoner.getSuperClasses(subClass, true)) {
				OWLClass superClass = tNode.getRepresentativeElement();
				addClause(subClass, superClass); 
			}
		}
		
		Set<OWLObjectPropertyExpression> properties;
		OWLObjectPropertyExpression lastProperty, currentProperty; 
		for (OWLObjectProperty subProperty: ontology.getObjectPropertiesInSignature()) {
			Node<OWLObjectPropertyExpression> node = hermitReasoner.getEquivalentObjectProperties(subProperty);
			if (!subProperty.equals(node.getRepresentativeElement())) continue; 
			
			properties = node.getEntities(); 
			lastProperty = subProperty; 
			for (Iterator<OWLObjectPropertyExpression> iter = properties.iterator(); iter.hasNext(); ) {
				currentProperty = iter.next(); 
				if (currentProperty.equals(subProperty)) continue; 
				addClause(lastProperty, currentProperty); 
				lastProperty = currentProperty; 
			}
			addClause(lastProperty, subProperty); 
			
			for (Node<OWLObjectPropertyExpression> tNode: hermitReasoner.getSuperObjectProperties(subProperty, true)) {
				OWLObjectPropertyExpression superProperty = tNode.getRepresentativeElement();
				addClause(subProperty, superProperty);
			}
		}

		m_program.enrich(clauses);
		Utility.logInfo("classification done and enriched lower bound rules."); 
	}
	

	private void addClause(OWLObjectPropertyExpression subProperty, OWLObjectPropertyExpression superProperty) {
		if (subProperty.equals(superProperty)) return ; 
		if (superProperty.toString().equals("owl:topObjectProperty")) return ; 
		clauses.add(DLClause.create(new Atom[] { getAtom(superProperty) }, new Atom[] { getAtom(subProperty) })); 
	}

	private Atom getAtom(OWLObjectPropertyExpression p) {
		if (p instanceof OWLObjectInverseOf)
			return Atom.create(AtomicRole.create(((OWLObjectProperty) ((OWLObjectInverseOf) p).getInverse()).toStringID()), Y, X);
		
		return Atom.create(AtomicRole.create(((OWLObjectProperty) p).toStringID()), X, Y);
	}

	private void addClause(OWLClass subClass, OWLClass superClass) {
		if (subClass.equals(superClass)) return ; 
		if (subClass.toString().equals("owl:Nothing")) return ; 
		if (superClass.toString().equals("owl:Thing")) return ; 
		clauses.add(DLClause.create(new Atom[] { getAtom(superClass) }, new Atom[] { getAtom(subClass) })); 		
	}

	private Atom getAtom(OWLClass c) {
		return Atom.create(AtomicConcept.create(c.toStringID()), X);
	}

}