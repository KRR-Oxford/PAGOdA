package uk.ac.ox.cs.pagoda.util;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.semanticweb.HermiT.model.AnnotatedEquality;
import org.semanticweb.HermiT.model.AtLeastConcept;
import org.semanticweb.HermiT.model.Atom;
import org.semanticweb.HermiT.model.AtomicConcept;
import org.semanticweb.HermiT.model.AtomicDataRange;
import org.semanticweb.HermiT.model.AtomicRole;
import org.semanticweb.HermiT.model.Constant;
import org.semanticweb.HermiT.model.DLPredicate;
import org.semanticweb.HermiT.model.Equality;
import org.semanticweb.HermiT.model.Individual;
import org.semanticweb.HermiT.model.Inequality;
import org.semanticweb.HermiT.model.Term;
import org.semanticweb.HermiT.model.Variable;

import uk.ac.ox.cs.pagoda.MyPrefixes;
import uk.ac.ox.cs.pagoda.hermit.RuleHelper;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.sparql.core.TriplePath;
import com.hp.hpl.jena.sparql.core.Var;
import com.hp.hpl.jena.sparql.syntax.Element;
import com.hp.hpl.jena.sparql.syntax.ElementAssign;
import com.hp.hpl.jena.sparql.syntax.ElementBind;
import com.hp.hpl.jena.sparql.syntax.ElementData;
import com.hp.hpl.jena.sparql.syntax.ElementDataset;
import com.hp.hpl.jena.sparql.syntax.ElementExists;
import com.hp.hpl.jena.sparql.syntax.ElementFilter;
import com.hp.hpl.jena.sparql.syntax.ElementGroup;
import com.hp.hpl.jena.sparql.syntax.ElementMinus;
import com.hp.hpl.jena.sparql.syntax.ElementNamedGraph;
import com.hp.hpl.jena.sparql.syntax.ElementNotExists;
import com.hp.hpl.jena.sparql.syntax.ElementOptional;
import com.hp.hpl.jena.sparql.syntax.ElementPathBlock;
import com.hp.hpl.jena.sparql.syntax.ElementService;
import com.hp.hpl.jena.sparql.syntax.ElementSubQuery;
import com.hp.hpl.jena.sparql.syntax.ElementTriplesBlock;
import com.hp.hpl.jena.sparql.syntax.ElementUnion;
import com.hp.hpl.jena.sparql.syntax.ElementVisitor;

public class SparqlHelper {

	public static String getSPARQLQuery(Atom[] atoms, String... vars) {
		Set<Variable> undistinguishedVars = new HashSet<Variable>(); 
		for (int i = 0; i < atoms.length; ++i) {
			atoms[i].getVariables(undistinguishedVars);
		}
		int xIndex = 1; 
		while (undistinguishedVars.contains(Variable.create("X" + xIndex))) ++xIndex;
		
		for (String var: vars) 
			if (var != null && !var.isEmpty())
				undistinguishedVars.remove(Variable.create(var));
		
		StringBuffer buffer = new StringBuffer();
		if (vars.length > 0)
			buffer.append("SELECT DISTINCT ");
		else 
			buffer.append("SELECT *");
		for (int i = 0; i < vars.length; ++i) {
			if (vars[i] != null && !vars[i].isEmpty())
				buffer.append("?").append(vars[i]).append(" ");
		}
		buffer.append( " WHERE {");
		for (Atom atom: atoms) 
			if (atom.getDLPredicate() instanceof AtLeastConcept) {
				AtLeastConcept atLeast = (AtLeastConcept) atom.getDLPredicate();
				int number = atLeast.getNumber(); 
				for (int i = 0; i < number; ++i) {
					Variable newVar = Variable.create("X" + (xIndex + i));

					Atom tAtom; 
					if (atLeast.getOnRole() instanceof AtomicRole)  
						tAtom = Atom.create(
								(AtomicRole) atLeast.getOnRole(), 
								atom.getArgument(0), 
								newVar); 
					else
						tAtom = Atom.create(
								(AtomicRole) atLeast.getOnRole().getInverse(),
								newVar, 
								atom.getArgument(0)); 
					buffer.append(" ");
					buffer.append(toSPARQLClause(tAtom, undistinguishedVars));
					buffer.append(" .");
				
					if (!atLeast.getToConcept().equals(AtomicConcept.THING)) {
						if (atLeast.getToConcept() instanceof AtomicConcept); 
						
						tAtom = Atom.create((AtomicConcept) atLeast.getToConcept(), newVar);
						buffer.append(" ");
						buffer.append(toSPARQLClause(tAtom, undistinguishedVars));
						buffer.append(" .");
					}
				}
				
				for (int i = 0; i < number; ++i)
					for (int j = i + 1; j < number; ++j) {
						Atom tAtom = Atom.create(Inequality.INSTANCE, Variable.create("X" + (xIndex + i)), Variable.create("X" + (xIndex + j))); 
						buffer.append(" ");
						buffer.append(toSPARQLClause(tAtom, undistinguishedVars));
						buffer.append(" .");
					}
				
				xIndex += number; 
			}
			else {
				buffer.append(" ");
				buffer.append(toSPARQLClause(atom, undistinguishedVars));
				buffer.append(" .");
			}
		buffer.append(" ").append("}");
		return buffer.toString();
	}
	
