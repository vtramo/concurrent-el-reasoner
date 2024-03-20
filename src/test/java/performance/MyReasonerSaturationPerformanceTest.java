package performance;

import indexing.OntologyIndex;
import indexing.OntologyIndexer;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;
import saturation.OntologySaturationProcess;

public class MyReasonerSaturationPerformanceTest extends SaturationPerformanceTest {

    @Benchmark
    public void classify() {
        OntologyIndexer ontologyIndexer = new OntologyIndexer(ontology);
        OntologyIndex ontologyIndex = ontologyIndexer.buildIndex();
        OntologySaturationProcess ontologySaturationProcess = new OntologySaturationProcess(ontology, ontologyIndex);
        ontologySaturationProcess.saturate();
    }

    public static void main(String[] args) throws RunnerException {
        Options opt = new OptionsBuilder()
            .include(MyReasonerSaturationPerformanceTest.class.getSimpleName())
            .build();

        new Runner(opt).run();
    }
}
