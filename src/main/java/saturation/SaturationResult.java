package saturation;

import org.semanticweb.owlapi.model.OWLSubClassOfAxiom;

import java.util.Collection;
import java.util.Set;

public record SaturationResult(Set<OWLSubClassOfAxiom> conclusions) {
    public Set<OWLSubClassOfAxiom> getAllAxioms() {
        return conclusions;
    }
    public boolean contains(OWLSubClassOfAxiom owlSubClassOfAxiom) {
        return conclusions.contains(owlSubClassOfAxiom);
    }
    public boolean containsAll(Collection<OWLSubClassOfAxiom> axioms) {
        return conclusions.containsAll(axioms);
    }
}
