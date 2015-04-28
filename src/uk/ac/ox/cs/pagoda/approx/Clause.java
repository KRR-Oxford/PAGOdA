package uk.ac.ox.cs.pagoda.approx;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.semanticweb.HermiT.model.*;
import org.semanticweb.owlapi.model.*;

import uk.ac.ox.cs.pagoda.hermit.DLClauseHelper;
import uk.ac.ox.cs.pagoda.util.Namespace;
import uk.ac.ox.cs.pagoda.util.Utility;

public class Clause {

	Set<Atom> headAtoms;
	Set<Atom> bodyAtoms;

	Set<String> dataProperties;
	OWLDataFactory factory;
	// OWLClass top = null;

	private Set<OWLClassExpression> superClasses = new HashSet<OWLClassExpression>();
	private Set<OWLClassExpression> subClasses = new HashSet<OWLClassExpression>();

	public Clause(Clausifier clausifier, DLClause clause) {
		this.dataProperties = clausifier.dataProperties;
		this.factory = clausifier.factory;
		// top = ontology.top;
		
  		headAtoms = Utility.toSet(clause.getHeadAtoms());
 		bodyAtoms = Utility.toSet(clause.getBodyAtoms());

 		rollingUp();
	}

	private static final Variable X = Variable.create("X");

