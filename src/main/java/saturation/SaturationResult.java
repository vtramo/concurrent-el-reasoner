package saturation;

import org.semanticweb.owlapi.model.OWLSubClassOfAxiom;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public record SaturationResult(Set<OWLSubClassOfAxiom> conclusions, Set<OWLSubClassOfAxiom> discardedAxioms) {
    public Set<OWLSubClassOfAxiom> getAllAxioms() {
        return new HashSet<>() {{
            addAll(conclusions);
            addAll(discardedAxioms);
        }};
    }

    public boolean contains(OWLSubClassOfAxiom owlSubClassOfAxiom) {
        return conclusions.contains(owlSubClassOfAxiom) || discardedAxioms.contains(owlSubClassOfAxiom);
    }

    public boolean containsAll(Collection<OWLSubClassOfAxiom> axioms) {
        return getAllAxioms().containsAll(axioms);
    }
}
