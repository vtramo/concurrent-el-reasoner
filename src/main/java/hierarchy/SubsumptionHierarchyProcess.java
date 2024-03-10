package hierarchy;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.reasoner.impl.OWLClassNode;
import org.semanticweb.owlapi.reasoner.impl.OWLClassNodeSet;
import saturation.SaturationResult;

import java.util.*;

public class SubsumptionHierarchyProcess {
    private final Map<OWLClassExpression, Set<OWLClassExpression>> superConceptsMap = new HashMap<>();
    private final Map<OWLClassExpression, Set<OWLClassExpression>> equivalentConceptsMap = new HashMap<>();
    private final Map<OWLClassExpression, Set<OWLClassExpression>> directSuperConceptsMap = new HashMap<>();

    public SubsumptionHierarchy buildHierarchy(SaturationResult saturationResult) {
        Set<OWLSubClassOfAxiom> allAxioms = saturationResult.getAllAxioms();
        buildSuperConceptsMap(allAxioms);
        performTransitiveReduction();
        return buildTaxonomy();
    }

    private void buildSuperConceptsMap(Set<OWLSubClassOfAxiom> axioms) {
        for (OWLSubClassOfAxiom axiom: axioms) {
            OWLClassExpression subClass = axiom.getSubClass();
            OWLClassExpression superClass = axiom.getSuperClass();

            if (subClass instanceof OWLObjectSomeValuesFrom someValuesFrom) {
                OWLClassExpression filler = someValuesFrom.getFiller();
                superConceptsMap.computeIfAbsent(filler, __ -> new HashSet<>());
            }

            if (superClass instanceof OWLObjectSomeValuesFrom someValuesFrom) {
                OWLClassExpression filler = someValuesFrom.getFiller();
                superConceptsMap.computeIfAbsent(filler, __ -> new HashSet<>());
            }

            if ((subClass  .isClassExpressionLiteral() || subClass   instanceof OWLObjectOneOf) &&
                (superClass.isClassExpressionLiteral() || superClass instanceof OWLObjectOneOf)) {

                superConceptsMap
                    .computeIfAbsent(subClass, __ -> new HashSet<>())
                    .add(superClass);

                superConceptsMap.computeIfAbsent(superClass, __ -> new HashSet<>());

            }
        }
    }

    private void performTransitiveReduction() {
        for (OWLClassExpression conceptA: superConceptsMap.keySet()) {
            Set<OWLClassExpression> superConceptsA = this.superConceptsMap.get(conceptA);
            for (OWLClassExpression conceptC: superConceptsA) {
                Set<OWLClassExpression> superConceptsC = superConceptsMap.get(conceptC);
                if (superConceptsC.contains(conceptA)) {
                    equivalentConceptsMap
                        .computeIfAbsent(conceptA, __ -> new HashSet<>())
                        .add(conceptC);
                } else {
                    boolean isDirect = true;
                    Set<OWLClassExpression> directSuperConceptsA = directSuperConceptsMap.getOrDefault(conceptA, new HashSet<>());
                    Iterator<OWLClassExpression> directSuperConceptsAIterator = directSuperConceptsA.iterator();

                    while (directSuperConceptsAIterator.hasNext()) {
                        OWLClassExpression conceptB = directSuperConceptsAIterator.next();

                        Set<OWLClassExpression> directSuperConceptsB = directSuperConceptsMap.getOrDefault(conceptB, new HashSet<>());
                        if (directSuperConceptsB.contains(conceptC)) {
                            isDirect = false;
                            break;
                        }

                        if (superConceptsC.contains(conceptB)) {
                            directSuperConceptsAIterator.remove();
                        }
                    }

                    if (isDirect) {
                        directSuperConceptsMap
                            .computeIfAbsent(conceptA, __ -> new HashSet<>())
                            .add(conceptC);
                    }
                }
            }
        }
    }

