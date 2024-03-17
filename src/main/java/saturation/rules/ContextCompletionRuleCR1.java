package saturation.rules;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLSubClassOfAxiom;
import saturation.context.ContextCR1;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public final class ContextCompletionRuleCR1 implements ContextCompletionRule<ContextCR1> {

    @Override
    public Set<OWLSubClassOfAxiom> apply(OWLSubClassOfAxiom premise, ContextCR1 context) {
        OWLClassExpression subClass = premise.getSubClass();
        OWLClassExpression superClass = premise.getSuperClass();

        Map<OWLClassExpression, Set<OWLClassExpression>> superclassesBySubclassOntologyIndex = context.getToldSupsOntologyIndex();
        if (!superclassesBySubclassOntologyIndex.containsKey(superClass)) {
            return new HashSet<>();
        }

        Set<OWLClassExpression> conclusionSuperclasses = superclassesBySubclassOntologyIndex.get(superClass);
        return buildConclusions(subClass, conclusionSuperclasses);
    }

    private Set<OWLSubClassOfAxiom> buildConclusions(OWLClassExpression subClass, Set<OWLClassExpression> conclusionSuperclasses) {
        OWLDataFactory owlDataFactory = OWLManager.getOWLDataFactory();
        Set<OWLSubClassOfAxiom> conclusions = new HashSet<>();
        for (OWLClassExpression conclusionSuperclass: conclusionSuperclasses) {
            OWLSubClassOfAxiom conclusion = owlDataFactory.getOWLSubClassOfAxiom(subClass, conclusionSuperclass);
            conclusions.add(conclusion);
        }
        return conclusions;
    }
}
