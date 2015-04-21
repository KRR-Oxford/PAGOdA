package uk.ac.ox.cs.pagoda.hermit;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;

import org.semanticweb.HermiT.Prefixes;
import org.semanticweb.HermiT.model.AnnotatedEquality;
import org.semanticweb.HermiT.model.AtLeastDataRange;
import org.semanticweb.HermiT.model.Atom;
import org.semanticweb.HermiT.model.AtomicConcept;
import org.semanticweb.HermiT.model.AtomicDataRange;
import org.semanticweb.HermiT.model.AtomicNegationDataRange;
import org.semanticweb.HermiT.model.AtomicRole;
import org.semanticweb.HermiT.model.Constant;
import org.semanticweb.HermiT.model.ConstantEnumeration;
import org.semanticweb.HermiT.model.DLClause;
import org.semanticweb.HermiT.model.DLPredicate;
import org.semanticweb.HermiT.model.Equality;
import org.semanticweb.HermiT.model.Individual;
import org.semanticweb.HermiT.model.Inequality;
import org.semanticweb.HermiT.model.Term;
import org.semanticweb.HermiT.model.Variable;

import uk.ac.ox.cs.pagoda.owl.OWLHelper;
import uk.ac.ox.cs.pagoda.util.Namespace;
import uk.ac.ox.cs.pagoda.util.SparqlHelper;
import uk.ac.ox.cs.pagoda.util.Utility;

public class DLClauseHelper {

	public static String getIRI4Nominal(DLPredicate predicate) {
		return ((AtomicConcept) predicate).getIRI().replace("internal:nom#", "");
	}

	public static DLClause removeNominalConcept(DLClause clause) {
		boolean hasNominals = false; 
		Map<Variable, String> nom2iri = new HashMap<Variable, String>();
		DLPredicate predicate;
		Collection<Atom> remainingBodyAtoms = new LinkedList<Atom>();
		for (Atom atom: clause.getBodyAtoms()) {
			predicate = atom.getDLPredicate();
			if (predicate instanceof AtomicConcept && predicate.toString().startsWith("<internal:nom#")) {
				nom2iri.put(atom.getArgumentVariable(0), getIRI4Nominal(predicate));
				hasNominals = true; 
			}
			else remainingBodyAtoms.add(atom);
		}
		
		if (!hasNominals) return clause; 
		
		Atom[] newHeadAtoms = new Atom[clause.getHeadLength()];
		int i = 0; 
		for (Atom atom: clause.getHeadAtoms()) {
			newHeadAtoms[i++] = getNewAtom(atom, nom2iri); 
		}
		
		Atom[] newBodyAtoms = new Atom[remainingBodyAtoms.size()]; 
		i = 0; 
		for (Atom atom: remainingBodyAtoms) {
			newBodyAtoms[i++] = getNewAtom(atom, nom2iri);  
		}

		return DLClause.create(newHeadAtoms, newBodyAtoms); 
	}
	
	public static Atom getNewAtom(Atom atom, Map<Variable, String> nom2iri) {
		String iri; 
		DLPredicate predicate = atom.getDLPredicate();
		if (predicate instanceof AtomicRole) { 
			boolean updated = false; 
			Term newArg0 = atom.getArgument(0), newArg1 = atom.getArgument(1);
			if (newArg0 instanceof Variable && ((iri = nom2iri.get((Variable) newArg0)) != null))  {
				newArg0 = Individual.create(iri); 
				updated = true; 
			}
			if (newArg1 instanceof Variable && ((iri = nom2iri.get((Variable) newArg1)) != null))  {
				newArg1 = Individual.create(iri); 
				updated = true; 
			}
			if (updated) {
				return Atom.create(atom.getDLPredicate(), newArg0, newArg1);
			}
		}
		else if (predicate instanceof Equality || predicate instanceof AnnotatedEquality) {
//			System.out.println("To be removed ... \n" + atom); 
			Term two[] = new Term[] { atom.getArgument(0), atom.getArgument(1) }; 
			Term t; 
			for (int i = 0; i < 2; ++i) {
				t = atom.getArgument(i);
				if (t != null && t instanceof Variable && (iri = nom2iri.get((Variable) t)) != null) {
					two[i] = Individual.create(iri); 
				}
			}
			return Atom.create(Equality.INSTANCE, two);
		}
		
		return atom; 
	}
	
