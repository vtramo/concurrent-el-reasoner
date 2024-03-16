package saturation.context;

import index.OntologyIndex;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.*;

import java.util.*;

import static normalisation.NormalisationUtils.isSubclassBCConcept;
import static normalisation.NormalisationUtils.isSuperclassBCConcept;

public class MultiMapContextAllocator implements ContextAllocator {
    private final Map<OWLObject, Set<Context>> CR1CR2CR3Contexts = new HashMap<>();
    private final Map<OWLObject, Context> CR4Contexts = new HashMap<>();
    private final Map<OWLObject, Context> CR5Contexts = new HashMap<>();
    private final Map<OWLIndividual, Context> CR6Contexts = new HashMap<>();
    private final OWLOntology ontology;
    private final OntologyIndex ontologyIndex;

    public MultiMapContextAllocator(OWLOntology ontology, OntologyIndex ontologyIndex) {
        this.ontology = ontology;
        this.ontologyIndex = ontologyIndex;
    }

    @Override
    public Collection<Context> initContexts() {
        clearContexts();

        Set<OWLEntity> signature = ontology.getSignature();
        Set<Context> contexts = new HashSet<>(signature.size());

        for (OWLEntity entity: signature) {
            contexts.addAll(allocateContexts(entity));
        }

        return contexts;
    }

    private void clearContexts() {
        CR1CR2CR3Contexts.clear();
        CR4Contexts.clear();
        CR5Contexts.clear();
        CR6Contexts.clear();
    }

    private Set<Context> allocateContexts(OWLEntity entity) {
        return switch (entity) {
            case OWLClass conceptName -> {
                ContextCR1 contextCR1 = new ContextCR1(conceptName, ontologyIndex.getToldSups());
                ContextCR2 contextCR2 = new ContextCR2(conceptName, ontologyIndex.getSuperclassesByIntersectionOperands());
                ContextCR3 contextCR3 = new ContextCR3(conceptName, ontologyIndex.getExistentialRightSetBySubclass());
                ContextCR4 contextCR4 = new ContextCR4(conceptName, ontologyIndex.getGciLeftExistentialIndex());
                ContextCR5 contextCR5 = new ContextCR5(conceptName);

                CR1CR2CR3Contexts.put(conceptName, Set.of(contextCR1, contextCR2, contextCR3));
                CR4Contexts.put(conceptName, contextCR4);
                CR5Contexts.put(conceptName, contextCR5);

                yield Set.of(contextCR1, contextCR2, contextCR3, contextCR4, contextCR5);
            }

            case OWLIndividual individual -> {
                OWLDataFactory owlDataFactory = OWLManager.getOWLDataFactory();
                OWLObjectOneOf owlObjectOneOf = owlDataFactory.getOWLObjectOneOf(individual);
                ContextCR1 contextCR1 = new ContextCR1(owlObjectOneOf, ontologyIndex.getToldSups());
                ContextCR2 contextCR2 = new ContextCR2(owlObjectOneOf, ontologyIndex.getSuperclassesByIntersectionOperands());
                ContextCR3 contextCR3 = new ContextCR3(owlObjectOneOf, ontologyIndex.getExistentialRightSetBySubclass());
                ContextCR4 contextCR4 = new ContextCR4(owlObjectOneOf, ontologyIndex.getGciLeftExistentialIndex());
                ContextCR5 contextCR5 = new ContextCR5(owlObjectOneOf);
                ContextCR6 contextCR6 = new ContextCR6(individual);

                CR1CR2CR3Contexts.put(owlObjectOneOf, Set.of(contextCR1, contextCR2, contextCR3));
                CR4Contexts.put(owlObjectOneOf, contextCR4);
                CR6Contexts.put(individual, contextCR6);

                yield Set.of(contextCR1, contextCR2, contextCR3, contextCR4, contextCR5, contextCR6);
            }

            default -> Collections.emptySet();
        };
    }

    @Override
    public Collection<Context> getContexts(OWLSubClassOfAxiom subClassOfAxiom) {
        Set<Context> localContexts = new HashSet<>();

        OWLClassExpression subClass = subClassOfAxiom.getSubClass();
        OWLClassExpression superClass = subClassOfAxiom.getSuperClass();
        if (!isSubclassBCConcept(subClass) || (!isSuperclassBCConcept(superClass) && !(superClass instanceof OWLObjectSomeValuesFrom))) {
            return Collections.emptyList();
        }

        if (isSubclassBCConcept(subClass) && isSuperclassBCConcept(superClass)) {
            Optional
                .ofNullable(CR1CR2CR3Contexts.get(subClass))
                .ifPresent(localContexts::addAll);

            Optional
                .ofNullable(CR4Contexts.get(subClass))
                .ifPresent(localContexts::add);


            if (superClass.isOWLNothing()) {
                Optional
                    .ofNullable(CR5Contexts.get(subClass))
                    .ifPresent(localContexts::add);
            }

            Collection<Context> values = CR6Contexts.values();
            localContexts.addAll(values);
        }


        if (isSubclassBCConcept(subClass) && superClass instanceof OWLObjectSomeValuesFrom someValuesFrom) {
            OWLClassExpression filler = someValuesFrom.getFiller();
            Optional
                .ofNullable(CR4Contexts.get(filler))
                .ifPresent(localContexts::add);

            Optional
                .ofNullable(CR5Contexts.get(filler))
                .ifPresent(localContexts::add);

            Collection<Context> values = CR6Contexts.values();
            localContexts.addAll(values);
        }

        return localContexts;
    }
}
