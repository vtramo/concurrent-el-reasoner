package normalisation;

import normalisation.rules.NormalisationRule;
import normalisation.rules.NormalisationRuleFactory;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.model.parameters.Imports;
import org.semanticweb.owlapi.profiles.OWL2ELProfile;
import org.semanticweb.owlapi.profiles.OWLProfileReport;
import org.semanticweb.owlapi.profiles.violations.UndeclaredEntityViolation;

import java.util.*;
import java.util.concurrent.LinkedBlockingQueue;

import static normalisation.NormalisationChecks.isSubclassBCConcept;
import static normalisation.NormalisationChecks.isSuperclassBCConcept;

public class OntologyNormaliser implements OWLAxiomVisitor {

    private final OWLOntology abnormalOntology;
    private final IRI abnormalOntologyIri;
    private final List<OWLAxiom> abnormalTBoxAxioms;

    private ListIterator<OWLAxiom> abnormalTBoxAxiomsListIterator;
    private OWLOntology normalisedOntology;

    public OntologyNormaliser(OWLOntology abnormalOntology) {
        checkIfOntologyIsInOWL2ELProfile(abnormalOntology);

        Set<OWLAxiom> tBoxAxioms = abnormalOntology.getTBoxAxioms(Imports.INCLUDED);
        int totalTBoxAxioms = tBoxAxioms.size();

        this.abnormalTBoxAxioms = new ArrayList<>(totalTBoxAxioms);
        this.abnormalOntology = abnormalOntology;

        OWLOntologyManager abnormalOntologyManager = abnormalOntology.getOWLOntologyManager();
        this.abnormalOntologyIri = abnormalOntologyManager.getOntologyDocumentIRI(abnormalOntology);
    }

    private static void checkIfOntologyIsInOWL2ELProfile(OWLOntology abnormalOntology) {
        OWL2ELProfile owl2ELProfile = new OWL2ELProfile();
        OWLProfileReport owlProfileReport = owl2ELProfile.checkOntology(abnormalOntology);

        boolean isInProfile = owlProfileReport
            .getViolations()
            .stream()
            .allMatch(violation -> violation instanceof UndeclaredEntityViolation);

        if (!isInProfile) {
            System.out.println("ONTOLOGY NOT IN OWL2EL PROFILE!");
        }
    }

    public OWLOntology createNormalisedOntology() {
        createInitialEmptyNormalisedOntology();
        identifyAbnormalTBoxAxioms();
        normaliseOntology();
        return normalisedOntology;
    }

    private void createInitialEmptyNormalisedOntology() {
        OWLOntologyManager emptyNormalisedOntologyManager = OWLManager.createOWLOntologyManager();

        try {
            normalisedOntology = emptyNormalisedOntologyManager.createOntology();
        } catch (OWLOntologyCreationException e) {
            throw new RuntimeException(e);
        }
    }

    private void identifyAbnormalTBoxAxioms() {
        abnormalOntology.axioms()
            .forEach(axiom -> {
                switch (axiom) {
                    case OWLSubClassOfAxiom subClassOfAxiom -> {
                        if (isAbnormalTBoxAxiom(subClassOfAxiom)) {
                            abnormalTBoxAxioms.add(axiom);
                        } else {
                            normalisedOntology.add(axiom);
                        }
                    }
                    case OWLEquivalentClassesAxiom equivalentClassesAxiom -> {
                        Collection<OWLSubClassOfAxiom> owlSubClassOfAxioms = equivalentClassesAxiom.asOWLSubClassOfAxioms();
                        owlSubClassOfAxioms.forEach(subClassOfAxiom -> {
                            if (isAbnormalTBoxAxiom(subClassOfAxiom)) {
                                abnormalTBoxAxioms.add(equivalentClassesAxiom);
                            } else {
                                normalisedOntology.add(axiom);
                            }
                        });
                    }
                    default -> normalisedOntology.add(axiom);
                }
            });
    }

    private void normaliseOntology() {
        do {

            abnormalTBoxAxiomsListIterator = abnormalTBoxAxioms.listIterator();

            while (abnormalTBoxAxiomsListIterator.hasNext()) {
                OWLAxiom potentiallyAbnormalTBoxAxiom = abnormalTBoxAxiomsListIterator.next();
                abnormalTBoxAxiomsListIterator.remove();
                potentiallyAbnormalTBoxAxiom.accept(this);
            }

        } while (!abnormalTBoxAxioms.isEmpty());
    }

