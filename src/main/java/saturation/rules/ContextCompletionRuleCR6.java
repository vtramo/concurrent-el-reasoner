package saturation.rules;

import com.google.common.graph.Graph;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.*;
import saturation.context.ContextCR6;

import java.util.*;

import static java.util.stream.Collectors.toSet;

public final class ContextCompletionRuleCR6 implements ContextCompletionRule<ContextCR6> {
    @Override
    public Set<OWLSubClassOfAxiom> apply(OWLSubClassOfAxiom premise, ContextCR6 context) {
        Set<OWLClassExpression> processedIndividualSubclasses = context.getProcessedIndividualSubclasses();
        if (processedIndividualSubclasses.size() < 2) {
            return new HashSet<>();
        }

        Set<OWLSubClassOfAxiom> conclusions = new HashSet<>();
        for (OWLClassExpression processedSubclass1: processedIndividualSubclasses) {
            for (OWLClassExpression processedSubclass2: processedIndividualSubclasses) {
                if (Objects.equals(processedSubclass1, processedSubclass2)) {
                    continue;
                }

                Set<OWLSubClassOfAxiom> results = inferConclusions(context, processedSubclass1, processedSubclass2);
                conclusions.addAll(results);
            }
        }

        return conclusions;
    }

    private Set<OWLSubClassOfAxiom> inferConclusions(ContextCR6 context, OWLClassExpression origin, OWLClassExpression destination) {
        Set<OWLSubClassOfAxiom> conclusions = new HashSet<>();
        OWLDataFactory owlDataFactory = OWLManager.getOWLDataFactory();

        boolean find = reachability(context, origin, destination);
        if (!find) {
            Set<OWLIndividual> processedIndividuals = context.getProcessedIndividuals();
            for (OWLIndividual processedIndividual: processedIndividuals) {
                if (Objects.equals(processedIndividual, context.getIndividual())) break;
                OWLObjectOneOf owlObjectOneOf = owlDataFactory.getOWLObjectOneOf(processedIndividual);
                find = reachability(context, owlObjectOneOf, destination);
                if (find) break;
            }
        }

        if (find) {
            Map<OWLClassExpression, Set<OWLClassExpression>> superclassesBySubclassProcessedAxioms =
                context.getSimpleSuperclassesBySubclassProcessedAxiomsMap();

            Set<OWLClassExpression> superclassesDestination = superclassesBySubclassProcessedAxioms.getOrDefault(destination, new HashSet<>());

            conclusions.addAll(
                superclassesDestination.stream()
                    .map(conclusionSuperclass -> owlDataFactory.getOWLSubClassOfAxiom(origin, conclusionSuperclass))
                    .collect(toSet())
            );

        }

        return conclusions;
    }

    private boolean reachability(ContextCR6 context, OWLObject origin, OWLObject destination) {
        Graph<OWLObject> graph = context.getGraph();
        Set<OWLObject> reachedObjects = new HashSet<>(graph.nodes().size());

        List<OWLObject> queue = new ArrayList<>();
        queue.add(origin);

        boolean find = false;
        while (!queue.isEmpty()) {
            OWLObject node = queue.removeFirst();
            reachedObjects.add(node);

            for (OWLObject successor: graph.successors(node)) {
                if (Objects.equals(successor, destination)) {
                    find = true;
                    break;
                }

                if (!reachedObjects.contains(successor)) {
                    queue.add(successor);
                }
            }

            if (find) break;
        }

        return find;
    }
}
