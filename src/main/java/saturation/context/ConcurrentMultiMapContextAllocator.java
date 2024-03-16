package saturation.context;

import com.google.common.collect.ImmutableSet;
import index.OntologyIndex;
import org.semanticweb.owlapi.model.*;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import static normalisation.NormalisationUtils.isSubclassBCConcept;
import static normalisation.NormalisationUtils.isSuperclassBCConcept;

public class ConcurrentMultiMapContextAllocator implements ContextAllocator {

    private final OntologyIndex ontologyIndex;
    private final OWLOntology ontology;
    private final ConcurrentMap<OWLClassExpression, Set<Context>> simpleRuleContextsBySubclass = new ConcurrentHashMap<>();
    private final ConcurrentMap<OWLClassExpression, Set<Context>> CR4Contexts = new ConcurrentHashMap<>();
    private final ConcurrentMap<OWLClassExpression, Set<Context>> CR5Contexts = new ConcurrentHashMap<>();
    private final ConcurrentMap<OWLIndividual, Context> individualContextByIndividual = new ConcurrentHashMap<>();

    public ConcurrentMultiMapContextAllocator(OWLOntology ontology, OntologyIndex ontologyIndex) {
        this.ontology = ontology;
        this.ontologyIndex = ontologyIndex;
    }

    @Override
    public Collection<Context> initContexts() {
        clearAll();

        Set<Context> individualContexts = initIndividualContexts(ontology);
        Set<Context> subclassOfAxiomContexts = initSubclassOfAxiomsContexts(ontology);

        subclassOfAxiomContexts.addAll(individualContexts);

        return subclassOfAxiomContexts;
    }

    private Set<Context> initIndividualContexts(OWLOntology ontology) {
        Set<Context> contexts = new HashSet<>();

        Set<OWLSubClassOfAxiom> axioms = ontology.getAxioms(AxiomType.SUBCLASS_OF);
        for (OWLSubClassOfAxiom axiom: axioms) {
            OWLClassExpression superClass = axiom.getSuperClass();

            if (!(superClass instanceof OWLIndividual individual)) continue;
            Context individualContext = getContextForIndividual(individual);
            contexts.add(individualContext);
        }

        return contexts;
    }

    private Context getContextForIndividual(OWLIndividual individual) {
        return individualContextByIndividual.computeIfAbsent(individual, this::buildIndividualContext);
    }

    private Context buildIndividualContext(OWLIndividual individual) {
        return new ContextCR6(individual);
    }


    private Set<Context> initSubclassOfAxiomsContexts(OWLOntology ontology) {
        Set<Context> contexts = new HashSet<>();

        for (OWLSubClassOfAxiom axiom: ontology.getAxioms(AxiomType.SUBCLASS_OF)) {
            OWLClassExpression subClass = axiom.getSubClass();
            OWLClassExpression superClass = axiom.getSuperClass();

            if (!isSubclassBCConcept(subClass)) {
                continue;
            }

            if (isSuperclassBCConcept(superClass)) {
                Set<Context> genericSubclassOfAxiomContexts = getContextsForGenericSubclassOfAxiom(subClass);
                contexts.addAll(genericSubclassOfAxiomContexts);

                if (superClass.isOWLNothing()) {
                    Set<Context> contextsForSubclassOfAxiomRightNothing = getContextsForSubclassOfAxiomRightNothing(subClass);
                    contexts.addAll(contextsForSubclassOfAxiomRightNothing);
                }
            }

            if (superClass instanceof OWLObjectSomeValuesFrom objectSomeValuesFrom) {
                OWLClassExpression filler = objectSomeValuesFrom.getFiller();
                Set<Context> contextsForSubclassOfAxiomRightExistential = getContextsForSubclassOfAxiomRightExistential(filler);
                contexts.addAll(contextsForSubclassOfAxiomRightExistential);
            }
        }

        return contexts;
    }

