package indexing;

import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLObjectIntersectionOf;

import java.util.*;

public class OntologyIndex {
    private final Map<OWLClassExpression, Set<OWLClassExpression>> toldSups = new HashMap<>();
    private final Map<OWLClassExpression, Map<OWLClassExpression, Set<OWLClassExpression>>> superclassesByIntersectionOperands = new HashMap<>();
    private final Map<OWLClassExpression, Set<RoleAndFiller>> existentialRightSetBySubclass = new HashMap<>();
    private final LeftExistentialOntologyIndex leftExistentialOntologyIndex = new LeftExistentialOntologyIndex();

    public boolean putInToldSups(OWLClassExpression subclass, OWLClassExpression superclass) {
        return toldSups
            .computeIfAbsent(subclass, __ -> new HashSet<>())
            .add(superclass);
    }

    public boolean putInSuperclassesByIntersectionOperandsMap(OWLObjectIntersectionOf intersection, OWLClassExpression superclass) {
        List<OWLClassExpression> operands = intersection.getOperandsAsList();

        OWLClassExpression left = operands.getFirst();
        OWLClassExpression right = operands.getLast();

        boolean leftAdded = superclassesByIntersectionOperands
            .computeIfAbsent(left, __ -> new HashMap<>())
            .computeIfAbsent(right, __ -> new HashSet<>())
            .add(superclass);

        boolean rightAdded = superclassesByIntersectionOperands
            .computeIfAbsent(right, __ -> new HashMap<>())
            .computeIfAbsent(left, __ -> new HashSet<>())
            .add(superclass);

        return leftAdded || rightAdded;
    }

    public boolean putInExistentialRightSetBySubclass(OWLClassExpression subclass, RoleAndFiller rightRoleAndFiller) {
        return existentialRightSetBySubclass
            .computeIfAbsent(subclass, __ -> new HashSet<>())
            .add(rightRoleAndFiller);
    }

    public boolean putInLeftExistentialOntologyIndex(RoleAndFiller roleAndFiller, OWLClassExpression superclass) {
        return leftExistentialOntologyIndex.put(roleAndFiller, superclass);
    }

    public Map<OWLClassExpression, Set<OWLClassExpression>> getToldSups() {
        return Collections.unmodifiableMap(toldSups);
    }

    public Map<OWLClassExpression, Map<OWLClassExpression, Set<OWLClassExpression>>> getSuperclassesByIntersectionOperands() {
        return Collections.unmodifiableMap(superclassesByIntersectionOperands);
    }

    public Map<OWLClassExpression, Set<RoleAndFiller>> getExistentialRightSetBySubclass() {
        return Collections.unmodifiableMap(existentialRightSetBySubclass);
    }

    public LeftExistentialOntologyIndex getGciLeftExistentialIndex() {
        return leftExistentialOntologyIndex;
    }
}