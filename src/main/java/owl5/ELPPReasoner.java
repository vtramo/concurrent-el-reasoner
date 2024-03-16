package owl5;

import hierarchy.SubsumptionHierarchy;
import hierarchy.SubsumptionHierarchyProcess;
import index.OntologyIndex;
import index.OntologyIndexer;
import normalisation.OntologyNormaliser;
import normalisation.rules.NormalisationRule;
import normalisation.rules.NormalisationRuleFactory;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.reasoner.*;
import org.semanticweb.owlapi.reasoner.impl.OWLClassNode;
import org.semanticweb.owlapi.util.Version;
import saturation.OntologySaturationProcess;
import saturation.SaturationResult;

import java.util.*;

import static normalisation.NormalisationUtils.*;

public class ELPPReasoner implements OWLReasoner {
    private final OWLOntology ontology;
    private final IRI ontologyIRI;
    private boolean isClassHierarchyPrecomputed;
    private SaturationResult saturationResult;
    private SubsumptionHierarchy subsumptionHierarchy;

    public ELPPReasoner(OWLOntology ontology) {
        this.ontology = ontology;

        OWLOntologyManager owlOntologyManager = ontology.getOWLOntologyManager();
        this.ontologyIRI = owlOntologyManager.getOntologyDocumentIRI(ontology);
    }

    @Override
    public String getReasonerName() {
        return null;
    }

    @Override
    public Version getReasonerVersion() {
        return null;
    }

    @Override
    public OWLOntology getRootOntology() {
        return ontology;
    }

    @Override
    public void precomputeInferences(InferenceType... inferenceTypes) {
        Arrays.stream(inferenceTypes)
            .filter(inferenceType -> !Objects.equals(inferenceType, InferenceType.CLASS_HIERARCHY))
            .findAny()
            .ifPresent(unsupportedInferenceType -> {
                throw new UnsupportedOperationException(unsupportedInferenceType.toString());
            });

        if (isClassHierarchyPrecomputed) {
            return;
        }

        // TBox normalisation
        OntologyNormaliser ontologyNormaliser = new OntologyNormaliser(ontology);
        OWLOntology normalisedOntology = ontologyNormaliser.createNormalisedOntology();

        // Ontology indexing
        OntologyIndexer ontologyIndexer = new OntologyIndexer(normalisedOntology);
        OntologyIndex ontologyIndex = ontologyIndexer.buildIndex();

        // Ontology saturation process
        OntologySaturationProcess ontologySaturationProcess = new OntologySaturationProcess(normalisedOntology, ontologyIndex);
        this.saturationResult = ontologySaturationProcess.saturate();

        // Build subsumption hierarchy
        SubsumptionHierarchyProcess subsumptionHierarchyProcess = new SubsumptionHierarchyProcess();

        this.subsumptionHierarchy = subsumptionHierarchyProcess.buildHierarchy(saturationResult);
        isClassHierarchyPrecomputed = true;
    }

    @Override
    public boolean isPrecomputed(InferenceType inferenceType) {
        if (!Objects.equals(inferenceType, InferenceType.CLASS_HIERARCHY)) {
            return false;
        }

        return isClassHierarchyPrecomputed;
    }

    @Override
    public Set<InferenceType> getPrecomputableInferenceTypes() {
        return Set.of(InferenceType.CLASS_HIERARCHY);
    }

    @Override
    public boolean isConsistent() {
        return true;
    }

    @Override
    public boolean isSatisfiable(OWLClassExpression owlClassExpression) {
        return true;
    }

    @Override
    public Node<OWLClass> getUnsatisfiableClasses() {
        return new OWLClassNode();
    }

