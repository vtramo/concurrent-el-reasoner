package saturation.context;

import org.semanticweb.owlapi.model.OWLSubClassOfAxiom;

import javax.annotation.concurrent.ThreadSafe;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

@ThreadSafe
public sealed interface Context permits ContextCR1, ContextCR2, ContextCR3, ContextCR4, ContextCR5, ContextCR6 {

    boolean addTodoAxiom(OWLSubClassOfAxiom axiom);
    Optional<OWLSubClassOfAxiom> pollTodoAxiom();
    boolean isQueueTodoAxiomsEmpty();

    boolean addProcessedAxiom(OWLSubClassOfAxiom axiom);
    boolean containsProcessedAxiom(OWLSubClassOfAxiom axiom);
    Set<OWLSubClassOfAxiom> getProcessedAxioms();

    void accept(ContextVisitor contextVisitor);

    AtomicBoolean getIsActive();

    boolean isInitialized();
    Set<OWLSubClassOfAxiom> initialize();

}