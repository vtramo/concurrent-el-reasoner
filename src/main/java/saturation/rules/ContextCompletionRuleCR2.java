package saturation.rules;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLSubClassOfAxiom;
import saturation.context.ContextCR2;

import java.util.*;

public final class ContextCompletionRuleCR2 implements ContextCompletionRule<ContextCR2> {

    @Override
    public Collection<OWLSubClassOfAxiom> apply(OWLSubClassOfAxiom premise, ContextCR2 context) {
        Map<OWLClassExpression, Map<OWLClassExpression, Set<OWLClassExpression>>>
            superclassesByIntersectionOperandsOntologyIndex =
                context.getSuperclassesByIntersectionOperandsOntologyIndex();

        OWLClassExpression superClass = premise.getSuperClass();
        if (!superclassesByIntersectionOperandsOntologyIndex.containsKey(superClass)) {
            return Collections.emptyList();
        }

        OWLClassExpression subClass = premise.getSubClass();
        Map<OWLClassExpression, Set<OWLClassExpression>> intersectionSuperclassesByRightOperand = superclassesByIntersectionOperandsOntologyIndex.get(superClass);
        Set<OWLClassExpression> conclusionSuperclasses = searchConclusiveSuperclasses(subClass, context, intersectionSuperclassesByRightOperand);

        return buildConclusions(subClass, conclusionSuperclasses);
    }

    private Set<OWLClassExpression> searchConclusiveSuperclasses(
        OWLClassExpression subClass,
        ContextCR2 context,
        Map<OWLClassExpression, Set<OWLClassExpression>> intersectionSuperclassesByRightOperand
    ) {
        OWLDataFactory owlDataFactory = OWLManager.getOWLDataFactory();
        Set<OWLSubClassOfAxiom> processedAxioms = context.getProcessedAxioms();

        Set<OWLClassExpression> conclusionSuperclasses = new HashSet<>();
        for (OWLClassExpression rightOperand: intersectionSuperclassesByRightOperand.keySet()) {
            OWLSubClassOfAxiom objectivePremise = owlDataFactory.getOWLSubClassOfAxiom(subClass, rightOperand);

            if (!Objects.equals(rightOperand, context.getContextClassExpression()) && !processedAxioms.contains(objectivePremise)) {
                continue;
            }

            conclusionSuperclasses.addAll(intersectionSuperclassesByRightOperand.get(rightOperand));
        }

        return conclusionSuperclasses;
    }

    private Set<OWLSubClassOfAxiom> buildConclusions(OWLClassExpression subClass, Set<OWLClassExpression> conclusionSuperclasses) {
        OWLDataFactory owlDataFactory = OWLManager.getOWLDataFactory();
        Set<OWLSubClassOfAxiom> conclusions = new HashSet<>(conclusionSuperclasses.size());

        for (OWLClassExpression conclusionSuperclass: conclusionSuperclasses) {
            conclusions.add(owlDataFactory.getOWLSubClassOfAxiom(subClass, conclusionSuperclass));
        }

        return conclusions;
    }
}