    @Override
    public boolean isEntailed(OWLAxiom owlAxiom) {
        if (!isEntailmentCheckingSupported(owlAxiom.getAxiomType())) {
            throw new UnsupportedEntailmentTypeException(owlAxiom);
        }

        precomputeInferences(InferenceType.CLASS_HIERARCHY);

        return switch (owlAxiom) {
            case OWLSubClassOfAxiom owlSubClassOfAxiom -> {
                if (!isAbnormalTBoxAxiom(owlSubClassOfAxiom)) {
                    yield saturationResult.contains(owlSubClassOfAxiom);
                }

                Set<OWLSubClassOfAxiom> normalisedAxioms = normaliseAxioms(Set.of(owlSubClassOfAxiom));
                yield saturationResult.containsAll(normalisedAxioms);
            }

            case OWLEquivalentClassesAxiom owlEquivalentClassesAxiom -> {
                Iterator<OWLSubClassOfAxiom> owlSubClassOfAxiomIterator =
                    owlEquivalentClassesAxiom
                        .asOWLSubClassOfAxioms()
                        .iterator();

                OWLSubClassOfAxiom firstSubClassOfAxiom = owlSubClassOfAxiomIterator.next();
                OWLSubClassOfAxiom secondSubClassOfAxiom = owlSubClassOfAxiomIterator.next();

                Set<OWLSubClassOfAxiom> normalisedAxioms = normaliseAxioms(Set.of(firstSubClassOfAxiom, secondSubClassOfAxiom));
                yield saturationResult.containsAll(normalisedAxioms);
            }

            default -> false;
        };
    }

    @Override
    public boolean isEntailed(Set<? extends OWLAxiom> set) {
        List<Boolean> booleanIsEntailedAxiomList = set.stream()
            .map(this::isEntailed)
            .takeWhile(isEntailed -> isEntailed)
            .toList();

        return set.size() == booleanIsEntailedAxiomList.size();
    }

    private Set<OWLSubClassOfAxiom> normaliseAxioms(Set<OWLSubClassOfAxiom> subClassOfAxioms) {
        Set<OWLSubClassOfAxiom> normalisedAxioms = new HashSet<>(subClassOfAxioms.size());
        List<OWLSubClassOfAxiom> queueAbnormalAxioms = new ArrayList<>(subClassOfAxioms);

        while (!queueAbnormalAxioms.isEmpty()) {
            OWLSubClassOfAxiom axiom = queueAbnormalAxioms.removeFirst();

            if (!isAbnormalTBoxAxiom(axiom)) {
                normalisedAxioms.add(axiom);
                continue;
            }

            NormalisationRule normalisationRule = NormalisationRuleFactory.getTBoxAxiomNormalisationRule(axiom);
            Collection<OWLSubClassOfAxiom> normalisationResultAxioms = normalisationRule.normalise(ontologyIRI, axiom);
            queueAbnormalAxioms.addAll(normalisationResultAxioms);
        }

        return normalisedAxioms;
    }

    @Override
    public boolean isEntailmentCheckingSupported(AxiomType<?> axiomType) {
        return Objects.equals(axiomType, AxiomType.SUBCLASS_OF) || Objects.equals(axiomType, AxiomType.EQUIVALENT_CLASSES);
    }

    @Override
    public Node<OWLClass> getTopClassNode() {
        return subsumptionHierarchy.getOriginalEquivalentClasses(OWLManager.getOWLDataFactory().getOWLThing());
    }

    @Override
    public Node<OWLClass> getBottomClassNode() {
        return subsumptionHierarchy.getOriginalEquivalentClasses(OWLManager.getOWLDataFactory().getOWLNothing());
    }

    @Override
    public NodeSet<OWLClass> getSubClasses(OWLClassExpression owlClassExpression, boolean direct) {
        if (!(owlClassExpression instanceof OWLClass owlClass)) throw new IllegalArgumentException();
        precomputeInferences(InferenceType.CLASS_HIERARCHY);
        return direct ? subsumptionHierarchy.getOriginalDirectSubClasses(owlClass) : subsumptionHierarchy.getOriginalSubClasses(owlClass);
    }

    @Override
    public NodeSet<OWLClass> getSuperClasses(OWLClassExpression owlClassExpression, boolean direct) {
        if (!(owlClassExpression instanceof OWLClass owlClass)) throw new IllegalArgumentException();
        precomputeInferences(InferenceType.CLASS_HIERARCHY);
        return direct ? subsumptionHierarchy.getOriginalDirectSuperClasses(owlClass) : subsumptionHierarchy.getOriginalSuperClasses(owlClass);
    }

    @Override
    public Node<OWLClass> getEquivalentClasses(OWLClassExpression owlClassExpression) {
        if (!(owlClassExpression instanceof OWLClass owlClass)) throw new IllegalArgumentException();
        precomputeInferences(InferenceType.CLASS_HIERARCHY);
        return subsumptionHierarchy.getOriginalEquivalentClasses(owlClass);
    }

