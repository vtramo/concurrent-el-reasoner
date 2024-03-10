package saturation;

import org.semanticweb.owlapi.model.OWLSubClassOfAxiom;

import java.util.HashSet;
import java.util.Set;

public record SaturationResult(Set<OWLSubClassOfAxiom> conclusions, Set<OWLSubClassOfAxiom> discardedAxioms) {
    public Set<OWLSubClassOfAxiom> getAllAxioms() {
        return new HashSet<>() {{
            addAll(conclusions);
            addAll(discardedAxioms);
        }};
    }
}
