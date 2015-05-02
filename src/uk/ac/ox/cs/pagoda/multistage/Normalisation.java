package uk.ac.ox.cs.pagoda.multistage;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;

import org.semanticweb.HermiT.model.AtLeast;
import org.semanticweb.HermiT.model.AtLeastConcept;
import org.semanticweb.HermiT.model.AtLeastDataRange;
import org.semanticweb.HermiT.model.Atom;
import org.semanticweb.HermiT.model.AtomicConcept;
import org.semanticweb.HermiT.model.AtomicNegationConcept;
import org.semanticweb.HermiT.model.AtomicRole;
import org.semanticweb.HermiT.model.Constant;
import org.semanticweb.HermiT.model.ConstantEnumeration;
import org.semanticweb.HermiT.model.DLClause;
import org.semanticweb.HermiT.model.Individual;
import org.semanticweb.HermiT.model.Inequality;
import org.semanticweb.HermiT.model.InverseRole;
import org.semanticweb.HermiT.model.LiteralConcept;
import org.semanticweb.HermiT.model.Role;
import org.semanticweb.HermiT.model.Variable;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLDataHasValue;
import org.semanticweb.owlapi.model.OWLDataProperty;
import org.semanticweb.owlapi.model.OWLDataPropertyExpression;
import org.semanticweb.owlapi.model.OWLLiteral;
import org.semanticweb.owlapi.model.OWLObjectHasSelf;
import org.semanticweb.owlapi.model.OWLObjectInverseOf;
import org.semanticweb.owlapi.model.OWLObjectMinCardinality;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.model.OWLObjectPropertyExpression;
import org.semanticweb.owlapi.model.OWLObjectSomeValuesFrom;
import org.semanticweb.owlapi.model.OWLOntology;

import uk.ac.ox.cs.pagoda.MyPrefixes;
import uk.ac.ox.cs.pagoda.approx.Clause;
import uk.ac.ox.cs.pagoda.approx.Clausifier;
import uk.ac.ox.cs.pagoda.constraints.BottomStrategy;
import uk.ac.ox.cs.pagoda.rules.OverApproxExist;
import uk.ac.ox.cs.pagoda.util.Namespace;
import uk.ac.ox.cs.pagoda.util.Utility;

public class Normalisation {
	
//	MultiStageUpperProgram m_program; 
	OWLOntology m_ontology;
	BottomStrategy m_botStrategy;
	Collection<DLClause> m_rules;
	Set<DLClause> m_normClauses = new HashSet<DLClause>();
	Map<DLClause, DLClause> exist2original = new HashMap<DLClause, DLClause>(); 
	
	public Set<DLClause> getNormlisedClauses() {
		return m_normClauses;
	}
	
	public Normalisation(Collection<DLClause> rules, OWLOntology ontology, BottomStrategy botStrategy) {
//		m_program = program; 
		m_ontology = ontology; 
		m_rules = rules; 
		m_botStrategy = botStrategy; 
	}
	
	public void process() {
		for (DLClause clause: m_rules) 
			if (m_botStrategy.isBottomRule(clause)) 
				processBottomRule(clause);
			else if (clause.getHeadLength() == 1) {
				if (clause.getHeadAtom(0).getDLPredicate() instanceof AtLeast)
					processExistentialRule(clause);
				else 
					m_normClauses.add(clause);
			}
			else 
				processDisjunctiveRule(clause);
	}
	
	private void processExistentialRule(DLClause clause) {
		if (clause.getBodyLength() == 1 && clause.getBodyAtom(0).getDLPredicate() instanceof AtomicConcept) {
			m_normClauses.add(clause);
			return ;
		}
		
		Atom headAtom = clause.getHeadAtom(0); 
		if (headAtom.getDLPredicate() instanceof AtLeastDataRange) {
			m_normClauses.add(clause);
			return ;
		}
		AtLeastConcept alc = (AtLeastConcept) headAtom.getDLPredicate(); 
		AtomicConcept ac = getRightAuxiliaryConcept(alc, OverApproxExist.getNewIndividual(clause, 0));
		DLClause newClause; 
		m_normClauses.add(DLClause.create(new Atom[] {Atom.create(ac, headAtom.getArgument(0)) }, clause.getBodyAtoms()));
		m_normClauses.add(newClause = DLClause.create(new Atom[] {Atom.create(alc, X)}, new Atom[] {Atom.create(ac, X)}));
		exist2original.put(newClause, clause); 
	}
	