	private static String toSPARQLClause(Atom atom, Set<Variable> undisVars ) {
		DLPredicate predicate = atom.getDLPredicate();
		String r, a, b;
		
		if (predicate instanceof Equality || predicate instanceof AnnotatedEquality) 
			atom = Atom.create(predicate = AtomicRole.create(Namespace.EQUALITY), atom.getArgument(0), atom.getArgument(1));
		else if (predicate instanceof Inequality)
			atom = Atom.create(predicate = AtomicRole.create(Namespace.INEQUALITY), atom.getArgument(0), atom.getArgument(1));
		
		if (predicate instanceof AtomicConcept) {
			r = Namespace.RDF_TYPE_QUOTED;
			a = MyPrefixes.PAGOdAPrefixes.getQuotedIRI(getName(atom.getArgument(0), undisVars));
			b = MyPrefixes.PAGOdAPrefixes.getQuotedIRI(RuleHelper.getText(predicate)); 
		}
		else if (predicate instanceof AtomicRole) {
			r = MyPrefixes.PAGOdAPrefixes.getQuotedIRI(RuleHelper.getText(predicate));
			a = MyPrefixes.PAGOdAPrefixes.getQuotedIRI(getName(atom.getArgument(0), undisVars)); 
			b = MyPrefixes.PAGOdAPrefixes.getQuotedIRI(getName(atom.getArgument(1), undisVars)); 
		}
		else if (predicate instanceof AtomicDataRange) {
			r = Namespace.RDF_TYPE_QUOTED;
			a = MyPrefixes.PAGOdAPrefixes.getQuotedIRI(getName(atom.getArgument(0), undisVars));
			b = MyPrefixes.PAGOdAPrefixes.getQuotedIRI(RuleHelper.getText(predicate)); 
		}
 		else {
			Utility.logError("error!!!!!!!!!!!");
			return null;
		}
		
		return a + " " + r + " " + b; 
	}
	
	private static String getName(Term t, Set<Variable> undisVars) {
		if (t instanceof Variable)
			if (undisVars.contains(t))
				return "_:" + ((Variable) t).getName(); 
			else return "?" + ((Variable) t).getName(); 
		return MyPrefixes.PAGOdAPrefixes.abbreviateIRI(t.toString());
	}

	public static Query parse(String text, Collection<String> vars, Collection<Atom> atoms) {
		Query query = QueryFactory.create(text);
		if (vars != null) {
			vars.clear();
			for (Var var: query.getProjectVars()) 
				vars.add(var.getName());
		}
		ElementVisitor visitor = new MySparqlElementVisitor(atoms); 
		query.getQueryPattern().visit(visitor);
		return query; 
	}
	
}