	private void rollingUp() {
		Map<Variable, Set<Variable>> varCliques = new HashMap<Variable, Set<Variable>>();

		for (Iterator<Atom> iter = bodyAtoms.iterator(); iter.hasNext();) {
			Atom atom = iter.next();
			if (atom.getDLPredicate() instanceof Inequality)
				if (atom.getArgument(0) instanceof Variable
						&& atom.getArgument(1) instanceof Variable) {
					Variable var1 = atom.getArgumentVariable(0), var2 = atom
							.getArgumentVariable(1);
					Set<Variable> rep;
					if ((rep = varCliques.get(var1)) == null)
						if ((rep = varCliques.get(var2)) == null)
							rep = new HashSet<Variable>();
					rep.add(var1);
					rep.add(var2);
					varCliques.put(var1, rep);
					varCliques.put(var2, rep);
					iter.remove();
				}
		}

		eliminateEquality();

		Map<Variable, Atom> var2atom = new HashMap<Variable, Atom>();

		getVariableOccurrence(var2atom, headAtoms);
		getVariableOccurrence(var2atom, bodyAtoms);

		DLPredicate predicate;
 		Term W = null;

		Map<Variable, String> nom2iri = new HashMap<Variable, String>();
		Map<Variable, Constant> nom2datatype = new HashMap<Variable, Constant>();

		for (Iterator<Atom> iter = headAtoms.iterator(); iter.hasNext();) {
			Atom tAtom = iter.next();
			predicate = tAtom.getDLPredicate();
			if (predicate instanceof AtomicNegationDataRange) {
				AtomicNegationDataRange andr = (AtomicNegationDataRange) predicate;
				AtomicDataRange adr = andr.getNegatedDataRange();
				if (adr instanceof ConstantEnumeration) {
					ConstantEnumeration e = (ConstantEnumeration) adr;
					if (e.getNumberOfConstants() == 1) {
						Variable v = tAtom.getArgumentVariable(0);
						nom2datatype.put(v, e.getConstant(0));
						iter.remove();
						continue;
					}
				}
			}
		}

		for (Atom atom : bodyAtoms) {
			predicate = atom.getDLPredicate();
			if (predicate instanceof AtomicConcept) {
				AtomicConcept concept = (AtomicConcept) predicate;
				Variable v = atom.getArgumentVariable(0);
				if (v == X)
					subClasses.add(factory.getOWLClass(IRI.create(concept.getIRI())));
				else if (predicate.toString().startsWith("<internal:nom#"))
					nom2iri.put(v, DLClauseHelper.getIRI4Nominal(concept));
			} else if (predicate instanceof AtomicRole) {
				AtomicRole role = (AtomicRole) predicate;

				if (dataProperties.contains(role.getIRI())) {
					OWLDataRange dataRange;
					OWLDataPropertyExpression dataPropertyExp = factory
							.getOWLDataProperty(IRI.create(role.getIRI()));
					Term term = atom.getArgument(1);
					if (term instanceof Constant)
						subClasses.add(factory
								.getOWLDataHasValue(dataPropertyExp,
										getOWLLiteral((Constant) term)));
					else if (term instanceof Variable) {
						W = (Variable) term;
						if (nom2datatype.containsKey(W)) {
							subClasses.add(factory.getOWLDataHasValue(
									dataPropertyExp,
									getOWLLiteral(nom2datatype.get(W))));
						} else if (var2atom.containsKey(W)) {
							Atom tAtom = var2atom.get(W);
							DLPredicate tPredicate = tAtom.getDLPredicate();
							if (tPredicate instanceof DatatypeRestriction) {
								DatatypeRestriction restriction = (DatatypeRestriction) tPredicate;
								dataRange = factory.getOWLDatatype(IRI
										.create(restriction.getDatatypeURI()));
							}
							// else if (tPredicate instanceof
							// AtomicNegationDataRange) {
							// // TODO how to deal with AtomicNegationDataRange
							// e.g. not({ "5"^^xsd:integer })
							//
							// }
							else if (tPredicate instanceof AtomicConcept) {
								dataRange = factory.getOWLDatatype(IRI
										.create(((AtomicConcept) tPredicate)
												.getIRI()));
							} else {
								dataRange = null;
								Utility.logError(tPredicate,
										"strange ... -___-|||");
							}

							if (headAtoms.contains(tAtom)) {
								superClasses.add(factory
										.getOWLDataAllValuesFrom(
												dataPropertyExp, dataRange));
								subClasses.add(factory
										.getOWLDataSomeValuesFrom(
												dataPropertyExp,
												factory.getTopDatatype()));
								headAtoms.remove(tAtom);
							} else
								subClasses.add(factory
										.getOWLDataSomeValuesFrom(
												dataPropertyExp, dataRange));

						} else
							subClasses.add(factory.getOWLDataSomeValuesFrom(
									dataPropertyExp, factory.getTopDatatype()));
					} else {
						Utility.logError(term, "strange ... -___-|||");
					}
					continue;
				}

				OWLObjectPropertyExpression roleExp = factory
						.getOWLObjectProperty(IRI.create(role.getIRI()));
				if ((W = atom.getArgument(1)).equals(X)) {
					roleExp = roleExp.getInverseProperty();
					W = atom.getArgument(0);
				}

				if (X == W) 
					subClasses.add(factory.getOWLObjectHasSelf(roleExp));
				else if (W instanceof Individual)
					subClasses.add(factory.getOWLObjectHasValue(roleExp, factory.getOWLNamedIndividual(IRI.create(((Individual) W).getIRI()))));
				else {
					AtomicConcept concept;
					OWLClassExpression clsExp = null;
					int number = 1;
					Set<Variable> set = varCliques.get(W);
					if (set != null)
						number = set.size();
	
					if (var2atom.containsKey(W)) {
						Atom tAtom = var2atom.get(W);
						DLPredicate tPredicate = tAtom.getDLPredicate();
						if (tPredicate instanceof AtomicConcept) {
							concept = (AtomicConcept) tPredicate;
							clsExp = factory.getOWLClass(IRI.create(concept
									.getIRI()));
							if (headAtoms.contains(tAtom)) {
								superClasses.add(factory.getOWLObjectAllValuesFrom(
										roleExp, clsExp));
								subClasses.add(factory.getOWLObjectSomeValuesFrom(
										roleExp, factory.getOWLThing()));
								headAtoms.remove(tAtom);
							} else {
								if (number == 1)
									subClasses.add(factory
											.getOWLObjectSomeValuesFrom(roleExp,
													clsExp));
								else
									subClasses.add(factory
											.getOWLObjectMinCardinality(number,
													roleExp, clsExp));
							}
						} else {
							Utility.logDebug(tAtom, "strange ... -___-|||");
						}
					}
					else {
						if (number == 1)
							subClasses.add(factory.getOWLObjectSomeValuesFrom(
									roleExp, factory.getOWLThing()));
						else
							subClasses.add(factory.getOWLObjectMinCardinality(
									number, roleExp));
					}
				}
			}
		}

		OWLObjectPropertyExpression objExp;
		for (Atom atom : headAtoms) {
			predicate = atom.getDLPredicate();
			if (predicate instanceof AtomicConcept) {
				if (atom.getArgumentVariable(0) == X)
					superClasses
							.add(getClassExpression((AtomicConcept) predicate));
			} else if (predicate instanceof AtomicRole) {
				if (!dataProperties.contains(((AtomicRole) predicate).getIRI())) {
					objExp = factory.getOWLObjectProperty(IRI
							.create(((AtomicRole) predicate).getIRI()));
					Term V = atom.getArgument(1);
					if (V == X) {
						objExp = factory.getOWLObjectInverseOf(objExp);
						V = atom.getArgument(0);
					}
	
					if (V == X)
						superClasses.add(factory.getOWLObjectHasSelf(objExp));
					else if (V instanceof Individual) {
						superClasses.add(factory.getOWLObjectHasValue(objExp,
								factory.getOWLNamedIndividual(IRI
										.create(((Individual) V).getIRI()))));
					} else
						superClasses.add(factory.getOWLObjectHasValue(objExp,
								factory.getOWLNamedIndividual(IRI.create(nom2iri
										.get((Variable) V)))));
				}
				else {
					Constant c = (Constant) atom.getArgument(1);
					OWLDataProperty dataProp = factory.getOWLDataProperty(IRI.create(((AtomicRole) predicate).getIRI())); 
					superClasses.add(factory.getOWLDataHasValue(dataProp, getOWLLiteral(c))); 
				}
			} else if (predicate instanceof AtLeastConcept)
				superClasses
						.add(getMinCardinalityExpression((AtLeastConcept) predicate));
			else if (predicate instanceof AtLeastDataRange)
				superClasses
						.add(getDataMinCardinalityExpression((AtLeastDataRange) predicate));

			else {
				Utility.logError(atom.toString(),
						"strange head atoms left here~~~~~");
				// superClasses.add(getDataRange(getDataRange((LiteralDataRange)
				// predicate)));
			}
		}
	}

