package saturation.context;


import com.github.jsonldjava.shaded.com.google.common.graph.EndpointPair;
import com.github.jsonldjava.shaded.com.google.common.graph.Graph;
import com.github.jsonldjava.shaded.com.google.common.graph.GraphBuilder;
import com.github.jsonldjava.shaded.com.google.common.graph.MutableGraph;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.*;

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
    private final MutableGraph<OWLObject> graph = GraphBuilder.directed().build();
    private final AtomicBoolean isActive = new AtomicBoolean(false);

    private boolean isInitialized;

    public ContextCR6(OWLIndividual individual) {
        this.individual = individual;
    }

    @Override
    public boolean addTodoAxiom(OWLSubClassOfAxiom axiom) {
        checkAxiomValidity(axiom);

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
        checkAxiomValidity(axiom);

        OWLClassExpression subClass = axiom.getSubClass();
        OWLClassExpression superClass = axiom.getSuperClass();

        if (isSubclassBCConcept(subClass)) {

            graph.addNode(subClass);

            if (isSuperclassBCConcept(superClass)) {
                simpleSuperclassesBySubclassProcessedAxiomsMap
                    .computeIfAbsent(subClass, __ -> new HashSet<>())
                    .add(superClass);
            }

        }

        if (superClass instanceof OWLClass conceptName) {

            graph.addNode(conceptName);

        }

        if (subClass instanceof OWLObjectOneOf objectOneOf) {

            Set<OWLIndividual> individuals = objectOneOf.getIndividuals();
            if (individuals.size() > 1) throw new IllegalArgumentException();
            graph.addNode(objectOneOf);

        }

        if (superClass instanceof OWLObjectOneOf objectOneOf) {

            Set<OWLIndividual> individuals = objectOneOf.getIndividuals();
            if (individuals.size() > 1) throw new IllegalArgumentException();
            graph.addNode(objectOneOf);
            processedIndividuals.addAll(individuals);

            individuals.forEach(individual -> {
                if (isSubclassBCConcept(subClass) && Objects.equals(individual, this.getIndividual())) {
                    processedIndividualSubclasses.add(subClass);
                }
            });

        }

        if (isSubclassBCConcept(subClass) && superClass instanceof OWLObjectSomeValuesFrom objectSomeValuesFrom && isSubclassBCConcept(objectSomeValuesFrom.getFiller())) {

            OWLClassExpression filler = objectSomeValuesFrom.getFiller();
            graph.addNode(filler);
            if (!Objects.equals(subClass, filler)) {
                EndpointPair<OWLObject> ordered = EndpointPair.ordered(subClass, filler);

                graph.putEdge(ordered);
            }

        }

        return processedAxioms.add(axiom);
    }

    private void checkAxiomValidity(OWLSubClassOfAxiom axiom) {
        OWLClassExpression subClass = axiom.getSubClass();
        OWLClassExpression superClass = axiom.getSuperClass();

        if (!isSubclassBCConcept(subClass) || (!(superClass instanceof OWLObjectSomeValuesFrom) && !isSuperclassBCConcept(superClass))) {
            throw new IllegalArgumentException();
        }
    }

    @Override
    public boolean containsProcessedAxiom(OWLSubClassOfAxiom axiom) {
        return processedAxioms.contains(axiom);
    }

    @Override
    public Set<OWLSubClassOfAxiom> getProcessedAxioms() {
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

    @Override
    public boolean isInitialized() {
        return isInitialized;
    }

    @Override
    public Set<OWLSubClassOfAxiom> initialize() {
        if (isInitialized) throw new IllegalStateException();

        OWLDataFactory owlDataFactory = OWLManager.getOWLDataFactory();

        OWLObjectOneOf individualObjectOneOf = owlDataFactory.getOWLObjectOneOf(individual);
        OWLSubClassOfAxiom selfSubClassOf = owlDataFactory.getOWLSubClassOfAxiom(individualObjectOneOf, individualObjectOneOf);
        OWLSubClassOfAxiom subClassOfThing = owlDataFactory.getOWLSubClassOfAxiom(individualObjectOneOf, owlDataFactory.getOWLThing());

        isInitialized = true;

        return new HashSet<>() {{ add(selfSubClassOf); add(subClassOfThing); }};
    }

    public Map<OWLClassExpression, Set<OWLClassExpression>> getSimpleSuperclassesBySubclassProcessedAxiomsMap() {
        return simpleSuperclassesBySubclassProcessedAxiomsMap;
    }

    public Graph<OWLObject> getGraph() { return graph; }

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
