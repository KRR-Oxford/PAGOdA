package uk.ac.ox.cs.pagoda.tracking;

import org.semanticweb.HermiT.model.*;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.model.OWLOntology;
import uk.ac.ox.cs.pagoda.hermit.DLClauseHelper;
import uk.ac.ox.cs.pagoda.reasoner.light.BasicQueryEngine;
import uk.ac.ox.cs.pagoda.rules.UpperDatalogProgram;
import uk.ac.ox.cs.pagoda.util.Namespace;

import java.util.Collection;
import java.util.LinkedList;

public class TrackingRuleEncoderWithoutGap extends TrackingRuleEncoder {

	public TrackingRuleEncoderWithoutGap(UpperDatalogProgram program, BasicQueryEngine store) {
		super(program, store); 
	}
	
	@Override
	protected String getEqualityRelatedRuleText() {
		if (equalityRelatedRuleText != null) return equalityRelatedRuleText.replace("_tn", getTrackingPredicate("")); 
		
		Collection<DLClause> equalityRelatedClauses = new LinkedList<DLClause>(); 
		Variable X = Variable.create("X"); 
		AtomicRole trackingSameAs = AtomicRole.create(Namespace.EQUALITY + "_tn");  
		OWLOntology onto = program.getOntology();
		Atom[] headAtom, bodyAtom; 
		for (OWLClass cls: onto.getClassesInSignature(true)) {
			String clsIRI = cls.getIRI().toString(); 
			unaryPredicates.add(clsIRI); 
			headAtom = new Atom[] {Atom.create(trackingSameAs, X, X)}; 
			bodyAtom = new Atom[] {
					Atom.create(AtomicConcept.create(clsIRI + "_tn"), X),
//					Atom.create(AtomicConcept.create(GapTupleIterator.getGapPredicate(clsIRI)), X1), 
					Atom.create(AtomicConcept.create(clsIRI), X)}; 
			equalityRelatedClauses.add(DLClause.create(headAtom, bodyAtom)); 
		}
		Variable Y = Variable.create("Y"); 
		for (OWLObjectProperty prop: onto.getObjectPropertiesInSignature(true)) {
			String propIRI = prop.getIRI().toString();
			binaryPredicates.add(propIRI); 
			AtomicRole trackingRole = AtomicRole.create(propIRI + "_tn"); 
//			AtomicRole gapRole = AtomicRole.create(GapTupleIterator.getGapPredicate(propIRI)); 
			AtomicRole role = AtomicRole.create(propIRI); 
			headAtom = new Atom[] {Atom.create(trackingSameAs, X, X)}; 
			bodyAtom = new Atom[] {
					Atom.create(trackingRole, X, Y), 
//					Atom.create(gapRole, X1, Y), 
					Atom.create(role, X, Y)}; 
			equalityRelatedClauses.add(DLClause.create(headAtom, bodyAtom));
			
			bodyAtom = new Atom[] {
					Atom.create(trackingRole, Y, X), 
//					Atom.create(gapRole, Y, X1), 
					Atom.create(role, Y, X)}; 
			equalityRelatedClauses.add(DLClause.create(headAtom, bodyAtom)); 
		}
		
		equalityRelatedClauses.add(
				DLClause.create(
						new Atom[] {Atom.create(trackingSameAs, Y, X)}, 
						new Atom[] {Atom.create(trackingSameAs, X, Y)}));
		
		equalityRelatedRuleText = DLClauseHelper.toString(equalityRelatedClauses).toString();
		return equalityRelatedRuleText.replace("_tn", getTrackingPredicate(""));
	}

	@Override
	protected void encodingRule(DLClause clause) {
		LinkedList<Atom> newHeadAtoms = new LinkedList<Atom>();
		newHeadAtoms.add(Atom.create(selected, getIndividual4GeneralRule(clause)));
		
		Atom headAtom;
		for (Atom atom: clause.getBodyAtoms()) {
			headAtom = Atom.create(
					getTrackingDLPredicate(atom.getDLPredicate()), 
					DLClauseHelper.getArguments(atom));
			newHeadAtoms.add(headAtom);
		}

		DLClause newClause;
		Atom[] newBodyAtoms = new Atom[clause.getBodyLength() + 1];
		headAtom = clause.getHeadAtom(0);
		newBodyAtoms[0] = Atom.create(
				getTrackingDLPredicate(headAtom.getDLPredicate()), 
				DLClauseHelper.getArguments(headAtom));
		
//		newBodyAtoms[1] = Atom.create(
//				getGapDLPredicate(headAtom.getDLPredicate()), 
//				DLClauseHelper.getArguments(headAtom));

		for (int i = 0; i < clause.getBodyLength(); ++i)
			newBodyAtoms[i + 1] = clause.getBodyAtom(i); 
		
		for (Atom atom: newHeadAtoms) {
			newClause = DLClause.create(new Atom[] {atom}, newBodyAtoms); 
			trackingClauses.add(newClause);
		}
		
	}
}