    private SubsumptionHierarchy buildTaxonomy() {

        Map<OWLClass, OWLClassNode> nodeByClass = new HashMap<>(superConceptsMap.size());
        Map<OWLClassNode, OWLClassNodeSet> directSuperclasses = new HashMap<>(superConceptsMap.size());
        Map<OWLClassNode, OWLClassNodeSet> directSubclasses = new HashMap<>(superConceptsMap.size());
        Map<OWLClassNode, OWLClassNodeSet> subclasses = new HashMap<>(superConceptsMap.size());
        Map<OWLClassNode, OWLClassNodeSet> superclasses = new HashMap<>(superConceptsMap.size());

        for (OWLClassExpression equivalentClassRepresentative: equivalentConceptsMap.keySet()) {
            if (equivalentClassRepresentative instanceof OWLClass owlEquivalentClassRepresentative) {
                if (nodeByClass.containsKey(owlEquivalentClassRepresentative)) {
                    continue;
                }

                OWLClassNode classes = new OWLClassNode();
                Set<OWLClassExpression> equivalentClasses = equivalentConceptsMap.get(equivalentClassRepresentative);
                for (OWLClassExpression equivalentClass : equivalentClasses) {
                    if (equivalentClass instanceof OWLClass owlClass) {
                        classes.add(owlClass);
                        nodeByClass.put(owlClass, classes);
                    }
                }
            }
        }

        OWLDataFactory owlDataFactory = OWLManager.getOWLDataFactory();
        OWLClass owlThing = owlDataFactory.getOWLThing();
        OWLClass owlNothing = owlDataFactory.getOWLNothing();
        OWLClassNode owlThingClassNode = new OWLClassNode(owlThing);
        OWLClassNode owlNothingClassNode = new OWLClassNode(owlNothing);
        nodeByClass.put(owlThing, owlThingClassNode);
        nodeByClass.put(owlNothing, owlNothingClassNode);

        for (OWLClassExpression conceptKey: directSuperConceptsMap.keySet()) {
            if (conceptKey instanceof OWLClass owlClassKey) {
                OWLClassNode owlClassNodeKey = nodeByClass.computeIfAbsent(owlClassKey, __ -> new OWLClassNode(owlClassKey));

                if (directSuperclasses.containsKey(owlClassNodeKey)) {
                    continue;
                }

                OWLClassNodeSet owlDirectSuperClassesNodeSet = new OWLClassNodeSet();
                for (OWLClassExpression directSuperConcept: directSuperConceptsMap.get(conceptKey)) {
                    if (directSuperConcept instanceof OWLClass owlSuperClass) {
                        OWLClassNode owlDirectSuperClassNode = nodeByClass.computeIfAbsent(owlSuperClass, __ -> new OWLClassNode(owlSuperClass));
                        owlDirectSuperClassesNodeSet.addNode(owlDirectSuperClassNode);

                        directSubclasses
                            .computeIfAbsent(owlDirectSuperClassNode, __ -> new OWLClassNodeSet())
                            .addNode(owlClassNodeKey);
                    }
                }

                directSuperclasses.put(owlClassNodeKey, owlDirectSuperClassesNodeSet);
            }
        }

        for (OWLClassExpression conceptKey: superConceptsMap.keySet()) {
            if (conceptKey instanceof OWLClass owlClassKey) {
                OWLClassNode owlClassNodeKey = nodeByClass.computeIfAbsent(owlClassKey, __ -> new OWLClassNode(owlClassKey));
                directSuperclasses.computeIfAbsent(owlClassNodeKey, __ -> new OWLClassNodeSet(owlThingClassNode));
                directSubclasses.computeIfAbsent(owlClassNodeKey, __ -> new OWLClassNodeSet(owlNothingClassNode));
                subclasses.computeIfAbsent(owlClassNodeKey, __ -> new OWLClassNodeSet(owlNothingClassNode));

                if (superclasses.containsKey(owlClassNodeKey)) {
                    continue;
                }

                OWLClassNodeSet owlSuperClassesNodeSet = new OWLClassNodeSet(owlThingClassNode);
                for (OWLClassExpression superConcept: superConceptsMap.get(conceptKey)) {
                    if (superConcept instanceof OWLClass owlSuperClass) {
                        OWLClassNode owlSuperClassNode = nodeByClass.computeIfAbsent(owlSuperClass, __ -> new OWLClassNode(owlSuperClass));

                        if (Objects.equals(owlSuperClassNode, owlClassNodeKey)) {
                            continue;
                        }

                        owlSuperClassesNodeSet.addNode(owlSuperClassNode);

                        subclasses
                            .computeIfAbsent(owlSuperClassNode, __ -> new OWLClassNodeSet(owlNothingClassNode))
                            .addNode(owlClassNodeKey);
                    }
                }

                superclasses.put(owlClassNodeKey, owlSuperClassesNodeSet);
            }
        }

        return new SubsumptionHierarchyBuilder()
            .nodeByClass(nodeByClass)
            .equivalentClassesByClass(equivalentConceptsMap)
            .directSubclasses(directSubclasses)
            .directSuperclasses(directSuperclasses)
            .subclasses(subclasses)
            .superclasses(superclasses)
            .build();
    }
}
