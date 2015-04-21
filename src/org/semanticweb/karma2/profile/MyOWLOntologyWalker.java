package org.semanticweb.karma2.profile;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

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
import org.semanticweb.owlapi.model.OWLObject;
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
import org.semanticweb.owlapi.model.OWLObjectVisitor;
import org.semanticweb.owlapi.model.OWLObjectVisitorEx;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLReflexiveObjectPropertyAxiom;
import org.semanticweb.owlapi.model.OWLSameIndividualAxiom;
import org.semanticweb.owlapi.model.OWLSubAnnotationPropertyOfAxiom;
import org.semanticweb.owlapi.model.OWLSubClassOfAxiom;
import org.semanticweb.owlapi.model.OWLSubDataPropertyOfAxiom;
import org.semanticweb.owlapi.model.OWLSubObjectPropertyOfAxiom;
import org.semanticweb.owlapi.model.OWLSubPropertyChainOfAxiom;
import org.semanticweb.owlapi.model.OWLSymmetricObjectPropertyAxiom;
import org.semanticweb.owlapi.model.OWLTransitiveObjectPropertyAxiom;
import org.semanticweb.owlapi.model.SWRLAtom;
import org.semanticweb.owlapi.model.SWRLBuiltInAtom;
import org.semanticweb.owlapi.model.SWRLClassAtom;
import org.semanticweb.owlapi.model.SWRLDArgument;
import org.semanticweb.owlapi.model.SWRLDataPropertyAtom;
import org.semanticweb.owlapi.model.SWRLDataRangeAtom;
import org.semanticweb.owlapi.model.SWRLDifferentIndividualsAtom;
import org.semanticweb.owlapi.model.SWRLIndividualArgument;
import org.semanticweb.owlapi.model.SWRLLiteralArgument;
import org.semanticweb.owlapi.model.SWRLObjectPropertyAtom;
import org.semanticweb.owlapi.model.SWRLRule;
import org.semanticweb.owlapi.model.SWRLSameIndividualAtom;
import org.semanticweb.owlapi.model.SWRLVariable;
import org.semanticweb.owlapi.util.OWLOntologyWalker;


public class MyOWLOntologyWalker extends OWLOntologyWalker {

    private final Collection<OWLOntology> ontologies;

    /**
     * @param objects the set of objects to visit
     */
    public MyOWLOntologyWalker(Set<OWLOntology> objects) {
        this(objects, true);
    }
    /**
     * @param visitDuplicates true if duplicates should be visited
     * @param objects the set of objects to visit
     */
    public MyOWLOntologyWalker(Set<OWLOntology> objects, boolean visitDuplicates) {
    	super(objects); 
        this.ontologies = new ArrayList<OWLOntology>(objects);
    }

    /**
     * @param v visitor to use over the objects
     */
    public void walkStructure(OWLObjectVisitorEx<?> v) {
        this.visitor = v;
        StructureWalker walker = new StructureWalker();
        for (OWLOntology o : ontologies) {
            o.accept(walker);
        }
    }

    private class StructureWalker implements OWLObjectVisitor {

        private final Set<OWLObject> visited = new HashSet<OWLObject>();

        public StructureWalker() {}

        private void process(OWLObject object) {
            if (!visitDuplicates) {
                if (!visited.contains(object)) {
                    visited.add(object);
                    object.accept(visitor);
                }
            }
            else {
                object.accept(visitor);
            }
        }

        @Override
        public void visit(IRI iri) {
            process(iri);
        }

        @Override
        public void visit(OWLOntology ontologyToVisit) {
            MyOWLOntologyWalker.this.ontology = ontologyToVisit;
            MyOWLOntologyWalker.this.ax = null;
            process(ontologyToVisit);
            for (OWLAnnotation anno : ontologyToVisit.getAnnotations()) {
                anno.accept(this);
            }
            for (OWLAxiom a : ontologyToVisit.getAxioms()) {
                a.accept(this);
            }
        }


        @Override
        public void visit(OWLAsymmetricObjectPropertyAxiom axiom) {
            MyOWLOntologyWalker.this.ax = axiom;
            process(axiom);
            axiom.getProperty().accept(this);
        }


        @Override
        public void visit(OWLClassAssertionAxiom axiom) {
            MyOWLOntologyWalker.this.ax = axiom;
            process(axiom);
            axiom.getIndividual().accept(this);
            axiom.getClassExpression().accept(this);
        }