    @Override
    public NodeSet<OWLClass> getDisjointClasses(OWLClassExpression owlClassExpression) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Node<OWLObjectPropertyExpression> getTopObjectPropertyNode() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Node<OWLObjectPropertyExpression> getBottomObjectPropertyNode() {
        throw new UnsupportedOperationException();
    }

    @Override
    public NodeSet<OWLObjectPropertyExpression> getSubObjectProperties(OWLObjectPropertyExpression owlObjectPropertyExpression, boolean b) {
        throw new UnsupportedOperationException();
    }

    @Override
    public NodeSet<OWLObjectPropertyExpression> getSuperObjectProperties(OWLObjectPropertyExpression owlObjectPropertyExpression, boolean b) {
        throw new UnsupportedOperationException();
    }

    @Override
    public BufferingMode getBufferingMode() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void flush() { throw new UnsupportedOperationException(); }

    @Override
    public List<OWLOntologyChange> getPendingChanges() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Set<OWLAxiom> getPendingAxiomAdditions() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Set<OWLAxiom> getPendingAxiomRemovals() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void interrupt() { throw new UnsupportedOperationException(); }

    @Override
    public Node<OWLObjectPropertyExpression> getEquivalentObjectProperties(OWLObjectPropertyExpression owlObjectPropertyExpression) {
        throw new UnsupportedOperationException();
    }

    @Override
    public NodeSet<OWLObjectPropertyExpression> getDisjointObjectProperties(OWLObjectPropertyExpression owlObjectPropertyExpression) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Node<OWLObjectPropertyExpression> getInverseObjectProperties(OWLObjectPropertyExpression owlObjectPropertyExpression) {
        throw new UnsupportedOperationException();
    }

    @Override
    public NodeSet<OWLClass> getObjectPropertyDomains(OWLObjectPropertyExpression owlObjectPropertyExpression, boolean b) {
        throw new UnsupportedOperationException();
    }

    @Override
    public NodeSet<OWLClass> getObjectPropertyRanges(OWLObjectPropertyExpression owlObjectPropertyExpression, boolean b) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Node<OWLDataProperty> getTopDataPropertyNode() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Node<OWLDataProperty> getBottomDataPropertyNode() {
        throw new UnsupportedOperationException();
    }

    @Override
    public NodeSet<OWLDataProperty> getSubDataProperties(OWLDataProperty owlDataProperty, boolean b) {
        throw new UnsupportedOperationException();
    }

    @Override
    public NodeSet<OWLDataProperty> getSuperDataProperties(OWLDataProperty owlDataProperty, boolean b) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Node<OWLDataProperty> getEquivalentDataProperties(OWLDataProperty owlDataProperty) {
        throw new UnsupportedOperationException();
    }

    @Override
    public NodeSet<OWLDataProperty> getDisjointDataProperties(OWLDataPropertyExpression owlDataPropertyExpression) {
        throw new UnsupportedOperationException();
    }

    @Override
    public NodeSet<OWLClass> getDataPropertyDomains(OWLDataProperty owlDataProperty, boolean b) {
        throw new UnsupportedOperationException();
    }

    @Override
    public NodeSet<OWLClass> getTypes(OWLNamedIndividual owlNamedIndividual, boolean b) {
        throw new UnsupportedOperationException();
    }

    @Override
    public NodeSet<OWLNamedIndividual> getInstances(OWLClassExpression owlClassExpression, boolean b) {
        throw new UnsupportedOperationException();
    }

    @Override
    public NodeSet<OWLNamedIndividual> getObjectPropertyValues(OWLNamedIndividual owlNamedIndividual, OWLObjectPropertyExpression owlObjectPropertyExpression) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Set<OWLLiteral> getDataPropertyValues(OWLNamedIndividual owlNamedIndividual, OWLDataProperty owlDataProperty) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Node<OWLNamedIndividual> getSameIndividuals(OWLNamedIndividual owlNamedIndividual) {
        throw new UnsupportedOperationException();
    }

    @Override
    public NodeSet<OWLNamedIndividual> getDifferentIndividuals(OWLNamedIndividual owlNamedIndividual) {
        throw new UnsupportedOperationException();
    }

    @Override
    public long getTimeOut() {
        throw new UnsupportedOperationException();
    }

    @Override
    public FreshEntityPolicy getFreshEntityPolicy() {
        throw new UnsupportedOperationException();
    }

    @Override
    public IndividualNodeSetPolicy getIndividualNodeSetPolicy() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void dispose() {
        throw new UnsupportedOperationException();
    }
}
