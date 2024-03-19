import org.junit.jupiter.api.Test;
import org.semanticweb.HermiT.ReasonerFactory;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.reasoner.InferenceDepth;
import org.semanticweb.owlapi.reasoner.InferenceType;
import org.semanticweb.owlapi.reasoner.OWLReasoner;
import org.semanticweb.owlapi.reasoner.OWLReasonerFactory;
import owl5.ELPPReasonerFactory;
import utils.OntologyUtils;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;

public class OWLOntologyClassHierarchyInferenceTest {

    @Test
    public void testOntologyA() {
        OWLOntology ontology = OntologyUtils.createOWLOntologyA();
        OWLReasonerFactory owlReasonerFactory = new ELPPReasonerFactory();
        OWLReasoner myReasoner = owlReasonerFactory.createReasoner(ontology);

        OWLReasonerFactory oracleFactoryHermiT = new ReasonerFactory();
        OWLReasoner hermiT = oracleFactoryHermiT.createReasoner(ontology);

        myReasoner.precomputeInferences(InferenceType.CLASS_HIERARCHY);
        hermiT.precomputeInferences(InferenceType.CLASS_HIERARCHY);

        for (OWLClass owlClass: ontology.getClassesInSignature()) {
            assertThat(myReasoner.getEquivalentClasses(owlClass), is(equalTo(hermiT.getEquivalentClasses(owlClass))));

            assertThat(myReasoner.getSubClasses(owlClass), is(equalTo(hermiT.getSubClasses(owlClass))));
            assertThat(myReasoner.getSubClasses(owlClass, InferenceDepth.DIRECT), is(equalTo(hermiT.getSubClasses(owlClass, InferenceDepth.DIRECT))));

            assertThat(myReasoner.getSuperClasses(owlClass), is(equalTo(hermiT.getSuperClasses(owlClass))));
            assertThat(myReasoner.getSuperClasses(owlClass, InferenceDepth.DIRECT), is(equalTo(hermiT.getSuperClasses(owlClass, InferenceDepth.DIRECT))));
        }

    }

    @Test
    public void testOntologyB() {
        OWLOntology ontology = OntologyUtils.createOWLOntologyB();
        OWLReasonerFactory owlReasonerFactory = new ELPPReasonerFactory();
        OWLReasoner myReasoner = owlReasonerFactory.createReasoner(ontology);

        OWLReasonerFactory oracleFactoryHermiT = new ReasonerFactory();
        OWLReasoner hermiT = oracleFactoryHermiT.createReasoner(ontology);

        myReasoner.precomputeInferences(InferenceType.CLASS_HIERARCHY);
        hermiT.precomputeInferences(InferenceType.CLASS_HIERARCHY);

        for (OWLClass owlClass: ontology.getClassesInSignature()) {
            assertThat(myReasoner.getEquivalentClasses(owlClass), is(equalTo(hermiT.getEquivalentClasses(owlClass))));

            assertThat(myReasoner.getSubClasses(owlClass), is(equalTo(hermiT.getSubClasses(owlClass))));
            assertThat(myReasoner.getSubClasses(owlClass, InferenceDepth.DIRECT), is(equalTo(hermiT.getSubClasses(owlClass, InferenceDepth.DIRECT))));

            assertThat(myReasoner.getSuperClasses(owlClass), is(equalTo(hermiT.getSuperClasses(owlClass))));
            assertThat(myReasoner.getSuperClasses(owlClass, InferenceDepth.DIRECT), is(equalTo(hermiT.getSuperClasses(owlClass, InferenceDepth.DIRECT))));
        }

    }

}
