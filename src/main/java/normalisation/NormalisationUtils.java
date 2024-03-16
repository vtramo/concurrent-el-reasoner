package normalisation;

import normalisation.rules.NormalisationRule;
import org.semanticweb.owlapi.model.*;

import java.util.List;

public class NormalisationUtils {

    public static boolean isSubclassBCConcept(OWLClassExpression subClass) {
        return (isConceptName(subClass) || isIndividual(subClass) || subClass.isOWLThing()) || subClass instanceof OWLObjectOneOf;
    }

    public static boolean isSuperclassBCConcept(OWLClassExpression superClass) {
        return (isConceptName(superClass) || isIndividual(superClass) || superClass.isOWLNothing() || superClass.isOWLThing()) || superClass instanceof OWLObjectOneOf;
    }

    public static boolean isIndividual(OWLClassExpression classExpression) {
        return classExpression instanceof OWLIndividual;
    }

    public static boolean isConceptName(OWLClassExpression classExpression) {
        return classExpression instanceof OWLClass && !classExpression.isOWLNothing() && !classExpression.isOWLThing();
    }

    public static boolean isAbnormalTBoxAxiom(OWLSubClassOfAxiom owlSubClassOfAxiom) {
        OWLClassExpression subClass = owlSubClassOfAxiom.getSubClass();
        OWLClassExpression superClass = owlSubClassOfAxiom.getSuperClass();

        if (isSubclassBCConcept(subClass) && isSuperclassBCConcept(superClass)) {
            return false;
        }

        if (subClass instanceof OWLObjectIntersectionOf objectIntersectionOf && isSuperclassBCConcept(superClass)) {
            List<OWLClassExpression> operands = objectIntersectionOf.getOperandsAsList();
            OWLClassExpression first = operands.getFirst();
            OWLClassExpression last = operands.getLast();

            if (isSubclassBCConcept(first)  && isSubclassBCConcept(last)) return false;
        }

        if (isSubclassBCConcept(subClass) && superClass instanceof OWLObjectSomeValuesFrom rightIsObjectSomeValuesFrom) {
            OWLClassExpression filler = rightIsObjectSomeValuesFrom.getFiller();
            if (isSubclassBCConcept(filler)) return false;
        }

        if (subClass instanceof OWLObjectSomeValuesFrom leftIsObjectSomeValuesFrom && isSuperclassBCConcept(superClass)) {
            OWLClassExpression filler = leftIsObjectSomeValuesFrom.getFiller();
            return !isSubclassBCConcept(filler);
        }

        return true;
    }

    public static boolean isGenerated(OWLClass owlClass) {
        return owlClass.toString().contains(NormalisationRule.GENERATED_CLASS);
    }

}