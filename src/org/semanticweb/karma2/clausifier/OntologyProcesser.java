package org.semanticweb.karma2.clausifier;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.semanticweb.HermiT.model.Atom;
import org.semanticweb.HermiT.model.AtomicConcept;
import org.semanticweb.HermiT.model.AtomicRole;
import org.semanticweb.HermiT.model.DLClause;
import org.semanticweb.HermiT.model.Individual;
import org.semanticweb.HermiT.model.Role;
import org.semanticweb.HermiT.model.Term;
import org.semanticweb.HermiT.model.Variable;
import org.semanticweb.HermiT.structural.BuiltInPropertyManager;
import org.semanticweb.HermiT.structural.OWLAxioms;
import org.semanticweb.HermiT.structural.OWLAxiomsExpressivity;
import org.semanticweb.HermiT.structural.OWLNormalization;
import org.semanticweb.HermiT.structural.ObjectPropertyInclusionManager;
import org.semanticweb.karma2.exception.IllegalInputOntologyException;
import org.semanticweb.karma2.model.Equality;
import org.semanticweb.karma2.profile.ELHOProfile;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassAssertionAxiom;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLClassExpressionVisitor;
import org.semanticweb.owlapi.model.OWLDataAllValuesFrom;
import org.semanticweb.owlapi.model.OWLDataExactCardinality;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLDataHasValue;
import org.semanticweb.owlapi.model.OWLDataMaxCardinality;
import org.semanticweb.owlapi.model.OWLDataMinCardinality;
import org.semanticweb.owlapi.model.OWLDataProperty;
import org.semanticweb.owlapi.model.OWLDataPropertyAssertionAxiom;
import org.semanticweb.owlapi.model.OWLDataPropertyExpression;
import org.semanticweb.owlapi.model.OWLDataSomeValuesFrom;
import org.semanticweb.owlapi.model.OWLDifferentIndividualsAxiom;
import org.semanticweb.owlapi.model.OWLIndividual;
import org.semanticweb.owlapi.model.OWLIndividualAxiom;
import org.semanticweb.owlapi.model.OWLNegativeDataPropertyAssertionAxiom;
import org.semanticweb.owlapi.model.OWLNegativeObjectPropertyAssertionAxiom;
import org.semanticweb.owlapi.model.OWLObjectAllValuesFrom;
import org.semanticweb.owlapi.model.OWLObjectComplementOf;
import org.semanticweb.owlapi.model.OWLObjectExactCardinality;
import org.semanticweb.owlapi.model.OWLObjectHasSelf;
import org.semanticweb.owlapi.model.OWLObjectHasValue;
import org.semanticweb.owlapi.model.OWLObjectIntersectionOf;
import org.semanticweb.owlapi.model.OWLObjectInverseOf;
import org.semanticweb.owlapi.model.OWLObjectMaxCardinality;
import org.semanticweb.owlapi.model.OWLObjectMinCardinality;
import org.semanticweb.owlapi.model.OWLObjectOneOf;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.model.OWLObjectPropertyAssertionAxiom;
import org.semanticweb.owlapi.model.OWLObjectPropertyExpression;
import org.semanticweb.owlapi.model.OWLObjectSomeValuesFrom;
import org.semanticweb.owlapi.model.OWLObjectUnionOf;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLSameIndividualAxiom;
import org.semanticweb.owlapi.profiles.OWLProfileReport;
import org.semanticweb.owlapi.util.OWLAxiomVisitorAdapter;

import uk.ac.ox.cs.pagoda.util.Utility;

public class OntologyProcesser {


	protected static final Variable X=Variable.create("?X");
    protected static final Variable Y=Variable.create("?Y");
    protected static final Variable Z=Variable.create("?Z");
    
    
    public static void  transformOntology(OWLOntology root, File dataFile, File ruleFile) throws IllegalInputOntologyException {
		ELHOProfile profile = new ELHOProfile();
		OWLProfileReport report = profile.checkOntology(root);
		if (!report.isInProfile()) {
			Utility.logError(report.toString());
			throw new IllegalInputOntologyException("the ontology is not ELHO");
		}
		OntologyProcesser processer = new OntologyProcesser();
		processer.preprocessAndClausify(root, dataFile, ruleFile);
	}
    

