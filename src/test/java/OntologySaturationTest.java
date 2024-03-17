import hierarchy.SubsumptionHierarchy;
import hierarchy.SubsumptionHierarchyProcess;
import indexing.OntologyIndex;
import indexing.OntologyIndexer;
import normalisation.OntologyNormaliser;
import org.junit.jupiter.api.Test;
import org.semanticweb.HermiT.ReasonerFactory;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.parameters.Imports;
import org.semanticweb.owlapi.reasoner.InferenceDepth;
import org.semanticweb.owlapi.reasoner.InferenceType;
import org.semanticweb.owlapi.reasoner.OWLReasoner;
import saturation.OntologySaturationProcess;
import saturation.SaturationResult;
import utils.OntologyUtils;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;

public class OntologySaturationTest {

    @Test
    public void testWithoutCR6Inferences() {
        OWLOntology ontologyWithoutCR6Inferences = OntologyUtils.createOWLOntologyAWithoutCR6Inferences();
        OntologyNormaliser ontologyNormaliser = new OntologyNormaliser(ontologyWithoutCR6Inferences);
        OWLOntology normalisedOntologyA = ontologyNormaliser.createNormalisedOntology();
        OntologyIndexer ontologyAIndexer = new OntologyIndexer(normalisedOntologyA);
        OntologyIndex ontologyAIndex = ontologyAIndexer.buildIndex();
        OntologySaturationProcess ontologyASaturationProcess = new OntologySaturationProcess(normalisedOntologyA, ontologyAIndex);
        SaturationResult saturation = ontologyASaturationProcess.saturate();
        SubsumptionHierarchyProcess subsumptionHierarchyProcess = new SubsumptionHierarchyProcess();
        SubsumptionHierarchy subsumptionHierarchy = subsumptionHierarchyProcess.buildHierarchy(saturation);

        ReasonerFactory reasonerFactory = new ReasonerFactory();
        OWLReasoner reasoner = reasonerFactory.createReasoner(normalisedOntologyA);
        reasoner.precomputeInferences(InferenceType.CLASS_HIERARCHY);

        normalisedOntologyA.getClassesInSignature(Imports.INCLUDED).forEach(owlClass -> {
            assertThat(subsumptionHierarchy.getSubClasses(owlClass), is(equalTo(reasoner.getSubClasses(owlClass))));
            assertThat(subsumptionHierarchy.getSuperClasses(owlClass), is(equalTo(reasoner.getSuperClasses(owlClass))));
            assertThat(subsumptionHierarchy.getDirectSuperClasses(owlClass), is(equalTo(reasoner.getSuperClasses(owlClass, InferenceDepth.DIRECT))));
            assertThat(subsumptionHierarchy.getDirectSubClasses(owlClass), is(equalTo(reasoner.getSubClasses(owlClass, InferenceDepth.DIRECT))));
        });
    }

    @Test
    public void testWithoutCR6InferencesModified() {
        OWLOntology ontologyWithoutCR6Inferences = OntologyUtils.createOWLOntologyAWithoutCR6InferencesModified();
        OntologyNormaliser ontologyNormaliser = new OntologyNormaliser(ontologyWithoutCR6Inferences);
        OWLOntology normalisedOntologyA = ontologyNormaliser.createNormalisedOntology();
        OntologyIndexer ontologyAIndexer = new OntologyIndexer(normalisedOntologyA);
        OntologyIndex ontologyAIndex = ontologyAIndexer.buildIndex();
        OntologySaturationProcess ontologyASaturationProcess = new OntologySaturationProcess(normalisedOntologyA, ontologyAIndex);
        SaturationResult saturation = ontologyASaturationProcess.saturate();
        SubsumptionHierarchyProcess subsumptionHierarchyProcess = new SubsumptionHierarchyProcess();
        SubsumptionHierarchy subsumptionHierarchy = subsumptionHierarchyProcess.buildHierarchy(saturation);

        ReasonerFactory reasonerFactory = new ReasonerFactory();
        OWLReasoner reasoner = reasonerFactory.createReasoner(normalisedOntologyA);
        reasoner.precomputeInferences(InferenceType.CLASS_HIERARCHY);

        saturation.getAllAxioms().forEach(axiom -> {
            assertThat(reasoner.isEntailed(axiom), is(equalTo(true)));
        });

        normalisedOntologyA.getClassesInSignature(Imports.INCLUDED).forEach(owlClass -> {
            assertThat(subsumptionHierarchy.getEquivalentClasses(owlClass), is(equalTo(reasoner.getEquivalentClasses(owlClass))));
        });

        normalisedOntologyA.getClassesInSignature(Imports.INCLUDED).forEach(owlClass -> {
            assertThat(subsumptionHierarchy.getDirectSubClasses(owlClass), is(equalTo(reasoner.getSubClasses(owlClass, InferenceDepth.DIRECT))));
        });

        normalisedOntologyA.getClassesInSignature(Imports.INCLUDED).forEach(owlClass -> {
            assertThat(subsumptionHierarchy.getDirectSuperClasses(owlClass), is(equalTo(reasoner.getSuperClasses(owlClass, InferenceDepth.DIRECT))));
        });

        normalisedOntologyA.getClassesInSignature(Imports.INCLUDED).forEach(owlClass -> {
            assertThat(subsumptionHierarchy.getSuperClasses(owlClass), is(equalTo(reasoner.getSuperClasses(owlClass))));
        });

        normalisedOntologyA.getClassesInSignature(Imports.INCLUDED).forEach(owlClass -> {
            assertThat(subsumptionHierarchy.getSubClasses(owlClass), is(equalTo(reasoner.getSubClasses(owlClass))));
        });
    }
}