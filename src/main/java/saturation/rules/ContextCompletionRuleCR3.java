package saturation.rules;

import indexing.RoleAndFiller;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.*;
import saturation.context.ContextCR3;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public final class ContextCompletionRuleCR3 implements ContextCompletionRule<ContextCR3> {

    @Override
    public Set<OWLSubClassOfAxiom> apply(OWLSubClassOfAxiom premise, ContextCR3 context) {
        OWLClassExpression superClass = premise.getSuperClass();

        Map<OWLClassExpression, Set<RoleAndFiller>> existentialRightSetBySubclassOntologyIndex =
            context.getExistentialRightSetBySubclassOntologyIndex();

        if (!existentialRightSetBySubclassOntologyIndex.containsKey(superClass)) {
            return new HashSet<>();
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
