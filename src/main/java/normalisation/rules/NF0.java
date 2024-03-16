package normalisation.rules;

import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLSubClassOfAxiom;
import uk.ac.manchester.cs.owl.owlapi.OWLDataFactoryImpl;

import java.util.Collection;
import java.util.List;

import static normalisation.rules.NormalisationRule.*;

public class NF0 implements NormalisationRule {
    @Override
    public Collection<OWLSubClassOfAxiom> normalise(IRI ontologyIri, OWLSubClassOfAxiom subClassOfAxiom) {
        OWLClassExpression subClass = subClassOfAxiom.getSubClass();
        OWLClassExpression superClass = subClassOfAxiom.getSuperClass();

        OWLDataFactoryImpl owlDataFactory = new OWLDataFactoryImpl();
        OWLClass newClass = generateClass(ontologyIri, subClass, superClass);

        OWLSubClassOfAxiom firstNewSubClassOfAxiom = owlDataFactory.getOWLSubClassOfAxiom(subClass, newClass);
        OWLSubClassOfAxiom secondNewSubClassOfAxiom = owlDataFactory.getOWLSubClassOfAxiom(newClass, superClass);

        return List.of(firstNewSubClassOfAxiom, secondNewSubClassOfAxiom);
    }
}
