package normalisation;

import org.semanticweb.owlapi.model.*;

public class NormalisationChecks {

    public static boolean isSubclassBCConcept(OWLClassExpression subClass) {
        return (isConceptName(subClass) || isIndividual(subClass) || subClass.isOWLThing()) || subClass instanceof OWLObjectOneOf || subClass instanceof OWLIndividual;
    }

    public static boolean isSuperclassBCConcept(OWLClassExpression superClass) {
        return (isConceptName(superClass) || isIndividual(superClass) || superClass.isOWLNothing() || superClass.isOWLThing()) || superClass instanceof OWLObjectOneOf || superClass instanceof OWLIndividual;
    }

    public static boolean isIndividual(OWLClassExpression classExpression) {
        return classExpression instanceof OWLIndividual;
    }

    public static boolean isConceptName(OWLClassExpression classExpression) {
        return classExpression instanceof OWLClass && !classExpression.isOWLNothing() && !classExpression.isOWLThing();
    }

    public static boolean isObjectSomeValuesFrom(OWLClassExpression classExpression) {
        return classExpression instanceof OWLObjectSomeValuesFrom;
    }

}
