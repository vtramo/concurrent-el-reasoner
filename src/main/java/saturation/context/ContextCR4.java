package saturation.context;

import index.GCILeftExistentialIndex;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.*;

import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicBoolean;

import static normalisation.NormalisationUtils.isSubclassBCConcept;
import static normalisation.NormalisationUtils.isSuperclassBCConcept;

public final class ContextCR4 implements Context {
    private final ContextType contextType = ContextType.CR4;
    private final OWLClassExpression contextClassExpression;
    private final Queue<OWLSubClassOfAxiom> todoAxioms = new ConcurrentLinkedQueue<>();
    private final GCILeftExistentialIndex gciLeftExistentialOntologyIndex;
    private final Map<OWLObjectPropertyExpression, Set<OWLClassExpression>> subclassesByRoleProcessedAxioms = new HashMap<>();
    private final Set<OWLSubClassOfAxiom> simpleProcessedAxioms = new HashSet<>();
    private AtomicBoolean isActive = new AtomicBoolean(false);

    public ContextCR4(OWLClassExpression classExpression) {
        this(classExpression, new GCILeftExistentialIndex());
    }

    public ContextCR4(OWLClassExpression classExpression, GCILeftExistentialIndex gciLeftExistentialOntologyIndex) {
        this.contextClassExpression = classExpression;
        this.gciLeftExistentialOntologyIndex = gciLeftExistentialOntologyIndex;
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
    public boolean addProcessedAxiom(OWLSubClassOfAxiom subClassOfAxiom) {
        checkAxiomValidity(subClassOfAxiom);

        OWLClassExpression subClass = subClassOfAxiom.getSubClass();
        OWLClassExpression superClass = subClassOfAxiom.getSuperClass();

        if (superClass instanceof OWLObjectSomeValuesFrom someValuesFrom) {
            OWLObjectPropertyExpression role = someValuesFrom.getProperty();
            return subclassesByRoleProcessedAxioms
                .computeIfAbsent(role, __ -> new HashSet<>())
                .add(subClass);
        } else {
            return simpleProcessedAxioms.add(subClassOfAxiom);
        }
    }

    private void checkAxiomValidity(OWLSubClassOfAxiom axiom) {
        OWLClassExpression subClass = axiom.getSubClass();
        OWLClassExpression superClass = axiom.getSuperClass();
        if (!isSubclassBCConcept(subClass) || (!isSuperclassBCConcept(superClass) && !(superClass instanceof OWLObjectSomeValuesFrom))) {
            throw new IllegalArgumentException();
        }

        if (isSubclassBCConcept(subClass) && isSuperclassBCConcept(superClass) && !Objects.equals(subClass, contextClassExpression)) {
            throw new IllegalArgumentException();
        }

        if (superClass instanceof OWLObjectSomeValuesFrom objectSomeValuesFrom && !Objects.equals(objectSomeValuesFrom.getFiller(), contextClassExpression)) {
            throw new IllegalArgumentException();
        }
    }

    @Override
    public boolean containsProcessedAxiom(OWLSubClassOfAxiom axiom) {
        OWLClassExpression superClass = axiom.getSuperClass();

        if (superClass instanceof OWLObjectSomeValuesFrom someValuesFrom) {
            OWLObjectPropertyExpression role = someValuesFrom.getProperty();

            if (!subclassesByRoleProcessedAxioms.containsKey(role)) {
                return false;
            }

            Set<OWLClassExpression> processedSubclasses = subclassesByRoleProcessedAxioms.get(role);
            return processedSubclasses.contains(superClass);
        } else {
            return simpleProcessedAxioms.contains(axiom);
        }
    }

    @Override
    public Collection<OWLSubClassOfAxiom> getProcessedAxioms() {
        Set<OWLSubClassOfAxiom> processedAxioms = new HashSet<>(simpleProcessedAxioms);

        OWLDataFactory owlDataFactory = OWLManager.getOWLDataFactory();
        for (OWLObjectPropertyExpression role: subclassesByRoleProcessedAxioms.keySet()) {
            for (OWLClassExpression subclass: subclassesByRoleProcessedAxioms.get(role)) {
                OWLObjectSomeValuesFrom existsRoleContextClassExpression = owlDataFactory.getOWLObjectSomeValuesFrom(role, contextClassExpression);
                OWLSubClassOfAxiom subClassOfAxiom = owlDataFactory.getOWLSubClassOfAxiom(subclass, existsRoleContextClassExpression);
                processedAxioms.add(subClassOfAxiom);
            }
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

    public GCILeftExistentialIndex getGciLeftExistentialOntologyIndex() {
        return gciLeftExistentialOntologyIndex;
    }

    public Map<OWLObjectPropertyExpression, Set<OWLClassExpression>> getSubclassesByRoleProcessedAxioms() {
        return subclassesByRoleProcessedAxioms;
    }

    public Set<OWLSubClassOfAxiom> getSimpleProcessedAxioms() {
        return simpleProcessedAxioms;
    }

    public ContextType getContextType() {
        return contextType;
    }

    public OWLClassExpression getContextClassExpression() {
        return contextClassExpression;
    }

    @Override
    public boolean equals(Object other) {
        if (!(other instanceof ContextCR4 otherContextCR4)) return false;
        return contextClassExpression.equals(otherContextCR4.contextClassExpression) && contextType.equals(otherContextCR4.getContextType());
    }

    @Override
    public int hashCode() {
        return (contextClassExpression.toString() + contextType).hashCode();
    }
}
