package normalisation.rules;

import org.semanticweb.owlapi.model.*;
import uk.ac.manchester.cs.owl.owlapi.OWLDataFactoryImpl;

import java.util.Collection;
import java.util.List;

import static normalisation.rules.NormalisationRule.*;

public class NF1Left implements NormalisationRule {
    @Override
    public Collection<OWLSubClassOfAxiom> normalise(IRI ontologyIri, OWLSubClassOfAxiom subClassOfAxiom) {
        OWLClassExpression subClass = subClassOfAxiom.getSubClass();
        OWLClassExpression superClass = subClassOfAxiom.getSuperClass();

        if (!(subClass instanceof OWLObjectIntersectionOf objectIntersectionOf) || !superClass.isNamed()) {
            throw new IllegalArgumentException();
        }

        List<OWLClassExpression> objectIntersectionOfOperands = objectIntersectionOf.getOperandsAsList();
        OWLClassExpression leftObjectIntersectionOfOperand = objectIntersectionOfOperands.getFirst();
        OWLClassExpression rightObjectIntersectionOfOperand = objectIntersectionOfOperands.getLast();

        OWLDataFactoryImpl owlDataFactory = new OWLDataFactoryImpl();
        OWLClass newClass = generateClass(ontologyIri, subClass, superClass);

        OWLSubClassOfAxiom firstNewSubClassOfAxiom = owlDataFactory.getOWLSubClassOfAxiom(leftObjectIntersectionOfOperand, newClass);

        OWLObjectIntersectionOf newObjectIntersectionOf = owlDataFactory.getOWLObjectIntersectionOf(newClass, rightObjectIntersectionOfOperand);
        OWLSubClassOfAxiom secondNewSubClassOfAxiom = owlDataFactory.getOWLSubClassOfAxiom(newObjectIntersectionOf, superClass);

        return List.of(firstNewSubClassOfAxiom, secondNewSubClassOfAxiom);
    }
}