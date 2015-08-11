package uk.ac.ox.cs.pagoda.hermit;

import org.semanticweb.HermiT.model.DLClause;
import org.testng.Assert;
import org.testng.annotations.Test;
import uk.ac.ox.cs.pagoda.MyPrefixes;
import uk.ac.ox.cs.pagoda.owl.OWLHelper;
import uk.ac.ox.cs.pagoda.rules.DatalogProgram;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.Charset;

public class TestRuleHelper {

    @Test
    public static void test_disjunctiveRules() {
        String disjunctiveRules =  "prefix0:Woman(?X) | prefix0:Man(?X) :- prefix0:Human(?X).\n";

        for(String prefix: PREFIXES_ARRAY) {
            String[] split = prefix.split(" ");
            MyPrefixes.PAGOdAPrefixes.declarePrefix(split[1], OWLHelper.removeAngles(split[2]));
        }

        InputStream is = new ByteArrayInputStream(disjunctiveRules.getBytes(Charset.defaultCharset()));
        DatalogProgram datalogProgram = new DatalogProgram(is);

        System.out.println(">> Upper <<");
        System.out.println(datalogProgram.getUpper().toString());

        for(DLClause clause: datalogProgram.getUpper().getClauses())
            Assert.assertTrue(clause.getBodyLength() > 0);
    }

//    @Test
    public static void test_lowerUpperProgram() {
        String existentialRules = "owl:sameAs(?Z,?W) :- prefix0:isHeadOf(?Y2,?X), prefix0:isHeadOf(?Y1,?X).\n" +
                "prefix0:WomanCollege(?Y) :- prefix0:College(?X).\n";

        for(String prefix: PREFIXES_ARRAY) {
            String[] split = prefix.split(" ");
            MyPrefixes.PAGOdAPrefixes.declarePrefix(split[1], OWLHelper.removeAngles(split[2]));
        }

        InputStream is = new ByteArrayInputStream((RULES + existentialRules).getBytes(Charset.defaultCharset()));
        DatalogProgram datalogProgram = new DatalogProgram(is);

        System.out.println(">> Upper <<");
        System.out.println(datalogProgram.getUpper().toString());

        System.out.println(">> General <<");
        System.out.println(datalogProgram.getGeneral().toString());
        System.out.flush();

//        boolean assertCondition = true;
//        for(String existentialRule: existentialRules.split("\n")) {
//            assertCondition &= !lowerProgramStr.contains(existentialRule);
//        }
//
//        Assert.assertTrue(assertCondition);
    }

//    @Test
    public static void test_lowerProgramExistentialRemoval() {
        String existentialRules = "owl:sameAs(?Z,?W) :- prefix0:isHeadOf(?Y2,?X), prefix0:isHeadOf(?Y1,?X).\n" +
                "prefix0:WomanCollege(?Y) :- prefix0:College(?X).\n";

        for(String prefix: PREFIXES_ARRAY) {
            String[] split = prefix.split(" ");
            MyPrefixes.PAGOdAPrefixes.declarePrefix(split[1], OWLHelper.removeAngles(split[2]));
        }

        InputStream is = new ByteArrayInputStream((RULES + existentialRules).getBytes(Charset.defaultCharset()));
        DatalogProgram datalogProgram = new DatalogProgram(is);
        String lowerProgramStr = datalogProgram.getLower().toString();

        boolean assertCondition = true;
        for(String existentialRule: existentialRules.split("\n")) {
            assertCondition &= !lowerProgramStr.contains(existentialRule);
        }

        Assert.assertTrue(assertCondition);
    }

    @Test
    public static void someTest() {
        for(String line: PREFIXES_ARRAY) {
            String[] split = line.split(" ");
            MyPrefixes.PAGOdAPrefixes.declarePrefix(split[1], OWLHelper.removeAngles(split[2]));
        }

        InputStream is = new ByteArrayInputStream(RULES.getBytes(Charset.defaultCharset()));
        DatalogProgram datalogProgram = new DatalogProgram(is);
        System.out.println(">> General <<");
        System.out.println(datalogProgram.getGeneral().toString());
        System.out.println(">> Lower <<");
        System.out.println(datalogProgram.getLower().toString());
        System.out.println(">> Upper <<");
        System.out.println(datalogProgram.getUpper().toString());
        System.out.flush();
    }

    private static final String PREFIXES;
    private static final String[] PREFIXES_ARRAY;
    private static final String RULES;
    private static final String[] RULES_ARRAY;

    static {
        PREFIXES = "PREFIX anony: <http://www.cs.ox.ac.uk/PAGOdA/skolemised#>\n" +
                "PREFIX aux: <http://www.cs.ox.ac.uk/PAGOdA/auxiliary#>\n" +
                "PREFIX owl: <http://www.w3.org/2002/07/owl#>\n" +
                "PREFIX prefix0: <http://semantics.crl.ibm.com/univ-bench-dl.owl#>\n" +
                "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n" +
                "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>\n" +
                "PREFIX ruleml: <http://www.w3.org/2003/11/ruleml#>\n" +
                "PREFIX swrl: <http://www.w3.org/2003/11/swrl#>\n" +
                "PREFIX swrlb: <http://www.w3.org/2003/11/swrlb#>\n" +
                "PREFIX swrlx: <http://www.w3.org/2003/11/swrlx#>\n" +
                "PREFIX xsd: <http://www.w3.org/2001/XMLSchema#>\n";

         RULES = "owl:Nothing(?X) :- owl:Nothing2(?X).\n" +
                "prefix0:WomanCollege(?X) :- prefix0:College(?X).\n" +
                "owl:Nothing5(?X) :- prefix0:WomanCollege(?X), prefix0:hasStudent(?X,?Y), prefix0:Man(?Y).\n" +
                "prefix0:SportsFan(?X) :- prefix0:Person(?X), prefix0:isCrazyAbout(?X,?Y), prefix0:Sports(?Y).\n" +
                "prefix0:Organization(?X) :- prefix0:isAffiliateOf(?X,?Y).\n" +
                "prefix0:Person(?X) :- prefix0:lastName(?X,?Y).\n" +
                "owl:sameAs(?Y1,?Y2) :- prefix0:isHeadOf(?Y1,?X), prefix0:isHeadOf(?Y2,?X).\n" +
                "prefix0:WomanCollege(?Y) :- prefix0:College(?X).\n" +
                "prefix0:Woman(?X) | prefix0:Man(?X) :- prefix0:Human(?X).\n" +
                "prefix0:isMemberOf(?Y,?X) :- prefix0:hasMember(?X,?Y).\n";

        PREFIXES_ARRAY = PREFIXES.split("\n");
        RULES_ARRAY = RULES.split("\n");
    }
}
