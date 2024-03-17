package saturation;

import indexing.OntologyIndex;
import org.semanticweb.owlapi.model.AxiomType;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLSubClassOfAxiom;
import saturation.context.*;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import static java.util.stream.Collectors.toSet;
import static saturation.OntologySaturationWorker.buildOntologySaturationWorkers;

public class OntologySaturationProcess {
    private final OWLOntology ontology;
    private final ContextAllocator contextAllocator;
    private final Set<OWLSubClassOfAxiom> discardedAxioms = new HashSet<>();

    private ActiveContextsQueue<Context> activeContexts;

    public OntologySaturationProcess(OWLOntology ontology, OntologyIndex ontologyIndex) {
        this(ontology, new MultiMapContextAllocator(ontology, ontologyIndex));
    }

    public OntologySaturationProcess(OWLOntology ontology, ContextAllocator contextAllocator) {
        this.ontology = ontology;
        this.contextAllocator = contextAllocator;
        this.activeContexts = new ConcurrentActiveContextsLinkedQueue();
    }

    public SaturationResult saturate() {
        createInitialContexts();

        int processors = Runtime.getRuntime().availableProcessors();
        Collection<OntologySaturationWorker> workers = buildOntologySaturationWorkers(processors, activeContexts, contextAllocator);

        workers.forEach(OntologySaturationWorker::start);

        workers.forEach(contextOntologySaturationWorker -> {
            try {
                contextOntologySaturationWorker.join();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        });

        Set<OWLSubClassOfAxiom> conclusions = workers
            .stream()
            .map(OntologySaturationWorker::getProcessedContexts)
            .filter(processedContexts -> !processedContexts.isEmpty())
            .flatMap(Collection::stream)
            .map(Context::getProcessedAxioms)
            .flatMap(Collection::stream)
            .collect(toSet());

        return new SaturationResult(conclusions, discardedAxioms);
    }

    private void createInitialContexts() {
        clearProcess();

        contextAllocator.initContexts();
        for (OWLSubClassOfAxiom axiom: ontology.getAxioms(AxiomType.SUBCLASS_OF)) {
            Collection<Context> contexts = contextAllocator.getContexts(axiom);
            if (contexts.isEmpty()) discardedAxioms.add(axiom);
            for (Context context: contexts) {
                context.addTodoAxiom(axiom);
                activeContexts.activateContext(context);
            }
        }
    }

    private void clearProcess() {
        activeContexts = new ConcurrentActiveContextsLinkedQueue();
        discardedAxioms.clear();
    }
}