	private OWLLiteral getOWLLiteral(Constant constant) {
		if (!constant.getDatatypeURI().equals(Namespace.RDF_PLAIN_LITERAL))
			return factory.getOWLLiteral(constant.getLexicalForm(), factory
					.getOWLDatatype(IRI.create(constant.getDatatypeURI())));
		else {
			String lexicalForm = constant.getLexicalForm();
			int index = lexicalForm.indexOf("@");
			return factory.getOWLLiteral(lexicalForm.substring(0, index),
					lexicalForm.substring(index + 1));
		}
	}

	// private OWLObjectSomeValuesFrom
	// addSomeValuesFromAxiom(OWLObjectPropertyExpression roleExp,
	// OWLClassExpression classExp) {
	// return factory.getOWLObjectSomeValuesFrom(roleExp, classExp);
	// }

	private void getVariableOccurrence(Map<Variable, Atom> var2atom,
			Set<Atom> atoms) {
		for (Atom atom : atoms)
			if (atom.getArity() == 1 && atom.getArgument(0) instanceof Variable
					&& !atom.getArgument(0).equals(X))
				var2atom.put((Variable) atom.getArgumentVariable(0), atom);
	}

	private OWLClassExpression getMinCardinalityExpression(
			AtLeastConcept atLeast) {
		OWLObjectPropertyExpression propExp = getObjectPropertyExpression(atLeast
				.getOnRole());
		OWLClassExpression clsExp = getClassExpression(atLeast.getToConcept());
		if (atLeast.getNumber() == 1)
			return factory.getOWLObjectSomeValuesFrom(propExp, clsExp);
		else
			return factory.getOWLObjectMinCardinality(atLeast.getNumber(),
					propExp, clsExp);
	}