    private void preprocessAndClausify(OWLOntology rootOntology, File dataFile, File ruleFile) {
        OWLDataFactory factory=rootOntology.getOWLOntologyManager().getOWLDataFactory();
        String ontologyIRI=rootOntology.getOntologyID().getDefaultDocumentIRI()==null ? "urn:hermit:kb" : rootOntology.getOntologyID().getDefaultDocumentIRI().toString();
        Collection<OWLOntology> importClosure=rootOntology.getImportsClosure();
        OWLAxioms axioms=new OWLAxioms();
        OWLNormalization normalization=new OWLNormalization(factory,axioms,0);
        for (OWLOntology ontology : importClosure) {
            normalization.processOntology(ontology);
        }
        BuiltInPropertyManager builtInPropertyManager=new BuiltInPropertyManager(factory);
        builtInPropertyManager.axiomatizeBuiltInPropertiesAsNeeded(axioms);
        ObjectPropertyInclusionManager objectPropertyInclusionManager=new ObjectPropertyInclusionManager(axioms);
        objectPropertyInclusionManager.rewriteAxioms(factory,axioms,0);
        OWLAxiomsExpressivity axiomsExpressivity=new OWLAxiomsExpressivity(axioms);
        clausify(factory,ontologyIRI,axioms,axiomsExpressivity, dataFile,ruleFile);
        writeTopRules(rootOntology.getClassesInSignature(), rootOntology.getObjectPropertiesInSignature(),  ruleFile);
        
}
    
    
    private void writeTopRules(Set<OWLClass> classes, Set<OWLObjectProperty> properties,  File ruleFile) {
    	PrintWriter writer = null;
    	try {
			writer = new PrintWriter(new BufferedWriter(new FileWriter(ruleFile, true)));
			for (OWLClass cls : classes) {
				writer.println("<http://www.w3.org/2002/07/owl#Thing>(?X) :- <" + cls.toStringID() + ">(?X).");
			}
			
			for (OWLObjectProperty prop : properties) {
				writer.println("<http://www.w3.org/2002/07/owl#Thing>(?X) :- <" + prop.toStringID() + ">(?X,?Y).");
				writer.println("<http://www.w3.org/2002/07/owl#Thing>(?Y) :- <" + prop.toStringID() + ">(?X,?Y).");
			}

		}catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally{
			writer.close();
			classes.clear();
			properties.clear();
		}
    	
    }
    
    private void writeDataFile(Set<Atom> positiveFacts,  File dataFile) {
    	PrintWriter writer = null;
    	try {
			writer = new PrintWriter(dataFile);
			for (Atom a: positiveFacts) {
				if (a.getArity() == 1) {
					writer.println(a.getArgument(0)+ " <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> " + a.getDLPredicate() + " . ");
				}
				
				if (a.getArity() == 2) {
					writer.println(a.getArgument(0)+ "   " + a.getDLPredicate() + "  "+  a.getArgument(1) + " . ");
				}
			}
		}catch (FileNotFoundException e) {
			e.printStackTrace();
		} finally{
			writer.close();
			positiveFacts.clear();
			positiveFacts = null;
		}
    	
    }
    
    
    private void writeRules(Set<DLClause> clauses,  File ruleFile) {
    	PrintWriter writer = null;
    	boolean first; 
    	Atom emptyHeadAtom = Atom.create(AtomicConcept.NOTHING, X); 
    	try {
			writer = new PrintWriter(ruleFile);
			for (DLClause clause : clauses) {
				Atom headAtom = clause.getHeadLength() > 0 ? clause.getHeadAtom(0) : emptyHeadAtom;
				writer.print(headAtom + " :- ");
				first = true; 
				for (Atom bodyAtom : clause.getBodyAtoms()) 
					if (first) {
						writer.print( bodyAtom);
						first = false; 
					}
					else 
						writer.print( ", " + bodyAtom);

				writer.println(" .");
			}
			writer.println("<http://www.w3.org/2002/07/owl#sameas>(?X,?Z) :- <http://www.w3.org/2002/07/owl#sameas>(?X,?Y), <http://www.w3.org/2002/07/owl#sameas>(?Y,?Z) .");
			writer.println("<http://www.w3.org/2002/07/owl#sameas>(?Y,?X) :- <http://www.w3.org/2002/07/owl#sameas>(?X,?Y) .");

		}catch (FileNotFoundException e) {
			e.printStackTrace();
		} finally{
			writer.close();
			clauses.clear();
			clauses = null;
		}
    	
    }
    
    
	public void clausify(OWLDataFactory factory,String ontologyIRI,OWLAxioms axioms,OWLAxiomsExpressivity axiomsExpressivity, File dataFile, File ruleFile) {
        Set<DLClause> dlClauses=new LinkedHashSet<DLClause>();
        Set<Atom> positiveFacts=new HashSet<Atom>();
        for (OWLObjectPropertyExpression[] inclusion : axioms.m_simpleObjectPropertyInclusions) {
            Atom subRoleAtom=getRoleAtom(inclusion[0],X,Y);
            Atom superRoleAtom=getRoleAtom(inclusion[1],X,Y);
            DLClause dlClause=DLClause.create(new Atom[] { superRoleAtom },new Atom[] { subRoleAtom });
            dlClauses.add(dlClause);
        }
        NormalizedDatalogAxiomClausifier clausifier=new NormalizedDatalogAxiomClausifier(positiveFacts,factory);
        for (OWLClassExpression[] inclusion : axioms.m_conceptInclusions) {
            for (OWLClassExpression description : inclusion)
                description.accept(clausifier);
            for(DLClause dlClause :clausifier.getDLClause())
            	dlClauses.add(dlClause.getSafeVersion(AtomicConcept.THING));
        }
        DatalogFactClausifier factClausifier=new DatalogFactClausifier(positiveFacts);
        for (OWLIndividualAxiom fact : axioms.m_facts)
            fact.accept(factClausifier);
        writeDataFile(positiveFacts, dataFile);
        writeRules(dlClauses, ruleFile);
    }
	
