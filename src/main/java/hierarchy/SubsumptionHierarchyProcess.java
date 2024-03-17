package hierarchy;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.reasoner.impl.OWLClassNode;
import org.semanticweb.owlapi.reasoner.impl.OWLClassNodeSet;
import saturation.SaturationResult;

import java.util.*;

public class SubsumptionHierarchyProcess {
    private static final OWLClass thing = OWLManager.getOWLDataFactory().getOWLThing();
    private static final OWLClass nothing = OWLManager.getOWLDataFactory().getOWLNothing();
    private final Map<OWLClassExpression, Set<OWLClassExpression>> superclassesMap = new HashMap<>();
    private final Map<OWLClassExpression, Set<OWLClassExpression>> equivalentClassesMap = new HashMap<>();
    private final Map<OWLClassExpression, Set<OWLClassExpression>> directSuperclassesMap = new HashMap<>();

    public SubsumptionHierarchy buildHierarchy(Set<OWLSubClassOfAxiom> allAxioms) {
        buildSuperConceptsMap(allAxioms);
        performTransitiveReduction();
        return buildTaxonomy();
    }

    public SubsumptionHierarchy buildHierarchy(SaturationResult saturationResult) {
        Set<OWLSubClassOfAxiom> allAxioms = saturationResult.getAllAxioms();
        return buildHierarchy(allAxioms);
    }

    private void buildSuperConceptsMap(Set<OWLSubClassOfAxiom> axioms) {
        for (OWLSubClassOfAxiom axiom: axioms) {
            OWLClassExpression subClass = axiom.getSubClass();
            OWLClassExpression superClass = axiom.getSuperClass();

            if (subClass instanceof OWLObjectSomeValuesFrom someValuesFrom) {
                OWLClassExpression filler = someValuesFrom.getFiller();
                superclassesMap.computeIfAbsent(filler, __ -> new HashSet<>() {{ add(thing); }});
            }

            if (superClass instanceof OWLObjectSomeValuesFrom someValuesFrom) {
                OWLClassExpression filler = someValuesFrom.getFiller();
                superclassesMap.computeIfAbsent(filler, __ -> new HashSet<>() {{ add(thing); }});
            }

            if ((subClass  .isClassExpressionLiteral() || subClass   instanceof OWLObjectOneOf) &&
                (superClass.isClassExpressionLiteral() || superClass instanceof OWLObjectOneOf)) {

                superclassesMap
                    .computeIfAbsent(subClass, __ -> new HashSet<>())
                    .add(superClass);

                superclassesMap.computeIfAbsent(superClass, __ -> new HashSet<>() {{ add(thing); }});

            }
        }
    }

    private void performTransitiveReduction() {
        for (OWLClassExpression conceptA: superclassesMap.keySet()) {
            Set<OWLClassExpression> superConceptsA = superclassesMap.get(conceptA);
            for (OWLClassExpression conceptC: superConceptsA) {
                Set<OWLClassExpression> superConceptsC = superclassesMap.get(conceptC);
                if (superConceptsC.contains(conceptA)) {
                    equivalentClassesMap
                        .computeIfAbsent(conceptA, __ -> new HashSet<>())
                        .add(conceptC);
                } else {
                    boolean isDirect = true;
                    Set<OWLClassExpression> directSuperConceptsA = directSuperclassesMap.computeIfAbsent(conceptA, __ -> new HashSet<>());
                    Iterator<OWLClassExpression> directSuperConceptsAIterator = directSuperConceptsA.iterator();

                    while (directSuperConceptsAIterator.hasNext()) {
                        OWLClassExpression conceptB = directSuperConceptsAIterator.next();

                        Set<OWLClassExpression> superConceptsB = superclassesMap.computeIfAbsent(conceptB, __ -> new HashSet<>());
                        if (superConceptsB.contains(conceptC)) {
                            isDirect = false;
                            break;
                        }

                        if (superConceptsC.contains(conceptB)) {
                            directSuperConceptsAIterator.remove();
                        }
                    }

                    if (isDirect) {
                        directSuperclassesMap
                            .computeIfAbsent(conceptA, __ -> new HashSet<>())
                            .add(conceptC);
                    }
                }
            }
        }
    }

