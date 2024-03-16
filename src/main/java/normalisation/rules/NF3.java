package normalisation.rules;

import org.semanticweb.owlapi.model.*;
import uk.ac.manchester.cs.owl.owlapi.OWLDataFactoryImpl;

import java.util.Collection;
import java.util.List;

import static normalisation.rules.NormalisationRule.*;

public class NF3 implements NormalisationRule {
    @Override
    public Collection<OWLSubClassOfAxiom> normalise(IRI ontologyIri, OWLSubClassOfAxiom subClassOfAxiom) {
        OWLClassExpression subClass = subClassOfAxiom.getSubClass();
        OWLClassExpression superClass = subClassOfAxiom.getSuperClass();

        if (!(superClass instanceof OWLObjectSomeValuesFrom objectSomeValuesFrom) || !subClass.isNamed()) {
            throw new IllegalArgumentException();
        }

        OWLClassExpression filler = objectSomeValuesFrom.getFiller();
        if (filler.isNamed() || filler.isTopEntity()) {
            throw new IllegalArgumentException();
        }

        OWLDataFactoryImpl owlDataFactory = new OWLDataFactoryImpl();
        OWLClass newClass = generateClass(ontologyIri, subClass, superClass);

        OWLSubClassOfAxiom firstNewSubClassOfAxiom = owlDataFactory.getOWLSubClassOfAxiom(newClass, filler);

        OWLObjectPropertyExpression property = objectSomeValuesFrom.getProperty();
        OWLObjectSomeValuesFrom newObjectSomeValuesFrom = owlDataFactory.getOWLObjectSomeValuesFrom(property, newClass);
        OWLSubClassOfAxiom secondNewSubClassOfAxiom = owlDataFactory.getOWLSubClassOfAxiom(subClass, newObjectSomeValuesFrom);

        return List.of(firstNewSubClassOfAxiom, secondNewSubClassOfAxiom);
    }
}