	protected static AtomicRole getAtomicRole(OWLDataPropertyExpression dataPropertyExpression) {
        return AtomicRole.create(((OWLDataProperty)dataPropertyExpression).getIRI().toString());
    }
    protected static Atom getRoleAtom(OWLObjectPropertyExpression objectProperty,Term first,Term second) {
        objectProperty=objectProperty.getSimplified();
        if (!objectProperty.isAnonymous()) {
            AtomicRole role=AtomicRole.create(objectProperty.asOWLObjectProperty().getIRI().toString());
            return Atom.create(role,first,second);
        }
        else if (objectProperty.isAnonymous()) {
            OWLObjectProperty internalObjectProperty=objectProperty.getNamedProperty();
            AtomicRole role=AtomicRole.create(internalObjectProperty.getIRI().toString());
            return Atom.create(role,second,first);
        }
        else
            throw new IllegalStateException("Internal error: unsupported type of object property!");
    }
    
    
    protected static Role getRole(OWLObjectPropertyExpression objectPropertyExpression) {
        objectPropertyExpression=objectPropertyExpression.getSimplified();
        if (objectPropertyExpression instanceof OWLObjectProperty)
            return AtomicRole.create(((OWLObjectProperty)objectPropertyExpression).getIRI().toString());
        else if (objectPropertyExpression instanceof OWLObjectInverseOf) {
            OWLObjectPropertyExpression internal=((OWLObjectInverseOf)objectPropertyExpression).getInverse();
            if (!(internal instanceof OWLObjectProperty))
                throw new IllegalStateException("Internal error: invalid normal form.");
            return AtomicRole.create(((OWLObjectProperty)internal).getIRI().toString()).getInverse();
        }
        else
            throw new IllegalStateException("Internal error: invalid normal form.");
    }
    
    protected static Atom getRoleAtom(OWLDataPropertyExpression dataProperty,Term first,Term second) {
        if (dataProperty instanceof OWLDataProperty) {
            AtomicRole property=AtomicRole.create(((OWLDataProperty)dataProperty).getIRI().toString());
            return Atom.create(property,first,second);
        }
        else
            throw new IllegalStateException("Internal error: unsupported type of data property!");
    }
    protected static Individual getIndividual(OWLIndividual individual) {
        if (individual.isAnonymous())
            return Individual.createAnonymous(individual.asOWLAnonymousIndividual().getID().toString());
        else
            return Individual.create(individual.asOWLNamedIndividual().getIRI().toString());
    }
	
	
	protected static class NormalizedDatalogAxiomClausifier implements OWLClassExpressionVisitor {
        protected final List<Atom> m_headAtoms;
        protected final List<Atom> m_bodyAtoms;
        protected final List<Atom> m_auxAtoms;
        protected final Set<Atom> m_positiveFacts;
        protected final OWLDataFactory m_factory;
        protected int m_yIndex;
        protected int m_zIndex;