    private Set<Context> getContextsForGenericSubclassOfAxiom(OWLClassExpression subclass) {
        Set<Context> contexts = new HashSet<>();

        Set<Context> simpleRuleContexts = simpleRuleContextsBySubclass.computeIfAbsent(subclass, this::buildSimpleContexts);
        Set<Context> doublePremisesSimpleExistentialContexts = CR4Contexts.computeIfAbsent(subclass, this::buildCR4Contexts);
        Collection<Context> individualContexts = individualContextByIndividual.values();

        contexts.addAll(simpleRuleContexts);
        contexts.addAll(doublePremisesSimpleExistentialContexts);
        contexts.addAll(individualContexts);

        return contexts;
    }

    private Set<Context> buildSimpleContexts(OWLClassExpression conceptName) {
        return ImmutableSet.of(
            new ContextCR1(conceptName, ontologyIndex.getToldSups()),
            new ContextCR2(conceptName, ontologyIndex.getSuperclassesByIntersectionOperands()),
            new ContextCR3(conceptName, ontologyIndex.getExistentialRightSetBySubclass())
        );
    }

    private Set<Context> getContextsForSubclassOfAxiomRightExistential(OWLClassExpression filler) {
        Set<Context> contexts = new HashSet<>();

        Set<Context> doublePremisesSimpleExistentialContexts = CR4Contexts.computeIfAbsent(filler, this::buildCR4Contexts);
        Set<Context> doublePremisesSimpleNothingContexts = CR5Contexts.computeIfAbsent(filler, this::buildCR5Contexts);
        Collection<Context> individualContexts = individualContextByIndividual.values();

        contexts.addAll(doublePremisesSimpleExistentialContexts);
        contexts.addAll(doublePremisesSimpleNothingContexts);
        contexts.addAll(individualContexts);

        return contexts;
    }

    private Set<Context> buildCR4Contexts(OWLClassExpression classExpression) {
        return ImmutableSet.of(new ContextCR4(classExpression, ontologyIndex.getGciLeftExistentialIndex()));
    }

    private Set<Context> getContextsForSubclassOfAxiomRightNothing(OWLClassExpression subClass) {
        return CR5Contexts.computeIfAbsent(subClass, this::buildCR5Contexts);
    }

    private Set<Context> buildCR5Contexts(OWLClassExpression classExpression) {
        return ImmutableSet.of(new ContextCR5(classExpression));
    }

    @Override
    public Collection<Context> getContexts(OWLSubClassOfAxiom axiom) {
        Set<Context> contexts = new HashSet<>();

        OWLClassExpression subClass = axiom.getSubClass();
        OWLClassExpression superClass = axiom.getSuperClass();


        if (!isSubclassBCConcept(subClass)) return Collections.emptyList();

        if (isSuperclassBCConcept(superClass)) {
            Set<Context> genericSubclassOfAxiomContexts = getContextsForGenericSubclassOfAxiom(subClass);
            contexts.addAll(genericSubclassOfAxiomContexts);

            if (superClass.isOWLNothing()) {
                Set<Context> contextsForSubclassOfAxiomRightNothing = getContextsForSubclassOfAxiomRightNothing(subClass);
                contexts.addAll(contextsForSubclassOfAxiomRightNothing);
            }

            if (superClass instanceof OWLIndividual individual) {
                Context contextForIndividual = getContextForIndividual(individual);
                contexts.add(contextForIndividual);
            }
        }

        if (superClass instanceof OWLObjectSomeValuesFrom objectSomeValuesFrom) {
            OWLClassExpression filler = objectSomeValuesFrom.getFiller();
            Set<Context> contextsForSubclassOfAxiomRightExistential = getContextsForSubclassOfAxiomRightExistential(filler);
            contexts.addAll(contextsForSubclassOfAxiomRightExistential);
        }

        return contexts;
    }

    private void clearAll() {
        simpleRuleContextsBySubclass.clear();
        CR4Contexts.clear();
        CR5Contexts.clear();
        individualContextByIndividual.clear();
    }
}
