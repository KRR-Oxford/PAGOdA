package uk.ac.ox.cs.pagoda.reasoner.light;

import org.semanticweb.HermiT.model.*;
import uk.ac.ox.cs.JRDFox.JRDFStoreException;
import uk.ac.ox.cs.JRDFox.model.Datatype;
import uk.ac.ox.cs.JRDFox.model.GroundTerm;
import uk.ac.ox.cs.JRDFox.store.DataStore;
import uk.ac.ox.cs.JRDFox.store.DataStore.UpdateType;
import uk.ac.ox.cs.JRDFox.store.Dictionary;
import uk.ac.ox.cs.JRDFox.store.Resource;
import uk.ac.ox.cs.pagoda.owl.OWLHelper;
import uk.ac.ox.cs.pagoda.util.Namespace;

import java.util.*;

public class RDFoxTripleManager {
	
	UpdateType m_incrementally; 
//	boolean m_incrementally; 

	DataStore m_store;
	Dictionary m_dict; 
//	Set<Atom> triplesByTerm = new HashSet<Atom>();
	
	public RDFoxTripleManager(DataStore store, boolean incrementally) {
		m_store = store;
//		m_incrementally = incrementally; 
		if (incrementally)
			m_incrementally = UpdateType.ScheduleForAddition;
		else 
			m_incrementally = UpdateType.Add; 
		
		try {
			m_dict = store.getDictionary();
			resourceID = m_dict.resolveResources(
					new String[] {Namespace.RDF_TYPE,  Namespace.EQUALITY, Namespace.INEQUALITY},
					new int[] {Datatype.IRI_REFERENCE.value(), Datatype.IRI_REFERENCE.value(), Datatype.IRI_REFERENCE.value()} 
					); 
		} catch (JRDFStoreException e) {
			e.printStackTrace();
		}
	}
	
	public boolean isRdfTypeID(int id) {
		return id == resourceID[0]; 
	}
	
	public void addTripleByID(int[] tuple) {
//		System.out.println(getRawTerm(tuple[0]) + " " + getRawTerm(tuple[1]) + " " + getRawTerm(tuple[2]) + " ."); 
		try {
//			Resource[] rsc = new Resource[3]; 
//			m_dict.getResources(tuple, 0, 3, rsc);
//			
//			GroundTerm[] terms = new GroundTerm[3]; 
//			for (int i = 0; i < 3; ++i)
//				terms[i] = uk.ac.ox.cs.JRDFox.model.Individual.create(rsc[i].m_lexicalForm); 
//			m_store.addTriples(terms, m_incrementally);
			
			m_store.addTriplesByResourceIDs(tuple, m_incrementally);
		} catch (JRDFStoreException e) {
			e.printStackTrace();
		}
	}
	
	public void addTripleByTerm(Atom atom) {
		try {
			m_store.addTriples(getRDFoxTriple(atom), m_incrementally);
		} catch (JRDFStoreException e) {
			e.printStackTrace();
		} 
	}
	