        public NormalizedDatalogAxiomClausifier(Set<Atom> positiveFacts,OWLDataFactory factory) {
            m_headAtoms=new ArrayList<Atom>();
            m_bodyAtoms=new ArrayList<Atom>();
            m_auxAtoms=new ArrayList<Atom>();
            m_positiveFacts=positiveFacts;
            m_factory=factory;
        }
        
        
        
        protected Set<DLClause> getDLClause() {
        	
        	Set<DLClause> clauses = new HashSet<DLClause>();
            Atom[] headAtoms=new Atom[m_headAtoms.size()];
            m_headAtoms.toArray(headAtoms);
            Atom[] bodyAtoms=new Atom[m_bodyAtoms.size()];
            m_bodyAtoms.toArray(bodyAtoms);
            clauses.add(DLClause.create(headAtoms,bodyAtoms));
            if (!m_auxAtoms.isEmpty()) {
            	Atom[] auxAtoms=new Atom[m_auxAtoms.size()];
            	m_auxAtoms.toArray(auxAtoms);
            	clauses.add(DLClause.create(auxAtoms,bodyAtoms));
            }
            m_headAtoms.clear();
            m_bodyAtoms.clear();
            m_auxAtoms.clear();
            m_yIndex=0;
            m_zIndex=0;
            return clauses;
        }
        protected void ensureYNotZero() {
            if (m_yIndex==0)
                m_yIndex++;
        }
        protected Variable nextY() {
            Variable result;
            if (m_yIndex==0)
                result=Y;
            else
                result=Variable.create("?Y"+m_yIndex);
            m_yIndex++;
            return result;
        }
        protected Variable nextZ() {
            Variable result;
            if (m_zIndex==0)
                result=Z;
            else
                result=Variable.create("?Z"+m_zIndex);
            m_zIndex++;
            return result;
        }
        
        


        private void existentialRestriction(OWLObjectProperty prop, OWLClassExpression filler) {
        	if (filler.isAnonymous())
        		throw new IllegalStateException("Internal error: invalid normal form.");
        	String propertyID = prop.asOWLObjectProperty().toStringID();
        	String propertyShortID = propertyID.substring(propertyID.indexOf('#')+1);
        	String classID = filler.asOWLClass().toStringID();
        	String classShortID = classID.substring(classID.indexOf('#')+1);
        	Individual auxInd = Individual.create("http://www.cs.ox.ac.uk/KARMA/anonymous#:"+propertyShortID + "-"+classShortID);
            m_headAtoms.add(Atom.create(AtomicRole.create(propertyID), X, auxInd));
            m_auxAtoms.add(Atom.create(AtomicConcept.create(classID), auxInd));
        }
        
        
        // Various types of descriptions

        public void visit(OWLClass object) {
        	
            m_headAtoms.add(Atom.create(AtomicConcept.create(object.getIRI().toString()),X));
        }
        
        
        public void visit(OWLObjectIntersectionOf object) {
            throw new IllegalStateException("Internal error: invalid normal form.");
        }
        public void visit(OWLObjectUnionOf object) {
            throw new IllegalStateException("Internal error: invalid normal form.");
        }
        
        
        public void visit(OWLObjectComplementOf object) {
            OWLClassExpression description=object.getOperand();
            if (description instanceof OWLObjectHasSelf) {
                OWLObjectPropertyExpression objectProperty=((OWLObjectHasSelf)description).getProperty();
                Atom roleAtom=getRoleAtom(objectProperty,X,X);
                m_bodyAtoms.add(roleAtom);
                throw new IllegalStateException("Internal error: invalid normal form.");
            }
            else if (description instanceof OWLObjectOneOf && ((OWLObjectOneOf)description).getIndividuals().size()==1) {
                OWLIndividual individual=((OWLObjectOneOf)description).getIndividuals().iterator().next();
                m_bodyAtoms.add(Atom.create(Equality.INSTANCE,X, getIndividual(individual)));
            }
            else if (!(description instanceof OWLClass))
                throw new IllegalStateException("Internal error: invalid normal form.");
            else
                m_bodyAtoms.add(Atom.create(AtomicConcept.create(((OWLClass)description).getIRI().toString()),X));
        }
        
        
        
