package uk.ac.ox.cs.data.datatype;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.Normalizer;
import java.util.HashSet;
import java.util.Set;

import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.impl.StatementImpl;
import org.openrdf.model.impl.URIImpl;
import org.openrdf.rio.RDFHandler;
import org.openrdf.rio.RDFHandlerException;
import org.openrdf.rio.RDFParseException;
import org.openrdf.rio.RDFWriter;
import org.openrdf.rio.turtle.TurtleParser;
import org.openrdf.rio.turtle.TurtleWriter;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAnnotation;
import org.semanticweb.owlapi.model.OWLAnnotationAssertionAxiom;
import org.semanticweb.owlapi.model.OWLAnnotationProperty;
import org.semanticweb.owlapi.model.OWLAnnotationPropertyDomainAxiom;
import org.semanticweb.owlapi.model.OWLAnnotationPropertyRangeAxiom;
import org.semanticweb.owlapi.model.OWLAnonymousIndividual;
import org.semanticweb.owlapi.model.OWLAsymmetricObjectPropertyAxiom;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassAssertionAxiom;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLDataAllValuesFrom;
import org.semanticweb.owlapi.model.OWLDataComplementOf;
import org.semanticweb.owlapi.model.OWLDataExactCardinality;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLDataHasValue;
import org.semanticweb.owlapi.model.OWLDataIntersectionOf;
import org.semanticweb.owlapi.model.OWLDataMaxCardinality;
import org.semanticweb.owlapi.model.OWLDataMinCardinality;
import org.semanticweb.owlapi.model.OWLDataOneOf;
import org.semanticweb.owlapi.model.OWLDataProperty;
import org.semanticweb.owlapi.model.OWLDataPropertyAssertionAxiom;
import org.semanticweb.owlapi.model.OWLDataPropertyDomainAxiom;
import org.semanticweb.owlapi.model.OWLDataPropertyExpression;
import org.semanticweb.owlapi.model.OWLDataPropertyRangeAxiom;
import org.semanticweb.owlapi.model.OWLDataRange;
import org.semanticweb.owlapi.model.OWLDataSomeValuesFrom;
import org.semanticweb.owlapi.model.OWLDataUnionOf;
import org.semanticweb.owlapi.model.OWLDatatype;
import org.semanticweb.owlapi.model.OWLDatatypeDefinitionAxiom;
import org.semanticweb.owlapi.model.OWLDatatypeRestriction;
import org.semanticweb.owlapi.model.OWLDeclarationAxiom;
import org.semanticweb.owlapi.model.OWLDifferentIndividualsAxiom;
import org.semanticweb.owlapi.model.OWLDisjointClassesAxiom;
import org.semanticweb.owlapi.model.OWLDisjointDataPropertiesAxiom;
import org.semanticweb.owlapi.model.OWLDisjointObjectPropertiesAxiom;
import org.semanticweb.owlapi.model.OWLDisjointUnionAxiom;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLEquivalentClassesAxiom;
import org.semanticweb.owlapi.model.OWLEquivalentDataPropertiesAxiom;
import org.semanticweb.owlapi.model.OWLEquivalentObjectPropertiesAxiom;
import org.semanticweb.owlapi.model.OWLFacetRestriction;
import org.semanticweb.owlapi.model.OWLFunctionalDataPropertyAxiom;
import org.semanticweb.owlapi.model.OWLFunctionalObjectPropertyAxiom;
import org.semanticweb.owlapi.model.OWLHasKeyAxiom;
import org.semanticweb.owlapi.model.OWLIndividual;
import org.semanticweb.owlapi.model.OWLInverseFunctionalObjectPropertyAxiom;
import org.semanticweb.owlapi.model.OWLInverseObjectPropertiesAxiom;
import org.semanticweb.owlapi.model.OWLIrreflexiveObjectPropertyAxiom;
import org.semanticweb.owlapi.model.OWLLiteral;
import org.semanticweb.owlapi.model.OWLNamedIndividual;
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
import org.semanticweb.owlapi.model.OWLObjectPropertyDomainAxiom;
import org.semanticweb.owlapi.model.OWLObjectPropertyExpression;
import org.semanticweb.owlapi.model.OWLObjectPropertyRangeAxiom;
import org.semanticweb.owlapi.model.OWLObjectSomeValuesFrom;
import org.semanticweb.owlapi.model.OWLObjectUnionOf;
import org.semanticweb.owlapi.model.OWLObjectVisitorEx;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.model.OWLOntologyStorageException;
import org.semanticweb.owlapi.model.OWLReflexiveObjectPropertyAxiom;
import org.semanticweb.owlapi.model.OWLSameIndividualAxiom;
import org.semanticweb.owlapi.model.OWLSubAnnotationPropertyOfAxiom;
import org.semanticweb.owlapi.model.OWLSubClassOfAxiom;
import org.semanticweb.owlapi.model.OWLSubDataPropertyOfAxiom;
import org.semanticweb.owlapi.model.OWLSubObjectPropertyOfAxiom;
import org.semanticweb.owlapi.model.OWLSubPropertyChainOfAxiom;
import org.semanticweb.owlapi.model.OWLSymmetricObjectPropertyAxiom;
import org.semanticweb.owlapi.model.OWLTransitiveObjectPropertyAxiom;
import org.semanticweb.owlapi.model.SWRLBuiltInAtom;
import org.semanticweb.owlapi.model.SWRLClassAtom;
import org.semanticweb.owlapi.model.SWRLDataPropertyAtom;
import org.semanticweb.owlapi.model.SWRLDataRangeAtom;
import org.semanticweb.owlapi.model.SWRLDifferentIndividualsAtom;
import org.semanticweb.owlapi.model.SWRLIndividualArgument;
import org.semanticweb.owlapi.model.SWRLLiteralArgument;
import org.semanticweb.owlapi.model.SWRLObjectPropertyAtom;
import org.semanticweb.owlapi.model.SWRLRule;
import org.semanticweb.owlapi.model.SWRLSameIndividualAtom;
import org.semanticweb.owlapi.model.SWRLVariable;

