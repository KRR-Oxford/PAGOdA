package uk.ac.ox.cs.pagoda.hermit;

import org.testng.annotations.Test;
import uk.ac.ox.cs.pagoda.MyPrefixes;
import uk.ac.ox.cs.pagoda.owl.OWLHelper;
import uk.ac.ox.cs.pagoda.rules.DatalogProgram;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.Charset;

public class TestRuleHelper {

    @Test
    public static void someTest() {
        String prefixes = "PREFIX anony: <http://www.cs.ox.ac.uk/PAGOdA/skolemised#>\n" +
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
        String rule = "owl:Nothing(?X) :- owl:Nothing2(?X).\n" +
                "prefix0:WomanCollege(?X) :- prefix0:College(?X).\n" +
                "owl:Nothing5(?X) :- prefix0:WomanCollege(?X), prefix0:hasStudent(?X,?Y), prefix0:Man(?Y).\n" +
                "prefix0:SportsFan(?X) :- prefix0:Person(?X), prefix0:isCrazyAbout(?X,?Y), prefix0:Sports(?Y).\n" +
                "prefix0:Organization(?X) :- prefix0:isAffiliateOf(?X,?Y).\n" +
                "prefix0:Person(?X) :- prefix0:lastName(?X,?Y).\n" +
                "owl:sameAs(?Y1,?Y2) :- prefix0:isHeadOf(?Y1,?X), prefix0:isHeadOf(?Y2,?X).\n" +
                "prefix0:isMemberOf(?Y,?X) :- prefix0:hasMember(?X,?Y).\n" +
                "prefix0:Person(?X) :- prefix0:hasMasterDegreeFrom(?X,?Y).\n" +
                "prefix0:Person(?X) :- prefix0:TeachingAssistant(?X).\n" +
                "prefix0:Employee(?X) :- prefix0:Person(?X), prefix0:worksFor(?X,?Y), prefix0:Organization(?Y).\n" +
                "prefix0:FineArts(?X) :- prefix0:Media_Arts_And_ciencesClass(?X).\n" +
                "prefix0:FineArts(?X) :- prefix0:Medieval_ArtsClass(?X).\n" +
                "prefix0:Professor(?X) :- prefix0:Chair(?X).\n" +
                "prefix0:Faculty(?Y) :- prefix0:isTaughtBy(?X,?Y).\n" +
                "prefix0:worksFor(?X,anony:individual25) :- prefix0:Employee(?X).\n" +
                "prefix0:Engineering(?X) :- prefix0:Chemical_EngineeringClass(?X).\n" +
                "prefix0:BaseballClass(anony:individual1) :- prefix0:BaseballLover(?X).\n" +
                "prefix0:Course(?Y) :- prefix0:listedCourse(?X,?Y).\n" +
                "prefix0:worksFor(?X,?Y) :- prefix0:isHeadOf(?X,?Y).\n" +
                "prefix0:Faculty(?X) :- prefix0:teacherOf(?X,?Y).\n" +
                "prefix0:Course(?Y) :- prefix0:teachingAssistantOf(?X,?Y).\n" +
                "owl:Nothing(?X) :- owl:Nothing1(?X).\n" +
                "prefix0:Person(?X) :- prefix0:Student(?X).\n" +
                "prefix0:isFriendOf(?Y,?X) :- prefix0:isFriendOf(?X,?Y).\n" +
                "prefix0:Student(?X) :- prefix0:UndergraduateStudent(?X).\n" +
                "prefix0:Person(?X) :- prefix0:BasketBallLover(?X).\n" +
                "prefix0:Person(?Y) :- prefix0:hasSameHomeTownWith(?X,?Y).\n" +
                "prefix0:Employee(?X) :- prefix0:Faculty(?X).\n" +
                "prefix0:Insterest(?X) :- prefix0:Music(?X).\n" +
                "owl:Nothing1(?X) :- prefix0:NonScienceStudent(?X), prefix0:hasMajor(?X,?Y), prefix0:Science(?Y).\n" +
                "owl:sameAs(?Y1,?Y2) :- prefix0:isTaughtBy(?X,?Y1), prefix0:isTaughtBy(?X,?Y2).\n" +
                "prefix0:hasStudent(?Y,?X) :- prefix0:isStudentOf(?X,?Y).\n" +
                "prefix0:Student(?X) :- prefix0:ScienceStudent(?X).\n" +
                "prefix0:like(?X,anony:individual7) :- prefix0:PeopleWithHobby(?X).\n" +
                "prefix0:Publication(?X) :- prefix0:Article(?X).\n" +
                "prefix0:isTaughtBy(?Y,?X) :- prefix0:teacherOf(?X,?Y).\n" +
                "prefix0:isCrazyAbout(?X,anony:individual19) :- prefix0:TennisFan(?X).\n" +
                "prefix0:Science(?X) :- prefix0:Marine_ScienceClass(?X).\n" +
                "prefix0:SupportingStaff(?X) :- prefix0:SystemsStaff(?X).\n" +
                "prefix0:hasStudent(?X,anony:individual10) :- prefix0:College(?X).\n" +
                "prefix0:FineArts(?X) :- prefix0:Theatre_and_DanceClass(?X).\n" +
                "prefix0:Organization(?Y) :- prefix0:subOrganizationOf(?X,?Y).\n" +
                "prefix0:Engineering(?X) :- prefix0:Materical_Science_EngineeringClass(?X).\n" +
                "prefix0:hasMember(?Y,?X) :- prefix0:isMemberOf(?X,?Y).\n" +
                "prefix0:Student(?Y) :- prefix0:hasStudent(?X,?Y).\n" +
                "owl:Nothing(?X) :- owl:Nothing5(?X).\n" +
                "prefix0:isCrazyAbout(?X,anony:individual18) :- prefix0:SwimmingFan(?X).\n" +
                "prefix0:Publication(?Y) :- prefix0:orgPublication(?X,?Y).\n" +
                "prefix0:Chair(?X) :- prefix0:Person(?X), prefix0:isHeadOf(?X,?Y), prefix0:Department(?Y).\n" +
                "prefix0:isStudentOf(?Y,?X) :- prefix0:hasStudent(?X,?Y).\n" +
                "prefix0:Person(?X) :- prefix0:isAdvisedBy(?X,?Y).\n" +
                "prefix0:hasMajor(?X,anony:individual2) :- prefix0:Student(?X).\n" +
                "prefix0:Publication(?X) :- prefix0:publicationDate(?X,?Y).\n" +
                "owl:Nothing(?X) :- owl:Nothing6(?X).\n" +
                "prefix0:Director(?X) :- prefix0:Person(?X), prefix0:isHeadOf(?X,?Y), prefix0:Program(?Y).\n" +
                "prefix0:Professor(?X) :- prefix0:AssociateProfessor(?X).\n" +
                "prefix0:TeachingAssistant(?X) :- prefix0:Person(?X), prefix0:teachingAssistantOf(?X,?Y), prefix0:Course(?Y).\n" +
                "prefix0:Research(?Y) :- prefix0:researchProject(?X,?Y).\n" +
                "prefix0:TeachingAssistant(?X) :- prefix0:teachingAssistantOf(?X,?Y).\n" +
                "prefix0:University(?X) :- prefix0:hasAlumnus(?X,?Y).\n" +
                "prefix0:Faculty(?X) :- prefix0:Lecturer(?X).\n" +
                "prefix0:isHeadOf(?X,anony:individual21) :- prefix0:Dean(?X).\n" +
                "prefix0:like(?X,?Y) :- prefix0:isCrazyAbout(?X,?Y).\n" +
                "prefix0:Person(?X) :- prefix0:hasSameHomeTownWith(?X,?Y).\n" +
                "prefix0:Organization(?X) :- prefix0:orgPublication(?X,?Y).\n" +
                "prefix0:Person(?Y) :- prefix0:hasMember(?X,?Y).\n" +
                "prefix0:HumanitiesAndSocial(?X) :- prefix0:LinguisticsClass(?X).\n" +
                "prefix0:Engineering(?X) :- prefix0:Aeronautical_EngineeringClass(?X).\n" +
                "owl:Nothing(?X) :- owl:Nothing3(?X).\n" +
                "prefix0:isHeadOf(?X,anony:individual12) :- prefix0:Director(?X).\n" +
                "prefix0:Publication(?X) :- prefix0:publicationAuthor(?X,?Y).\n" +
                "prefix0:Organization(?X) :- prefix0:College(?X).\n" +
                "prefix0:isCrazyAbout(?X,anony:individual24) :- prefix0:BaseballFan(?X).\n" +
                "prefix0:FineArts(?X) :- prefix0:Performing_ArtsClass(?X).\n" +
                "prefix0:Sports(?X) :- prefix0:BasketBallClass(?X).\n" +
                "prefix0:HumanitiesAndSocial(?X) :- prefix0:PsychologyClass(?X).\n" +
                "prefix0:Person(?X) :- prefix0:Employee(?X).\n" +
                "prefix0:FineArts(?X) :- prefix0:DramaClass(?X).\n" +
                "prefix0:Faculty(?X) :- prefix0:PostDoc(?X).\n" +
                "prefix0:University(?Y) :- prefix0:hasDoctoralDegreeFrom(?X,?Y).\n" +
                "prefix0:Sports(anony:individual0) :- prefix0:SportsLover(?X).\n" +
                "prefix0:Person(?Y) :- prefix0:hasAlumnus(?X,?Y).\n" +
                "prefix0:FineArts(?X) :- prefix0:Modern_ArtsClass(?X).\n" +
                "prefix0:hasMember(?X,?Y) :- prefix0:hasStudent(?X,?Y).\n" +
                "prefix0:Course(?X) :- prefix0:isTaughtBy(?X,?Y).\n" +
                "prefix0:NonScienceStudent(?X) :- prefix0:Student(?X).\n" +
                "prefix0:BasketBallLover(?X) :- prefix0:Person(?X), prefix0:like(?X,?Y), prefix0:BasketBallClass(?Y).\n" +
                "prefix0:Professor(?X) :- prefix0:VisitingProfessor(?X).\n" +
                "prefix0:GraduateCourse(?Y) :- prefix0:GraduateStudent(?X), prefix0:takesCourse(?X,?Y).\n" +
                "prefix0:TennisClass(anony:individual19) :- prefix0:TennisFan(?X).\n" +
                "prefix0:Work(?X) :- prefix0:Research(?X).\n" +
                "prefix0:TennisFan(?X) :- prefix0:Person(?X), prefix0:isCrazyAbout(?X,?Y), prefix0:TennisClass(?Y).\n" +
                "prefix0:ScienceStudent(?X) :- prefix0:Student(?X), prefix0:hasMajor(?X,?Y), prefix0:Science(?Y).\n" +
                "prefix0:Person(?X) :- prefix0:Woman(?X).\n" +
                "prefix0:Man(?X) :- prefix0:Person(?X).\n" +
                "prefix0:Person(?X) :- prefix0:hasUndergraduateDegreeFrom(?X,?Y).\n" +
                "prefix0:ResearchGroup(?X) :- prefix0:researchProject(?X,?Y).\n" +
                "prefix0:hasSameHomeTownWith(?X,?Z) :- prefix0:hasSameHomeTownWith(?X,?Y), prefix0:hasSameHomeTownWith(?Y,?Z).\n" +
                "prefix0:Person(?X) :- prefix0:isFriendOf(?X,?Y).\n" +
                "prefix0:Person(?Y) :- prefix0:publicationAuthor(?X,?Y).\n" +
                "prefix0:Person(?X) :- prefix0:Chair(?X).\n" +
                "prefix0:Publication(?X) :- prefix0:Manual(?X).\n" +
                "prefix0:Publication(?X) :- prefix0:UnofficialPublication(?X).\n" +
                "prefix0:Engineering(?X) :- prefix0:Industry_EngineeringClass(?X).\n" +
                "prefix0:Science(?X) :- prefix0:StatisticsClass(?X).\n" +
                "prefix0:Organization(?Y) :- prefix0:isStudentOf(?X,?Y).\n" +
                "prefix0:SwimmingFan(?X) :- prefix0:Person(?X), prefix0:isCrazyAbout(?X,?Y), prefix0:SwimmingClass(?Y).\n" +
                "prefix0:Person(?X) :- prefix0:emailAddress(?X,?Y).\n" +
                "prefix0:FineArts(?X) :- prefix0:Latin_ArtsClass(?X).\n" +
                "prefix0:Organization(?X) :- prefix0:ResearchGroup(?X).\n" +
                "prefix0:AcademicSubject(?X) :- prefix0:HumanitiesAndSocial(?X).\n" +
                "prefix0:Professor(?X) :- prefix0:Dean(?X).\n" +
                "prefix0:SwimmingClass(anony:individual8) :- prefix0:SwimmingLover(?X).\n" +
                "prefix0:University(?Y) :- prefix0:hasMasterDegreeFrom(?X,?Y).\n" +
                "prefix0:Article(?X) :- prefix0:ConferencePaper(?X).\n" +
                "prefix0:Person(?X) :- prefix0:BasketBallFan(?X).\n" +
                "prefix0:HumanitiesAndSocial(?X) :- prefix0:ReligionsClass(?X).\n" +
                "prefix0:Science(?X) :- prefix0:PhysicsClass(?X).\n" +
                "prefix0:Dean(?X) :- prefix0:isHeadOf(?X,?Y), prefix0:College(?Y).\n" +
                "prefix0:University(?Y) :- prefix0:hasDegreeFrom(?X,?Y).\n" +
                "prefix0:Organization(?X) :- prefix0:hasMember(?X,?Y).\n" +
                "prefix0:Engineering(?X) :- prefix0:Computer_EngineeringClass(?X).\n" +
                "prefix0:Publication(?X) :- prefix0:Software(?X).\n" +
                "prefix0:Science(?X) :- prefix0:GeosciencesClass(?X).\n" +
                "prefix0:hasMajor(?X,anony:individual9) :- prefix0:ScienceStudent(?X).\n" +
                "prefix0:hasDegreeFrom(?Y,?X) :- prefix0:hasAlumnus(?X,?Y).\n" +
                "prefix0:Software(?X) :- prefix0:softwareDocumentation(?X,?Y).\n" +
                "prefix0:isMemberOf(?X,?Y) :- prefix0:isStudentOf(?X,?Y).\n" +
                "prefix0:Organization(?X) :- prefix0:hasStudent(?X,?Y).\n" +
                "owl:Nothing2(?X) :- prefix0:GraduateCourse(?X), prefix0:GraduateCourse_neg(?X).\n" +
                "prefix0:BaseballFan(?X) :- prefix0:Person(?X), prefix0:isCrazyAbout(?X,?Y), prefix0:BaseballClass(?Y).\n" +
                "prefix0:Publication(?X) :- prefix0:publicationResearch(?X,?Y).\n" +
                "prefix0:like(?X,anony:individual14) :- prefix0:PeopleWithManyHobbies(?X).\n" +
                "prefix0:SportsLover(?X) :- prefix0:Person(?X), prefix0:like(?X,?Y), prefix0:Sports(?Y).\n" +
                "prefix0:Organization(?X) :- prefix0:University(?X).\n" +
                "prefix0:hasAlumnus(?Y,?X) :- prefix0:hasDegreeFrom(?X,?Y).\n" +
                "prefix0:Science(?X) :- prefix0:Materials_ScienceClass(?X).\n" +
                "prefix0:Professor(?X) :- prefix0:tenured(?X,?Y).\n" +
                "prefix0:Faculty(?X) :- prefix0:Professor(?X).\n" +
                "prefix0:Student(?X) :- prefix0:NonScienceStudent(?X).\n" +
                "prefix0:Person(?X) :- prefix0:telephone(?X,?Y).\n" +
                "prefix0:FineArts(?X) :- prefix0:ArchitectureClass(?X).\n" +
                "prefix0:University(?Y) :- prefix0:hasUndergraduateDegreeFrom(?X,?Y).\n" +
                "prefix0:Man(anony:individual10) :- prefix0:College(?X).\n" +
                "prefix0:Person(?X) :- prefix0:Man(?X).\n" +
                "prefix0:Person(?X) :- prefix0:title(?X,?Y).\n" +
                "prefix0:subOrganizationOf(?X,?Z) :- prefix0:subOrganizationOf(?X,?Y), prefix0:subOrganizationOf(?Y,?Z).\n" +
                "owl:sameAs(?Y1,?Y2) :- prefix0:like(?X,?Y1), prefix0:like(?X,?Y2).\n" +
                "prefix0:takesCourse(?X,anony:individual4) :- prefix0:GraduateStudent(?X).\n" +
                "prefix0:Sports(?X) :- prefix0:TennisClass(?X).\n" +
                "prefix0:Engineering(?X) :- prefix0:Petroleuml_EngineeringClass(?X).\n" +
                "prefix0:Organization(?X) :- prefix0:Institute(?X).\n" +
                "prefix0:isCrazyAbout(?X,anony:individual16) :- prefix0:BasketBallFan(?X).\n" +
                "prefix0:Science(?X) :- prefix0:BiologyClass(?X).\n" +
                "prefix0:Person(?X) :- prefix0:SportsFan(?X).\n" +
                "prefix0:Course(?X) :- prefix0:GraduateCourse(?X).\n" +
                "prefix0:Person(?X) :- prefix0:Director(?X).\n" +
                "prefix0:HumanitiesAndSocial(?X) :- prefix0:EconomicsClass(?X).\n" +
                "prefix0:Person(?X) :- prefix0:BaseballLover(?X).\n" +
                "prefix0:HumanitiesAndSocial(?X) :- prefix0:HistoryClass(?X).\n" +
                "prefix0:FineArts(?X) :- prefix0:Asian_ArtsClass(?X).\n" +
                "prefix0:isStudentOf(?X,?Y) :- prefix0:enrollIn(?X,?Y).\n" +
                "prefix0:isHeadOf(?X,anony:individual20) :- prefix0:Chair(?X).\n" +
                "prefix0:Person(?X) :- prefix0:PeopleWithHobby(?X).\n" +
                "prefix0:Sports(anony:individual5) :- prefix0:SportsFan(?X).\n" +
                "prefix0:Science(anony:individual9) :- prefix0:ScienceStudent(?X).\n" +
                "prefix0:Engineering(?X) :- prefix0:Biomedical_EngineeringClass(?X).\n" +
                "prefix0:HumanitiesAndSocial(?X) :- prefix0:Modern_LanguagesClass(?X).\n" +
                "prefix0:like(?X,?Y) :- prefix0:love(?X,?Y).\n" +
                "prefix0:hasStudent(?X,anony:individual11) :- prefix0:College(?X).\n" +
                "prefix0:Science(?X) :- prefix0:ChemistryClass(?X).\n" +
                "prefix0:Student(?X) :- prefix0:takesCourse(?X,?Y).\n" +
                "prefix0:teacherOf(?Y,?X) :- prefix0:isTaughtBy(?X,?Y).\n" +
                "prefix0:HumanitiesAndSocial(?X) :- prefix0:AnthropologyClass(?X).\n" +
                "prefix0:Person(?X) :- prefix0:hasDegreeFrom(?X,?Y).\n" +
                "prefix0:Person(?X) :- prefix0:hasDoctoralDegreeFrom(?X,?Y).\n" +
                "prefix0:Engineering(?X) :- prefix0:Electrical_EngineeringClass(?X).\n" +
                "owl:differentFrom(anony:individual13,anony:individual14) :- prefix0:PeopleWithManyHobbies(?X).\n" +
                "prefix0:Person(?X) :- prefix0:SportsLover(?X).\n" +
                "prefix0:Organization(?X) :- prefix0:subOrganizationOf(?X,?Y).\n" +
                "prefix0:SwimmingLover(?X) :- prefix0:Person(?X), prefix0:like(?X,?Y), prefix0:SwimmingClass(?Y).\n" +
                "prefix0:BaseballLover(?X) :- prefix0:Person(?X), prefix0:like(?X,?Y), prefix0:BaseballClass(?Y).\n" +
                "prefix0:Science(?X) :- prefix0:Computer_ScienceClass(?X).\n" +
                "prefix0:Sports(?X) :- prefix0:SwimmingClass(?X).\n" +
                "prefix0:Science(?X) :- prefix0:AstronomyClass(?X).\n" +
                "prefix0:Work(?X) :- prefix0:Course(?X).\n" +
                "prefix0:Science(?X) :- prefix0:MathematicsClass(?X).\n" +
                "prefix0:AcademicSubject(?X) :- prefix0:Engineering(?X).\n" +
                "prefix0:hasDegreeFrom(?X,?Y) :- prefix0:hasUndergraduateDegreeFrom(?X,?Y).\n" +
                "prefix0:like(?X,anony:individual15) :- prefix0:PeopleWithManyHobbies(?X).\n" +
                "prefix0:Sports(?X) :- prefix0:BaseballClass(?X).\n" +
                "prefix0:Student(?X) :- prefix0:isStudentOf(?X,?Y).\n" +
                "prefix0:Professor(?Y) :- prefix0:isAdvisedBy(?X,?Y).\n" +
                "prefix0:SwimmingClass(anony:individual18) :- prefix0:SwimmingFan(?X).\n" +
                "prefix0:like(?X,anony:individual22) :- prefix0:BasketBallLover(?X).\n" +
                "prefix0:like(?X,anony:individual1) :- prefix0:BaseballLover(?X).\n" +
                "prefix0:Schedule(?X) :- prefix0:listedCourse(?X,?Y).\n" +
                "owl:Nothing6(?X) :- owl:differentFrom(?X,?X).\n" +
                "prefix0:PeopleWithManyHobbies(?X) :- prefix0:like(?X,?Y3).\n" +
                "prefix0:Course(anony:individual23) :- prefix0:TeachingAssistant(?X).\n" +
                "prefix0:takesCourse(?X,anony:individual3) :- prefix0:takesCourse(?X,?Y).\n" +
                "prefix0:love(?X,?Y) :- prefix0:like(?X,?Y).\n" +
                "prefix0:AcademicSubject(?X) :- prefix0:Science(?X).\n" +
                "prefix0:Person(?X) :- prefix0:ResearchAssistant(?X).\n" +
                "prefix0:Insterest(?X) :- prefix0:Sports(?X).\n" +
                "prefix0:Article(?X) :- prefix0:TechnicalReport(?X).\n" +
                "prefix0:UndergraduateStudent(?Y) :- prefix0:WomanCollege(?X), prefix0:hasStudent(?X,?Y).\n" +
                "prefix0:Department(anony:individual20) :- prefix0:Chair(?X).\n" +
                "prefix0:Woman(?X) :- prefix0:Person(?X).\n" +
                "owl:Nothing4(?X) :- prefix0:UndergraduateStudent(?X), prefix0:UndergraduateStudent_neg(?X).\n" +
                "prefix0:HumanitiesAndSocial(?X) :- prefix0:HumanitiesClass(?X).\n" +
                "prefix0:GraduateCourse_neg(anony:individual3) :- prefix0:takesCourse(?X,?Y).\n" +
                "prefix0:Organization(?Y) :- prefix0:isAffiliatedOrganizationOf(?X,?Y).\n" +
                "owl:Nothing(?X) :- owl:Nothing4(?X).\n" +
                "prefix0:like(?X,anony:individual0) :- prefix0:SportsLover(?X).\n" +
                "prefix0:Research(?Y) :- prefix0:publicationResearch(?X,?Y).\n" +
                "prefix0:Professor(?X) :- prefix0:AssistantProfessor(?X).\n" +
                "prefix0:Program(anony:individual12) :- prefix0:Director(?X).\n" +
                "prefix0:isMemberOf(?X,?Y) :- prefix0:worksFor(?X,?Y).\n" +
                "prefix0:Organization(anony:individual25) :- prefix0:Employee(?X).\n" +
                "prefix0:hasDegreeFrom(?X,?Y) :- prefix0:hasDoctoralDegreeFrom(?X,?Y).\n" +
                "prefix0:Person(?Y) :- prefix0:isAffiliateOf(?X,?Y).\n" +
                "prefix0:Student(?X) :- prefix0:Person(?X), prefix0:isStudentOf(?X,?Y), prefix0:Organization(?Y).\n" +
                "prefix0:PeopleWithHobby(?X) :- prefix0:Person(?X), prefix0:like(?X,?Y).\n" +
                "prefix0:Organization(anony:individual17) :- prefix0:Student(?X).\n" +
                "prefix0:Engineering(?X) :- prefix0:Mechanical_EngineeringClass(?X).\n" +
                "prefix0:Employee(?X) :- prefix0:SupportingStaff(?X).\n" +
                "prefix0:Organization(?X) :- prefix0:Department(?X).\n" +
                "prefix0:HumanitiesAndSocial(?X) :- prefix0:PhilosophyClass(?X).\n" +
                "prefix0:College(anony:individual21) :- prefix0:Dean(?X).\n" +
                "prefix0:UndergraduateStudent_neg(anony:individual11) :- prefix0:College(?X).\n" +
                "owl:Nothing3(?X) :- prefix0:Man(?X), prefix0:Woman(?X).\n" +
                "owl:sameAs(?Y1,?Y2) :- prefix0:takesCourse(?X,?Y1), prefix0:LeisureStudent(?X), prefix0:takesCourse(?X,?Y2).\n" +
                "prefix0:Organization(?X) :- prefix0:isAffiliatedOrganizationOf(?X,?Y).\n" +
                "prefix0:isCrazyAbout(?X,anony:individual5) :- prefix0:SportsFan(?X).\n" +
                "prefix0:Software(?X) :- prefix0:softwareVersion(?X,?Y).\n" +
                "prefix0:Science(anony:individual2) :- prefix0:Student(?X).\n" +
                "prefix0:SupportingStaff(?X) :- prefix0:ClericalStaff(?X).\n" +
                "prefix0:Person(?X) :- prefix0:SwimmingLover(?X).\n" +
                "prefix0:Person(?X) :- prefix0:age(?X,?Y).\n" +
                "prefix0:BasketBallClass(anony:individual22) :- prefix0:BasketBallLover(?X).\n" +
                "prefix0:like(?X,anony:individual8) :- prefix0:SwimmingLover(?X).\n" +
                "prefix0:Person(?X) :- prefix0:firstName(?X,?Y).\n" +
                "prefix0:Department(?Y) :- prefix0:enrollIn(?X,?Y).\n" +
                "prefix0:Publication(?Y) :- prefix0:softwareDocumentation(?X,?Y).\n" +
                "prefix0:hasDegreeFrom(?X,?Y) :- prefix0:hasMasterDegreeFrom(?X,?Y).\n" +
                "prefix0:AcademicSubject(?Y) :- prefix0:hasMajor(?X,?Y).\n" +
                "prefix0:Article(?X) :- prefix0:JournalArticle(?X).\n" +
                "prefix0:Organization(?X) :- prefix0:Program(?X).\n" +
                "prefix0:Course(?Y) :- prefix0:teacherOf(?X,?Y).\n" +
                "prefix0:AcademicSubject(?X) :- prefix0:FineArts(?X).\n" +
                "prefix0:Person(?X) :- prefix0:TennisFan(?X).\n" +
                "prefix0:GraduateStudent(?X) :- prefix0:takesCourse(?X,?Y).\n" +
                "prefix0:BasketBallFan(?X) :- prefix0:Person(?X), prefix0:isCrazyAbout(?X,?Y), prefix0:BasketBallClass(?Y).\n" +
                "prefix0:Publication(?X) :- prefix0:Specification(?X).\n" +
                "prefix0:worksFor(?X,anony:individual6) :- prefix0:ResearchAssistant(?X).\n" +
                "prefix0:Person(?X) :- prefix0:SwimmingFan(?X).\n" +
                "prefix0:BasketBallClass(anony:individual16) :- prefix0:BasketBallFan(?X).\n" +
                "prefix0:Person(?X) :- prefix0:BaseballFan(?X).\n" +
                "prefix0:Person(?Y) :- prefix0:isFriendOf(?X,?Y).\n" +
                "prefix0:like(?X,anony:individual13) :- prefix0:PeopleWithManyHobbies(?X).\n" +
                "owl:differentFrom(anony:individual14,anony:individual15) :- prefix0:PeopleWithManyHobbies(?X).\n" +
                "prefix0:hasSameHomeTownWith(?Y,?X) :- prefix0:hasSameHomeTownWith(?X,?Y).\n" +
                "prefix0:ResearchGroup(anony:individual6) :- prefix0:ResearchAssistant(?X).\n" +
                "prefix0:College(?X) :- prefix0:WomanCollege(?X).\n" +
                "prefix0:BaseballClass(anony:individual24) :- prefix0:BaseballFan(?X).\n" +
                "owl:differentFrom(anony:individual13,anony:individual15) :- prefix0:PeopleWithManyHobbies(?X).\n" +
                "prefix0:Publication(?X) :- prefix0:Book(?X).\n" +
                "prefix0:Professor(?X) :- prefix0:FullProfessor(?X).\n" +
                "prefix0:Engineering(?X) :- prefix0:Civil_EngineeringClass(?X).\n" +
                "prefix0:isStudentOf(?X,anony:individual17) :- prefix0:Student(?X).\n" +
                "prefix0:HumanitiesAndSocial(?X) :- prefix0:EnglishClass(?X).\n" +
                "prefix0:teachingAssistantOf(?X,anony:individual23) :- prefix0:TeachingAssistant(?X).\n" +
                "prefix0:woman(?X) | prefix0:man(?X) :- prefix0:human(?X).\n" +
                "prefix0:FineArts(?X) :- prefix0:MusicsClass(?X).\n";

        for(String line: prefixes.split("\n")) {
            String[] split = line.split(" ");
            MyPrefixes.PAGOdAPrefixes.declarePrefix(split[1], OWLHelper.removeAngles(split[2]));
        }

        InputStream is = new ByteArrayInputStream(rule.getBytes(Charset.defaultCharset()));
        DatalogProgram datalogProgram = new DatalogProgram(is);
        System.out.println(">> General <<");
        System.out.println(datalogProgram.getGeneral().toString());
        System.out.println(">> Lower <<");
        System.out.println(datalogProgram.getLower().toString());
        System.out.println(">> Upper <<");
        System.out.println(datalogProgram.getUpper().toString());
        System.out.flush();
    }
}