        @Override
        public void visit(OWLDataPropertyAssertionAxiom axiom) {
            MyOWLOntologyWalker.this.ax = axiom;
            process(axiom);
            axiom.getSubject().accept(this);
            axiom.getProperty().accept(this);
            axiom.getObject().accept(this);
        }


        @Override
        public void visit(OWLDataPropertyDomainAxiom axiom) {
            MyOWLOntologyWalker.this.ax = axiom;
            process(axiom);
            axiom.getDomain().accept(this);
            axiom.getProperty().accept(this);
        }


        @Override
        public void visit(OWLDataPropertyRangeAxiom axiom) {
            MyOWLOntologyWalker.this.ax = axiom;
            process(axiom);
            axiom.getRange().accept(this);
            axiom.getProperty().accept(this);
        }


        @Override
        public void visit(OWLSubDataPropertyOfAxiom axiom) {
            MyOWLOntologyWalker.this.ax = axiom;
            process(axiom);
            axiom.getSubProperty().accept(this);
            axiom.getSuperProperty().accept(this);
        }


        @Override
        public void visit(OWLDeclarationAxiom axiom) {
            MyOWLOntologyWalker.this.ax = axiom;
            process(axiom);
            axiom.getEntity().accept(this);
        }


        @Override
        public void visit(OWLDifferentIndividualsAxiom axiom) {
            MyOWLOntologyWalker.this.ax = axiom;
            process(axiom);
            for (OWLIndividual ind : axiom.getIndividuals()) {
                ind.accept(this);
            }
        }


        @Override
        public void visit(OWLDisjointClassesAxiom axiom) {
            MyOWLOntologyWalker.this.ax = axiom;
            process(axiom);
            for (OWLClassExpression desc : axiom.getClassExpressions()) {
                desc.accept(this);
            }
        }


        @Override
        public void visit(OWLDisjointDataPropertiesAxiom axiom) {
            MyOWLOntologyWalker.this.ax = axiom;
            process(axiom);
            for (OWLDataPropertyExpression prop : axiom.getProperties()) {
                prop.accept(this);
            }
        }


        @Override
        public void visit(OWLDisjointObjectPropertiesAxiom axiom) {
            MyOWLOntologyWalker.this.ax = axiom;
            process(axiom);
            for (OWLObjectPropertyExpression prop : axiom.getProperties()) {
                prop.accept(this);
            }
        }


        @Override
        public void visit(OWLDisjointUnionAxiom axiom) {
            MyOWLOntologyWalker.this.ax = axiom;
            process(axiom);
            axiom.getOWLClass().accept(this);
            for (OWLClassExpression desc : axiom.getClassExpressions()) {
                desc.accept(this);
            }
        }


        @Override
        public void visit(OWLAnnotationAssertionAxiom axiom) {
            MyOWLOntologyWalker.this.ax = axiom;
            process(axiom);
            axiom.getSubject().accept(this);
            axiom.getAnnotation().accept(this);
        }

        @Override
        public void visit(OWLAnnotationPropertyDomainAxiom axiom) {
            MyOWLOntologyWalker.this.ax = axiom;
            process(axiom);
            axiom.getProperty().accept(this);
            axiom.getDomain().accept(this);
        }

        @Override
        public void visit(OWLAnnotationPropertyRangeAxiom axiom) {
            MyOWLOntologyWalker.this.ax = axiom;
            process(axiom);
            axiom.getProperty().accept(this);
            axiom.getRange().accept(this);
        }

        @Override
        public void visit(OWLSubAnnotationPropertyOfAxiom axiom) {
            MyOWLOntologyWalker.this.ax = axiom;
            process(axiom);
            axiom.getSubProperty().accept(this);
            axiom.getSuperProperty().accept(this);
        }

        @Override
        public void visit(OWLAnnotation node) {
            process(node);
            annotation = node;
            node.getProperty().accept(this);
            node.getValue().accept(this);
        }

        @Override
        public void visit(OWLEquivalentClassesAxiom axiom) {
            MyOWLOntologyWalker.this.ax = axiom;
            process(axiom);
            for (OWLClassExpression desc : axiom.getClassExpressions()) {
                desc.accept(this);
            }
        }


        @Override
        public void visit(OWLEquivalentDataPropertiesAxiom axiom) {
            MyOWLOntologyWalker.this.ax = axiom;
            process(axiom);
            for (OWLDataPropertyExpression prop : axiom.getProperties()) {
                prop.accept(this);
            }
        }