import uk.ac.ox.cs.data.dbpedia.DataFilterRDFHandler;
import uk.ac.ox.cs.pagoda.owl.OWLHelper;
import uk.ac.ox.cs.pagoda.util.Utility;

public class DataToObject {
	
	private static final String FLAG = "-replaced"; 
	public static final String PREFIX_LITERAL = "http://www.datatypevalue.org#"; 
	
	String m_ontoFile, m_dataFile; 
	String m_newOntoFile, m_newDataFile;
	
	Set<String> m_dataProperties = new HashSet<String>();
	String m_prefix; 

	public DataToObject(String prefix, String ontoFile, String dataFile) {
		m_prefix = prefix;
		
		m_ontoFile = ontoFile; 
		String ext = m_ontoFile.substring(m_ontoFile.lastIndexOf(".")); 
		m_newOntoFile = m_ontoFile.replace(ext, FLAG + ext);
		
		if (dataFile == null || dataFile.isEmpty()) 
			m_dataFile = m_newDataFile = null; 
		else {
			m_dataFile = dataFile;
			m_newDataFile = m_dataFile.replace(".ttl", FLAG + ".ttl");
		}
	}
	
	public static void main(String[] args) {
		DataToObject p = new DataToObject(
//				"http://dbpedia.org/ontology/", 
//				"/home/yzhou/ontologies/dbpedia/integratedOntology-all-in-one.owl", 
//				"/home/yzhou/workspace/payQ/ontologies/dbpedia/dbpedia.ttl");
		
				// for NPD dataset 
//				"http://sws.ifi.uio.no/vocab/npd-all.owl", 
//				"/home/yzhou/ontologies/npd/npd-all.owl", 
//				"/home/yzhou/ontologies/npd/data/npd-data-dump-processed.ttl");
				
				// for ChEmBL
				"http://rdf.ebi.ac.uk/terms/chembl#", 
				"/home/scratch/yzhou/ontologies/bio2rdf/chembl/cco (copy).ttl", 
				null); 
		
		p.processOntology();
		Utility.logInfo("Ontology Processing DONE.");
		
		p.processData(); 
		Utility.logInfo("Data Processing DONE.");	}
	
