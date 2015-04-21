package uk.ac.ox.cs.pagoda.util;

public class Namespace {
	
    public static final String RDF_NS = "http://www.w3.org/1999/02/22-rdf-syntax-ns#";
    public static final String RDFS_NS = "http://www.w3.org/2000/01/rdf-schema#";
    public static final String OWL_NS = "http://www.w3.org/2002/07/owl#";
    public static final String XSD_NS = "http://www.w3.org/2001/XMLSchema#";
    public static final String SWRL_NS = "http://www.w3.org/2003/11/swrl#";
    public static final String SWRLB_NS = "http://www.w3.org/2003/11/swrlb#";
    public static final String SWRLX_NS = "http://www.w3.org/2003/11/swrlx#";
    public static final String RULEML_NS = "http://www.w3.org/2003/11/ruleml#";
    
    public static final String RDF_TYPE_QUOTED = "<" + RDF_NS + "type>"; 
    public static final String RDF_TYPE = RDF_NS + "type"; 
    public static final String RDF_TYPE_ABBR = "rdf:type";
    
    public static final String EQUALITY = OWL_NS + "sameAs"; 
    public static final String EQUALITY_QUOTED = "<" + EQUALITY + ">"; 
    public static final String EQUALITY_ABBR = "owl:sameAs";

    public static final String INEQUALITY = OWL_NS + "differentFrom"; 
    public static final String INEQUALITY_ABBR = "owl:differentFrom"; 
	public static final String INEQUALITY_QUOTED = "<" + INEQUALITY + ">";

	public static final String RDF_PLAIN_LITERAL = RDF_NS + "PlainLiteral";
	public static final String XSD_STRING = XSD_NS + "string";
	
	public static final String PAGODA_ANONY = "http://www.cs.ox.ac.uk/PAGOdA/skolemised#";
	public static final String PAGODA_AUX = "http://www.cs.ox.ac.uk/PAGOdA/auxiliary#";
	public static final String KARMA_ANONY = "http://www.cs.ox.ac.uk/KARMA/anonymous"; 
	public static final String PAGODA_ORIGINAL = PAGODA_AUX + "Original";
 
}
