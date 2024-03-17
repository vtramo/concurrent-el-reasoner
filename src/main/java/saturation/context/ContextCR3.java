package saturation.context;

import indexing.RoleAndFiller;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLSubClassOfAxiom;

import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicBoolean;

import static normalisation.NormalisationUtils.isSubclassBCConcept;
import static normalisation.NormalisationUtils.isSuperclassBCConcept;

public final class ContextCR3 implements Context {
    private final ContextType contextType = ContextType.CR3;
    private final OWLClassExpression contextClassExpression;
    private final Map<OWLClassExpression, Set<RoleAndFiller>> existentialRightSetBySubclassOntologyIndex;
    private final Queue<OWLSubClassOfAxiom> todoAxioms = new ConcurrentLinkedQueue<>();
    private final Set<OWLSubClassOfAxiom> processedAxioms = new HashSet<>();
    private final AtomicBoolean isActive = new AtomicBoolean(false);

    private boolean isInitialized;

    public ContextCR3(OWLClassExpression contextClassExpression) {
        this(contextClassExpression, Collections.emptyMap());
    }

    public ContextCR3(OWLClassExpression contextClassExpression, Map<OWLClassExpression, Set<RoleAndFiller>> existentialRightSetBySubclassOntologyIndex) {
        this.contextClassExpression = contextClassExpression;
        this.existentialRightSetBySubclassOntologyIndex = existentialRightSetBySubclassOntologyIndex;
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

    @Override
    public boolean isInitialized() {
        return isInitialized;
    }

    @Override
    public Set<OWLSubClassOfAxiom> initialize() {
        if (isInitialized) throw new IllegalStateException();

        OWLDataFactory owlDataFactory = OWLManager.getOWLDataFactory();

        OWLSubClassOfAxiom selfSubClassOf = owlDataFactory.getOWLSubClassOfAxiom(contextClassExpression, contextClassExpression);
        OWLSubClassOfAxiom subClassOfThing = owlDataFactory.getOWLSubClassOfAxiom(contextClassExpression, owlDataFactory.getOWLThing());

        isInitialized = true;

        return new HashSet<>() {{ add(selfSubClassOf); add(subClassOfThing); }};
    }

    public Map<OWLClassExpression, Set<RoleAndFiller>> getExistentialRightSetBySubclassOntologyIndex() {
        return existentialRightSetBySubclassOntologyIndex;
    }

    public ContextType getContextType() {
        return contextType;
    }

    @Override
    public boolean equals(Object other) {
        if (!(other instanceof ContextCR3 otherContextCR3)) return false;
        return contextClassExpression.equals(otherContextCR3.contextClassExpression) && contextType.equals(otherContextCR3.getContextType());
    }

    @Override
    public int hashCode() {
        return (contextClassExpression.toString() + contextType).hashCode();
    }
}