	public void setOutputOntologyFile(String file) {
		m_newOntoFile = file; 
	}
	
	public void setOutputDataFile(String file) {
		m_newDataFile = file; 
	}
	
	public String processData() {
		if (m_dataFile == null) 
			return null; 
		
		TurtleParser parser = new TurtleParser(); 
		TurtleWriter writer;
		try {
			writer = new TurtleWriter(new FileOutputStream(m_newDataFile));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			new File(m_newDataFile).delete(); 
			return null; 
		} 
		
		parser.setRDFHandler(new DataToObjectRDFHandler(writer, m_dataProperties));
		try {
			parser.parse(new FileInputStream(m_dataFile), m_prefix);
		} catch (RDFParseException e) {
			e.printStackTrace();
		} catch (RDFHandlerException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} 

		return m_newDataFile; 
	}
	
	public String processOntology() {
		OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
		OWLOntology newOntology, oldOntology; 
		oldOntology = OWLHelper.loadOntology(manager, m_ontoFile);
		for (OWLDataProperty property: oldOntology.getDataPropertiesInSignature())
			m_dataProperties.add(property.toStringID()); 
		
		DataToObjectVisitor visitor = new DataToObjectVisitor(manager); 
		newOntology = (OWLOntology) oldOntology.accept(visitor);
		
		try {
			manager.saveOntology(newOntology, IRI.create(new File(m_newOntoFile)));
		} catch (OWLOntologyStorageException e) {
			e.printStackTrace();
		} 
		
		return m_newOntoFile; 
	}
	
	 protected class DataToObjectVisitor implements OWLObjectVisitorEx<Object> {

	        private final OWLOntologyManager m_manager;
	        private final OWLDataFactory m_factory; 

	        public DataToObjectVisitor(OWLOntologyManager man) {
	            m_manager = man;
	            m_factory = man.getOWLDataFactory(); 
	        }
	        
	        private void reportUnsupportedFeature() {
	        	Utility.logError("Unsupported features"); 
	        }
	        
	        @Override
			public Object visit(OWLDataProperty property) {
	        	return m_factory.getOWLObjectProperty(property.getIRI()); 
	        }

	        @Override
			public Object visit(OWLObjectOneOf ce) {
	        	return ce;
	        }
	        
	        @Override
			public Object visit(OWLDataHasValue node) {
	        	return m_factory.getOWLObjectHasValue(
	        			(OWLObjectPropertyExpression) node.getProperty().accept(this), 
	        			(OWLIndividual) node.getValue().accept(this)
	        			); 
	        }
	        
	        @Override
			public Object visit(OWLDataSomeValuesFrom node) {
	        	OWLClassExpression exp = null; 
	        	try {
	        		exp =	m_factory.getOWLObjectSomeValuesFrom(
	        			(OWLObjectPropertyExpression) node.getProperty().accept(this), 
	        			(OWLClassExpression) node.getFiller().accept(this)
	        			);
	        		return exp; 
	        	} catch (Exception e) {
	        		e.printStackTrace();
	        	}
	        	return node; 
	        }
	        
	        @Override
			public Object visit(OWLDataIntersectionOf node) {
	        	Set<OWLClassExpression> exps = new HashSet<OWLClassExpression>(); 
	        	for (OWLDataRange range: node.getOperands())
	        		exps.add((OWLClassExpression) range.accept(this)); 
	            
	            return m_factory.getOWLObjectIntersectionOf(exps); 
	        }
	        
	        @Override
			public Object visit(OWLSubDataPropertyOfAxiom axiom) {
	        	return m_factory.getOWLSubObjectPropertyOfAxiom(
	        			(OWLObjectPropertyExpression) axiom.getSubProperty().accept(this), 
	        			(OWLObjectPropertyExpression) axiom.getSuperProperty().accept(this)); 
	        }
	        
