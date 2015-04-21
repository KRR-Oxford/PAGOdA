package uk.ac.ox.cs.pagoda.rules;

import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.model.OWLOntology;
import uk.ac.ox.cs.pagoda.owl.OWLHelper;
import uk.ac.ox.cs.pagoda.util.Namespace;
import uk.ac.ox.cs.pagoda.util.Utility;

public class EqualityAxiomatiser {

	OWLOntology ontology; 
	
	public EqualityAxiomatiser(String fileName) {
		ontology = OWLHelper.loadOntology(OWLManager.createOWLOntologyManager(), fileName);
	}
	
	public EqualityAxiomatiser(OWLOntology ontology) {
		this.ontology = ontology; 
	}

	public static void main(String[] args) throws IOException {
		if (args.length == 0) {
			args = new String[1];
			args[0] = "../uobmGenerator/ontologies/2rl/univ-bench-dl-TBox.owl";
		}
		
		EqualityAxiomatiser axiomatiser; 
		for (String fileName: args) {
			axiomatiser = new EqualityAxiomatiser(fileName); 
			String outputFileName = fileName.replace(".owl", "-axiomatised.rule");
			BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(outputFileName)));
			writer.write(axiomatiser.getRuleTexts());
			writer.close();
		}
	}
	
	public String getRuleTexts() {
		StringBuffer buf = new StringBuffer(); 
//		buf.append(reflexivity()).append(Utility.LINE_SEPARATOR);
		buf.append(symmetry()).append(Utility.LINE_SEPARATOR);
		buf.append(transitivity()).append(Utility.LINE_SEPARATOR);
		
		for (OWLObjectProperty p: ontology.getObjectPropertiesInSignature(true)) 
			buf.append(addingEqualities4Properties(OWLHelper.addAngles(p.getIRI().toString()))).append(Utility.LINE_SEPARATOR);
		
		for (OWLClass c: ontology.getClassesInSignature(true)) 
			buf.append(addingEqualities4Class(OWLHelper.addAngles(c.getIRI().toString()))).append(Utility.LINE_SEPARATOR);

		return buf.toString(); 
	}
	
	private static String transitivity() {
		StringBuffer buffer = new StringBuffer();
		buffer.append(Namespace.EQUALITY_QUOTED).append("(?Y0,?Y2) :- ").append(Namespace.EQUALITY_QUOTED ).append("(?Y0,?Y1), ").append(Namespace.EQUALITY_QUOTED ).append("(?Y1,?Y2) .");
		return buffer.toString();
	}
	
	private static String symmetry() {
		StringBuffer buffer = new StringBuffer();
		buffer.append(Namespace.EQUALITY_QUOTED ).append("(?Y1,?Y0) :- ").append(Namespace.EQUALITY_QUOTED ).append("(?Y0,?Y1) .");
		return buffer.toString();
	}

	@SuppressWarnings("unused")
	private static String reflexivity() {
		StringBuffer buffer = new StringBuffer();
		buffer.append(Namespace.EQUALITY_QUOTED ).append("(?Y0,?Y0) :- .");
		return buffer.toString();
	}
	
	private static String addingEqualities4Properties(String property) {
		StringBuffer buffer = new StringBuffer();
		buffer.append(property).append("(?Y2,?Y1) :- ").append(property).append("(?Y0,?Y1), ").append(Namespace.EQUALITY_QUOTED ).append("(?Y0,?Y2) .\n");
		buffer.append(property).append("(?Y0,?Y2) :- ").append(property).append("(?Y0,?Y1), ").append(Namespace.EQUALITY_QUOTED ).append("(?Y1,?Y2) .");
		return buffer.toString();
	}

	private static String addingEqualities4Class(String clazz) {
		StringBuffer buffer = new StringBuffer();
		buffer.append(clazz).append("(?Y1) :- ").append(clazz).append("(?Y0), ").append(Namespace.EQUALITY_QUOTED ).append("(?Y0,?Y1) .");
		return buffer.toString();
	}

}
