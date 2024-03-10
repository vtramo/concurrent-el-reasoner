package hierarchy;

import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.reasoner.impl.OWLClassNode;
import org.semanticweb.owlapi.reasoner.impl.OWLClassNodeSet;

import java.util.Map;
import java.util.Set;

public class HierarchySubsumption {

    private final Map<OWLClassExpression, Set<OWLClassExpression>> equivalentClassesByClass;
    private final Map<OWLClass, OWLClassNode> nodeByClass;
    private final Map<OWLClassNode, OWLClassNodeSet> directSuperclasses;
    private final Map<OWLClassNode, OWLClassNodeSet> directSubclasses;
    private final Map<OWLClassNode, OWLClassNodeSet> subclasses;
    private final Map<OWLClassNode, OWLClassNodeSet> superclasses;


    public HierarchySubsumption(
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


}