	private OWLClassExpression getDataMinCardinalityExpression(
			AtLeastDataRange atLeast) {
		OWLDataPropertyExpression propExp = getDataPropertyExpression(atLeast
				.getOnRole());
		OWLDataRange dataRange = getDataRange(atLeast.getToDataRange());
		if (atLeast.getNumber() == 1)
			return factory.getOWLDataSomeValuesFrom(propExp, dataRange);
		else
			return factory.getOWLDataMinCardinality(atLeast.getNumber(),
					propExp, dataRange);
	}

	public Set<OWLClassExpression> getSuperClasses() {
		return superClasses;
	}

	public Set<OWLClassExpression> getSubClasses() {
		return subClasses;
	}

	// public OWLClassExpression getSubClass() {
	// if (subClasses.isEmpty())
	// return factory.getOWLThing();
	// if (subClasses.size() == 1)
	// return subClasses.iterator().next();
	//
	// return factory.getOWLObjectIntersectionOf(subClasses);
	// }

	private void eliminateEquality() {
		Set<Atom> eHeadAtoms = new HashSet<Atom>();
		Set<Atom> eBodyAtoms = new HashSet<Atom>();
		Set<Variable> eVariables = new HashSet<Variable>();
		seperateEquality4Clause(eBodyAtoms, eHeadAtoms, eVariables);
	
		OWLNamedIndividual individual; 
		/*
		 * remove equalities that are introduced by MaxCardinalityConstraints
		 */
		DLPredicate predicate;
		Map<Variable, Set<Variable>> groups = new HashMap<Variable, Set<Variable>>();
		OWLObjectMaxCardinality maxCardinality;
		OWLClassExpression exp;
		Set<Variable> mVariables = new HashSet<Variable>();
		Variable tVar, tVar1, tVar2; 
		Set<Variable> tVariables; 
		
		for (Iterator<Atom> iter = eHeadAtoms.iterator(); iter.hasNext(); ){
			Atom atom = iter.next();
			predicate = atom.getDLPredicate();
			if (predicate instanceof AnnotatedEquality) { 
				superClasses.add(maxCardinality = getMaxCardinalityExpression((AnnotatedEquality)predicate));
				if (!((exp = maxCardinality.getFiller()) instanceof OWLObjectComplementOf))
					subClasses.add(factory.getOWLObjectSomeValuesFrom(maxCardinality.getProperty(), exp));
				else 
					subClasses.add(factory.getOWLObjectSomeValuesFrom(maxCardinality.getProperty(), factory.getOWLThing()));
				mVariables.add(atom.getArgumentVariable(0)); 
				mVariables.add(atom.getArgumentVariable(1)); 
				iter.remove();
			}
			else if (predicate instanceof Equality) {
				if (atom.getArgument(0) instanceof Variable && atom.getArgument(1) instanceof Variable) {
					mVariables.add(tVar1 = atom.getArgumentVariable(0)); 
					mVariables.add(tVar2 = atom.getArgumentVariable(1)); 
					iter.remove();
					
					if (tVar1.getName().compareTo(tVar2.getName()) > 0) {
						tVar = tVar1; tVar1 = tVar2; tVar2 = tVar; 
					}
					tVariables = groups.get(tVar1);
					if (groups.containsKey(tVar2)) {
						if (tVariables == null)    
							groups.put(tVar1, tVariables = groups.get(tVar2));
						else { 
							tVariables.addAll(groups.get(tVar2));
							groups.get(tVar2).clear();
							groups.put(tVar2, tVariables); 
						}
					}
					if (tVariables == null) {
						groups.put(tVar1, tVariables = new HashSet<Variable>());
						groups.put(tVar2, tVariables);
					}
					tVariables.add(tVar1); 
					tVariables.add(tVar2); 
				}
			}
		}
		
		Map<Variable, Object> maxCardToConcepts = new HashMap<Variable, Object>();
		
		for (Iterator<Atom> iter = eBodyAtoms.iterator(); iter.hasNext(); ) {
			Atom atom = iter.next(); 
			if (atom.getArity() == 1 && atom.getArgument(0) instanceof Variable) {
				if (mVariables.contains(tVar = atom.getArgumentVariable(0))) {
					maxCardToConcepts.put(tVar, atom.getDLPredicate()); 
					iter.remove();
				}
			}
		}
		
		for (Iterator<Atom> iter = eHeadAtoms.iterator(); iter.hasNext(); ) {
			Atom atom = iter.next(); 
			if (atom.getArity() == 1 && atom.getArgument(0) instanceof Variable) {
				if (mVariables.contains(tVar = atom.getArgumentVariable(0))) {
					maxCardToConcepts.put(tVar, AtomicNegationConcept.create((AtomicConcept) atom.getDLPredicate())); 
					iter.remove();
				}
			}
		}

		Map<Variable, Object> maxCardToProperty = new HashMap<Variable, Object>(); 
		
		for (Iterator<Atom> iter = eBodyAtoms.iterator(); iter.hasNext(); ) {
			Atom atom = iter.next(); 
			if (atom.getArity() == 2 && atom.getArgument(0) instanceof Variable && atom.getArgument(1) instanceof Variable) {
				tVar1 = atom.getArgumentVariable(0); tVar2 = atom.getArgumentVariable(1); 
				if (mVariables.contains(tVar1)) {
					if (groups.containsKey(tVar1))
						maxCardToProperty.put(tVar1, ((AtomicRole) atom.getDLPredicate()).getInverse()); 
					iter.remove(); 
				} else if (mVariables.contains(tVar2)) {
					if (groups.containsKey(tVar2))
						maxCardToProperty.put(tVar2, atom.getDLPredicate());  
					iter.remove(); 
				}
			}
		}
		
		int n; 
		Object r, A;
		for (Variable var: groups.keySet()) {
			if ((tVariables = groups.get(var)).isEmpty())
				continue; 
			n = tVariables.size() - 1;
			tVariables.clear();
			r = maxCardToProperty.get(var); 
			if (r instanceof AtomicRole) {
				if (isDataProperty(r)) {
					if ((A = maxCardToConcepts.get(var)) != null) {
						Utility.logError("Unknown data range: " + A);
					}
						
					superClasses.add(
							factory.getOWLDataMaxCardinality(
									n, 
									factory.getOWLDataProperty(IRI.create(((AtomicRole) r).getIRI())))); 
				}
				else {
					OWLClassExpression clsExp = null;
					if ((A = maxCardToConcepts.get(var)) != null)  
						if (A instanceof AtomicConcept) 
							clsExp = factory.getOWLClass(IRI.create(((AtomicConcept) A).getIRI())); 
						else if (A instanceof AtomicNegationConcept) 
							clsExp = factory.getOWLObjectComplementOf(factory.getOWLClass(IRI.create(((AtomicNegationConcept) A).getNegatedAtomicConcept().getIRI()))); 
						else 
							Utility.logError("Unknown to concept: " + A);

					if (A == null)
					superClasses.add(
							factory.getOWLObjectMaxCardinality(
									n, 
									factory.getOWLObjectProperty(IRI.create(((AtomicRole) r).getIRI())) 
									));
					else 
					superClasses.add(
							factory.getOWLObjectMaxCardinality(
									n, 
									factory.getOWLObjectProperty(IRI.create(((AtomicRole) r).getIRI())), 
									clsExp)); 
				}
			}
			else if (r instanceof InverseRole) {
				OWLClassExpression clsExp = null;
				if ((A = maxCardToConcepts.get(var)) != null) {
					if (A instanceof AtomicConcept) 
						clsExp = factory.getOWLClass(IRI.create(((AtomicConcept) A).getIRI())); 
					else if (A instanceof AtomicNegationConcept) 
						clsExp = factory.getOWLObjectComplementOf(factory.getOWLClass(IRI.create(((AtomicNegationConcept) A).getNegatedAtomicConcept().getIRI()))); 
					else 
						Utility.logError("Unknown to concept: " + A);
				}
				
				if (A == null)
					superClasses.add(
							factory.getOWLObjectMaxCardinality(
									n, 
									factory.getOWLObjectInverseOf(factory.getOWLObjectProperty(IRI.create(((InverseRole) r).getInverseOf().getIRI()))) 
									)); 
				else 
				superClasses.add(
						factory.getOWLObjectMaxCardinality(
								n, 
								factory.getOWLObjectInverseOf(factory.getOWLObjectProperty(IRI.create(((InverseRole) r).getInverseOf().getIRI()))), 
								clsExp)); 

			}
			else 
				Utility.logError("Unknown property: " + r);
		}
		
		/*
		 * dealing with equalities of nominal
		 */
		Map<Variable, String> nom2iri = new HashMap<Variable, String>();
		for (Iterator<Atom> iter = eBodyAtoms.iterator(); iter.hasNext(); ) {
			Atom atom = iter.next(); 
			predicate = atom.getDLPredicate();
			if (predicate instanceof AtomicConcept && predicate.toString().startsWith("<internal:nom#")) {
				nom2iri.put(atom.getArgumentVariable(0), DLClauseHelper.getIRI4Nominal(predicate));
				iter.remove();
			}
		}
		
		Term first, second;
		Map<Variable, Set<Term>> equEdges = new HashMap<Variable, Set<Term>>();
		Set<Term> terms = new HashSet<Term>();
		for (Atom atom: eHeadAtoms) {
			predicate = atom.getDLPredicate();
			if (predicate instanceof Equality) {
				first = atom.getArgument(0);
				second = atom.getArgument(1);
				
				if (first instanceof Variable) {
					if ((terms = equEdges.get(first)) == null) 
						equEdges.put((Variable) first, (terms = new HashSet<Term>()));
					terms.add(second);
				}
				
				if (second instanceof Variable) {
					if ((terms = equEdges.get(second)) == null) 
						equEdges.put((Variable) second, (terms = new HashSet<Term>()));
						terms.add(first);
				}
			}
		}
		
		OWLObjectPropertyExpression objExp;
		
		Set<OWLNamedIndividual> individuals = new HashSet<OWLNamedIndividual>(); 
		if (equEdges.containsKey(X)) {
			for (Term t: equEdges.get(X)) 
				if (t instanceof Variable) {
					Variable var = (Variable) t; 					
					individual = factory.getOWLNamedIndividual(IRI.create(nom2iri.get(var)));
//					superClasses.add(factory.getOWLObjectOneOf(individual));
					individuals.add(individual); 
				}
				else if (t instanceof Individual)
					individuals.add(factory.getOWLNamedIndividual(IRI.create(((Individual) t).getIRI()))); 
		}
		
		if (individuals.size() > 0) {
			superClasses.add(factory.getOWLObjectOneOf(individuals));
			individuals.clear();
		}
		
		for (Atom atom: eBodyAtoms) {
			predicate = atom.getDLPredicate();
			if (predicate instanceof AtomicRole) {
				first = atom.getArgumentVariable(0);
				second = atom.getArgumentVariable(1);
				
				objExp = factory.getOWLObjectProperty(IRI.create(((AtomicRole) predicate).getIRI()));
				if (eVariables.contains(first)) {
					second = first;
					objExp = factory.getOWLObjectInverseOf(objExp);
				}

				for (Term t: equEdges.get(second)) {
					if (t instanceof Variable) {
						Variable var = (Variable) t; 
						individuals.add(factory.getOWLNamedIndividual(IRI.create(nom2iri.get(var))));
					}
					else if (t instanceof Individual) {
						individuals.add(factory.getOWLNamedIndividual(IRI.create(((Individual) t).getIRI())));
					}
				}
				if (!individuals.isEmpty()) {
					superClasses.add(factory.getOWLObjectAllValuesFrom(objExp, factory.getOWLObjectOneOf(individuals)));
					individuals.clear();
				}
			}
		}
		
	}

