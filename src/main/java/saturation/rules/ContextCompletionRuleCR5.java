package saturation.rules;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLSubClassOfAxiom;
import saturation.context.ContextCR5;

import java.util.HashSet;
import java.util.Set;

public final class ContextCompletionRuleCR5 implements ContextCompletionRule<ContextCR5> {
    @Override
    public Set<OWLSubClassOfAxiom> apply(OWLSubClassOfAxiom premise, ContextCR5 context) {
        OWLClassExpression superClass = premise.getSuperClass();

        Set<OWLSubClassOfAxiom> conclusions;

        if (superClass.isOWLNothing()) {
            Set<OWLSubClassOfAxiom> existentialRightProcessedAxioms = context.getExistentialRightProcessedAxioms();
            conclusions = processNothingPremise(existentialRightProcessedAxioms);
        } else {
            OWLClassExpression conclusionSubclass = premise.getSubClass();
            conclusions = processExistentialRightPremise(conclusionSubclass, context);
        }

        return conclusions;
    }

    private Set<OWLSubClassOfAxiom> processNothingPremise(Set<OWLSubClassOfAxiom> existentialRightProcessedAxioms) {
        OWLDataFactory owlDataFactory = OWLManager.getOWLDataFactory();
        Set<OWLSubClassOfAxiom> conclusions = new HashSet<>();

        OWLClass nothing = owlDataFactory.getOWLNothing();
        for (OWLSubClassOfAxiom existentialRightProcessedAxiom: existentialRightProcessedAxioms) {
            OWLClassExpression conclusionSubclass = existentialRightProcessedAxiom.getSubClass();
            OWLSubClassOfAxiom conclusion = owlDataFactory.getOWLSubClassOfAxiom(conclusionSubclass, nothing);
            conclusions.add(conclusion);
        }

        return conclusions;
    }

    private Set<OWLSubClassOfAxiom> processExistentialRightPremise(OWLClassExpression conclusionSubclass, ContextCR5 context) {
        Set<OWLSubClassOfAxiom> conclusions = new HashSet<>();

        context
            .getProcessedSubclassOfNothingAxiom()
            .ifPresent(__ -> {
                OWLDataFactory owlDataFactory = OWLManager.getOWLDataFactory();
                OWLClass nothing = owlDataFactory.getOWLNothing();
                OWLSubClassOfAxiom conclusion = owlDataFactory.getOWLSubClassOfAxiom(conclusionSubclass, nothing);
                conclusions.add(conclusion);
            });

        return conclusions;
    }
}
