package saturation.rules;

import indexing.LeftExistentialOntologyIndex;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.*;
import saturation.context.ContextCR4;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public final class ContextCompletionRuleCR4 implements ContextCompletionRule<ContextCR4> {
    @Override
    public Set<OWLSubClassOfAxiom> apply(OWLSubClassOfAxiom premise, ContextCR4 context) {
        OWLClassExpression subClass = premise.getSubClass();
        OWLClassExpression superClass = premise.getSuperClass();

        Set<OWLSubClassOfAxiom> conclusions;
        if (superClass instanceof OWLObjectSomeValuesFrom objectSomeValuesFrom) {
            OWLObjectPropertyExpression role = objectSomeValuesFrom.getProperty();
            conclusions = processRightExistentialPremise(subClass, role, context);
        } else {
            conclusions = processSimplePremise(superClass, context);
        }

        return conclusions;
    }

    private Set<OWLSubClassOfAxiom> processRightExistentialPremise(OWLClassExpression subClass,  OWLObjectPropertyExpression role, ContextCR4 context) {
        LeftExistentialOntologyIndex gciLeftExistentialOntologyIndex = context.getGciLeftExistentialOntologyIndex();

        if (!gciLeftExistentialOntologyIndex.containsRole(role)) {
            return new HashSet<>();
        }

        Set<OWLSubClassOfAxiom> conclusions = new HashSet<>();
        OWLDataFactory owlDataFactory = OWLManager.getOWLDataFactory();
        Set<OWLSubClassOfAxiom> simpleProcessedAxioms = context.getSimpleProcessedAxioms();

        Set<OWLClassExpression> fillers = gciLeftExistentialOntologyIndex.getFillersByRole(role);
        for (OWLClassExpression filler: fillers) {
            OWLSubClassOfAxiom simpleProcessedAxiom = owlDataFactory.getOWLSubClassOfAxiom(context.getContextClassExpression(), filler);
            if (!simpleProcessedAxioms.contains(simpleProcessedAxiom)) {
                continue;
            }

            Set<OWLClassExpression> conclusionSubclasses = Set.of(subClass);
            Set<OWLClassExpression> conclusionSuperclasses = gciLeftExistentialOntologyIndex.getSuperclassesByRoleFiller(role, filler);
            conclusions.addAll(buildConclusions(conclusionSubclasses, conclusionSuperclasses));
        }

        return conclusions;
    }

    private Set<OWLSubClassOfAxiom> processSimplePremise(OWLClassExpression superClass, ContextCR4 context) {
        LeftExistentialOntologyIndex gciLeftExistentialOntologyIndex = context.getGciLeftExistentialOntologyIndex();

        if (!gciLeftExistentialOntologyIndex.containsFiller(superClass)) {
            return new HashSet<>();
        }

        Set<OWLSubClassOfAxiom> conclusions = new HashSet<>();

        Map<OWLObjectPropertyExpression, Set<OWLClassExpression>> subclassesByRoleProcessedAxioms = context.getSubclassesByRoleProcessedAxioms();
        Set<OWLObjectPropertyExpression> roles = gciLeftExistentialOntologyIndex.getRolesByFiller(superClass);
        for (OWLObjectPropertyExpression role: roles) {
            if (!subclassesByRoleProcessedAxioms.containsKey(role)) {
                continue;
            }

            Set<OWLClassExpression> conclusionSubclasses = subclassesByRoleProcessedAxioms.get(role);
            Set<OWLClassExpression> conclusionSuperclasses = gciLeftExistentialOntologyIndex.getSuperclassesByFillerRole(superClass, role);
            conclusions.addAll(buildConclusions(conclusionSubclasses, conclusionSuperclasses));
        }

        return conclusions;
    }

    private Set<OWLSubClassOfAxiom> buildConclusions(Set<OWLClassExpression> conclusionSubclasses, Set<OWLClassExpression> conclusionSuperclasses) {
        OWLDataFactory owlDataFactory = OWLManager.getOWLDataFactory();
        Set<OWLSubClassOfAxiom> conclusions = new HashSet<>();

        for (OWLClassExpression conclusionSubclass: conclusionSubclasses) {
            for (OWLClassExpression conclusionSuperclass: conclusionSuperclasses) {
                OWLSubClassOfAxiom conclusion = owlDataFactory.getOWLSubClassOfAxiom(conclusionSubclass, conclusionSuperclass);
                conclusions.add(conclusion);
            }
        }

        return conclusions;
    }

}
