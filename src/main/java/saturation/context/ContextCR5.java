package saturation.context;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLObjectSomeValuesFrom;
import org.semanticweb.owlapi.model.OWLSubClassOfAxiom;

import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicBoolean;

import static normalisation.NormalisationUtils.isSubclassBCConcept;

public final class ContextCR5 implements Context {
    private final ContextType contextType = ContextType.CR5;
    private final OWLClassExpression contextClassExpression;
    private final Queue<OWLSubClassOfAxiom> todoAxioms = new ConcurrentLinkedQueue<>();
    private final Set<OWLSubClassOfAxiom> existentialRightProcessedAxioms = new HashSet<>();
    private OWLSubClassOfAxiom processedSubclassOfNothingAxiom;
    private final AtomicBoolean isActive = new AtomicBoolean(false);

    private boolean isInitialized;

    public ContextCR5(OWLClassExpression contextClassExpression) {
        this.contextClassExpression = contextClassExpression;
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

        OWLClassExpression superClass = axiom.getSuperClass();
        if (superClass.isOWLNothing()) {
            boolean added = (processedSubclassOfNothingAxiom == null);
            processedSubclassOfNothingAxiom = axiom;
            return added;
        } else {
            return existentialRightProcessedAxioms.add(axiom);
        }
    }

    private void checkAxiomValidity(OWLSubClassOfAxiom axiom) {
        OWLClassExpression subClass = axiom.getSubClass();
        OWLClassExpression superClass = axiom.getSuperClass();
        if (!isSubclassBCConcept(subClass) || (!superClass.isOWLNothing() && !(superClass instanceof OWLObjectSomeValuesFrom))) {
            throw new IllegalArgumentException();
        }

        if (superClass instanceof OWLObjectSomeValuesFrom someValuesFrom && !Objects.equals(someValuesFrom.getFiller(), contextClassExpression)) {
            throw new IllegalArgumentException();
        }

        if (superClass.isOWLNothing() && !Objects.equals(subClass, contextClassExpression)) {
            throw new IllegalArgumentException();
        }
    }

    @Override
    public boolean containsProcessedAxiom(OWLSubClassOfAxiom axiom) {
        if (axiom instanceof OWLObjectSomeValuesFrom) {
            return existentialRightProcessedAxioms.contains(axiom);
        } else {
            return Objects.equals(axiom, processedSubclassOfNothingAxiom);
        }
    }

    @Override
    public Set<OWLSubClassOfAxiom> getProcessedAxioms() {
        Set<OWLSubClassOfAxiom> processedAxioms = new HashSet<>(existentialRightProcessedAxioms);

        if (processedSubclassOfNothingAxiom != null) {
            processedAxioms.add(processedSubclassOfNothingAxiom);
        }

        return processedAxioms;
    }

    public Set<OWLSubClassOfAxiom> getExistentialRightProcessedAxioms() {
        Set<OWLSubClassOfAxiom> processedAxioms = new HashSet<>(existentialRightProcessedAxioms);

        if (processedSubclassOfNothingAxiom != null) {
            processedAxioms.add(processedSubclassOfNothingAxiom);
        }

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

    public Optional<OWLSubClassOfAxiom> getProcessedSubclassOfNothingAxiom() {
        return Optional.ofNullable(processedSubclassOfNothingAxiom);
    }

    public ContextType getContextType() {
        return contextType;
    }

    @Override
    public boolean equals(Object other) {
        if (!(other instanceof ContextCR5 otherContextCR5)) return false;
        return contextClassExpression.equals(otherContextCR5.contextClassExpression) && contextType.equals(otherContextCR5.getContextType());
    }

    @Override
    public int hashCode() {
        return (contextClassExpression.toString() + contextType).hashCode();
    }
}
