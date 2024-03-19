import hierarchy.SubsumptionHierarchy;
import hierarchy.SubsumptionHierarchyProcess;
import indexing.OntologyIndex;
import indexing.OntologyIndexer;
import normalisation.OntologyNormaliser;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.semanticweb.HermiT.ReasonerFactory;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.model.parameters.Imports;
import org.semanticweb.owlapi.reasoner.InferenceDepth;
import org.semanticweb.owlapi.reasoner.InferenceType;
import org.semanticweb.owlapi.reasoner.OWLReasoner;
import saturation.OntologySaturationProcess;
import saturation.SaturationResult;
import utils.OntologyUtils;

import java.io.File;
import java.util.Set;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;

public class OntologySaturationTest {

    @Test
    @DisplayName("Test Case 1 - Ontology A")
    public void testCase1() {
        OWLOntology ontologyA = OntologyUtils.createOWLOntologyA();

        OntologyNormaliser ontologyNormaliser = new OntologyNormaliser(ontologyA);
        OWLOntology normalisedOntologyA = ontologyNormaliser.createNormalisedOntology();

        OntologyIndexer ontologyAIndexer = new OntologyIndexer(normalisedOntologyA);
        OntologyIndex ontologyAIndex = ontologyAIndexer.buildIndex();

        OntologySaturationProcess ontologyASaturationProcess = new OntologySaturationProcess(normalisedOntologyA, ontologyAIndex);
        SaturationResult saturation = ontologyASaturationProcess.saturate();

        SubsumptionHierarchyProcess subsumptionHierarchyProcess = new SubsumptionHierarchyProcess();
        SubsumptionHierarchy subsumptionHierarchy = subsumptionHierarchyProcess.buildHierarchy(saturation);

        ReasonerFactory reasonerFactory = new org.semanticweb.HermiT.ReasonerFactory();
        OWLReasoner hermiT = reasonerFactory.createReasoner(normalisedOntologyA);
        hermiT.precomputeInferences(InferenceType.CLASS_HIERARCHY);


        Set<OWLClass> signature = normalisedOntologyA.getClassesInSignature(Imports.INCLUDED);

        saturation.getAllAxioms().forEach(axiom ->
            assertThat(hermiT.isEntailed(axiom),
                is(equalTo(true))));

        signature.forEach(owlClass ->
            assertThat(subsumptionHierarchy.getEquivalentClasses(owlClass),
                is(equalTo(hermiT.getEquivalentClasses(owlClass)))));

        signature.forEach(owlClass ->
            assertThat(subsumptionHierarchy.getDirectSubClasses(owlClass),
                is(equalTo(hermiT.getSubClasses(owlClass, InferenceDepth.DIRECT)))));

        signature.forEach(owlClass ->
            assertThat(subsumptionHierarchy.getDirectSuperClasses(owlClass),
                is(equalTo(hermiT.getSuperClasses(owlClass, InferenceDepth.DIRECT)))));

        signature.forEach(owlClass ->
            assertThat(subsumptionHierarchy.getSuperClasses(owlClass),
                is(equalTo(hermiT.getSuperClasses(owlClass)))));

        signature.forEach(owlClass ->
            assertThat(subsumptionHierarchy.getSubClasses(owlClass),
                is(equalTo(hermiT.getSubClasses(owlClass)))));
    }

    @Test
    @DisplayName("Test Case 2 - Ontology B")
    public void testCase2() {
        OWLOntology ontologyB = OntologyUtils.createOWLOntologyB();

        OntologyNormaliser ontologyNormaliser = new OntologyNormaliser(ontologyB);
        OWLOntology normalisedOntologyB = ontologyNormaliser.createNormalisedOntology();

        OntologyIndexer ontologyBIndexer = new OntologyIndexer(normalisedOntologyB);
        OntologyIndex ontologyBIndex = ontologyBIndexer.buildIndex();

        OntologySaturationProcess ontologyBSaturationProcess = new OntologySaturationProcess(normalisedOntologyB, ontologyBIndex);
        SaturationResult saturation = ontologyBSaturationProcess.saturate();

        SubsumptionHierarchyProcess subsumptionHierarchyProcess = new SubsumptionHierarchyProcess();
        SubsumptionHierarchy subsumptionHierarchy = subsumptionHierarchyProcess.buildHierarchy(saturation);

        ReasonerFactory reasonerFactory = new ReasonerFactory();
        OWLReasoner hermiT = reasonerFactory.createReasoner(normalisedOntologyB);
        hermiT.precomputeInferences(InferenceType.CLASS_HIERARCHY);


        Set<OWLClass> signature = normalisedOntologyB.getClassesInSignature(Imports.INCLUDED);

        signature.forEach(owlClass ->
            assertThat(subsumptionHierarchy.getEquivalentClasses(owlClass),
                is(equalTo(hermiT.getEquivalentClasses(owlClass)))));

        saturation.getAllAxioms().forEach(axiom ->
            assertThat(hermiT.isEntailed(axiom),
                is(equalTo(true))));

        signature.forEach(owlClass ->
            assertThat(subsumptionHierarchy.getEquivalentClasses(owlClass),
                is(equalTo(hermiT.getEquivalentClasses(owlClass)))));

        signature.forEach(owlClass ->
            assertThat(subsumptionHierarchy.getDirectSubClasses(owlClass),
                is(equalTo(hermiT.getSubClasses(owlClass, InferenceDepth.DIRECT)))));

        signature.forEach(owlClass ->
            assertThat(subsumptionHierarchy.getDirectSuperClasses(owlClass),
                is(equalTo(hermiT.getSuperClasses(owlClass, InferenceDepth.DIRECT)))));

        signature.forEach(owlClass ->
            assertThat(subsumptionHierarchy.getSubClasses(owlClass),
                is(equalTo(hermiT.getSubClasses(owlClass)))));

        signature.forEach(owlClass ->
            assertThat(subsumptionHierarchy.getSuperClasses(owlClass),
                is(equalTo(hermiT.getSuperClasses(owlClass)))));
    }

    @Test
    public void load() {
        OWLOntologyManager manager = OWLManager.createOWLOntologyManager();

        OWLOntology owlOntology;
        try {
            owlOntology = manager.loadOntologyFromOntologyDocument(new File("/home/vincenzo/Documents/bench/OWL2EL-350.owl"));
        } catch (OWLOntologyCreationException e) {
            throw new RuntimeException(e);
        }

        int a = 3;


        OntologyNormaliser ontologyNormaliser = new OntologyNormaliser(owlOntology);
        OWLOntology normalisedOntology = ontologyNormaliser.createNormalisedOntology();

        OntologyIndexer ontologyIndexer = new OntologyIndexer(normalisedOntology);
        OntologyIndex ontologyIndex = ontologyIndexer.buildIndex();

        OntologySaturationProcess ontologyBSaturationProcess = new OntologySaturationProcess(normalisedOntology, ontologyIndex);
        SaturationResult saturation = ontologyBSaturationProcess.saturate();

        SubsumptionHierarchyProcess subsumptionHierarchyProcess = new SubsumptionHierarchyProcess();
        SubsumptionHierarchy subsumptionHierarchy = subsumptionHierarchyProcess.buildHierarchy(saturation);
    }

}