        @Override
        public void visit(OWLEquivalentObjectPropertiesAxiom axiom) {
            MyOWLOntologyWalker.this.ax = axiom;
            process(axiom);
            for (OWLObjectPropertyExpression prop : axiom.getProperties()) {
                prop.accept(this);
            }
        }


        @Override
        public void visit(OWLFunctionalDataPropertyAxiom axiom) {
            MyOWLOntologyWalker.this.ax = axiom;
            process(axiom);
            axiom.getProperty().accept(this);
        }


        @Override
        public void visit(OWLFunctionalObjectPropertyAxiom axiom) {
            MyOWLOntologyWalker.this.ax = axiom;
            process(axiom);
            axiom.getProperty().accept(this);
        }

        @Override
        public void visit(OWLInverseFunctionalObjectPropertyAxiom axiom) {
            MyOWLOntologyWalker.this.ax = axiom;
            process(axiom);
            axiom.getProperty().accept(this);
        }


        @Override
        public void visit(OWLInverseObjectPropertiesAxiom axiom) {
            MyOWLOntologyWalker.this.ax = axiom;
            process(axiom);
            axiom.getFirstProperty().accept(this);
            axiom.getSecondProperty().accept(this);
        }


        @Override
        public void visit(OWLIrreflexiveObjectPropertyAxiom axiom) {
            MyOWLOntologyWalker.this.ax = axiom;
            process(axiom);
            axiom.getProperty().accept(this);
        }


        @Override
        public void visit(OWLNegativeDataPropertyAssertionAxiom axiom) {
            MyOWLOntologyWalker.this.ax = axiom;
            process(axiom);
            axiom.getSubject().accept(this);
            axiom.getProperty().accept(this);
            axiom.getObject().accept(this);
        }


        @Override
        public void visit(OWLNegativeObjectPropertyAssertionAxiom axiom) {
            MyOWLOntologyWalker.this.ax = axiom;
            process(axiom);
            axiom.getSubject().accept(this);
            axiom.getProperty().accept(this);
            axiom.getObject().accept(this);
        }


        @Override
        public void visit(OWLObjectPropertyAssertionAxiom axiom) {
            MyOWLOntologyWalker.this.ax = axiom;
            process(axiom);
            axiom.getSubject().accept(this);
            axiom.getProperty().accept(this);
            axiom.getObject().accept(this);
        }


        @Override
        public void visit(OWLSubPropertyChainOfAxiom axiom) {
            MyOWLOntologyWalker.this.ax = axiom;
            process(axiom);
            for (OWLObjectPropertyExpression prop : axiom.getPropertyChain()) {
                prop.accept(this);
            }
            axiom.getSuperProperty().accept(this);
        }


        @Override
        public void visit(OWLObjectPropertyDomainAxiom axiom) {
            MyOWLOntologyWalker.this.ax = axiom;
            process(axiom);
            axiom.getDomain().accept(this);
            axiom.getProperty().accept(this);
        }


        @Override
        public void visit(OWLObjectPropertyRangeAxiom axiom) {
            MyOWLOntologyWalker.this.ax = axiom;
            process(axiom);
            axiom.getProperty().accept(this);
            axiom.getRange().accept(this);
        }


        @Override
        public void visit(OWLSubObjectPropertyOfAxiom axiom) {
            MyOWLOntologyWalker.this.ax = axiom;
            process(axiom);
            axiom.getSubProperty().accept(this);
            axiom.getSuperProperty().accept(this);
        }


        @Override
        public void visit(OWLReflexiveObjectPropertyAxiom axiom) {
            MyOWLOntologyWalker.this.ax = axiom;
            process(axiom);
            axiom.getProperty().accept(this);
        }


        @Override
        public void visit(OWLSameIndividualAxiom axiom) {
            MyOWLOntologyWalker.this.ax = axiom;
            process(axiom);
            for (OWLIndividual ind : axiom.getIndividuals()) {
                ind.accept(this);
            }
        }


        @Override
        public void visit(OWLSubClassOfAxiom axiom) {
            MyOWLOntologyWalker.this.ax = axiom;
            process(axiom);
            // -ve polarity
            axiom.getSubClass().accept(this);
            // +ve polarity
            axiom.getSuperClass().accept(this);
        }


        @Override
        public void visit(OWLSymmetricObjectPropertyAxiom axiom) {
            MyOWLOntologyWalker.this.ax = axiom;
            process(axiom);
            axiom.getProperty().accept(this);
        }


        @Override
        public void visit(OWLTransitiveObjectPropertyAxiom axiom) {
            MyOWLOntologyWalker.this.ax = axiom;
            process(axiom);
            axiom.getProperty().accept(this);
        }