	        @Override
			public Object visit(OWLEquivalentDataPropertiesAxiom axiom) {
	        	Set<OWLObjectPropertyExpression> props = new HashSet<OWLObjectPropertyExpression>();
	        	for (OWLDataPropertyExpression dataProperty: axiom.getProperties())
	        		props.add((OWLObjectPropertyExpression) dataProperty.accept(this)); 
	        	return m_factory.getOWLEquivalentObjectPropertiesAxiom(props); 
	        }
	        
	        @Override
			public Object visit(OWLTransitiveObjectPropertyAxiom axiom) {
	            return axiom;
	        }
	        
	        @Override
			public Object visit(OWLReflexiveObjectPropertyAxiom axiom) {
	            return axiom;
	        }
	        
	        @Override
			public Object visit(OWLDataPropertyDomainAxiom axiom) {
	            return m_factory.getOWLObjectPropertyDomainAxiom(
	            		(OWLObjectPropertyExpression) axiom.getProperty().accept(this), 
	            		(OWLClassExpression) axiom.getDomain().accept(this)
	            		);
	        }
	        
	        @Override
			public Object visit(OWLDataPropertyRangeAxiom axiom) {
	        	return m_factory.getOWLObjectPropertyRangeAxiom(
	        			(OWLObjectPropertyExpression) axiom.getProperty().accept(this), 
	        			(OWLClassExpression) axiom.getRange().accept(this)
	        			); 
	        }
	        
	        @Override
			public Object visit(OWLDataPropertyAssertionAxiom axiom) {
	            return m_factory.getOWLObjectPropertyAssertionAxiom(
	            		(OWLObjectPropertyExpression) axiom.getProperty().accept(this), 
	            		axiom.getSubject(), 
	            		(OWLIndividual) axiom.getObject().accept(this)
	            		);
	        }
	        
	        @Override
			public Object visit(OWLNegativeDataPropertyAssertionAxiom axiom) {
	        	return m_factory.getOWLNegativeObjectPropertyAssertionAxiom(
	        			(OWLObjectPropertyExpression) axiom.getProperty().accept(this), 
	        			axiom.getSubject(), 
	        			(OWLIndividual) axiom.getObject().accept(this)
	        			);
	        }
	        
	        @Override
			public Object visit(OWLNegativeObjectPropertyAssertionAxiom axiom) {
	            return axiom;
	        }
	        
	        @Override
			public Object visit(OWLFunctionalDataPropertyAxiom axiom) {
	            return m_factory.getOWLFunctionalObjectPropertyAxiom(
	            		(OWLObjectPropertyExpression) axiom.getProperty().accept(this)
	            		); 	            		
	        }
	        
	        @Override
			public Object visit(OWLHasKeyAxiom axiom) {
	        	Set<OWLObjectPropertyExpression> props = new HashSet<OWLObjectPropertyExpression>(axiom.getObjectPropertyExpressions());
	        	for (OWLDataPropertyExpression dataProperty: axiom.getDataPropertyExpressions())
	        		props.add((OWLObjectPropertyExpression) dataProperty.accept(this)); 
	            return m_factory.getOWLHasKeyAxiom(
	            		(OWLClassExpression) axiom.getClassExpression().accept(this), 
	            		props
	            		);
	        }
	       
	        
	        @Override
			public Object visit(OWLObjectHasSelf node) {
	        	return node; 
	        }

	        
	        @Override
			public Object visit(OWLDataOneOf node) {
	        	Set<OWLIndividual> individuals = new HashSet<OWLIndividual>(); 
	            for (OWLLiteral literal: node.getValues())
	            	individuals.add((OWLIndividual) literal.accept(this)); 
	            return m_factory.getOWLObjectOneOf(individuals);
	        }

	        

	        @Override
			public Object visit(OWLSubPropertyChainOfAxiom axiom) {
	            return axiom;
	        }

