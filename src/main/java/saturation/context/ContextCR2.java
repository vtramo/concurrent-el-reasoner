package saturation.context;

import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLSubClassOfAxiom;

import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicBoolean;

import static normalisation.NormalisationUtils.isSubclassBCConcept;
import static normalisation.NormalisationUtils.isSuperclassBCConcept;

public final class ContextCR2 implements Context {
    private final ContextType contextType = ContextType.CR2;
    private final OWLClassExpression contextClassExpression;
    private final Queue<OWLSubClassOfAxiom> todoAxioms = new ConcurrentLinkedQueue<>();
    private final Set<OWLSubClassOfAxiom> processedAxioms = new HashSet<>();
    private final Map<OWLClassExpression, Map<OWLClassExpression, Set<OWLClassExpression>>> superclassesByIntersectionOperandsOntologyIndex;
    private final AtomicBoolean isActive = new AtomicBoolean(false);

    public ContextCR2(OWLClassExpression contextClassExpression) { this(contextClassExpression, Collections.emptyMap()); }

    public ContextCR2(
        OWLClassExpression contextClassExpression,
        Map<OWLClassExpression, Map<OWLClassExpression, Set<OWLClassExpression>>> superclassesByIntersectionOperandsOntologyIndex
    ) {
        this.contextClassExpression = contextClassExpression;
        this.superclassesByIntersectionOperandsOntologyIndex = superclassesByIntersectionOperandsOntologyIndex;
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

        return processedAxioms.add(axiom);
    }

    private void checkAxiomValidity(OWLSubClassOfAxiom axiom) {
        OWLClassExpression subClass = axiom.getSubClass();
        OWLClassExpression superClass = axiom.getSuperClass();
        if (!isSubclassBCConcept(subClass) || !isSuperclassBCConcept(superClass)) {
            throw new IllegalArgumentException();
        }

        if (!Objects.equals(subClass, contextClassExpression)) {
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

    public Map<OWLClassExpression, Map<OWLClassExpression, Set<OWLClassExpression>>> getSuperclassesByIntersectionOperandsOntologyIndex() {
        return superclassesByIntersectionOperandsOntologyIndex;
    }

    public ContextType getContextType() {
        return contextType;
    }

    @Override
    public boolean equals(Object other) {
        if (!(other instanceof ContextCR2 otherContextCR2)) return false;
        return contextClassExpression.equals(otherContextCR2.contextClassExpression) && contextType.equals(otherContextCR2.getContextType());
    }

    @Override
    public int hashCode() {
        return (contextClassExpression.toString() + contextType).hashCode();
    }
}
