package normalisation.rules;

import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLSubClassOfAxiom;
import uk.ac.manchester.cs.owl.owlapi.OWLDataFactoryImpl;

import java.util.Collection;

public interface NormalisationRule {
    String GENERATED_CLASS = "#GENERATED";

    Collection<OWLSubClassOfAxiom> normalise(IRI ontologyIri, OWLSubClassOfAxiom subClassOfAxiom);

    static OWLClass generateClass(IRI ontologyIRI, OWLClassExpression subclass, OWLClassExpression superclass) {
        OWLDataFactoryImpl owlDataFactory = new OWLDataFactoryImpl();
        return owlDataFactory.getOWLClass(ontologyIRI + GENERATED_CLASS + subclass.hashCode() + superclass.hashCode());
    }
}