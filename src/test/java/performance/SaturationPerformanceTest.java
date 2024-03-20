package performance;


import normalisation.OntologyNormaliser;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;

import java.io.File;
import java.util.concurrent.TimeUnit;

@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.SECONDS)
public class SaturationPerformanceTest {

    static final String ONTOLOGY_PATH = "src/test/resources/full-galen-modified.owl";
    static final OWLOntology ontology;

    static {
        OWLOntologyManager manager = OWLManager.createOWLOntologyManager();

        OWLOntology notNormalisedOntology;
        try {
            notNormalisedOntology = manager.loadOntologyFromOntologyDocument(new File(ONTOLOGY_PATH));
        } catch (OWLOntologyCreationException e) {
            throw new RuntimeException(e);
        }

        OntologyNormaliser ontologyNormaliser = new OntologyNormaliser(notNormalisedOntology);
        ontology = ontologyNormaliser.createNormalisedOntology();
    }
}