    private SubsumptionHierarchy buildTaxonomy() {

        Map<OWLClass, OWLClassNode>        nodeByClass                   = new HashMap<>(superclassesMap.size());
        Map<OWLClassNode, OWLClassNodeSet> directSuperclassesByClassNode = new HashMap<>(superclassesMap.size());
        Map<OWLClassNode, OWLClassNodeSet> directSubclassesByClassNode   = new HashMap<>(superclassesMap.size());
        Map<OWLClassNode, OWLClassNodeSet> subclassesByClassNode         = new HashMap<>(superclassesMap.size());
        Map<OWLClassNode, OWLClassNodeSet> superclassesByClassNode       = new HashMap<>(superclassesMap.size());

        for (OWLClassExpression equivalentClassRepresentative: equivalentClassesMap.keySet()) {
            if (equivalentClassRepresentative instanceof OWLClass owlEquivalentClassRepresentative) {
                if (nodeByClass.containsKey(owlEquivalentClassRepresentative)) {
                    continue;
                }

                OWLClassNode classes = new OWLClassNode();
                Set<OWLClassExpression> equivalentClasses = equivalentClassesMap.get(equivalentClassRepresentative);
                for (OWLClassExpression equivalentClass : equivalentClasses) {
                    if (equivalentClass instanceof OWLClass owlClass) {
                        classes.add(owlClass);
                        nodeByClass.put(owlClass, classes);
                    }
                }
            }
        }

        OWLClassNode owlThingClassNode = new OWLClassNode(thing);
        OWLClassNode owlNothingClassNode = new OWLClassNode(nothing);
        nodeByClass.put(thing, owlThingClassNode);
        nodeByClass.put(nothing, owlNothingClassNode);

        for (OWLClassExpression conceptKey: directSuperclassesMap.keySet()) {
            if (conceptKey instanceof OWLClass owlClassKey) {
                OWLClassNode owlClassNodeKey = nodeByClass.computeIfAbsent(owlClassKey, __ -> new OWLClassNode(owlClassKey));

                if (directSuperclassesByClassNode.containsKey(owlClassNodeKey)) {
                    continue;
                }

                OWLClassNodeSet owlDirectSuperClassesNodeSet = new OWLClassNodeSet();
                for (OWLClassExpression directSuperConcept: directSuperclassesMap.get(conceptKey)) {
                    if (directSuperConcept instanceof OWLClass owlSuperClass) {
                        OWLClassNode owlDirectSuperClassNode = nodeByClass.computeIfAbsent(owlSuperClass, __ -> new OWLClassNode(owlSuperClass));
                        owlDirectSuperClassesNodeSet.addNode(owlDirectSuperClassNode);

                        directSubclassesByClassNode
                            .computeIfAbsent(owlDirectSuperClassNode, __ -> new OWLClassNodeSet())
                            .addNode(owlClassNodeKey);
                    }
                }

                directSuperclassesByClassNode.put(owlClassNodeKey, owlDirectSuperClassesNodeSet);
            }
        }

        for (OWLClassExpression conceptKey: superclassesMap.keySet()) {
            if (conceptKey instanceof OWLClass owlClassKey) {
                OWLClassNode owlClassNodeKey = nodeByClass.computeIfAbsent(owlClassKey, __ -> new OWLClassNode(owlClassKey));
                directSuperclassesByClassNode.computeIfAbsent(owlClassNodeKey, __ -> new OWLClassNodeSet(owlThingClassNode));
                directSubclassesByClassNode.computeIfAbsent(owlClassNodeKey, __ -> new OWLClassNodeSet(owlNothingClassNode));
                subclassesByClassNode.computeIfAbsent(owlClassNodeKey, __ -> new OWLClassNodeSet(owlNothingClassNode));

                if (superclassesByClassNode.containsKey(owlClassNodeKey)) {
                    continue;
                }

                OWLClassNodeSet owlSuperClassesNodeSet = new OWLClassNodeSet(owlThingClassNode);
                for (OWLClassExpression superConcept: superclassesMap.get(conceptKey)) {
                    if (superConcept instanceof OWLClass owlSuperClass) {
                        OWLClassNode owlSuperClassNode = nodeByClass.computeIfAbsent(owlSuperClass, __ -> new OWLClassNode(owlSuperClass));

                        if (Objects.equals(owlSuperClassNode, owlClassNodeKey)) {
                            continue;
                        }

                        owlSuperClassesNodeSet.addNode(owlSuperClassNode);

                        subclassesByClassNode
                            .computeIfAbsent(owlSuperClassNode, __ -> new OWLClassNodeSet(owlNothingClassNode))
                            .addNode(owlClassNodeKey);
                    }
                }

                superclassesByClassNode.put(owlClassNodeKey, owlSuperClassesNodeSet);
            }
        }

        return new SubsumptionHierarchyBuilder()
            .nodeByClass(nodeByClass)
            .equivalentClassesByClass(equivalentClassesMap)
            .directSubclasses(directSubclassesByClassNode)
            .directSuperclasses(directSuperclassesByClassNode)
            .subclasses(subclassesByClassNode)
            .superclasses(superclassesByClassNode)
            .build();
    }
}
