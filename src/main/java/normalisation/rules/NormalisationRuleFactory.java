package normalisation.rules;

import org.semanticweb.owlapi.model.*;

import java.util.List;

import static normalisation.NormalisationUtils.*;

public class NormalisationRuleFactory {

    public static NormalisationRule getTBoxAxiomNormalisationRule(OWLSubClassOfAxiom owlSubClassOfAxiom) {
        NormalisationRuleType normalisationRuleType = determineNormalisationRuleType(owlSubClassOfAxiom);

        return switch (normalisationRuleType) {
            case NF0 -> new NF0();
            case NF1_RIGHT -> new NF1Right();
            case NF1_LEFT -> new NF1Left();
            case NF2 -> new NF2();
            case NF3 -> new NF3();
            case NF4 -> new NF4();
        };
    }

    private static NormalisationRuleType determineNormalisationRuleType(OWLSubClassOfAxiom subClassOfAxiom) {
        OWLClassExpression subClass = subClassOfAxiom.getSubClass();
        OWLClassExpression superClass = subClassOfAxiom.getSuperClass();

        if (isSuperclassBCConcept(superClass)) {

            if (subClass instanceof OWLObjectIntersectionOf objectIntersectionOf) {
                List<OWLClassExpression> objectIntersectionOfOperands = objectIntersectionOf.getOperandsAsList();
                OWLClassExpression leftObjectIntersectionOf = objectIntersectionOfOperands.getFirst();
                OWLClassExpression rightObjectIntersectionOf = objectIntersectionOfOperands.getLast();

                if (!isSubclassBCConcept(rightObjectIntersectionOf)) {
                    return NormalisationRuleType.NF1_RIGHT;
                }

                if (!isSubclassBCConcept(leftObjectIntersectionOf)) {
                    return NormalisationRuleType.NF1_LEFT;
                }

            }

            if (subClass instanceof OWLObjectSomeValuesFrom objectSomeValuesFrom) {
                OWLClassExpression filler = objectSomeValuesFrom.getFiller();

                if (!isSubclassBCConcept(filler)) {
                    return NormalisationRuleType.NF2;
                }

            }

        }

        if (isSubclassBCConcept(subClass)) {

            if (superClass instanceof OWLObjectSomeValuesFrom objectSomeValuesFrom) {
                OWLClassExpression filler = objectSomeValuesFrom.getFiller();

                if (!isSuperclassBCConcept(filler)) {
                    return NormalisationRuleType.NF3;
                }
            }

            if (superClass instanceof OWLObjectIntersectionOf) {
                return NormalisationRuleType.NF4;
            }

        }

        if (subClass.isTopEntity() || superClass.isTopEntity()) {
            throw new IllegalArgumentException();
        }

        return NormalisationRuleType.NF0;
    }

    private static boolean isNotNamedAndIsNotTopEntity(OWLClassExpression owlClassExpression) {
        return !owlClassExpression.isNamed() && !owlClassExpression.isTopEntity();
    }
}
