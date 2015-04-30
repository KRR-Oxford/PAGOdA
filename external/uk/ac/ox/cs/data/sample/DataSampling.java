package uk.ac.ox.cs.data.sample;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.*;
import java.util.Map.Entry;

import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.impl.StatementImpl;
import org.openrdf.model.impl.URIImpl;
import org.openrdf.rio.RDFHandler;
import org.openrdf.rio.RDFHandlerException;
import org.openrdf.rio.RDFParseException;
import org.openrdf.rio.RDFParser;
import org.openrdf.rio.ntriples.NTriplesParser;
import org.openrdf.rio.turtle.*;

import uk.ac.ox.cs.pagoda.owl.OWLHelper;
import uk.ac.ox.cs.pagoda.util.Namespace;
import uk.ac.ox.cs.pagoda.util.Utility;

public class DataSampling {
	
	File[] m_list;
	RDFGraph m_graph;
	double m_percentage;
	Set<String> excludeEntities = new HashSet<String>(); 
	
	public DataSampling(String prefix, String fileName, String excludeFile, double percentage, boolean inTurtle) {
		if (excludeFile != null) {
			try {
				Scanner scanner = new Scanner(new File(excludeFile));
				while (scanner.hasNextLine())
					excludeEntities.add(OWLHelper.removeAngles(scanner.nextLine().trim())); 
				scanner.close(); 
			} catch (FileNotFoundException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		}
		excludeEntities.add("http://www.w3.org/2002/07/owl#imports"); 

		File file = new File(fileName); 
		if (file.isDirectory())	m_list = file.listFiles();
		else m_list = new File[] {file};
		m_percentage = percentage; 
		
		RDFParser parser = inTurtle ? new TurtleParser() : new NTriplesParser();
		
		GraphRDFHandler handler = new GraphRDFHandler(excludeEntities); 
		parser.setRDFHandler(handler);
		
		FileInputStream istream;
		try {
			for (File tFile: m_list) {
				parser.parse(istream = new FileInputStream(tFile), prefix);
				istream.close();
			}
		} catch (IOException e) {
			e.printStackTrace();
		} catch (RDFParseException e) {
			e.printStackTrace();
		} catch (RDFHandlerException e) {
			e.printStackTrace();
		}
		
		m_graph = handler.getGraph(); 
	}
	
	public void sample(String outputFile, boolean multiStart) {
		try {
			FileOutputStream ostream = new FileOutputStream(outputFile); 
			TurtleWriter writer = new TurtleWriter(ostream);
			writer.startRDF();
			
			if (m_percentage < 100) {
				Sampler sam = multiStart ? 
						new RandomWalkMulti(m_graph, writer) : 
							new RandomWalk(m_graph, writer);
				sam.setLimit((int) (m_graph.numberOfStatement / 100 * m_percentage));
				System.out.println("Statement limit: " + (m_graph.numberOfStatement / 100 * m_percentage)); 
				sam.sample();
				sam.dispose(); 
			}
			else {
				m_graph.visit(writer);
			}
			writer.endRDF();
			ostream.close();
		} catch (IOException e) {
			e.printStackTrace(); 
		} catch (RDFHandlerException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public static void main(String[] args) {
		sampleReactome();
//		sampleChEMBL(); 
	}
	
	static void sampleReactome() {
//		double[] ps = {40, 70, 100}; 
		double[] ps = {0.25, 0.5, 0.75}; 
		for (double per: ps) {
		DataSampling sampling = new DataSampling(
				"http://www.biopax.org/release/biopax-level3.owl#", 
//				"/home/yzhou/krr-nas-share/Yujiao/ontologies/bio2rdf/reactome/data/data.ttl", 
				"/home/yzhou/krr-nas-share/Yujiao/ontologies/bio2rdf/reactome/data/simplified.nt",
//				"/home/yzhou/krr-nas-share/Yujiao/ontologies/bio2rdf/reactome/graph sampling/reactome_sample_40.ttl", 
				"/home/yzhou/krr-nas-share/Yujiao/ontologies/bio2rdf/reactome/graph sampling/exclude",
				per, 
				true); 
		sampling.sample("/home/yzhou/krr-nas-share/Yujiao/ontologies/bio2rdf/reactome/graph sampling/sample_test_" + per + ".ttl", true);
//		sampling.sample("/home/yzhou/krr-nas-share/Yujiao/ontologies/bio2rdf/reactome/graph sampling/simplifed_sample_test_" + per + ".ttl", true);
//		sampling.sample("output/sample_reactome_multi.ttl", true);
		}
	}

	static void sampleChEMBL() {
		DataSampling sampling = new DataSampling(
				"http://rdf.ebi.ac.uk/terms/chembl", 
				"/home/yzhou/RDFdata/ChEMBL/facts/chembl_kbfile.nt", 
				null,
				100, 
				false);
		
		sampling.sample("output/sample_chembl_multi.ttl", true); 
		sampling.sample("output/sample_chembl.ttl", false); 
	}
	
}

class RDFGraph {
	
	Map<Value, Integer> index = new HashMap<Value, Integer>();
	Map<Integer, Value> inverseIndex = new HashMap<Integer, Value>(); 
	MapToList<Integer> labels = new MapToList<Integer>(); 
	
	MapToList<RDFEdge> edges = new MapToList<RDFEdge>();
	Set<String> excludeEntities; 
	
	int numberOfIndividuals = 0, numberOfProperties = 0; 
	
	public RDFGraph(Set<String> exclude) {
		excludeEntities = exclude; 
		for (String str: excludeEntities) 
			System.out.println(str); 
		System.out.println("---------------"); 
	}

	public void visit(TurtleWriter writer) throws RDFHandlerException {
		Integer key; 
		for (Entry<Integer, LinkedList<Integer>> entry: labels.entrySet()) {
			key = entry.getKey(); 
			for (Integer type: entry.getValue())
				writer.handleStatement(getStatement(key, type)); 
		}
		
		for (Entry<Integer, LinkedList<RDFEdge>> entry: edges.entrySet()) {
			key = entry.getKey();
			if ((inverseIndex.get(key) instanceof URI) && 
					((URI) inverseIndex.get(key)).toString().equals("http://www.reactome.org/biopax/46/879693#UnificationXref9"))
				System.out.println("Here");

			for (RDFEdge edge: entry.getValue())
				writer.handleStatement(getStatement(key, edge.m_label, edge.m_dst));
		}
	}

	private int getID(Value v, boolean isIndividual) {
		if (v.toString().contains("imports"))
			System.out.println(v.toString()); 
		if (excludeEntities.contains(v.toString())) {
			return 0; 
		}
		
		Integer id = index.get(v); 
		if (id == null)
			if (isIndividual) {
				index.put(v, id = ++numberOfIndividuals);
				inverseIndex.put(id, v);
			}
			else {
				index.put(v, id = --numberOfProperties);
				inverseIndex.put(id, v); 
			}
		return id; 
	}
	
	int numberOfStatement = 0;
	int counter = 0; 
	
	public void addTriple(Resource s, URI p, Value o) {
		++numberOfStatement; 
		if (numberOfStatement % 1000000 == 0) {
			Utility.logInfo("No.of statements: " + numberOfStatement, "\tNo.of individuals: " + numberOfIndividuals, "\tNo.of predicates: " + (-numberOfProperties));
		}
		
		if (p.equals(rdftype)) {
			int type = getID(o, false), i = getID(s, true);
			if (i == 0) {
//				System.out.println("<" + s + "> <" + p + "> <" + o + ">"); 
				return ; 
			}
			labels.add(i, type); 
		}
		else { 
			int i = getID(s, true), j = getID(o, true), prop = getID(p, false) ;
			if (i == 0 || j == 0 || prop == 0) {
//				System.out.println("<" + s + "> <" + p + "> <" + o + ">"); 
				return ; 
			}
			edges.add(i, new RDFEdge(prop, j)); 
		}
	}
	
	URI rdftype = new URIImpl(Namespace.RDF_TYPE); 

	public Statement getStatement(int... args) {
		if (args.length == 2) 
			return new StatementImpl((Resource) inverseIndex.get(args[0]), rdftype, (Value) inverseIndex.get(args[1]));
		else if (args.length == 3) 
			return new StatementImpl((Resource) inverseIndex.get(args[0]), (URI) inverseIndex.get(args[1]), (Value) inverseIndex.get(args[2]));
		return null;
	}

	public String getRawString(int id) {
		return inverseIndex.get(id).toString();
	}
	
}

class MapToList<T> {
	
	private Map<Integer, LinkedList<T>> map = new HashMap<Integer, LinkedList<T>>();
	
	public void add(int key, T value) {
		LinkedList<T> list = map.get(key); 
		if (list == null) 
			map.put(key, list = new LinkedList<T>()); 
		list.add(value); 
	}

	public Set<Map.Entry<Integer, LinkedList<T>>> entrySet() {
		return map.entrySet();
	}

	public void shuffle() {
		for (List<T> list: map.values())
			Collections.shuffle(list);
	}

	public LinkedList<T> get(int key) {
		return map.get(key); 
	}

}

class RDFEdge {
	
	int m_label, m_dst; 

	public RDFEdge(int label, int dst) {
		m_label = label; 
		m_dst = dst; 
	}

}

class GraphRDFHandler implements RDFHandler {
	
	RDFGraph m_graph;
	Set<String> m_exclude; 
	
	public GraphRDFHandler(Set<String> excludeEntities) {
		m_exclude = excludeEntities; 
	}
	
	@Override
	public void startRDF() throws RDFHandlerException {
		m_graph = new RDFGraph(m_exclude);		
	}

	public RDFGraph getGraph() {
		return m_graph;
	}

	@Override
	public void endRDF() throws RDFHandlerException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void handleNamespace(String prefix, String uri)
			throws RDFHandlerException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void handleStatement(Statement st) throws RDFHandlerException {
		m_graph.addTriple(st.getSubject(), st.getPredicate(), st.getObject());
	}

	@Override
	public void handleComment(String comment) throws RDFHandlerException {
		// TODO Auto-generated method stub
		
	}
	
}