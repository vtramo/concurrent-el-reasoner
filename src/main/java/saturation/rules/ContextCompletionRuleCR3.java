package saturation.rules;

import index.RoleAndFiller;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.*;
import saturation.context.ContextCR3;

import java.util.*;

public final class ContextCompletionRuleCR3 implements ContextCompletionRule<ContextCR3> {

    @Override
    public Collection<OWLSubClassOfAxiom> apply(OWLSubClassOfAxiom premise, ContextCR3 context) {
        OWLClassExpression superClass = premise.getSuperClass();

        Map<OWLClassExpression, Set<RoleAndFiller>> existentialRightSetBySubclassOntologyIndex =
            context.getExistentialRightSetBySubclassOntologyIndex();

        if (!existentialRightSetBySubclassOntologyIndex.containsKey(superClass)) {
            return Collections.emptyList();
        }

        OWLClassExpression subClass = premise.getSubClass();
        Set<RoleAndFiller> roleAndFillerSet = existentialRightSetBySubclassOntologyIndex.get(superClass);
        return buildConclusions(subClass, roleAndFillerSet);
    }

    private Set<OWLSubClassOfAxiom> buildConclusions(OWLClassExpression subClass, Set<RoleAndFiller> roleAndFillerSet) {
        OWLDataFactory owlDataFactory = OWLManager.getOWLDataFactory();
        Set<OWLSubClassOfAxiom> conclusions = new HashSet<>();

        for (RoleAndFiller roleAndFiller: roleAndFillerSet) {
            OWLObjectPropertyExpression role = roleAndFiller.role();
            OWLClassExpression filler = roleAndFiller.filler();

            OWLObjectSomeValuesFrom existsRoleFiller = owlDataFactory.getOWLObjectSomeValuesFrom(role, filler);

            OWLSubClassOfAxiom conclusion = owlDataFactory.getOWLSubClassOfAxiom(subClass, existsRoleFiller);

            conclusions.add(conclusion);
        }

        return conclusions;
    }
}