	public static GroundTerm[] getRDFoxTriple(Atom instance) {
		if (instance.getArity() == 1) 
			return new GroundTerm[] {
					uk.ac.ox.cs.JRDFox.model.Individual.create(((Individual) instance.getArgument(0)).getIRI()), 
					uk.ac.ox.cs.JRDFox.model.Individual.RDF_TYPE, 
					uk.ac.ox.cs.JRDFox.model.Individual.create(((AtomicConcept) instance.getDLPredicate()).getIRI()) };
		else if (instance.getDLPredicate() instanceof Equality || instance.getDLPredicate() instanceof AnnotatedEquality) 
			return new GroundTerm[] {
					uk.ac.ox.cs.JRDFox.model.Individual.create(((Individual) instance.getArgument(0)).getIRI()), 
					uk.ac.ox.cs.JRDFox.model.Individual.SAME_AS, 
					uk.ac.ox.cs.JRDFox.model.Individual.create(((Individual) instance.getArgument(1)).getIRI()) };
		else if (instance.getDLPredicate() instanceof Inequality) 
			return new GroundTerm[] {
					uk.ac.ox.cs.JRDFox.model.Individual.create(((Individual) instance.getArgument(0)).getIRI()), 
					uk.ac.ox.cs.JRDFox.model.Individual.DIFFERENT_FROM, 
					uk.ac.ox.cs.JRDFox.model.Individual.create(((Individual) instance.getArgument(1)).getIRI()) }; 
		else 
			return new GroundTerm[] {
					uk.ac.ox.cs.JRDFox.model.Individual.create(((Individual) instance.getArgument(0)).getIRI()), 
					uk.ac.ox.cs.JRDFox.model.Individual.create(((AtomicRole) instance.getDLPredicate()).getIRI()), 
					uk.ac.ox.cs.JRDFox.model.Individual.create(((Individual) instance.getArgument(1)).getIRI()) };
	}
	
	int[] resourceID; // rdf:type, owl:sameAs, owl:differentFrom 

	public int[] getInstance(Atom atom, Map<Variable, Integer> assignment) {
		DLPredicate p = atom.getDLPredicate(); 
		if (p instanceof Equality || p instanceof AnnotatedEquality) 
			return new int[] {
					getResourceID(atom.getArgument(0), assignment),
					resourceID[1],
					getResourceID(atom.getArgument(1), assignment)
			}; 
		else if (p instanceof Inequality) 
			return new int[] {
					getResourceID(atom.getArgument(0), assignment), 
					resourceID[2], 
					getResourceID(atom.getArgument(1), assignment)
			}; 
		else if (atom.getArity() == 1) 
			return new int[] {
					getResourceID(atom.getArgument(0), assignment), 
					resourceID[0], 
					getResourceID(p)
			}; 
		else 
			return new int[] {
					getResourceID(atom.getArgument(0), assignment), 
					getResourceID(p), 
					getResourceID(atom.getArgument(1), assignment)
			}; 
	}
	
	public String getRawTerm(int id) {
		Resource[] res = new Resource[1]; 
		try {
			m_dict.getResources(new int[] {id}, 0, 1, res);
		} catch (JRDFStoreException e) {
			e.printStackTrace();
		}
		return getQuotedTerm(res[0]); 
	}

	Map<String, Integer> predicateCache = new HashMap<String, Integer>(); 
	
	public int getResourceID(DLPredicate p) {
		Integer id; 
		String name = p instanceof AtomicConcept ? ((AtomicConcept) p).getIRI() : ((AtomicRole) p).getIRI();
		if ((id = predicateCache.get(name)) != null) return id;
		try {
			predicateCache.put(name, id = resolveResource(name, Datatype.IRI_REFERENCE.value()));
			
		} catch (JRDFStoreException e) {
			e.printStackTrace();
		} 
		return id;
	}
	
	public int getResourceID(String name) {
		Integer id = null; 
		try {
			id =  resolveResource(name, Datatype.IRI_REFERENCE.value()); 
		} catch (JRDFStoreException e) {
			e.printStackTrace();
		}
		return id; 
	}
	
	private int resolveResource(String name, int type) throws JRDFStoreException {
		String[] lexicalForms = new String[] {name}; 
		int[] types = new int[] {type};
		return m_dict.resolveResources(lexicalForms, types)[0]; 
	}
	
	Map<Term, Integer> termCache = new HashMap<Term, Integer>(); 
	Queue<Term> termList = new LinkedList<Term>();
	int sizeLimit = 10000; 