        public void visit(OWLObjectOneOf object) {
            for (OWLIndividual individual : object.getIndividuals()) {
                m_headAtoms.add(Atom.create(Equality.INSTANCE,X,getIndividual(individual)));
            }
        }
        
       
        
        
        public void visit(OWLObjectSomeValuesFrom object) {
        	
            OWLClassExpression filler=object.getFiller();
            if (filler instanceof OWLObjectOneOf) {
                for (OWLIndividual individual : ((OWLObjectOneOf)filler).getIndividuals()) {
                    m_headAtoms.add(getRoleAtom(object.getProperty(),X,getIndividual(individual)));
                }
            } else {
            	if (filler.isAnonymous())
            		throw new IllegalStateException("Internal error: invalid normal form.");
            	existentialRestriction(object.getProperty().asOWLObjectProperty(), filler);
            }
        }
        
        
        public void visit(OWLObjectAllValuesFrom object) {
            Variable y=nextY();
            m_bodyAtoms.add(getRoleAtom(object.getProperty(),X,y));
            OWLClassExpression filler=object.getFiller();

            if (filler instanceof OWLClass) {
                AtomicConcept atomicConcept=AtomicConcept.create(((OWLClass)filler).getIRI().toString());
                if (!atomicConcept.isAlwaysFalse())
                    m_headAtoms.add(Atom.create(atomicConcept,y));
            }
            else if (filler instanceof OWLObjectOneOf) {
                for (OWLIndividual individual : ((OWLObjectOneOf)filler).getIndividuals()) {
                    m_headAtoms.add(Atom.create(Equality.INSTANCE,y,getIndividual(individual)));
                }
            }
            else if (filler instanceof OWLObjectComplementOf) {
                OWLClassExpression operand=((OWLObjectComplementOf)filler).getOperand();
                if (operand instanceof OWLClass) {
                    AtomicConcept internalAtomicConcept=AtomicConcept.create(((OWLClass)operand).getIRI().toString());
                    if (!internalAtomicConcept.isAlwaysTrue())
                        m_bodyAtoms.add(Atom.create(internalAtomicConcept,y));
                }
                else if (operand instanceof OWLObjectOneOf && ((OWLObjectOneOf)operand).getIndividuals().size()==1) {
                    OWLIndividual individual=((OWLObjectOneOf)operand).getIndividuals().iterator().next();
                    m_bodyAtoms.add(Atom.create(Equality.INSTANCE,y,getIndividual(individual)));
                }
                else
                    throw new IllegalStateException("Internal error: invalid normal form.");
            }
            else
                throw new IllegalStateException("Internal error: invalid normal form.");
        }
        public void visit(OWLObjectHasValue object) {
            throw new IllegalStateException("Internal error: invalid normal form.");
        }
        public void visit(OWLObjectHasSelf object) {
        	throw new IllegalStateException("Internal error: invalid normal form.");
        }
        