        @Override
        public void visit(SWRLRule rule) {
            MyOWLOntologyWalker.this.ax = rule;
            process(rule);
            for (SWRLAtom at : rule.getBody()) {
                at.accept(this);
            }
            for (SWRLAtom at : rule.getHead()) {
                at.accept(this);
            }
        }

        @Override
        public void visit(OWLHasKeyAxiom axiom) {
            MyOWLOntologyWalker.this.ax = axiom;
            process(axiom);
            axiom.getClassExpression().accept(this);
            for (OWLObjectPropertyExpression prop : axiom.getObjectPropertyExpressions()) {
                prop.accept(this);
            }
            for (OWLDataPropertyExpression prop : axiom.getDataPropertyExpressions()) {
                prop.accept(this);
            }
        }

        @Override
        public void visit(OWLClass desc) {
            pushClassExpression(desc);
            process(desc);
            desc.getIRI().accept(this);
            popClassExpression();
        }


        @Override
        public void visit(OWLDataAllValuesFrom desc) {
            pushClassExpression(desc);
            process(desc);
            desc.getProperty().accept(this);
            desc.getFiller().accept(this);
            popClassExpression();
        }


        @Override
        public void visit(OWLDataExactCardinality desc) {
            pushClassExpression(desc);
            process(desc);
            desc.getProperty().accept(this);
            desc.getFiller().accept(this);
            popClassExpression();
        }


        @Override
        public void visit(OWLDataMaxCardinality desc) {
            pushClassExpression(desc);
            process(desc);
            desc.getProperty().accept(this);
            desc.getFiller().accept(this);
            popClassExpression();
        }


        @Override
        public void visit(OWLDataMinCardinality desc) {
            pushClassExpression(desc);
            process(desc);
            desc.getProperty().accept(this);
            desc.getFiller().accept(this);
            popClassExpression();
        }


        @Override
        public void visit(OWLDataSomeValuesFrom desc) {
            pushClassExpression(desc);
            process(desc);
            desc.getProperty().accept(this);
            desc.getFiller().accept(this);
            popClassExpression();
        }


        @Override
        public void visit(OWLDataHasValue desc) {
            pushClassExpression(desc);
            process(desc);
            desc.getProperty().accept(this);
            desc.getValue().accept(this);
            popClassExpression();
        }


        @Override
        public void visit(OWLObjectAllValuesFrom desc) {
            pushClassExpression(desc);
            process(desc);
            desc.getProperty().accept(this);
            desc.getFiller().accept(this);
            popClassExpression();
        }


        @Override
        public void visit(OWLObjectComplementOf desc) {
            pushClassExpression(desc);
            process(desc);
            desc.getOperand().accept(this);
            popClassExpression();
        }


        @Override
        public void visit(OWLObjectExactCardinality desc) {
            pushClassExpression(desc);
            process(desc);
            desc.getProperty().accept(this);
            desc.getFiller().accept(this);
            popClassExpression();
        }


        @Override
        public void visit(OWLObjectIntersectionOf desc) {
            pushClassExpression(desc);
            process(desc);

            for (OWLClassExpression op : desc.getOperands()) {
                op.accept(this);
            }
            popClassExpression();
        }


        @Override
        public void visit(OWLObjectMaxCardinality desc) {
            pushClassExpression(desc);
            process(desc);
            desc.getProperty().accept(this);
            desc.getFiller().accept(this);
            popClassExpression();
        }


        @Override
        public void visit(OWLObjectMinCardinality desc) {
            pushClassExpression(desc);
            process(desc);
            desc.getProperty().accept(this);
            desc.getFiller().accept(this);
            popClassExpression();
        }


        @Override
        public void visit(OWLObjectOneOf desc) {
            pushClassExpression(desc);
            process(desc);
            for (OWLIndividual ind : desc.getIndividuals()) {
                ind.accept(this);
            }
            popClassExpression();
        }


        @Override
        public void visit(OWLObjectHasSelf desc) {
            pushClassExpression(desc);
            process(desc);
            desc.getProperty().accept(this);
            popClassExpression();
        }


        @Override
        public void visit(OWLObjectSomeValuesFrom desc) {
            pushClassExpression(desc);
            process(desc);
            desc.getProperty().accept(this);
            desc.getFiller().accept(this);
            popClassExpression();
        }