	private int getResourceID(Term arg, Map<Variable, Integer> assignment) {
		// FIXME infinite loop
//		while (termCache.size() > sizeLimit)
//			termCache.remove(termList.poll());
		
		if (arg instanceof Variable) return assignment.get(arg);
		Integer id = null; 
		if ((id = termCache.get(arg)) != null)
			return id; 
		
//		if (arg instanceof Individual) {
			try {
				if (arg instanceof Individual)
					termCache.put(arg, id = resolveResource(((Individual) arg).getIRI(), Datatype.IRI_REFERENCE.value())); 
				else if (arg instanceof Constant)
					termCache.put(arg, id = resolveResource(((Constant) arg).getLexicalForm(), getDatatypeID(((Constant) arg).getDatatypeURI()))); 
				
			} catch (JRDFStoreException e) {
				e.printStackTrace();
			} 
//		}
			
		return id;
	}
	
	private static int getDatatypeID(String uri) {
		if (uri.equals("http://www.w3.org/2001/XMLSchema#string")) return Datatype.XSD_STRING.value(); 
		if (uri.equals("http://www.w3.org/1999/02/22-rdf-syntax-ns#PlainLiteral")) return Datatype.RDF_PLAIN_LITERAL.value(); 
		if (uri.equals("http://www.w3.org/2001/XMLSchema#integer")) return Datatype.XSD_INTEGER.value(); 
		if (uri.equals("http://www.w3.org/2001/XMLSchema#float")) return Datatype.XSD_FLOAT.value(); 
		if (uri.equals("http://www.w3.org/2001/XMLSchema#double")) return Datatype.XSD_DOUBLE.value(); 
		if (uri.equals("http://www.w3.org/2001/XMLSchema#boolean")) return Datatype.XSD_BOOLEAN.value(); 
		if (uri.equals("http://www.w3.org/2001/XMLSchema#dateTime")) return Datatype.XSD_DATE_TIME.value(); 
		if (uri.equals("http://www.w3.org/2001/XMLSchema#time")) return Datatype.XSD_TIME.value(); 
		if (uri.equals("http://www.w3.org/2001/XMLSchema#date")) return Datatype.XSD_DATE.value(); 
		if (uri.equals("http://www.w3.org/2001/XMLSchema#gYearMonth")) return Datatype.XSD_G_YEAR_MONTH.value(); 
		if (uri.equals("http://www.w3.org/2001/XMLSchema#gYear")) return Datatype.XSD_G_YEAR.value(); 
		if (uri.equals("http://www.w3.org/2001/XMLSchema#gMonthDay")) return Datatype.XSD_G_MONTH_DAY.value(); 
		if (uri.equals("http://www.w3.org/2001/XMLSchema#gDay")) return Datatype.XSD_G_DAY.value(); 
		if (uri.equals("http://www.w3.org/2001/XMLSchema#gMonth")) return Datatype.XSD_G_MONTH.value(); 
		if (uri.equals("http://www.w3.org/2001/XMLSchema#duration")) return Datatype.XSD_DURATION.value(); 
		
		return -1;
	}

	public int[] getResourceIDs(Collection<uk.ac.ox.cs.JRDFox.model.Individual> individuals) {
		String[] str = new String[individuals.size()]; 
		int[] types = new int[individuals.size()]; 
		int index = 0; 
		for (uk.ac.ox.cs.JRDFox.model.Individual individual : individuals) {
			types[index] = Datatype.IRI_REFERENCE.value(); 
			str[index++] = individual.getIRI(); 
		}
			
		try {
			return m_dict.resolveResources(str, types);
		} catch (JRDFStoreException e) {
			e.printStackTrace();
			return null; 
		} 
	}
	
	public static String getQuotedTerm(Resource r) {
		if (r.m_datatype.equals(Datatype.IRI_REFERENCE))
			return OWLHelper.addAngles(r.m_lexicalForm); 
		if (r.m_datatype.equals(Datatype.XSD_STRING) || r.m_datatype.equals(Datatype.RDF_PLAIN_LITERAL))
			return "\"" + r.m_lexicalForm + "\"";
		else 
			return "\"" + r.m_lexicalForm + "\"^^<" + r.m_datatype.getIRI() + ">"; 
	}
	
}
