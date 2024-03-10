package index;

import org.semanticweb.owlapi.model.*;

import static normalisation.NormalisationChecks.isSubclassBCConcept;
import static normalisation.NormalisationChecks.isSuperclassBCConcept;

public class OntologyIndexer {

    private final OWLOntology ontology;
    private OntologyIndex index;

    public OntologyIndexer(OWLOntology ontology) {
        this.ontology = ontology;
    }

    public OntologyIndex buildIndex() {
        index = new OntologyIndex();

        ontology
            .axioms(AxiomType.SUBCLASS_OF)
            .forEach(this::indexAxiom);

        return index;
    }

    private void indexAxiom(OWLSubClassOfAxiom subClassOfAxiom) {
        OWLClassExpression subClass = subClassOfAxiom.getSubClass();
        OWLClassExpression superClass = subClassOfAxiom.getSuperClass();

        if (isSubclassBCConcept(subClass) && isSuperclassBCConcept(superClass)) {
            index.putInToldSups(subClass, superClass);
            return;
        }

        if (subClass instanceof OWLObjectIntersectionOf intersection && isSuperclassBCConcept(superClass)) {
            index.putInSuperclassesByIntersectionOperandsMap(intersection, superClass);
            return;
        }

        if (isSubclassBCConcept(subClass) && superClass instanceof OWLObjectSomeValuesFrom objectSomeValuesFrom) {
            OWLObjectPropertyExpression role = objectSomeValuesFrom.getProperty();
            OWLClassExpression filler = objectSomeValuesFrom.getFiller();
            RoleAndFiller roleAndFiller = new RoleAndFiller(role, filler);
            index.putInExistentialRightSetBySubclass(subClass, roleAndFiller);
            return;
        }

        if (subClass instanceof OWLObjectSomeValuesFrom objectSomeValuesFrom && isSuperclassBCConcept(superClass)) {
            OWLObjectPropertyExpression role = objectSomeValuesFrom.getProperty();
            OWLClassExpression filler = objectSomeValuesFrom.getFiller();
            index.putInGciLeftExistentialIndex(new RoleAndFiller(role, filler), superClass);
        }
    }
}
