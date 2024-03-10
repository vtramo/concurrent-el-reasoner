package saturation.rules;

import org.semanticweb.owlapi.model.OWLSubClassOfAxiom;
import saturation.context.Context;

import java.util.Collection;

@FunctionalInterface
public interface ContextCompletionRule<T extends Context> {

    Collection<OWLSubClassOfAxiom> apply(OWLSubClassOfAxiom premise, T context);

}