class MySparqlElementVisitor implements ElementVisitor {
	
	Collection<Atom> atoms; 
	
	public MySparqlElementVisitor(Collection<Atom> atoms) {
		this.atoms = atoms; 
	}
	
	@Override
	public void visit(ElementSubQuery el) {
		Utility.logError("ElmentSubQuery: " + el); 
	}
	
	@Override
	public void visit(ElementService el) {
		// TODO Auto-generated method stub
		Utility.logError("ElementService: " + el); 
	}
	
	@Override
	public void visit(ElementMinus el) {
		// TODO Auto-generated method stub
		Utility.logError("ElementMinus: " + el); 
	}
	
	@Override
	public void visit(ElementNotExists el) {
		// TODO Auto-generated method stub
		Utility.logError("ElementNotExists: " + el); 
	}
	
	@Override
	public void visit(ElementExists el) {
		// TODO Auto-generated method stub
		Utility.logError("ElementExists: " + el); 
	}
	
	@Override
	public void visit(ElementNamedGraph el) {
		// TODO Auto-generated method stub
		Utility.logError("ElementNamedGraph: " + el); 
	}
	
	@Override
	public void visit(ElementDataset el) {
		// TODO Auto-generated method stub
		Utility.logError("ElementDataset: " + el); 
	}
	
	@Override
	public void visit(ElementGroup el) {
		// TODO Auto-generated method stub
		for (Element e: el.getElements())
			e.visit(this);
	}
	
	@Override
	public void visit(ElementOptional el) {
		// TODO Auto-generated method stub
		Utility.logError("ElementOptional: " + el); 
	}
	
	@Override
	public void visit(ElementUnion el) {
		// TODO Auto-generated method stub
		Utility.logError("ElementUnion: " + el); 
	}
	
	@Override
	public void visit(ElementBind el) {
		// TODO Auto-generated method stub
		Utility.logError("ElementBind: " + el); 
	}
	
	@Override
	public void visit(ElementAssign el) {
		// TODO Auto-generated method stub
		Utility.logError("ElementAssign: " + el); 
	}
	
	@Override
	public void visit(ElementFilter el) {
		// TODO Auto-generated method stub
		Utility.logError("ElementFilter: " + el); 
	}
	
	@Override
	public void visit(ElementPathBlock el) {
		// TODO Auto-generated method stub
		for (TriplePath p: el.getPattern().getList()) {
			if (p.getPredicate().isVariable()) {
				AtomicRole r = AtomicRole.create("?" + p.getPredicate().getName()); 
				Term a = getTerm(p.getSubject()), b = getTerm(p.getObject());
				atoms.add(Atom.create(r, a, b)); 
			}
			else if (p.getPredicate().getURI().equals(Namespace.RDF_TYPE) && !p.getObject().isVariable()) {
				AtomicConcept A = AtomicConcept.create(p.getObject().getURI()); 
				Term c = getTerm(p.getSubject()); 
				atoms.add(Atom.create(A, c));
			}
			else {
				AtomicRole r = AtomicRole.create(p.getPredicate().getURI()); 
				Term a = getTerm(p.getSubject()), b = getTerm(p.getObject());
				atoms.add(Atom.create(r, a, b)); 
			}
		}
	}
	
	private Term getTerm(Node node) {
		if (node.isVariable()) 
			return Variable.create(node.getName());
		if (node.isLiteral()) 
			if (node.getLiteralDatatypeURI() == null)
				return Constant.create(node.getLiteralLexicalForm(), Namespace.XSD_STRING);
			else 
				return Constant.create(node.getLiteralLexicalForm(), node.getLiteralDatatypeURI());
			
			
		if (node.isURI())
			return Individual.create(node.getURI());
		Utility.logError("unknown node: " + node);
		return null; 
	}

	@Override
	public void visit(ElementTriplesBlock el) {
		// TODO Auto-generated method stub
		
		Utility.logError("ElementTriplesBlock: " + el); 
	}

	@Override
	public void visit(ElementData el) {
		// TODO Auto-generated method stub
		
	}
}