    // --- possible parallel implementation (to be improved)
    private void normaliseOntologyParallel() {
        LinkedBlockingQueue<OWLAxiom> linkedBlockingQueue = new LinkedBlockingQueue<>(abnormalTBoxAxioms);

        List<Thread> threads = new ArrayList<>();
        for (int i = 0; i < Runtime.getRuntime().availableProcessors(); i++) {
            Thread thread = new Thread(() -> {
                OWLAxiom axiom;
                while ((axiom = linkedBlockingQueue.poll()) != null) {
                    Collection<OWLSubClassOfAxiom> axioms = task(axiom);
                    axioms.forEach(axiomz -> {
                        if (isAbnormalTBoxAxiom(axiomz)) {
                            try {
                                linkedBlockingQueue.put(axiomz);
                            } catch (InterruptedException e) {
                                throw new RuntimeException(e);
                            }
                        } else {
                            normalisedOntology.add(axiomz);
                        }
                    });
                }
            });
            threads.add(thread);
        }

        threads.forEach(Thread::start);
        for (Thread thread : threads) {
            try {
                thread.join();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }
    // -----------

    public Collection<OWLSubClassOfAxiom> task(OWLAxiom axiom) {
        ArrayList<OWLSubClassOfAxiom> result = new ArrayList<>();
        if (axiom instanceof OWLEquivalentClassesAxiom equivalentAxiom) {
            Collection<OWLSubClassOfAxiom> owlSubClassOfAxioms = equivalentAxiom.asOWLSubClassOfAxioms();
            owlSubClassOfAxioms.forEach(subAxiom -> {
                NormalisationRule normalisationRule = NormalisationRuleFactory.getTBoxAxiomNormalisationRule(subAxiom);
                Collection<OWLSubClassOfAxiom> normalise = normalisationRule.normalise(abnormalOntologyIri, subAxiom);
                result.addAll(normalise);
            });
        } else {
            OWLSubClassOfAxiom subClassOfAxiom = (OWLSubClassOfAxiom) axiom;
            NormalisationRule normalisationRule = NormalisationRuleFactory.getTBoxAxiomNormalisationRule(subClassOfAxiom);
            Collection<OWLSubClassOfAxiom> normalise = normalisationRule.normalise(abnormalOntologyIri, subClassOfAxiom);
            result.addAll(normalise);
        }
        return result;
    }

    @Override
    public void visit(OWLSubClassOfAxiom subClassOfAxiom) {
        NormalisationRule normalisationRule = NormalisationRuleFactory.getTBoxAxiomNormalisationRule(subClassOfAxiom);
        Collection<OWLSubClassOfAxiom> tBoxAxiomsProducedByNormalisation = normalisationRule.normalise(abnormalOntologyIri, subClassOfAxiom);
        tBoxAxiomsProducedByNormalisation.forEach(this::processPotentiallyAbnormalTBoxAxiom);
    }

    @Override
    public void visit(OWLEquivalentClassesAxiom equivalentAxiom) {
        Collection<OWLSubClassOfAxiom> subClassOfAxioms = equivalentAxiom.asOWLSubClassOfAxioms();
        subClassOfAxioms.forEach(this::processPotentiallyAbnormalTBoxAxiom);
    }

    private void processPotentiallyAbnormalTBoxAxiom(OWLSubClassOfAxiom potentiallyAbnormalTBoxAxiom) {
        if (!isAbnormalTBoxAxiom(potentiallyAbnormalTBoxAxiom)) {
            normalisedOntology.addAxiom(potentiallyAbnormalTBoxAxiom);
        } else {
            abnormalTBoxAxiomsListIterator.add(potentiallyAbnormalTBoxAxiom);
        }
    }

    static boolean isAbnormalTBoxAxiom(OWLSubClassOfAxiom owlSubClassOfAxiom) {
        OWLClassExpression subClass = owlSubClassOfAxiom.getSubClass();
        OWLClassExpression superClass = owlSubClassOfAxiom.getSuperClass();

        if (isSubclassBCConcept(subClass) && isSuperclassBCConcept(superClass)) {
            return false;
        }

        if (subClass instanceof OWLObjectIntersectionOf objectIntersectionOf && isSuperclassBCConcept(superClass)) {
            List<OWLClassExpression> operands = objectIntersectionOf.getOperandsAsList();
            OWLClassExpression first = operands.getFirst();
            OWLClassExpression last = operands.getLast();

            if (isSubclassBCConcept(first)  && isSubclassBCConcept(last)) return false;
        }

        if (isSubclassBCConcept(subClass) && superClass instanceof OWLObjectSomeValuesFrom rightIsObjectSomeValuesFrom) {
            OWLClassExpression filler = rightIsObjectSomeValuesFrom.getFiller();
            if (isSubclassBCConcept(filler)) return false;
        }

        if (subClass instanceof OWLObjectSomeValuesFrom leftIsObjectSomeValuesFrom && isSuperclassBCConcept(superClass)) {
            OWLClassExpression filler = leftIsObjectSomeValuesFrom.getFiller();
            return !isSubclassBCConcept(filler);
        }

        return true;
    }

}