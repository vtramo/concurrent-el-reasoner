package hierarchy;

import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.reasoner.impl.OWLClassNode;
import org.semanticweb.owlapi.reasoner.impl.OWLClassNodeSet;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class SubsumptionHierarchyBuilder {
    private Map<OWLClassExpression, Set<OWLClassExpression>> equivalentClassesByClass = new HashMap<>();
    private Map<OWLClass, OWLClassNode> nodeByClass = new HashMap<>();
    private Map<OWLClassNode, OWLClassNodeSet> directSuperclasses = new HashMap<>();
    private Map<OWLClassNode, OWLClassNodeSet> directSubclasses = new HashMap<>();
    private Map<OWLClassNode, OWLClassNodeSet> subclasses = new HashMap<>();
    private Map<OWLClassNode, OWLClassNodeSet> superclasses = new HashMap<>();

    public SubsumptionHierarchyBuilder equivalentClassesByClass(Map<OWLClassExpression, Set<OWLClassExpression>> equivalentClassesByClass) {
        this.equivalentClassesByClass = equivalentClassesByClass;
        return this;
    }

    public SubsumptionHierarchyBuilder nodeByClass(Map<OWLClass, OWLClassNode> nodeByClass) {
        this.nodeByClass = nodeByClass;
        return this;
    }

    public SubsumptionHierarchyBuilder directSuperclasses(Map<OWLClassNode, OWLClassNodeSet> directSuperclasses) {
        this.directSuperclasses = directSuperclasses;
        return this;
    }

    public SubsumptionHierarchyBuilder directSubclasses(Map<OWLClassNode, OWLClassNodeSet> directSubclasses) {
        this.directSubclasses = directSubclasses;
        return this;
    }

    public SubsumptionHierarchyBuilder subclasses(Map<OWLClassNode, OWLClassNodeSet> subclasses) {
        this.subclasses = subclasses;
        return this;
    }

    public SubsumptionHierarchyBuilder superclasses(Map<OWLClassNode, OWLClassNodeSet> superclasses) {
        this.superclasses = superclasses;
        return this;
    }

    public SubsumptionHierarchy build() {
        return new SubsumptionHierarchy(equivalentClassesByClass, nodeByClass, directSuperclasses, directSubclasses, subclasses, superclasses);
    }
}