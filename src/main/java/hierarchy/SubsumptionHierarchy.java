package hierarchy;

import normalisation.NormalisationUtils;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.reasoner.Node;
import org.semanticweb.owlapi.reasoner.impl.OWLClassNode;
import org.semanticweb.owlapi.reasoner.impl.OWLClassNodeSet;

import java.util.Map;
import java.util.Set;

public class SubsumptionHierarchy {

    private static final OWLDataFactory owlDataFactory = OWLManager.getOWLDataFactory();
    private final Map<OWLClassExpression, Set<OWLClassExpression>> equivalentClassesByClass;
    private final Map<OWLClass, OWLClassNode> nodeByClass;
    private final Map<OWLClassNode, OWLClassNodeSet> directSuperclasses;
    private final Map<OWLClassNode, OWLClassNodeSet> directSubclasses;
    private final Map<OWLClassNode, OWLClassNodeSet> subclasses;
    private final Map<OWLClassNode, OWLClassNodeSet> superclasses;


    public SubsumptionHierarchy(
        Map<OWLClassExpression, Set<OWLClassExpression>> equivalentClassesByClass,
        Map<OWLClass, OWLClassNode> nodeByClass,
        Map<OWLClassNode, OWLClassNodeSet> directSuperclasses,
        Map<OWLClassNode, OWLClassNodeSet> directSubclasses,
        Map<OWLClassNode, OWLClassNodeSet> subclasses,
        Map<OWLClassNode, OWLClassNodeSet> superclasses
    ) {
        this.equivalentClassesByClass = equivalentClassesByClass;
        this.nodeByClass = nodeByClass;
        this.directSuperclasses = directSuperclasses;
        this.directSubclasses = directSubclasses;
        this.subclasses = subclasses;
        this.superclasses = superclasses;
    }

    public OWLClassNodeSet getSubClasses(OWLClass owlClass) {
        if (!nodeByClass.containsKey(owlClass)) {
            return owlClass.isBottomEntity()
                ? new OWLClassNodeSet()
                : new OWLClassNodeSet(getEquivalentClasses(owlDataFactory.getOWLNothing()));
        }

        OWLClassNode owlClassNode = nodeByClass.get(owlClass);
        return subclasses.get(owlClassNode);
    }
    
    public OWLClassNodeSet getOriginalSubClasses(OWLClass owlClass) {
        if (NormalisationUtils.isGenerated(owlClass)) throw new IllegalArgumentException();
        OWLClassNodeSet classNodeSet = getSubClasses(owlClass);
        return filterGeneratedClasses(classNodeSet);
    }

    public OWLClassNodeSet getSuperClasses(OWLClass owlClass) {
        if (!nodeByClass.containsKey(owlClass)) {
            return owlClass.isTopEntity()
                ? new OWLClassNodeSet()
                : new OWLClassNodeSet(getEquivalentClasses(owlDataFactory.getOWLThing()));
        }

        OWLClassNode owlClassNode = nodeByClass.get(owlClass);
        return superclasses.get(owlClassNode);
    }

    public OWLClassNodeSet getOriginalSuperClasses(OWLClass owlClass) {
        if (NormalisationUtils.isGenerated(owlClass)) throw new IllegalArgumentException();
        OWLClassNodeSet classNodeSet = getSuperClasses(owlClass);
        return filterGeneratedClasses(classNodeSet);
    }

    public OWLClassNodeSet getDirectSubClasses(OWLClass owlClass) {
        if (!nodeByClass.containsKey(owlClass)) {
            return owlClass.isOWLNothing()
                ? new OWLClassNodeSet()
                : new OWLClassNodeSet(getEquivalentClasses(owlDataFactory.getOWLNothing()));
        }

        OWLClassNode owlClassNode = nodeByClass.get(owlClass);
        return directSubclasses.get(owlClassNode);
    }

    public OWLClassNodeSet getOriginalDirectSuperClasses(OWLClass owlClass) {
        if (NormalisationUtils.isGenerated(owlClass)) throw new IllegalArgumentException();
        OWLClassNodeSet classNodeSet = getDirectSuperClasses(owlClass);
        OWLClassNodeSet filteredClassNodeSet = filterGeneratedClasses(classNodeSet);

        if (filteredClassNodeSet.isEmpty()) {
            filteredClassNodeSet.addNode(new OWLClassNode(owlDataFactory.getOWLThing()));
        }

        return filteredClassNodeSet;
    }

    public OWLClassNodeSet getDirectSuperClasses(OWLClass owlClass) {
        if (!nodeByClass.containsKey(owlClass)) {
            return owlClass.isTopEntity()
                ? new OWLClassNodeSet()
                : new OWLClassNodeSet(getEquivalentClasses(owlDataFactory.getOWLThing()));
        }

        OWLClassNode owlClassNode = nodeByClass.get(owlClass);
        return directSuperclasses.get(owlClassNode);
    }

    public OWLClassNodeSet getOriginalDirectSubClasses(OWLClass owlClass) {
        if (NormalisationUtils.isGenerated(owlClass)) throw new IllegalArgumentException();
        OWLClassNodeSet classNodeSet = getDirectSubClasses(owlClass);
        OWLClassNodeSet filteredClassNodeSet = filterGeneratedClasses(classNodeSet);

        if (filteredClassNodeSet.isEmpty()) {
            filteredClassNodeSet.addNode(new OWLClassNode(owlDataFactory.getOWLNothing()));
        }

        return filteredClassNodeSet;
    }

    public OWLClassNode getEquivalentClasses(OWLClass owlClass) {
        if (!nodeByClass.containsKey(owlClass)) {
            return new OWLClassNode(owlClass);
        }

        return nodeByClass.get(owlClass);
    }

    public OWLClassNode getOriginalEquivalentClasses(OWLClass owlClass) {
        if (!nodeByClass.containsKey(owlClass)) {
            return new OWLClassNode(owlClass);
        }

        OWLClassNode owlClasses = nodeByClass.get(owlClass);
        return filterGeneratedClasses(owlClasses);
    }

    private static OWLClassNodeSet filterGeneratedClasses(OWLClassNodeSet classNodeSet) {
        OWLClassNodeSet filteredClassNodeSet = new OWLClassNodeSet();

        for (Node<OWLClass> node: classNodeSet) {
            OWLClassNode filteredNode = filterGeneratedClasses(node);
            if (filteredNode.getSize() != 0) {
                filteredClassNodeSet.addNode(filteredNode);
            }
        }

        return filteredClassNodeSet;
    }

    private static OWLClassNode filterGeneratedClasses(Node<OWLClass> node) {
        OWLClassNode filteredNode = new OWLClassNode();

        node.forEach(nodeClass -> {
            if (NormalisationUtils.isGenerated(nodeClass)) return;
            filteredNode.add(nodeClass);
        });

        return filteredNode;
    }

}
