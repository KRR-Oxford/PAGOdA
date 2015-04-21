package uk.ac.ox.cs.data;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.Collection;
import java.util.HashSet;
import java.util.Scanner;
import java.util.Set;

import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLEquivalentClassesAxiom;
import org.semanticweb.owlapi.model.OWLOntology;
import uk.ac.ox.cs.pagoda.owl.OWLHelper;
import uk.ac.ox.cs.pagoda.util.Utility;

public class Comparator {

	public static void main(String[] args) throws IOException {
		compareFiles(args); 
	}
	
	public static void compareFiles(String[] args) throws IOException {
		String directory = "/users/yzhou/workspace/pagoda/"; 
		String name1 = "abox1.txt", name2 = "abox2.txt";
		
		args = (directory + name1 + " " +
				directory + name2 + " " +
				directory + "diff.dlog").split("\\ "); 
		
		Scanner s1 = new Scanner(new File(args[0])), s2 = new Scanner(new File(args[1]));
		HashSet<String> h1 = new HashSet<String>(), h2 = new HashSet<String>();
		while (s1.hasNextLine()) h1.add(s1.nextLine());
		s1.close();
		while (s2.hasNextLine()) h2.add(s2.nextLine().replace("an-minus.owl", "an.owl"));
		s2.close();
		
		BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(args[2]))); 

		writer.write("Elements in " + name1 + ", but not in " + name2);
		writer.newLine(); 
		for (String line: h1)
			if (!h2.contains(line)) {
				writer.write(line);
				writer.newLine();
			}
		
		writer.write("--------------------------------------------------------");
		writer.newLine();
		
		writer.write("Elements in " + name2 + ", but not in " + name1);
		writer.newLine();
		for (String line: h2) 
			if (!h1.contains(line)) {
				writer.write(line);
				writer.newLine();
			}
		
		writer.close();
	}
	
	
	public void compareOntologies(String[] args) throws IOException {
		String directory = "/home/scratch/yzhou/ontologies/fly/auxiliary/datalog/";
		String name1 = "eq/elho.owl", name2 = "noEQ/elho.owl"; 
		
		args = (directory + name1 + " " +
				directory + name2 + " " +
				directory + "diff.owl").split("\\ "); 
		
		OWLOntology o1 = OWLHelper.loadOntology(args[0]); 
		OWLOntology o2 = OWLHelper.loadOntology(args[1]);
		
		BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(args[2]))); 
		
		writer.write("Elements in " + name1 + ", but not in " + name2);
		writer.newLine(); 
		writer.write(compareOntologies(o1, o2));
		
		writer.write("--------------------------------------------------------");
		writer.newLine();
		
		writer.write("Elements in " + name2 + ", but not in " + name1);
		writer.newLine(); 
		writer.write(compareOntologies(o2, o1)); 
		
		writer.close();
	}
	
	private static String compareOntologies(OWLOntology o1, OWLOntology o2) {
		StringBuilder sb = new StringBuilder(); 
		
		Set<String> axioms = new HashSet<String>();
		OWLDataFactory factory1 = o1.getOWLOntologyManager().getOWLDataFactory(); 
		OWLDataFactory factory2 = o2.getOWLOntologyManager().getOWLDataFactory(); 
		
		for (OWLAxiom a: o2.getAxioms()) 
			for (OWLAxiom axiom: process(a, factory2)){
				axioms.add(axiom.toString());
			}
		
		for (OWLAxiom a: o1.getAxioms()) {
			for (OWLAxiom axiom: process(a, factory1))
				if (!axioms.contains(axiom.toString())) 
					sb.append(axiom.toString()).append(Utility.LINE_SEPARATOR);
		}

		return sb.toString(); 
	}

	private static Collection<OWLAxiom> process(OWLAxiom axiom, OWLDataFactory factory) {
		Set<OWLAxiom> axioms = new HashSet<OWLAxiom>(); 
		OWLEquivalentClassesAxiom equiv; 
		if (axiom instanceof OWLEquivalentClassesAxiom) {
			equiv = (OWLEquivalentClassesAxiom) axiom; 
			for (OWLClassExpression exp1: equiv.getClassExpressions())
				for (OWLClassExpression exp2: equiv.getClassExpressions())
					if (!exp1.equals(exp2)) 
						axioms.add(factory.getOWLSubClassOfAxiom(exp1, exp2)); 
		}
		else 
			axioms.add(axiom); 

		return axioms; 
	}
	
}
