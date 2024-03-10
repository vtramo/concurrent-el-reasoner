package normalisation.rules;

import org.semanticweb.owlapi.model.*;
import uk.ac.manchester.cs.owl.owlapi.OWLDataFactoryImpl;

import java.util.Collection;
import java.util.List;

public class NF4 implements NormalisationRule {

    @Override
    public Collection<OWLSubClassOfAxiom> normalise(IRI ontologyIri, OWLSubClassOfAxiom subClassOfAxiom) {
        OWLClassExpression subClass = subClassOfAxiom.getSubClass();
        OWLClassExpression superClass = subClassOfAxiom.getSuperClass();

        if (!(superClass instanceof OWLObjectIntersectionOf objectIntersectionOf) || !subClass.isNamed()) {
            throw new IllegalArgumentException();
        }

        List<OWLClassExpression> objectIntersectionOfOperands = objectIntersectionOf.getOperandsAsList();
        OWLClassExpression leftObjectIntersectionOfOperand = objectIntersectionOfOperands.getFirst();
        OWLClassExpression rightObjectIntersectionOfOperand = objectIntersectionOfOperands.getLast();

        OWLDataFactoryImpl owlDataFactory = new OWLDataFactoryImpl();

        OWLSubClassOfAxiom firstNewSubClassOfAxiom = owlDataFactory.getOWLSubClassOfAxiom(subClass, leftObjectIntersectionOfOperand);
        OWLSubClassOfAxiom secondNewSubClassOfAxiom = owlDataFactory.getOWLSubClassOfAxiom(subClass, rightObjectIntersectionOfOperand);

        return List.of(firstNewSubClassOfAxiom, secondNewSubClassOfAxiom);
    }
}