        public void visit(OWLObjectMinCardinality object) {
        	if (object.getCardinality() != 1)
        		throw new IllegalStateException("Internal error: invalid normal form.");
            existentialRestriction(object.getProperty().asOWLObjectProperty(), object.getFiller());
        }
        public void visit(OWLObjectMaxCardinality object) {
        	throw new IllegalStateException("Internal error: invalid normal form.");
//            int cardinality=object.getCardinality();
//            OWLObjectPropertyExpression onObjectProperty=object.getProperty();
//            OWLClassExpression filler=object.getFiller();
//            ensureYNotZero();
//            boolean isPositive;
//            AtomicConcept atomicConcept;
//            if (filler instanceof OWLClass) {
//                isPositive=true;
//                atomicConcept=AtomicConcept.create(((OWLClass)filler).getIRI().toString());
//                if (atomicConcept.isAlwaysTrue())
//                    atomicConcept=null;
//            }
//            else if (filler instanceof OWLObjectComplementOf) {
//                OWLClassExpression internal=((OWLObjectComplementOf)filler).getOperand();
//                if (!(internal instanceof OWLClass))
//                    throw new IllegalStateException("Internal error: Invalid ontology normal form.");
//                isPositive=false;
//                atomicConcept=AtomicConcept.create(((OWLClass)internal).getIRI().toString());
//                if (atomicConcept.isAlwaysFalse())
//                    atomicConcept=null;
//            }
//            else
//                throw new IllegalStateException("Internal error: Invalid ontology normal form.");
//            Role onRole=getRole(onObjectProperty);
//            LiteralConcept toConcept=getLiteralConcept(filler);
//            AnnotatedEquality annotatedEquality=AnnotatedEquality.create(cardinality,onRole,toConcept);
//            Variable[] yVars=new Variable[cardinality+1];
//            for (int i=0;i<yVars.length;i++) {
//                yVars[i]=nextY();
//                m_bodyAtoms.add(getRoleAtom(onObjectProperty,X,yVars[i]));
//                if (atomicConcept!=null) {
//                    Atom atom=Atom.create(atomicConcept,yVars[i]);
//                    if (isPositive)
//                        m_bodyAtoms.add(atom);
//                    else
//                        m_headAtoms.add(atom);
//                }
//            }
//            // Node ID comparisons are not needed in case of functionality axioms,
//            // as the effect of these is simulated by the way in which the rules are applied.
//            if (yVars.length>2) {
//                for (int i=0;i<yVars.length-1;i++)
//                    m_bodyAtoms.add(Atom.create(NodeIDLessEqualThan.INSTANCE,yVars[i],yVars[i+1]));
//                m_bodyAtoms.add(Atom.create(NodeIDsAscendingOrEqual.create(yVars.length),yVars));
//            }
//            for (int i=0;i<yVars.length;i++)
//                for (int j=i+1;j<yVars.length;j++)
//                    m_headAtoms.add(Atom.create(annotatedEquality,yVars[i],yVars[j],X));
        }
        public void visit(OWLObjectExactCardinality object) {
            throw new IllegalStateException("Internal error: invalid normal form.");
        }
        public void visit(OWLDataSomeValuesFrom object) {
        	throw new IllegalStateException("Internal error: invalid normal form.");
        }
        public void visit(OWLDataAllValuesFrom object) {
        	throw new IllegalStateException("Internal error: invalid normal form.");
        }
        public void visit(OWLDataHasValue object) {
            throw new IllegalStateException("Internal error: Invalid normal form.");
        }
        public void visit(OWLDataMinCardinality object) {
        	throw new IllegalStateException("Internal error: invalid normal form.");
        }
        public void visit(OWLDataMaxCardinality object) {
        	throw new IllegalStateException("Internal error: invalid normal form.");
        }
        public void visit(OWLDataExactCardinality object) {
            throw new IllegalStateException("Internal error: invalid normal form.");
        }
    }

	 protected static class DatalogFactClausifier extends OWLAxiomVisitorAdapter {
	        protected final Set<Atom> m_positiveFacts;

	        public DatalogFactClausifier(Set<Atom> positiveFacts) {
	            m_positiveFacts=positiveFacts;
	        }
	        public void visit(OWLSameIndividualAxiom object) {
	            OWLIndividual[] individuals=new OWLIndividual[object.getIndividuals().size()];
	            object.getIndividuals().toArray(individuals);
	            for (int i=0;i<individuals.length-1;i++)
	                m_positiveFacts.add(Atom.create(Equality.create(),getIndividual(individuals[i]),getIndividual(individuals[i+1])));
	        }
	        public void visit(OWLDifferentIndividualsAxiom object) {
	            throw new IllegalStateException("Internal error: invalid normal form.");
	        }
	        public void visit(OWLClassAssertionAxiom object) {
	            OWLClassExpression description=object.getClassExpression();
	            if (description instanceof OWLClass) {
	                AtomicConcept atomicConcept=AtomicConcept.create(((OWLClass)description).getIRI().toString());
	                m_positiveFacts.add(Atom.create(atomicConcept,getIndividual(object.getIndividual())));
	            }
	            else if (description instanceof OWLObjectComplementOf && ((OWLObjectComplementOf)description).getOperand() instanceof OWLClass) {
	            	throw new IllegalStateException("Internal error: invalid normal form.");
	            }
	            else if (description instanceof OWLObjectHasSelf) {
	            	throw new IllegalStateException("Internal error: invalid normal form.");
	            }
	            else if (description instanceof OWLObjectComplementOf && ((OWLObjectComplementOf)description).getOperand() instanceof OWLObjectHasSelf) {
	            	throw new IllegalStateException("Internal error: invalid normal form.");
	            }
	            else
	                throw new IllegalStateException("Internal error: invalid normal form.");
	        }
	        public void visit(OWLObjectPropertyAssertionAxiom object) {
	            m_positiveFacts.add(getRoleAtom(object.getProperty(),getIndividual(object.getSubject()),getIndividual(object.getObject())));
	        }
	        public void visit(OWLNegativeObjectPropertyAssertionAxiom object) {
	        	throw new IllegalStateException("Internal error: invalid normal form.");	        }
	        public void visit(OWLDataPropertyAssertionAxiom object) {
	            
	        }
	        public void visit(OWLNegativeDataPropertyAssertionAxiom object) {
	           
	        }
	    }

}