	/**
	 * This is a preprocess for StatOil:
	 *  
	 * if (AtomicNegationDataRange o (ConstantEnumerate (value))) (Z) 
	 * appears the head, then replace all Z in the clause by value. 
	 * 
	 * 
	 * @param clause
	 */
	public static DLClause replaceWithDataValue(DLClause clause) {
		Map<Variable, Constant> var2dataValue = new HashMap<Variable, Constant>();
		
		boolean update = false; 
		LinkedList<Atom> newHeadAtoms = new LinkedList<Atom>();
		for (Atom cAtom: clause.getHeadAtoms()) {
			DLPredicate dlPredicate = cAtom.getDLPredicate();
			if (dlPredicate instanceof AtomicNegationDataRange) {
				AtomicDataRange atomicDataRange = ((AtomicNegationDataRange) dlPredicate).getNegatedDataRange(); 
				if (atomicDataRange instanceof ConstantEnumeration) {
					ConstantEnumeration enumeration = (ConstantEnumeration) atomicDataRange;  
					if (enumeration.getNumberOfConstants() == 1) {
						Constant constant = enumeration.getConstant(0);
						//TODO replace unsupported datatypes
//						if (constant.getDatatypeURI().endsWith("boolean"))
//							constant = Constant.create(constant.getLexicalForm(), constant.getDatatypeURI().replace("boolean", "string"));  
						var2dataValue.put(cAtom.getArgumentVariable(0), constant);
					}
				}
			}
			else if (dlPredicate instanceof ConstantEnumeration) {
				ConstantEnumeration enu = (ConstantEnumeration) dlPredicate; 
				for (int i = 0; i < enu.getNumberOfConstants(); ++i) 
					newHeadAtoms.add(Atom.create(Equality.INSTANCE, cAtom.getArgument(0), enu.getConstant(i)));
				update = true; 
			}
			else if (dlPredicate instanceof AtLeastDataRange &&	((AtLeastDataRange) dlPredicate).getToDataRange() instanceof ConstantEnumeration) {
				AtLeastDataRange atLeast = (AtLeastDataRange) dlPredicate; 
				ConstantEnumeration enu = (ConstantEnumeration) atLeast.getToDataRange();
				for (int i = 0; i < enu.getNumberOfConstants(); ++i)
					newHeadAtoms.add(Atom.create((AtomicRole) atLeast.getOnRole(), cAtom.getArgument(0), enu.getConstant(i)));
				update = true; 
			}
			else 
				newHeadAtoms.add(cAtom); 
		}
		
		if (var2dataValue.isEmpty()) return update ? DLClause.create(newHeadAtoms.toArray(new Atom[0]), clause.getBodyAtoms()) : clause;
		
		Atom[] newBodyAtoms = new Atom[clause.getBodyLength()]; 
		Term[] newArgs; 
		int index = 0;
		boolean atomUpdated; 
		for (Atom bodyAtom: clause.getBodyAtoms()) {
			newBodyAtoms[index] = bodyAtom;
			atomUpdated = false; 
			newArgs = new Term[bodyAtom.getArity()]; 
			for (int i = 0; i < newArgs.length; ++i) {
				newArgs[i] = bodyAtom.getArgument(i); 
				if (newArgs[i] instanceof Variable && var2dataValue.containsKey(newArgs[i])) { 
					newArgs[i] = var2dataValue.get(newArgs[i]);
					atomUpdated = true; 
				}
			}
			if (atomUpdated)
				newBodyAtoms[index] = Atom.create(bodyAtom.getDLPredicate(), newArgs);
			++index; 
		}

		return DLClause.create(newHeadAtoms.toArray(new Atom[0]), newBodyAtoms); 
	}
	
	public static DLClause simplified(DLClause clause) {
		TermGraph termGraph = new TermGraph(clause); 
		return termGraph.simplify(); 
	}

	public static DLClause contructor_differentFrom(Individual u,	Individual v) {
		Atom equalityAtom = Atom.create(AtomicRole.create(Namespace.EQUALITY), u, v);
		return DLClause.create(new Atom[0], new Atom[] {equalityAtom});  
	}
	
