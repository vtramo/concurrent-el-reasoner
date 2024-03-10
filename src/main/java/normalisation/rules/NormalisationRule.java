package normalisation.rules;

import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLSubClassOfAxiom;

import java.util.Collection;

public interface NormalisationRule {

    Collection<OWLSubClassOfAxiom> normalise(IRI ontologyIri, OWLSubClassOfAxiom subClassOfAxiom);

}