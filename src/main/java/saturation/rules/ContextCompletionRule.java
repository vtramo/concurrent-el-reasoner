package saturation.rules;

import org.semanticweb.owlapi.model.OWLSubClassOfAxiom;
import saturation.context.Context;

import java.util.Set;

@FunctionalInterface
public interface ContextCompletionRule<T extends Context> {

    Set<OWLSubClassOfAxiom> apply(OWLSubClassOfAxiom premise, T context);

}