	public static String toString(Collection<DLClause> clauses) {
		StringBuilder buf = new StringBuilder();
		for (DLClause cls: clauses) {
			buf.append(RuleHelper.getText(cls));
			buf.append(Utility.LINE_SEPARATOR); 
		}
		return buf.toString(); 
	}

	public static DLClause getInstance(DLClause queryClause, Map<Variable, Term> assignment) {
		Atom[] newBodyAtoms = new Atom[queryClause.getBodyLength()];
		int index = 0; 
		for (Atom atom: queryClause.getBodyAtoms()) {
			newBodyAtoms[index++] = getInstance(atom, assignment); 
		}
		return DLClause.create(queryClause.getHeadAtoms(), newBodyAtoms);
	}
	
	public static Atom getInstance(Atom atom, Map<Variable, Term> assignment) {
		Term[] args = getArguments(atom);
		for (int i = 0; i < args.length; ++i)
			args[i] = getInstance(args[i], assignment); 
		return Atom.create(atom.getDLPredicate(), args);
	}
	
	private static Term getInstance(Term t, Map<Variable, Term> assignment) {
		if (t instanceof Variable && assignment.containsKey((Variable) t)) 
			return assignment.get(t);
		return t;
	}

	public static Term[] getArguments(Atom atom) {
		int len = atom.getArity(); 
		if (len >= 2) len = 2; 
		Term[] args = new Term[len];
		for (int i = 0; i < len; ++i)
			args[i] = atom.getArgument(i);
		return args;
	}
	
	public static DLClause getQuery(String queryText, Collection<String> answerVariables) {
		Collection<Atom> bodyAtoms = new LinkedList<Atom>();
		SparqlHelper.parse(queryText.replace("_:", "?"), answerVariables, bodyAtoms);
		return DLClause.create(new Atom[0], bodyAtoms.toArray(new Atom[0]));
	}
	
	public static DLClause getQuery_old(String queryText, Collection<String> answerVariables) {
		String key, value;
		Prefixes prefixes = new Prefixes(); 
		if (answerVariables != null) 
			answerVariables.clear();
		String[] temp;
		Term subject, object;
		LinkedList<Atom> bodyAtoms = new LinkedList<Atom>(); 

		for (String line: queryText.split("\n")) {
			line = line.trim(); 
			if (line.startsWith("PREFIX")) {
				key = line.substring(7, line.indexOf(':') + 1);
				value = line.substring(line.indexOf('<') + 1, line.length() - 1);
				prefixes.declarePrefix(key, value);
			} 
			else if (line.startsWith("SELECT")) {
				if (answerVariables == null)
					continue;
				temp = line.split(" ");
				for (int i = 1; i < temp.length; ++i)
					answerVariables.add(temp[i].substring(1));
			} 
			else if (line.startsWith("WHERE"))
				;
			else if (line.startsWith("}"))
				;
			else {
				temp = line.split(" ");
				subject = getTerm(getAbsoluteIRI(temp[0], prefixes));
				temp[1] = getAbsoluteIRI(temp[1], prefixes);
				temp[2] = getAbsoluteIRI(temp[2], prefixes); 
				if (temp[1].equals(Namespace.RDF_TYPE)) {
					temp[2] = getAbsoluteIRI(temp[2], prefixes); 
					bodyAtoms.add(Atom.create(AtomicConcept.create(temp[2]), subject));
				} else {
					object = getTerm(temp[2]);
					Term[] args = {subject, object};
					bodyAtoms.add(Atom.create(AtomicRole.create(temp[1]), args));
				}
			}
		}

		return DLClause.create(new Atom[0], bodyAtoms.toArray(new Atom[0]));
	}

	private static String getAbsoluteIRI(String iri, Prefixes prefixes) {
		if (prefixes.canBeExpanded(iri)) 
			return prefixes.expandAbbreviatedIRI(iri);
		if (iri.startsWith("<"))
			return OWLHelper.removeAngles(iri);
		return iri; 
	}

	private static Term getTerm(String iri) {
		if (iri.startsWith("?"))
			return Variable.create(iri.substring(1));
		else if (iri.contains("http")) 
			return Individual.create(iri); 
		else 
			return getConstant(iri); 
	}