	public static AtLeastConcept toAtLeastConcept(AtLeast p) {
		if (p instanceof AtLeastConcept) return (AtLeastConcept) p; 
		AtLeastDataRange aldr = (AtLeastDataRange) p; 
		return AtLeastConcept.create(aldr.getNumber(), aldr.getOnRole(), AtomicConcept.create(MyPrefixes.PAGOdAPrefixes.expandIRI(aldr.getToDataRange().toString()))); 
	}

	private void processDisjunctiveRule(DLClause clause) {
		boolean toNormalise = false; 
		for (Atom atom: clause.getHeadAtoms())
			if (!(atom.getDLPredicate() instanceof AtomicConcept)) {
				toNormalise = true;
				break; 
			}
		
		if (!toNormalise) {
			m_normClauses.add(clause);
			return ; 
		}
		
		Atom[] newHeadAtoms = new Atom[clause.getHeadLength()];
		Set<Atom> additionalAtoms = new HashSet<Atom>(); 
		int index = 0; 
		DLClause newClause; 
		for (Atom headAtom: clause.getHeadAtoms()) {
			if (headAtom.getDLPredicate() instanceof AtLeast) {
				AtLeast al = (AtLeast) headAtom.getDLPredicate();
				if (al instanceof AtLeastDataRange && ((AtLeastDataRange) al).getToDataRange() instanceof ConstantEnumeration) {
					ConstantEnumeration ldr = (ConstantEnumeration) ((AtLeastDataRange) al).getToDataRange();
					newHeadAtoms[index] = null;
					Atom newHeadAtom; 
					for (int i = 0; i < ldr.getNumberOfConstants(); ++i) {
						newHeadAtom = Atom.create(AtomicRole.create(((AtomicRole) ((AtLeastDataRange) al).getOnRole()).getIRI()), headAtom.getArgument(0), ldr.getConstant(i));
						if (newHeadAtoms[index] == null) newHeadAtoms[index] = newHeadAtom; 
						else additionalAtoms.add(newHeadAtom);
					}					
				} else {
					AtLeastConcept alc = toAtLeastConcept((AtLeast) headAtom.getDLPredicate()); 
					AtomicConcept ac = getRightAuxiliaryConcept(alc, OverApproxExist.getNewIndividual(clause, 0)); 
					newHeadAtoms[index] = Atom.create(ac, headAtom.getArgument(0));
					m_normClauses.add(newClause = DLClause.create(new Atom[] {Atom.create(alc, headAtom.getArgument(0))}, new Atom[] {newHeadAtoms[index]}));
					exist2original.put(newClause, clause); 
				}
			}
			else 
				newHeadAtoms[index] = headAtom;
			++index; 
		}
		
		if (!additionalAtoms.isEmpty()) {
			Atom[] tempHeadAtoms = newHeadAtoms; 
			newHeadAtoms = new Atom[newHeadAtoms.length + additionalAtoms.size()];
			for (int i = 0; i < tempHeadAtoms.length; ++i) 
				newHeadAtoms[i] = tempHeadAtoms[i];
			int tempI = tempHeadAtoms.length; 
			for (Iterator<Atom> iter = additionalAtoms.iterator(); iter.hasNext(); ) 
				newHeadAtoms[tempI++] = iter.next();
			additionalAtoms.clear();
		}

		m_normClauses.add(newClause = DLClause.create(newHeadAtoms, clause.getBodyAtoms()));
	}
	