	        @Override
			public Object visit(OWLOntology ontology) {
	        	OWLOntology newOntology = null;
				try {
					if (ontology.getOntologyID().getOntologyIRI() != null) {
						String ontologyIRI = ontology.getOntologyID().getOntologyIRI().toString();
						if (ontologyIRI.contains(".owl"))
							ontologyIRI = ontologyIRI.replace(".owl", FLAG + ".owl");
						else 
							ontologyIRI += FLAG; 
						
						newOntology = m_manager.createOntology(IRI.create(ontologyIRI));
					}
					else newOntology = m_manager.createOntology();
					
					for (OWLOntology onto: ontology.getImportsClosure())
			            for (OWLAxiom axiom: onto.getAxioms()) {
			            	OWLAxiom newAxiom = (OWLAxiom) axiom.accept(this);
			            	m_manager.addAxiom(newOntology, newAxiom);
			            }
		            
				} catch (OWLOntologyCreationException e) {
					e.printStackTrace();
				}
				
	            return newOntology;
	        }

			@Override
			public Object visit(OWLSubClassOfAxiom axiom) {
				return m_factory.getOWLSubClassOfAxiom(
						(OWLClassExpression) axiom.getSubClass().accept(this), 
						(OWLClassExpression) axiom.getSuperClass().accept(this)
						);  
			}

			@Override
			public Object visit(OWLAsymmetricObjectPropertyAxiom axiom) {
				return axiom; 
			}

			@Override
			public Object visit(OWLDisjointClassesAxiom axiom) {
				Set<OWLClassExpression> exps = new HashSet<OWLClassExpression>(); 
				for (OWLClassExpression exp: axiom.getClassExpressions())
					exps.add((OWLClassExpression) exp.accept(this)); 
				return m_factory.getOWLDisjointClassesAxiom(exps);
			}

			@Override
			public Object visit(OWLObjectPropertyDomainAxiom axiom) {
				return m_factory.getOWLObjectPropertyDomainAxiom(
						axiom.getProperty(), 
						(OWLClassExpression) axiom.getDomain().accept(this)
						); 
			}

			@Override
			public Object visit(OWLEquivalentObjectPropertiesAxiom axiom) {
				return axiom;
			}

			@Override
			public Object visit(OWLDifferentIndividualsAxiom axiom) {
				return axiom;
			}

			@Override
			public Object visit(OWLDisjointDataPropertiesAxiom axiom) {
				Set<OWLObjectPropertyExpression> props = new HashSet<OWLObjectPropertyExpression>(); 
				for (OWLDataPropertyExpression dataProperty: axiom.getProperties())
					props.add((OWLObjectPropertyExpression) dataProperty.accept(this)); 
				return m_factory.getOWLDisjointObjectPropertiesAxiom(props);
			}

			@Override
			public Object visit(OWLDisjointObjectPropertiesAxiom axiom) {
				return axiom;
			}

			@Override
			public Object visit(OWLObjectPropertyRangeAxiom axiom) {
				return axiom;
			}

			@Override
			public Object visit(OWLObjectPropertyAssertionAxiom axiom) {
				return axiom;
			}

			@Override
			public Object visit(OWLFunctionalObjectPropertyAxiom axiom) {
				return axiom;
			}

			@Override
			public Object visit(OWLSubObjectPropertyOfAxiom axiom) {
				return axiom;
			}

			@Override
			public Object visit(OWLDisjointUnionAxiom axiom) {
				Set<OWLClassExpression> exps = new HashSet<OWLClassExpression>(); 
				for (OWLClassExpression exp: axiom.getClassExpressions()) 
					exps.add((OWLClassExpression) exp.accept(this)); 
				return m_factory.getOWLDisjointUnionAxiom((OWLClass) axiom.getOWLClass().accept(this), exps);
			}

			@Override
			public Object visit(OWLDeclarationAxiom axiom) {
				OWLEntity entity = axiom.getEntity();
				if (entity instanceof OWLDataProperty)
					return m_factory.getOWLDeclarationAxiom(m_factory.getOWLObjectProperty(entity.getIRI()));
				else if (entity instanceof OWLDatatype)
					return m_factory.getOWLDeclarationAxiom((OWLClass) entity.accept(this)); 
				else 
					return axiom;
			}