	private static Constant getConstant(String iri) {
		String lexicalForm, datatypeIRI; 
		int index = iri.indexOf("^^"); 
		if (index == -1) { 
			lexicalForm = iri;
			int atPos = iri.indexOf("@"); 
			datatypeIRI = atPos == -1 ? Namespace.XSD_STRING : Namespace.RDF_PLAIN_LITERAL;
		}
		else { 
			if (iri.charAt(index + 2) == '<')
				datatypeIRI = iri.substring(index + 3, iri.length() - 1);
			else 
				datatypeIRI = iri.substring(index + 2);
			lexicalForm = iri.substring(1, index - 1); 
		}
		return Constant.create(lexicalForm, datatypeIRI); 
	}

	public static boolean isFunctionalDataPropertyAxioms(DLClause dlClause) {
		 if (dlClause.getBodyLength()==2 && dlClause.getHeadLength()==1) {
	            DLPredicate atomicRole=dlClause.getBodyAtom(0).getDLPredicate();
	            if (atomicRole instanceof AtomicRole) {
	                if (dlClause.getBodyAtom(1).getDLPredicate().equals(atomicRole) && 
	                		(dlClause.getHeadAtom(0).getDLPredicate() instanceof Equality ||
	                				dlClause.getHeadAtom(0).getDLPredicate() instanceof AnnotatedEquality)) {
	                    Variable x=dlClause.getBodyAtom(0).getArgumentVariable(0);
	                    if (x!=null && x.equals(dlClause.getBodyAtom(1).getArgument(0))) {
	                        Variable y1=dlClause.getBodyAtom(0).getArgumentVariable(1);
	                        Variable y2=dlClause.getBodyAtom(1).getArgumentVariable(1);
	                        Variable headY1=dlClause.getHeadAtom(0).getArgumentVariable(0);
	                        Variable headY2=dlClause.getHeadAtom(0).getArgumentVariable(1);
	                        if (y1!=null && y2!=null && !y1.equals(y2) && headY1!=null && headY2!=null && ((y1.equals(headY1) && y2.equals(headY2)) || (y1.equals(headY2) && y2.equals(headY1))))
	                            return true;
	                    }
	                }
	            }
	        }
	        return false;
	}
	
	/**
	 * 
	 * @param dlClause
	 * @return 000 -> 111 R \circ S \sqsubseteq T if R, S, T is inverse. -1 if it is not a role composition axiom.
	 */
	public static int isRoleCompositionAxioms(DLClause dlClause) {
		 if (dlClause.getBodyLength()==2 && dlClause.getHeadLength()==1) {
			 Atom body1 = dlClause.getBodyAtom(0), body2 = dlClause.getBodyAtom(1);
			 Atom head = dlClause.getHeadAtom(0); 
			 if (!(head.getDLPredicate() instanceof AtomicRole)) return -1; 
			 if (!(body1.getDLPredicate() instanceof AtomicRole)) return -1;
			 if (!(body2.getDLPredicate() instanceof AtomicRole)) return -1;
			 if (!(head.getArgument(0) instanceof Variable)) return -1;
			 if (!(head.getArgument(1) instanceof Variable)) return -1;
			 if (!(body1.getArgument(0) instanceof Variable)) return -1;
			 if (!(body1.getArgument(1) instanceof Variable)) return -1;
			 if (!(body2.getArgument(0) instanceof Variable)) return -1;
			 if (!(body2.getArgument(1) instanceof Variable)) return -1;
			 Variable rx, ry, sx, sy, tx, ty;
			 tx = head.getArgumentVariable(0); ty = head.getArgumentVariable(1); 
			 rx = body1.getArgumentVariable(0); ry = body1.getArgumentVariable(1); 
			 sx = body2.getArgumentVariable(0); sy = body2.getArgumentVariable(1);
			 
			 if (rx.equals(tx))
				 if (ry.equals(sx))
					 if (sy.equals(ty)) return 0; 
					 else return -1; 
				 else if (ry.equals(sy))
					 if (sx.equals(ty)) return 2;
					 else return -1; 
				 else 
					 return -1; 
			 else if (rx.equals(ty))
				 if (ry.equals(sx))
					 if (sy.equals(tx)) return 1; 
					 else return -1; 
				 else if (ry.equals(sy))
					 if (sx.equals(tx)) return 3; 
					 else return -1; 
				 else
					 return -1; 
			 else if (ry.equals(tx)) 
				 if (rx.equals(sx))
					 if (sy.equals(ty)) return 4; 
					 else return -1; 
				 else if (rx.equals(sy))
					 if (sx.equals(ty)) return 6; 
					 else return -1; 
				 else 
					 return -1; 
			 else if (ry.equals(ty))
				 if (rx.equals(sx))
					 if (sy.equals(tx)) return 5; 
					 else return -1; 
				 else if (rx.equals(sy))
					 if (sx.equals(tx)) return 7; 
					 else return -1; 
				 else return -1; 
			 else 
				 return -1; 
		 }
		 return -1;
	}

