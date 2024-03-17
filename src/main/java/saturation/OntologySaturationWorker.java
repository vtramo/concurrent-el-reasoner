package saturation;

import org.semanticweb.owlapi.model.OWLSubClassOfAxiom;
import saturation.context.*;
import saturation.rules.ContextCompletionRule;
import saturation.rules.ContextCompletionRuleFactory;

import java.util.*;

public class OntologySaturationWorker extends Thread implements ContextVisitor {
    private final ActiveContextsQueue<Context> activeContexts;
    private final ContextAllocator contextAllocator;
    private final Set<Context> processedContexts;
    private final ContextCompletionRuleFactory completionRuleFactory;

    private OWLSubClassOfAxiom currentPolledAxiom;

    public OntologySaturationWorker(ActiveContextsQueue<Context> activeContexts, ContextAllocator contextAllocator) {
        this(activeContexts, contextAllocator, new ContextCompletionRuleFactory());
    }

    public OntologySaturationWorker(
        ActiveContextsQueue<Context> activeContexts,
        ContextAllocator contextAllocator,
        ContextCompletionRuleFactory completionRuleFactory
    ) {
        this.activeContexts = activeContexts;
        this.contextAllocator = contextAllocator;
        this.completionRuleFactory = completionRuleFactory;
        this.processedContexts = new HashSet<>();
    }

    @Override
    public void run() {
        for (;;) {
            Optional<Context> optionalContext = activeContexts.poll();

            if (optionalContext.isEmpty()) {
                break;
            }

            Context context = optionalContext.get();
            for (;;) {
                Optional<OWLSubClassOfAxiom> optionalAxiom = context.pollTodoAxiom();

                if (optionalAxiom.isEmpty()) {
                    break;
                }

                currentPolledAxiom = optionalAxiom.get();
                if (context.containsProcessedAxiom(currentPolledAxiom)) {
                    continue;
                }

                context.addProcessedAxiom(currentPolledAxiom);
                context.accept(this);
            }

            activeContexts.deactivateContext(context);
        }
    }

    public Set<Context> getProcessedContexts() {
        return processedContexts;
    }

    @Override
    public void visit(ContextCR1 contextCR1) {
        ContextCompletionRule<ContextCR1> completionRule = completionRuleFactory.buildContextContextCompletionRule(contextCR1);

        Set<OWLSubClassOfAxiom> conclusions = completionRule.apply(currentPolledAxiom, contextCR1);

        processedContexts.add(contextCR1);

        processConclusions(conclusions);
    }

    @Override
    public void visit(ContextCR2 contextCR2) {
        ContextCompletionRule<ContextCR2> completionRule = completionRuleFactory.buildContextContextCompletionRule(contextCR2);

        Set<OWLSubClassOfAxiom> conclusions = completionRule.apply(currentPolledAxiom, contextCR2);

        processedContexts.add(contextCR2);

        processConclusions(conclusions);
    }

    @Override
    public void visit(ContextCR3 contextCR3) {
        ContextCompletionRule<ContextCR3> completionRule = completionRuleFactory.buildContextContextCompletionRule(contextCR3);

        Set<OWLSubClassOfAxiom> conclusions = completionRule.apply(currentPolledAxiom, contextCR3);

        processedContexts.add(contextCR3);

        processConclusions(conclusions);
    }

    @Override
    public void visit(ContextCR4 contextCR4) {
        ContextCompletionRule<ContextCR4> completionRule = completionRuleFactory.buildContextContextCompletionRule(contextCR4);

        Set<OWLSubClassOfAxiom> conclusions = completionRule.apply(currentPolledAxiom, contextCR4);

        processedContexts.add(contextCR4);

        processConclusions(conclusions);
    }

    @Override
    public void visit(ContextCR5 contextCR5) {
        ContextCompletionRule<ContextCR5> completionRule = completionRuleFactory.buildContextContextCompletionRule(contextCR5);

        Set<OWLSubClassOfAxiom> conclusions = completionRule.apply(currentPolledAxiom, contextCR5);

        processedContexts.add(contextCR5);

        processConclusions(conclusions);
    }

    @Override
    public void visit(ContextCR6 contextCR6) {
        ContextCompletionRule<ContextCR6> completionRule = completionRuleFactory.buildContextContextCompletionRule(contextCR6);

        Set<OWLSubClassOfAxiom> conclusions = completionRule.apply(currentPolledAxiom, contextCR6);

        processedContexts.add(contextCR6);

        processConclusions(conclusions);
    }

    private void processConclusions(Collection<OWLSubClassOfAxiom> conclusions) {

        for (OWLSubClassOfAxiom conclusion: conclusions) {
            for (Context conclusionContext: contextAllocator.getContexts(conclusion)) {
                conclusionContext.addTodoAxiom(conclusion);
                activeContexts.activateContext(conclusionContext);
            }
        }
    }

    public static Collection<OntologySaturationWorker> buildOntologySaturationWorkers(
        int processors,
        ActiveContextsQueue<Context> activeContextsQueue,
        ContextAllocator contextAllocator
    ) {
        List<OntologySaturationWorker> workers = new ArrayList<>(processors);

        for (int i = 0; i < processors; i++) {
            workers.add(new OntologySaturationWorker(activeContextsQueue, contextAllocator));
        }

        return workers;
    }
}