	private boolean isDataProperty(Object r) {
		if (!(r instanceof AtomicRole)) return false; 
		String iri = ((AtomicRole) r).getIRI(); 
		return dataProperties.contains(iri);
	}

	private OWLObjectMaxCardinality getMaxCardinalityExpression(
			AnnotatedEquality equ) {
		OWLObjectPropertyExpression propExp = getObjectPropertyExpression(equ
				.getOnRole());
		OWLClassExpression clsExp = getClassExpression(equ.getToConcept());
		return factory.getOWLObjectMaxCardinality(equ.getCaridnality(),
				propExp, clsExp);
	}

	private OWLObjectPropertyExpression getObjectPropertyExpression(Role role) {
		if (role instanceof AtomicRole)
			return factory.getOWLObjectProperty(IRI.create(((AtomicRole) role)
					.getIRI()));
		return factory.getOWLObjectProperty(
				IRI.create(((InverseRole) role).getInverseOf().getIRI()))
				.getInverseProperty();
	}

	private OWLDataProperty getDataPropertyExpression(Role role) {
		return factory.getOWLDataProperty(IRI.create(((AtomicRole) role)
				.getIRI()));
	}

	private OWLClassExpression getClassExpression(LiteralConcept concept) {
		if (concept instanceof AtomicConcept)
			return factory.getOWLClass(IRI.create(((AtomicConcept) concept)
					.getIRI()));
		return factory.getOWLClass(
				IRI.create(((AtomicNegationConcept) concept)
						.getNegatedAtomicConcept().getIRI()))
				.getComplementNNF();
	}