	public static boolean isGround(Atom tHeadAtom) {
		for (int i = 0; i < tHeadAtom.getArity(); ++i)
			if (tHeadAtom.getArgument(i) instanceof Variable)
				return false; 
		return true;
	}
	/**
	 * true if a \subseteq b
	 * @param a
	 * @param b
	 * @return
	 */
	private static boolean hasSubsetAtoms(Atom[] a, Atom[] b) {
		Set<String> atoms = new HashSet<String>();
		for (int i = 0; i < b.length; ++i)
			atoms.add(b[i].toString()); 
		
		for (int i = 0; i < a.length; ++i)
			if (!atoms.remove(a[i].toString()))
				return false; 
		
		return true;
	}

	public static boolean hasSubsetBodyAtoms(DLClause c1, DLClause c2) {
		return hasSubsetAtoms(c1.getBodyAtoms(), c2.getBodyAtoms()); 
	}

	public static DLClause replaceOtherBottom(DLClause dlClause) {
		Atom[] newHeadAtoms = dlClause.getHeadAtoms(), newBodyAtoms = dlClause.getBodyAtoms();
		if (containsOtherBottom(newHeadAtoms))
			newHeadAtoms = replaceOtherBottom(newHeadAtoms); 
		if (containsOtherBottom(newBodyAtoms))
			newBodyAtoms = replaceOtherBottom(newBodyAtoms); 

		if (newHeadAtoms == dlClause.getHeadAtoms() && newBodyAtoms == dlClause.getBodyAtoms())
			return dlClause;
		
		return DLClause.create(newHeadAtoms, newBodyAtoms); 
	}
	
	private static Atom[] replaceOtherBottom(Atom[] atoms) {
		Atom[] newAtoms = new Atom[atoms.length];
		int index = 0; 
		for (Atom atom: atoms) 
			if (isOtherBottom(atom.getDLPredicate()))
				newAtoms[index++] = Atom.create(AtomicConcept.NOTHING, atom.getArgument(0)); 
			else 
				newAtoms[index++] = atom;
		return newAtoms; 
	}

	private static boolean isOtherBottom(DLPredicate p) {
		if (!(p instanceof AtomicConcept)) return false; 
		
		String name = p.toString();
		int index; 
		char ch; 
		if ((index = name.indexOf("owl:Nothing")) != -1) {
			index += 11; 
			if (name.length() <= index) return true;  
			if ((ch = name.charAt(index)) == '_') return true; 
			if (ch >= '0' && ch <= '9') return true; 
		}
		return false; 
	}
	
	private static boolean containsOtherBottom(Atom[] atoms) {
		for (Atom atom: atoms) 
			if (isOtherBottom(atom.getDLPredicate()))
				return true; 
		return false; 
	}

	public static boolean isTautologyAboutDifferentFrom(DLClause clause) {
		if (clause.getHeadLength() > 1 || clause.getBodyLength() != 1) return false;
		if (clause.getHeadLength() == 1 && !clause.getHeadAtom(0).getDLPredicate().toString().contains(AtomicConcept.NOTHING.toString()))
			return false;
		Atom bodyAtom = clause.getBodyAtom(0); 
		return (bodyAtom.getDLPredicate() instanceof Inequality) && bodyAtom.getArgument(0).equals(bodyAtom.getArgument(1)); 
	}

}