			@Override
			public Object visit(OWLAnnotationAssertionAxiom axiom) {
				return axiom;
			}

			@Override
			public Object visit(OWLSymmetricObjectPropertyAxiom axiom) {
				return axiom;
			}

			@Override
			public Object visit(OWLClassAssertionAxiom axiom) {
				return m_factory.getOWLClassAssertionAxiom(
						(OWLClassExpression) axiom.getClassExpression().accept(this), 
						axiom.getIndividual()); 
			}

			@Override
			public Object visit(OWLEquivalentClassesAxiom axiom) {
				Set<OWLClassExpression> exps = new HashSet<OWLClassExpression>(); 
				for (OWLClassExpression exp: axiom.getClassExpressions())
					exps.add((OWLClassExpression) exp.accept(this)); 
				return m_factory.getOWLEquivalentClassesAxiom(exps); 
			}

			@Override
			public Object visit(OWLIrreflexiveObjectPropertyAxiom axiom) {
				return axiom;
			}

			@Override
			public Object visit(OWLInverseFunctionalObjectPropertyAxiom axiom) {
				return axiom;
			}

			@Override
			public Object visit(OWLSameIndividualAxiom axiom) {
				return axiom;
			}

			@Override
			public Object visit(OWLInverseObjectPropertiesAxiom axiom) {
				return axiom;
			}

			@Override
			public Object visit(OWLDatatypeDefinitionAxiom axiom) {
				reportUnsupportedFeature();
				return null; 
 			}

			@Override
			public Object visit(SWRLRule rule) {
				reportUnsupportedFeature();
				return null; 
			}

			@Override
			public Object visit(OWLSubAnnotationPropertyOfAxiom axiom) {
				return axiom;
			}

			@Override
			public Object visit(OWLAnnotationPropertyDomainAxiom axiom) {
				return axiom;
			}

			@Override
			public Object visit(OWLAnnotationPropertyRangeAxiom axiom) {
				return axiom;
			}

			@Override
			public Object visit(OWLClass ce) {
				return ce;
			}

			@Override
			public Object visit(OWLObjectIntersectionOf ce) {
				Set<OWLClassExpression> exps = new HashSet<OWLClassExpression>(); 
				for (OWLClassExpression exp: ce.getOperands())
					exps.add((OWLClassExpression) exp.accept(this)); 
				return m_factory.getOWLObjectIntersectionOf(exps);
			}

			@Override
			public Object visit(OWLObjectUnionOf ce) {
				Set<OWLClassExpression> exps = new HashSet<OWLClassExpression>(); 
				for (OWLClassExpression exp: ce.getOperands())
					exps.add((OWLClassExpression) exp.accept(this)); 
				return m_factory.getOWLObjectUnionOf(exps);
			}

			@Override
			public Object visit(OWLObjectComplementOf ce) {
				return m_factory.getOWLObjectComplementOf((OWLClassExpression) ce.getOperand().accept(this)); 
			}

			@Override
			public Object visit(OWLObjectSomeValuesFrom ce) {
				return m_factory.getOWLObjectSomeValuesFrom(ce.getProperty(), (OWLClassExpression) ce.getFiller().accept(this)); 
			}

			@Override
			public Object visit(OWLObjectAllValuesFrom ce) {
				return m_factory.getOWLObjectAllValuesFrom(ce.getProperty(), (OWLClassExpression) ce.getFiller().accept(this)); 
			}

			@Override
			public Object visit(OWLObjectHasValue ce) {
				return ce;
			}

			@Override
			public Object visit(OWLObjectMinCardinality ce) {
 				if (ce.getFiller().equals(m_factory.getOWLThing()))
					return ce; 
				else 
					return m_factory.getOWLObjectMinCardinality(
							ce.getCardinality(), 
							ce.getProperty(), 
							(OWLClassExpression) ce.getFiller().accept(this)
							); 
			}

			@Override
			public Object visit(OWLObjectExactCardinality ce) {
				if (ce.getFiller().equals(m_factory.getOWLThing()))
					return ce; 
				else 
					return m_factory.getOWLObjectExactCardinality(
							ce.getCardinality(), 
							ce.getProperty(), 
							(OWLClassExpression) ce.getFiller().accept(this)
							); 
			}