	private void processBottomRule(DLClause clause) {
		if (clause.getBodyLength() == 1) {
			Atom inequality = clause.getBodyAtom(0);
			if (inequality.getDLPredicate() instanceof Inequality && inequality.getArgument(0).equals(inequality.getArgument(1))) {
				m_normClauses.add(clause);
				return ;
			}
		}
		
		boolean toNormalise = false; 
		for (Atom atom: clause.getBodyAtoms())
			if (!(atom.getDLPredicate() instanceof AtomicConcept))
				toNormalise = true; 
		
		if (!toNormalise) { 
			m_normClauses.add(clause); 
			return ; 
		}
		
		Clause myClause = null; 
		try {
			myClause = new Clause(Clausifier.getInstance(m_ontology), clause);
		} catch (Exception e) {
			Utility.logError("The clause: " + clause + " cannot be rolled up into GCI.");
			m_normClauses.add(clause); 
			return ; 
		}
		
		Atom[] newBodyAtoms = new Atom [myClause.getSubClasses().size()];
		int index = 0; 
		for (OWLClassExpression clsExp: myClause.getSubClasses())  {
			if (clsExp instanceof OWLClass) 
				newBodyAtoms[index] = Atom.create(AtomicConcept.create(((OWLClass) clsExp).getIRI().toString()), X); 
			else if (clsExp instanceof OWLObjectSomeValuesFrom || clsExp instanceof OWLObjectMinCardinality) {
				int number; 
				OWLObjectPropertyExpression prop; 
				OWLClassExpression filler;
				if (clsExp instanceof OWLObjectSomeValuesFrom) {
					OWLObjectSomeValuesFrom owl = (OWLObjectSomeValuesFrom) clsExp;
					number = 1; 
					prop = owl.getProperty();
					filler = owl.getFiller(); 
				}
				else {
					OWLObjectMinCardinality owl = (OWLObjectMinCardinality) clsExp; 
					number = owl.getCardinality(); 
					prop = owl.getProperty(); 
					filler = owl.getFiller(); 
				}
				
				Role r = null; 
				if (prop instanceof OWLObjectProperty) 
					r = AtomicRole.create(((OWLObjectProperty) prop).getIRI().toString());
				else  
					r = InverseRole.create(AtomicRole.create(((OWLObjectProperty) (((OWLObjectInverseOf) prop).getInverse())).getIRI().toString()));
				
				LiteralConcept c = AtomicConcept.create(((OWLClass) filler).getIRI().toString());
				AtomicConcept ac = getLeftAuxiliaryConcept(AtLeastConcept.create(number, r, c), false);
				
				m_normClauses.add(exists_r_C_implies_A(number, r, c, ac)); 
				newBodyAtoms[index] = Atom.create(ac, X);
			}
//			else if (clsExp instanceof OWLDataSomeValuesFrom || clsExp instanceof OWLDataMinCardinality) {
//				int number; 
//				OWLDataPropertyExpression prop; 
//				OWLDataRange filler;
//				if (clsExp instanceof OWLDataSomeValuesFrom) {
//					OWLDataSomeValuesFrom owl = (OWLDataSomeValuesFrom) clsExp;
//					number = 1; 
//					prop = owl.getProperty();
//					filler = owl.getFiller(); 
//				}
//				else {
//					OWLDataMinCardinality owl = (OWLDataMinCardinality) clsExp; 
//					number = owl.getCardinality(); 
//					prop = owl.getProperty(); 
//					filler = owl.getFiller(); 
//				}
//				
//				Role r = AtomicRole.create(((OWLDataProperty) prop).getIRI().toString());
//				
//				LiteralConcept c = AtomicConcept.create(((OWLClass) filler).getIRI().toString());
//				AtomicConcept ac = getLeftAuxiliaryConcept(AtLeastConcept.create(number, r, c), false);
//				
//				m_normClauses.add(exists_r_C_implies_A(number, r, c, ac)); 
//				newBodyAtoms[index] = Atom.create(ac, X);
//			}
			else if (clsExp instanceof OWLObjectHasSelf) {
				OWLObjectPropertyExpression prop = ((OWLObjectHasSelf) clsExp).getProperty();
				AtomicRole r; 
				if (prop instanceof OWLObjectProperty) 
					r = AtomicRole.create(((OWLObjectProperty) prop).getIRI().toString());
				else  
					r = AtomicRole.create(((OWLObjectProperty) (((OWLObjectInverseOf) prop).getInverse())).getIRI().toString());
				newBodyAtoms[index] = Atom.create(r, X, X);  
			}
			else if (clsExp instanceof OWLDataHasValue) {
				OWLDataPropertyExpression prop = ((OWLDataHasValue) clsExp).getProperty(); 
				AtomicRole r = AtomicRole.create(((OWLDataProperty) prop).getIRI().toString());
				OWLLiteral l =  ((OWLDataHasValue) clsExp).getValue();
				if (l.getDatatype().toStringID().equals(Namespace.RDF_PLAIN_LITERAL))
					newBodyAtoms[index] = Atom.create(r, X, Constant.create(l.getLiteral() + "@" + l.getLang(), Namespace.RDF_PLAIN_LITERAL));  
				else 
					newBodyAtoms[index] = Atom.create(r, X, Constant.create(l.getLiteral(), l.getDatatype().toStringID()));  
			}
			else { 
				newBodyAtoms[index] = null;
				Utility.logError("counld not translate OWLClassExpression: " + clsExp + " in " + clause);
			}
			++index; 
		}
		
		m_normClauses.add(DLClause.create(clause.getHeadAtoms(), newBodyAtoms));
	}
	
	private static final Variable X = Variable.create("X"), Y = Variable.create("Y");
	
