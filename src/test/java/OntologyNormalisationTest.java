import normalisation.OntologyNormaliser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.model.parameters.Imports;
import utils.OntologyUtils;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;

@DisplayName("Ontology Normalisation Test Suite")
public class OntologyNormalisationTest {

    @Nested
    @DisplayName("Normalisation Test Case 1")
    class NormalisationTestCase1 {

        OWLOntology abnormalOntology;

        @BeforeEach
        void createEmptyOntology() {
            abnormalOntology = OntologyUtils.createEmptyOWLOntology();
        }

        /*
         *   src/test/resources/normalisation-test-case-1.png
         */
        @Test
        @DisplayName("Normalisation Test Case 1")
        void normalisationTest() {
            OWLSubClassOfAxiom abnormalSubClassOfAxiom = buildAbnormalSubClassOfAxiomTestCase1();
            abnormalOntology.add(abnormalSubClassOfAxiom);

            OntologyNormaliser ontologyNormaliser = new OntologyNormaliser(abnormalOntology);
            OWLOntology normalisedOntology = ontologyNormaliser.createNormalisedOntology();

            assertThat(normalisedOntology.getTBoxAxioms(Imports.INCLUDED), hasSize(6));
        }

        private OWLSubClassOfAxiom buildAbnormalSubClassOfAxiomTestCase1() {
            OWLOntologyManager manager = abnormalOntology.getOWLOntologyManager();
            IRI iri = manager.getOntologyDocumentIRI(abnormalOntology);
            OWLDataFactory owlDataFactory = manager.getOWLDataFactory();

            OWLClass A = owlDataFactory.getOWLClass(iri + "#A");
            OWLClass B = owlDataFactory.getOWLClass(iri + "#B");
            OWLObjectProperty R = owlDataFactory.getOWLObjectProperty(iri + "#R");
            OWLObjectProperty S = owlDataFactory.getOWLObjectProperty(iri + "#S");
            OWLObjectSomeValuesFrom RA = owlDataFactory.getOWLObjectSomeValuesFrom(R, A);
            OWLObjectSomeValuesFrom SA = owlDataFactory.getOWLObjectSomeValuesFrom(S, A);
            OWLObjectSomeValuesFrom RSA = owlDataFactory.getOWLObjectSomeValuesFrom(R, SA);
            OWLObjectIntersectionOf RAandRSA = owlDataFactory.getOWLObjectIntersectionOf(RA, RSA);
            OWLObjectIntersectionOf AandB = owlDataFactory.getOWLObjectIntersectionOf(A, B);

            return owlDataFactory.getOWLSubClassOfAxiom(RAandRSA, AandB);
        }
    }

    @Nested
    @DisplayName("Normalisation Test Case 2 (Simple University)")
    class NormalisationTestCase2 {

        /*
         *   src/test/resources/normalisation-test-case-2-simple-university.png
         */
        @Test
        @DisplayName("Normalise simple ontology university")
        void university() {
            OWLOntology ontologyUniversity = OntologyUtils.createSimpleOntologyUniversity();
            OntologyNormaliser ontologyNormaliser = new OntologyNormaliser(ontologyUniversity);

            OWLOntology normalisedOntology = ontologyNormaliser.createNormalisedOntology();

            assertThat(normalisedOntology.getTBoxAxioms(Imports.INCLUDED), hasSize(equalTo(13)));
        }

    }

    @Test
    @DisplayName("Normalisation Test Case 3 (Ontology A)")
    public void normaliseOntologyA() {
        OWLOntology ontologyA = OntologyUtils.createOWLOntologyA();
        OntologyNormaliser ontologyNormaliser = new OntologyNormaliser(ontologyA);

        OWLOntology normalisedOntology = ontologyNormaliser.createNormalisedOntology();

        assertThat(normalisedOntology.getTBoxAxioms(Imports.INCLUDED), hasSize(15));
    }
}