	private OWLDataRange getDataRange(LiteralDataRange dataRange) {
		if (dataRange instanceof InternalDatatype)
			return factory.getOWLDatatype(IRI
					.create(((InternalDatatype) dataRange).getIRI()));
		if (dataRange instanceof DatatypeRestriction)
			return factory
					.getOWLDatatype(IRI
							.create(((DatatypeRestriction) dataRange)
									.getDatatypeURI()));
		if (dataRange instanceof ConstantEnumeration) {
			ConstantEnumeration e = (ConstantEnumeration) dataRange;
			OWLLiteral[] values = new OWLLiteral[e.getNumberOfConstants()];
			for (int i = 0; i < values.length; ++i) {
				Constant c = e.getConstant(i);
				values[i] = factory.getOWLLiteral(c.getDataValue().toString(),
						factory.getOWLDatatype(IRI.create(c.getDatatypeURI())));
			}
			return factory.getOWLDataOneOf(values);
		}
		Utility.logError(dataRange.toString(), "strange data type!!!!");
		return null;
	}

	public void seperateEquality4Clause(Set<Atom> eBodyAtoms,
			Set<Atom> eHeadAtoms, Set<Variable> eVariables) {
		Set<Variable> variables = new HashSet<Variable>();
		DLPredicate predicate;
		for (Atom atom : headAtoms) {
			predicate = atom.getDLPredicate();
			if (predicate instanceof Equality
					|| predicate instanceof AnnotatedEquality) {
				variables.clear();
				atom.getVariables(variables);
				for (Variable variable : variables)
					eVariables.add(variable);
			}
		}
		eVariables.remove(X);

		seperateEquality(bodyAtoms, eBodyAtoms, eVariables);
		seperateEquality(headAtoms, eHeadAtoms, eVariables);
	}

	public void seperateEquality(Set<Atom> noEquality, Set<Atom> inEquality,
			Set<Variable> eVariables) {
		Set<Variable> variables = new HashSet<Variable>();
		for (Iterator<Atom> iter = noEquality.iterator(); iter.hasNext();) {
			Atom atom = iter.next();
			if (atom.getDLPredicate() instanceof Equality
					|| atom.getDLPredicate() instanceof AnnotatedEquality) {
				iter.remove();
				inEquality.add(atom);
			} else {
				variables.clear();
				atom.getVariables(variables);
				for (Variable variable : variables)
					if (eVariables.contains(variable)) {
						iter.remove();
						inEquality.add(atom);
						break;
					}
			}
		}
	}

	@Override
	public String toString() {
		StringBuilder ret = new StringBuilder();
		boolean first = true;
		for (OWLClassExpression exp : superClasses)
			if (first) {
				ret.append(exp.toString());
				first = false;
			} else
				ret.append(" v ").append(exp.toString());

		first = true;
		for (OWLClassExpression exp : subClasses)
			if (first) {
				ret.append(" :- ").append(exp.toString());
				first = false;
			} else
				ret.append(" ^ ").append(exp.toString());

		return ret.toString();
	}
}