	private DLClause exists_r_C_implies_A(int n, Role r, LiteralConcept c, AtomicConcept a) {
		Variable[] Ys = new Variable[n]; 
		if (n == 1) Ys[0] = Y; 
		else 
			for (int i = 0; i < n; ++i)
				Ys[i] = Variable.create("Y" + (i + 1));
		Collection<Atom> bodyAtoms = new LinkedList<Atom>(); 
		
		for (int i = 0; i < n; ++i) {
			Atom rxy = r instanceof AtomicRole ? 
					Atom.create(((AtomicRole) r), X, Ys[i]) : 
						Atom.create(((InverseRole) r).getInverseOf(), Ys[i], X);
			bodyAtoms.add(rxy); 
			if (!c.equals(AtomicConcept.THING))
				bodyAtoms.add(Atom.create((AtomicConcept) c, Ys[i]));
		}
		
		for (int i = 0; i < n; ++i)
			for (int j = i + 1; j < n; ++j)
				bodyAtoms.add(Atom.create(Inequality.INSTANCE, Ys[i], Ys[j])); 
		
		return DLClause.create(new Atom[] {Atom.create(a, X)}, bodyAtoms.toArray(new Atom[0])); 
	}
	
	private Map<AtLeastConcept, AtomicConcept> leftAuxiliaryConcept = new HashMap<AtLeastConcept, AtomicConcept>(); 
	private Map<AtomicConcept, AtLeastConcept> leftAuxiliaryConcept_inv = new HashMap<AtomicConcept, AtLeastConcept>();
	
	public static final String auxiliaryConceptPrefix = Namespace.PAGODA_AUX + "concept_"; 

	private static String getName(String iri) {
		int index = iri.lastIndexOf("#"); 
		if (index != -1) return iri.substring(index + 1); 
		index = iri.lastIndexOf("/"); 
		if (index != -1) return iri.substring(index + 1); 
		return iri;
	}
	
	private AtomicConcept getRightAuxiliaryConcept(AtLeastConcept alc, Individual... individuals) {
		String iri = getAuxiliaryConcept4Disjunct(alc, individuals);
		rightAuxiliaryConcept.put(iri, alc); 
		return AtomicConcept.create(iri); 
	}
	
	public static String getAuxiliaryConcept4Disjunct(AtLeastConcept alc, Individual... individuals) {
		Role r = alc.getOnRole(); 
		LiteralConcept c = alc.getToConcept();
		StringBuilder builder = new StringBuilder(auxiliaryConceptPrefix); 
		if (r instanceof AtomicRole)
			builder.append(getName(((AtomicRole) r).getIRI()));
		else 
			builder.append(getName(((InverseRole) r).getInverseOf().getIRI())).append("_inv");
		
		if (alc.getNumber() > 1)
			builder.append("_").append(alc.getNumber()); 
			
		if (c instanceof AtomicConcept) {
			if (!c.equals(AtomicConcept.THING))
				builder.append("_").append(getName(((AtomicConcept) c).getIRI())); 
		}
		else 
			builder.append("_").append(getName((OverApproxExist.getNegationConcept(((AtomicNegationConcept) c).getNegatedAtomicConcept()).getIRI())));
		
		if (individuals.length > 1)
			builder.append("_").append(getName(individuals[0].getIRI()));
		
		builder.append("_exist");
		
		return builder.toString(); 
	}

	public AtomicConcept getLeftAuxiliaryConcept(AtLeastConcept key, boolean available) {
//		AtLeastConcept key = AtLeastConcept.create(1, r, c); 
		AtomicConcept value = null; 
		if ((value = leftAuxiliaryConcept.get(key)) != null);  
		else if (!available) {
			value = AtomicConcept.create(getAuxiliaryConcept4Disjunct(key));
			leftAuxiliaryConcept.put(key, value); 
			leftAuxiliaryConcept_inv.put(value, key);
		}
		return value; 
	}

	public AtLeastConcept getLeftAtLeastConcept(AtomicConcept value) {
		return leftAuxiliaryConcept_inv.get(value); 
	}
	
	Map<String, AtLeastConcept> rightAuxiliaryConcept = new HashMap<String, AtLeastConcept>(); 

	public AtLeastConcept getRightAtLeastConcept(AtomicConcept p) {
		return rightAuxiliaryConcept.get(p.getIRI());
	}
	
	public DLClause getOriginalClause(DLClause clause) {
		DLClause original = exist2original.get(clause); 
		if (original == null) return clause; 
		else return original;   
	}
}
