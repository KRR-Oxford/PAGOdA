package uk.ac.ox.cs.data;

import java.io.*;
import java.util.HashMap;
import java.util.Map;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.Node_URI;
import com.hp.hpl.jena.graph.Node_Variable;
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

import uk.ac.ox.cs.pagoda.query.QueryManager;
import uk.ac.ox.cs.pagoda.util.Namespace;

public class PrepareQueries4Hydrowl {

	public static void main(String[] args) throws FileNotFoundException {
		if (args.length == 0)
//			args = new String[] {"/media/krr-nas-share/Yujiao/ontologies/dbpedia/queries/atomic_ground.sparql"}; 
			args = new String[] {"/home/yzhou/temp/ontologies/reactome/example.sparql"}; 
//		String fileName = args[0].substring(args[0].lastIndexOf(Utility.FILE_SEPARATOR) + 1); 
		
		PrintStream ps = new PrintStream(new File(args[0].replace(".sparql", "_hydrowl.sparql"))); 
		if (ps != null)	System.setOut(ps);
		
		StringBuilder sb = new StringBuilder(); 
		Map<String, Integer> vars = new HashMap<String, Integer>(); 
		for (String text: QueryManager.collectQueryTexts(args[0])) {
			Query query = QueryFactory.create(text);
			for (Var var: query.getProjectVars())
				sb.append(sb.length() == 0 ? "Q(?" : ",?").append(var.getName());
			sb.append(") <- ");
			ElementVisitor visitor = new HydrowlGeneratorVisitor(sb); 
			query.getQueryPattern().visit(visitor);
			sb.setLength(sb.length() - 2);
			System.out.println(sb);
			sb.setLength(0);
			vars.clear();
		}
		
		if (ps != null) ps.close();
	}

}

class HydrowlGeneratorVisitor implements ElementVisitor {
	
	StringBuilder m_text; 

	public HydrowlGeneratorVisitor(StringBuilder text) {
		m_text = text; 
	}

	@Override
	public void visit(ElementTriplesBlock el) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void visit(ElementPathBlock el) {
		// TODO Auto-generated method stub
		for (TriplePath p: el.getPattern().getList()) {
			if (p.getPredicate().getURI().equals(Namespace.RDF_TYPE) && !p.getObject().isVariable()) 
				m_text.append(p.getObject().getURI()).append("(").append(getURI(p.getSubject())).append("), "); 
			else 
				m_text.append(p.getPredicate().getURI()).append("(").append(getURI(p.getSubject())).append(", ").append(getURI(p.getObject())).append("), "); 
		}		
	}

	private String getURI(Node node) {
		if (node instanceof Node_URI) return node.getURI();
		if (node instanceof Node_Variable) return "?" + node.getName();
		System.out.println("Unknown node: " + node); 
		return null;
	}

	@Override
	public void visit(ElementFilter el) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void visit(ElementAssign el) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void visit(ElementBind el) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void visit(ElementUnion el) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void visit(ElementOptional el) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void visit(ElementGroup el) {
		// TODO Auto-generated method stub
		for (Element e: el.getElements())
			e.visit(this);		
	}

	@Override
	public void visit(ElementDataset el) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void visit(ElementNamedGraph el) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void visit(ElementExists el) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void visit(ElementNotExists el) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void visit(ElementMinus el) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void visit(ElementService el) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void visit(ElementSubQuery el) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void visit(ElementData el) {
		// TODO Auto-generated method stub
		
	}
	
}