package indexing;

import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLObjectPropertyExpression;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class LeftExistentialOntologyIndex {
    private final Map<OWLObjectPropertyExpression, Map<OWLClassExpression, Set<OWLClassExpression>>> superclassesBySubclassRoleFillerMap = new HashMap<>();
    private final Map<OWLClassExpression, Map<OWLObjectPropertyExpression, Set<OWLClassExpression>>> superclassesBySubclassFillerRoleMap = new HashMap<>();

    public boolean put(RoleAndFiller roleAndFiller, OWLClassExpression superclass) {
        OWLObjectPropertyExpression role = roleAndFiller.role();
        OWLClassExpression filler = roleAndFiller.filler();

        boolean roleFillerAdded = superclassesBySubclassRoleFillerMap
            .computeIfAbsent(role, __ -> new HashMap<>())
            .computeIfAbsent(filler, __ -> new HashSet<>())
            .add(superclass);

        boolean fillerRoleAdded = superclassesBySubclassFillerRoleMap
            .computeIfAbsent(filler, __ -> new HashMap<>())
            .computeIfAbsent(role, __ -> new HashSet<>())
            .add(superclass);

        return roleFillerAdded || fillerRoleAdded;
    }

    public boolean containsRole(OWLObjectPropertyExpression role) {
        return superclassesBySubclassRoleFillerMap.containsKey(role);
    }

    public boolean containsFiller(OWLClassExpression filler) {
        return superclassesBySubclassFillerRoleMap.containsKey(filler);
    }

    public Set<OWLObjectPropertyExpression> getRolesByFiller(OWLClassExpression filler) {
        Map<OWLObjectPropertyExpression, Set<OWLClassExpression>> superclassesByRole = superclassesBySubclassFillerRoleMap.get(filler);
        return superclassesByRole.keySet();
    }

    public Set<OWLClassExpression> getFillersByRole(OWLObjectPropertyExpression role) {
        Map<OWLClassExpression, Set<OWLClassExpression>> superclassesByFiller = superclassesBySubclassRoleFillerMap.get(role);
        return superclassesByFiller.keySet();
    }

    public Set<OWLClassExpression> getSuperclassesByRoleFiller(OWLObjectPropertyExpression role, OWLClassExpression filler) {
        return superclassesBySubclassRoleFillerMap
            .getOrDefault(role, new HashMap<>())
            .getOrDefault(filler, new HashSet<>());
    }

    public Set<OWLClassExpression> getSuperclassesByFillerRole(OWLClassExpression filler, OWLObjectPropertyExpression role) {
        return superclassesBySubclassFillerRoleMap
            .getOrDefault(filler, new HashMap<>())
            .getOrDefault(role, new HashSet<>());
    }
}