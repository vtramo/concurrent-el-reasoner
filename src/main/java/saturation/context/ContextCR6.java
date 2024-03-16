package saturation.context;

import org.semanticweb.owlapi.model.*;
import saturation.collections.FastReachabilityGraph;

import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicBoolean;

import static normalisation.NormalisationUtils.isSubclassBCConcept;
import static normalisation.NormalisationUtils.isSuperclassBCConcept;

public final class ContextCR6 implements Context {
    private final ContextType contextType = ContextType.CR6;
    private final OWLIndividual individual;
    private final Queue<OWLSubClassOfAxiom> todoAxioms = new ConcurrentLinkedQueue<>();
    private final Set<OWLIndividual> processedIndividuals = new HashSet<>();
    private final Set<OWLClassExpression> processedIndividualSubclasses = new HashSet<>();
    private final Map<OWLClassExpression, Set<OWLClassExpression>> simpleSuperclassesBySubclassProcessedAxiomsMap = new HashMap<>();
    private final Set<OWLSubClassOfAxiom> processedAxioms = new HashSet<>();
    private final FastReachabilityGraph<OWLObject> fastReachabilityGraph = new FastReachabilityGraph<>();
    private AtomicBoolean isActive = new AtomicBoolean(false);

    public ContextCR6(OWLIndividual individual) {
        this.individual = individual;
    }

    @Override
    public boolean addTodoAxiom(OWLSubClassOfAxiom axiom) {
        OWLClassExpression subClass = axiom.getSubClass();
        OWLClassExpression superClass = axiom.getSuperClass();
        if (!isSubclassBCConcept(subClass) || (!isSuperclassBCConcept(superClass) && !(superClass instanceof OWLObjectSomeValuesFrom))) {
            throw new IllegalArgumentException();
        }

        return todoAxioms.add(axiom);
    }

    @Override
    public Optional<OWLSubClassOfAxiom> pollTodoAxiom() {
        return Optional.ofNullable(todoAxioms.poll());
    }

    @Override
    public boolean isQueueTodoAxiomsEmpty() {
        return todoAxioms.isEmpty();
    }

    @Override
    public boolean addProcessedAxiom(OWLSubClassOfAxiom axiom) {
        OWLClassExpression subClass = axiom.getSubClass();
        OWLClassExpression superClass = axiom.getSuperClass();

        if (subClass instanceof OWLClass conceptName) {

            fastReachabilityGraph.addNode(conceptName);
            if (isSuperclassBCConcept(superClass)) {
                simpleSuperclassesBySubclassProcessedAxiomsMap
                    .computeIfAbsent(subClass, __ -> new HashSet<>())
                    .add(superClass);
            }

        }

        if (superClass instanceof OWLClass conceptName) {

            fastReachabilityGraph.addNode(conceptName);

        }

        if (subClass instanceof OWLObjectOneOf objectOneOf) {

            Set<OWLIndividual> individuals = objectOneOf.getIndividuals();
            if (individuals.size() > 1) throw new IllegalArgumentException();
            individuals.forEach(individual -> {
                fastReachabilityGraph.addNode(individual);
                processedIndividuals.add(individual);
            });

        }

        if (superClass instanceof OWLObjectOneOf objectOneOf) {

            Set<OWLIndividual> individuals = objectOneOf.getIndividuals();
            if (individuals.size() > 1) throw new IllegalArgumentException();
            individuals.forEach(individual -> {
                fastReachabilityGraph.addNode(individual);
                processedIndividuals.add(individual);
                if (isSubclassBCConcept(subClass)) {
                    processedIndividualSubclasses.add(subClass);
                }
            });

        }

        if (isSubclassBCConcept(subClass) && superClass instanceof OWLObjectSomeValuesFrom objectSomeValuesFrom) {

            OWLClassExpression filler = objectSomeValuesFrom.getFiller();
            fastReachabilityGraph.addNode(filler);
            fastReachabilityGraph.connectStronglyConnectedComponents(subClass, filler);

        }

        return processedAxioms.add(axiom);
    }

    @Override
    public boolean containsProcessedAxiom(OWLSubClassOfAxiom axiom) {
        return processedAxioms.contains(axiom);
    }

    @Override
    public Collection<OWLSubClassOfAxiom> getProcessedAxioms() {
        return processedAxioms;
    }

    @Override
    public void accept(ContextVisitor contextVisitor) {
        contextVisitor.visit(this);
    }

    @Override
    public AtomicBoolean getIsActive() {
        return isActive;
    }

    public Map<OWLClassExpression, Set<OWLClassExpression>> getSimpleSuperclassesBySubclassProcessedAxiomsMap() {
        return simpleSuperclassesBySubclassProcessedAxiomsMap;
    }

    public FastReachabilityGraph<OWLObject> getFastReachabilityGraph() {
        return fastReachabilityGraph;
    }

    public OWLIndividual getIndividual() {
        return individual;
    }

    public Set<OWLIndividual> getProcessedIndividuals() {
        return processedIndividuals;
    }

    public Set<OWLClassExpression> getProcessedIndividualSubclasses() {
        return processedIndividualSubclasses;
    }

    public ContextType getContextType() {
        return contextType;
    }

    @Override
    public boolean equals(Object other) {
        if (!(other instanceof ContextCR6 otherContextCR6)) return false;
        return individual.equals(otherContextCR6.individual) && contextType.equals(otherContextCR6.getContextType());
    }

    @Override
    public int hashCode() {
        return (individual.toString() + contextType).hashCode();
    }
}
