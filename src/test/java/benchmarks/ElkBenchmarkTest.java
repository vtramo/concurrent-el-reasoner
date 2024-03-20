package benchmarks;

import normalisation.OntologyNormaliser;
import org.openjdk.jmh.annotations.*;
import org.semanticweb.elk.owlapi.ElkReasonerFactory;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.reasoner.InferenceType;
import org.semanticweb.owlapi.reasoner.OWLReasoner;
import org.semanticweb.owlapi.reasoner.OWLReasonerFactory;

import java.io.File;
import java.io.IOException;

public class ElkBenchmarkTest {

    public OWLOntology ontology;

    @Setup(Level.Trial)
    public void setup() {
        OWLOntologyManager manager = OWLManager.createOWLOntologyManager();

        try {
            ontology = manager.loadOntologyFromOntologyDocument(new File("src/test/resources/full-galen-modified.owl"));
        } catch (OWLOntologyCreationException e) {
            throw new RuntimeException(e);
        }

        OntologyNormaliser ontologyNormaliser = new OntologyNormaliser(ontology);
        ontology = ontologyNormaliser.createNormalisedOntology();
    }

    @Benchmark
    @BenchmarkMode(Mode.AverageTime)
    public void benchmark() {
        OWLReasonerFactory reasonerFactory = new ElkReasonerFactory();
        OWLReasoner reasoner = reasonerFactory.createReasoner(ontology);
        reasoner.precomputeInferences(InferenceType.CLASS_HIERARCHY);
    }

    public static void main(String[] args) throws IOException {
        org.openjdk.jmh.Main.main(args);
    }

}