			@Override
			public Object visit(OWLObjectMaxCardinality ce) {
				if (ce.getFiller().equals(m_factory.getOWLThing()))
					return ce; 
				else 
					return m_factory.getOWLObjectMaxCardinality(
							ce.getCardinality(), 
							ce.getProperty(), 
							(OWLClassExpression) ce.getFiller().accept(this)
							); 
			}

			@Override
			public Object visit(OWLDataAllValuesFrom ce) {
				return m_factory.getOWLObjectAllValuesFrom(
						(OWLObjectPropertyExpression) ce.getProperty().accept(this), 
						(OWLClassExpression) ce.getFiller().accept(this)
						); 
			}

			@Override
			public Object visit(OWLDataMinCardinality ce) {
				if (ce.getFiller().equals(m_factory.getTopDatatype()))
					return m_factory.getOWLObjectMinCardinality(
							ce.getCardinality(),
							(OWLObjectPropertyExpression) ce.getProperty().accept(this)
							); 
				else 
					return m_factory.getOWLObjectMinCardinality(
							ce.getCardinality(),
							(OWLObjectPropertyExpression) ce.getProperty().accept(this),
							(OWLClassExpression) ce.getFiller().accept(this)
							); 
			}

			@Override
			public Object visit(OWLDataExactCardinality ce) {
				if (ce.getFiller().equals(m_factory.getTopDatatype()))
					return m_factory.getOWLObjectExactCardinality(
							ce.getCardinality(), 
							(OWLObjectPropertyExpression) ce.getProperty().accept(this)
							); 
				else 
					return m_factory.getOWLObjectExactCardinality(
							ce.getCardinality(), 
							(OWLObjectPropertyExpression) ce.getProperty().accept(this), 
							(OWLClassExpression) ce.getFiller().accept(this)
							); 
			}

			@Override
			public Object visit(OWLDataMaxCardinality ce) {
				if (ce.getFiller().equals(m_factory.getTopDatatype()))
					return m_factory.getOWLObjectMaxCardinality(
							ce.getCardinality(),
							(OWLObjectPropertyExpression) ce.getProperty().accept(this)
							); 
				else 
					return m_factory.getOWLObjectMaxCardinality(
							ce.getCardinality(),
							(OWLObjectPropertyExpression) ce.getProperty().accept(this),
							(OWLClassExpression) ce.getFiller().accept(this)
							);
			}

			@Override
			public Object visit(OWLDatatype node) {
				return m_factory.getOWLClass(node.getIRI());
			}

			@Override
			public Object visit(OWLDataComplementOf node) {
				return m_factory.getOWLObjectComplementOf(
						(OWLClassExpression) node.getDataRange().accept(this)
						); 
			}

			/* (non-Javadoc)
			 * @see org.semanticweb.owlapi.model.OWLDataVisitorEx#visit(org.semanticweb.owlapi.model.OWLDataUnionOf)
			 */
			@Override
			public Object visit(OWLDataUnionOf node) {
				Set<OWLClassExpression> exps = new HashSet<OWLClassExpression>(); 
				for (OWLDataRange range: node.getOperands())
					exps.add((OWLClassExpression) range.accept(this)); 
				return m_factory.getOWLObjectUnionOf(exps);
			}

			@Override
			public Object visit(OWLDatatypeRestriction node) {
				reportUnsupportedFeature();
				return null; 				
			}

			@Override
			public Object visit(OWLLiteral node) {
				String name = PREFIX_LITERAL + node.getLiteral() + getTypeTag(node.getDatatype()); 
				return m_factory.getOWLNamedIndividual(IRI.create(name));
			}

			private String getTypeTag(OWLDatatype datatype) {
				if (datatype.isBoolean()) return "_boolean"; 
				if (datatype.isDouble()) return "_double"; 
				if (datatype.isFloat()) return "_float"; 
				if (datatype.isInteger()) return "_integer"; 
				if (datatype.isRDFPlainLiteral()) return "_plain"; 
				if (datatype.isString()) return "_string"; 
				return null; 
			}

