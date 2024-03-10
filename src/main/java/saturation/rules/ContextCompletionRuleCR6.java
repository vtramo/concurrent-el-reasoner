package saturation.rules;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.*;
import saturation.collections.FastReachabilityGraph;
import saturation.context.ContextCR6;

import java.util.*;

import static java.util.stream.Collectors.toSet;

public final class ContextCompletionRuleCR6 implements ContextCompletionRule<ContextCR6> {
    @Override
    public Collection<OWLSubClassOfAxiom> apply(OWLSubClassOfAxiom premise, ContextCR6 context) {
        Set<OWLClassExpression> processedIndividualSubclasses = context.getProcessedIndividualSubclasses();
        if (processedIndividualSubclasses.size() < 2) return Collections.emptyList();

        Set<OWLSubClassOfAxiom> conclusions = new HashSet<>();
        for (OWLClassExpression processedSubclass1: processedIndividualSubclasses) {
            for (OWLClassExpression processedSubclass2: processedIndividualSubclasses) {
                if (Objects.equals(processedSubclass1, processedSubclass2)) continue;
                Set<OWLSubClassOfAxiom> results = inferConclusions(context, processedSubclass1, processedSubclass2);
                conclusions.addAll(results);
                results = inferConclusions(context, processedSubclass2, processedSubclass1);
                conclusions.addAll(results);
            }
        }

        return conclusions;
    }

    private Set<OWLSubClassOfAxiom> inferConclusions(ContextCR6 context, OWLClassExpression origin, OWLClassExpression destination) {
        Set<OWLSubClassOfAxiom> conclusions = new HashSet<>();
        OWLDataFactory owlDataFactory = OWLManager.getOWLDataFactory();

        if (reachability(context, origin, destination)) {
            Map<OWLClassExpression, Set<OWLClassExpression>> superclassesBySubclassProcessedAxioms =
                context.getSimpleSuperclassesBySubclassProcessedAxiomsMap();

            Set<OWLClassExpression> superclassesProcessedSubclass2 = superclassesBySubclassProcessedAxioms.getOrDefault(destination, new HashSet<>());
            Set<OWLClassExpression> superclassesProcessedSubclass1 = superclassesBySubclassProcessedAxioms.getOrDefault(origin, new HashSet<>());
            if (!superclassesProcessedSubclass1.containsAll(superclassesProcessedSubclass2)) {
                conclusions.addAll(
                    superclassesProcessedSubclass2.stream()
                        .map(conclusionSuperclass -> owlDataFactory.getOWLSubClassOfAxiom(origin, conclusionSuperclass))
                        .collect(toSet())
                );
            }
        }

        return conclusions;
    }

    private boolean reachability(ContextCR6 context, OWLClassExpression origin, OWLClassExpression destination) {
        FastReachabilityGraph<OWLObject> fastReachabilityGraph = context.getFastReachabilityGraph();
        if (fastReachabilityGraph.reachability(origin, destination)) return true;
        for (OWLIndividual individual: context.getProcessedIndividuals()) {
            if (fastReachabilityGraph.reachability(individual, destination)) return true;
        }
        return false;
    }
}