        @Override
        public void visit(OWLObjectUnionOf desc) {
            pushClassExpression(desc);
            process(desc);
            for (OWLClassExpression op : desc.getOperands()) {
                op.accept(this);
            }
            popClassExpression();
        }


        @Override
        public void visit(OWLObjectHasValue desc) {
            pushClassExpression(desc);
            process(desc);
            desc.getProperty().accept(this);
            desc.getValue().accept(this);
            popClassExpression();
        }


        @Override
        public void visit(OWLDataComplementOf node) {
            pushDataRange(node);
            process(node);
            node.getDataRange().accept(this);
            popDataRange();
        }


        @Override
        public void visit(OWLDataOneOf node) {
            pushDataRange(node);
            process(node);
            for (OWLLiteral con : node.getValues()) {
                con.accept(this);
            }
            popDataRange();
        }

        @Override
        public void visit(OWLDataIntersectionOf node) {
            pushDataRange(node);
            process(node);
            for (OWLDataRange rng : node.getOperands()) {
                rng.accept(this);
            }
            popDataRange();
        }

        @Override
        public void visit(OWLDataUnionOf node) {
            pushDataRange(node);
            process(node);
            for (OWLDataRange rng : node.getOperands()) {
                rng.accept(this);
            }
            popDataRange();
        }

        @Override
        public void visit(OWLFacetRestriction node) {
            process(node);
            node.getFacetValue().accept(this);
        }


        @Override
        public void visit(OWLDatatypeRestriction node) {
            pushDataRange(node);
            process(node);
            node.getDatatype().accept(this);
            for (OWLFacetRestriction fr : node.getFacetRestrictions()) {
                fr.accept(this);
            }
            popDataRange();
        }


        @Override
        public void visit(OWLDatatype node) {
            pushDataRange(node);
            process(node);
            popDataRange();
        }

        @Override
        public void visit(OWLLiteral node) {
            process(node);
            node.getDatatype().accept(this);
            popDataRange();
        }

        @Override
        public void visit(OWLAnnotationProperty property) {
            process(property);
            property.getIRI().accept(this);
        }

        @Override
        public void visit(OWLDataProperty property) {
            process(property);
            property.getIRI().accept(this);
        }


        @Override
        public void visit(OWLObjectProperty property) {
            process(property);
            property.getIRI().accept(this);
        }


        @Override
        public void visit(OWLObjectInverseOf property) {
            process(property);
            property.getInverse().accept(this);
        }


        @Override
        public void visit(OWLNamedIndividual individual) {
            process(individual);
            individual.getIRI().accept(this);
        }

        @Override
        public void visit(OWLAnonymousIndividual individual) {
            process(individual);
        }

        @Override
        public void visit(SWRLLiteralArgument node) {
            process(node);
            node.getLiteral().accept(this);
        }


        @Override
        public void visit(SWRLVariable node) {
            process(node);
        }


        @Override
        public void visit(SWRLIndividualArgument node) {
            process(node);
            node.getIndividual().accept(this);
        }


        @Override
        public void visit(SWRLBuiltInAtom node) {
            process(node);
            for (SWRLDArgument at : node.getArguments()) {
                at.accept(this);
            }
        }


        @Override
        public void visit(SWRLClassAtom node) {
            process(node);
            node.getArgument().accept(this);
            node.getPredicate().accept(this);
        }


        @Override
        public void visit(SWRLDataRangeAtom node) {
            process(node);
            node.getArgument().accept(this);
            node.getPredicate().accept(this);
        }


        @Override
        public void visit(SWRLDataPropertyAtom node) {
            process(node);
            node.getPredicate().accept(this);
            node.getFirstArgument().accept(this);
            node.getSecondArgument().accept(this);
        }


        @Override
        public void visit(SWRLDifferentIndividualsAtom node) {
            process(node);
            node.getFirstArgument().accept(this);
            node.getSecondArgument().accept(this);
        }


        @Override
        public void visit(SWRLObjectPropertyAtom node) {
            process(node);
            node.getPredicate().accept(this);
            node.getFirstArgument().accept(this);
            node.getSecondArgument().accept(this);
        }


        @Override
        public void visit(SWRLSameIndividualAtom node) {
            process(node);
            node.getFirstArgument().accept(this);
            node.getSecondArgument().accept(this);
        }


        @Override
        public void visit(OWLDatatypeDefinitionAxiom axiom) {
            MyOWLOntologyWalker.this.ax = axiom;
            process(axiom);
            axiom.getDatatype().accept(this);
            axiom.getDataRange().accept(this);
        }
    }
}