			@Override
			public Object visit(OWLFacetRestriction node) {
				reportUnsupportedFeature();
				return null; 				
			}

			@Override
			public Object visit(OWLObjectProperty property) {
				return property; 
			}

			@Override
			public Object visit(OWLObjectInverseOf property) {
				return property;
			}

			@Override
			public Object visit(OWLNamedIndividual individual) {
				return individual;
			}

			@Override
			public Object visit(OWLAnnotationProperty property) {
				return property;
			}

			@Override
			public Object visit(OWLAnnotation node) {
				return node;
			}

			@Override
			public Object visit(IRI iri) {
				return iri;
			}

			@Override
			public Object visit(OWLAnonymousIndividual individual) {
				return individual;
			}

			@Override
			public Object visit(SWRLClassAtom node) {
				reportUnsupportedFeature();
				return null; 
			}

			@Override
			public Object visit(SWRLDataRangeAtom node) {
				reportUnsupportedFeature();
				return null; 
			}

			@Override
			public Object visit(SWRLObjectPropertyAtom node) {
				reportUnsupportedFeature();
				return null;
			}

			@Override
			public Object visit(SWRLDataPropertyAtom node) {
				reportUnsupportedFeature();
				return null;
			}

			@Override
			public Object visit(SWRLBuiltInAtom node) {
				reportUnsupportedFeature();
				return null;
			}

			@Override
			public Object visit(SWRLVariable node) {
				reportUnsupportedFeature();
				return null;
			}

			@Override
			public Object visit(SWRLIndividualArgument node) {
				reportUnsupportedFeature();
				return null;
			}

			@Override
			public Object visit(SWRLLiteralArgument node) {
				reportUnsupportedFeature();
				return null;
			}

			@Override
			public Object visit(SWRLSameIndividualAtom node) {
				reportUnsupportedFeature();
				return null;
			}

			@Override
			public Object visit(SWRLDifferentIndividualsAtom node) {
				reportUnsupportedFeature();
				return null;
			}
	    }
	 
	 protected class DataToObjectRDFHandler implements RDFHandler {
		 
		RDFWriter m_writer;
		Set<String> m_properties; 
		DataToObjectVisitor m_visitor; 
		 
		public DataToObjectRDFHandler(TurtleWriter writer, Set<String> dataProperties) {
			m_writer = writer; 
			m_properties = dataProperties; 
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
			URI predicate = arg0.getPredicate(); 
			Resource subject = arg0.getSubject(); 
			Value object = arg0.getObject();
			
			if (subject instanceof URI) {
				String newSubject = Normalizer.normalize(arg0.getSubject().toString(), Normalizer.Form.NFKC); 
				if (!isValidIRI(newSubject)) {
					return ;
				}
				else subject = new URIImpl(newSubject); 
			}
			
			if (m_properties.contains(predicate.toString()) || object.toString().contains("\"^^")) {
				String newObject = Normalizer.normalize(getIndividual(object.toString()), Normalizer.Form.NFKC); 
				if (!isValidIRI(newObject)) {
					return ; 
				}

				m_writer.handleStatement(new StatementImpl(subject, predicate, new URIImpl(newObject)));
			}
			else 
				m_writer.handleStatement(arg0);
		}

		private boolean isValidIRI(String newSubject) {
			org.apache.jena.iri.IRI iri;
			try {
				iri = DataFilterRDFHandler.iriFactory.construct(newSubject); 
				if (iri.hasViolation(true)) return false;
			} catch (org.apache.jena.iri.IRIException e) {
				return false;
			}
			return true; 
		}

		private String getIndividual(String s) {
			if (s.startsWith("_:")) return s; 
			int left = s.indexOf("\""), right = s.lastIndexOf("\"");
			return PREFIX_LITERAL + s.substring(left + 1, right).replace(' ', '-');
		}

		@Override
		public void startRDF() throws RDFHandlerException {
			m_writer.startRDF();			
		}
		 
	 }
}
