package org.semanticweb.karma2.model;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Map;

import org.semanticweb.HermiT.model.Atom;
import org.semanticweb.HermiT.model.Term;
import org.semanticweb.karma2.exception.IllegalInputQueryException;
import org.semanticweb.karma2.model.cqparser.ConjunctiveQueryParser;

import uk.ac.ox.cs.JRDFox.Prefixes;


public class ConjunctiveQuery {

	
	protected final Atom[] m_queryAtoms;
    protected final Term[] m_answerTerms;
    protected final Term[] m_resultBuffer;
    protected Prefixes prefixes;

    public ConjunctiveQuery(Atom[] queryAtoms,Term[] answerTerms) {
        m_queryAtoms=queryAtoms;
        m_answerTerms=answerTerms;
        m_resultBuffer=answerTerms.clone();
        prefixes = new Prefixes();
    }
    
    
    public Prefixes getPrefixes() {
    	return this.prefixes;
    }
    
    public ConjunctiveQuery(Atom[] queryAtoms,Term[] answerTerms, Prefixes prefixes) {
        m_queryAtoms=queryAtoms;
        m_answerTerms=answerTerms;
        m_resultBuffer=answerTerms.clone();
        this.prefixes = prefixes;
    }

    public int getNumberOfQueryAtoms() {
        return m_queryAtoms.length;
    }
    public Atom getQueryAtom(int atomIndex) {
        return m_queryAtoms[atomIndex];
    }
    public int getNumberOfAnswerTerms() {
        return m_answerTerms.length;
    }
    public Term getAnswerTerm(int termIndex) {
        return m_answerTerms[termIndex];
    }
   

    public String toString() {
    	String res = "";
    	Map<String, String> iriMap = prefixes.getPrefixIRIsByPrefixName();
    	for (String shortIri: iriMap.keySet()) 
    		res += "prefix " + shortIri + " <" + iriMap.get(shortIri)  + ">\n";
    	res += "prefix rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n";
    	res += "select ";
    	for (Term t: m_answerTerms)
    		res+= " " + t + " ";
    	res += " WHERE { ";
    	for (Atom a : m_queryAtoms){
    		if (a.getArity() == 1)
    			res+= a.getArgument(0) + " rdf:type " + a.getDLPredicate().toString().replace('<', ' ').replace('>', ' ') + " . ";
    		if (a.getArity() == 2)
    			res+= a.getArgument(0) + "  " + a.getDLPredicate().toString().replace('<', ' ').replace('>', ' ') + " " + a.getArgument(1) + " . ";
    	}
    	return res + "}";
    	
    }
    
    
    public static ConjunctiveQuery parse(String query) throws FileNotFoundException, IllegalInputQueryException, IOException {
    	return (new ConjunctiveQueryParser(query)).parse();
    }
}