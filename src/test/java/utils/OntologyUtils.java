package utils;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.*;

public class OntologyUtils {

    public static OWLOntology createEmptyOWLOntology() {
        IRI iri = IRI.create("test");
        OWLOntologyManager manager = OWLManager.createOWLOntologyManager();

        try {
            return manager.createOntology(iri);
        } catch (OWLOntologyCreationException e) {
            throw new RuntimeException(e);
        }
    }

    public static OWLOntology createSimpleOntologyUniversity() {
        OWLOntology universityOntology = OntologyUtils.createEmptyOWLOntology();
        OWLOntologyManager owlOntologyManager = universityOntology.getOWLOntologyManager();
        OWLDataFactory df = owlOntologyManager.getOWLDataFactory();
        IRI iri = owlOntologyManager.getOntologyDocumentIRI(universityOntology);

        OWLClass course = df.getOWLClass(iri + "#Course");
        OWLClass person = df.getOWLClass(iri + "#Person");
        OWLClass UGC = df.getOWLClass(iri + "#UGC");
        OWLClass PGC = df.getOWLClass(iri + "#PGC");
        OWLClass teacher = df.getOWLClass(iri + "#Teacher");
        OWLClass student = df.getOWLClass(iri + "#Student");
        OWLObjectProperty teaches = df.getOWLObjectProperty(iri + "#teaches");
        OWLObjectProperty attends = df.getOWLObjectProperty(iri + "#attends");

        OWLObjectIntersectionOf courseAndPerson = df.getOWLObjectIntersectionOf(course, person);
        OWLSubClassOfAxiom courseAndPersonSubClassOfNothing = df.getOWLSubClassOfAxiom(courseAndPerson, df.getOWLNothing());

        OWLSubClassOfAxiom UGCSubClassOfCourse = df.getOWLSubClassOfAxiom(UGC, course);
        OWLSubClassOfAxiom PGCSubClassOfCourse = df.getOWLSubClassOfAxiom(PGC, course);

        OWLObjectSomeValuesFrom exisTeachesCourse = df.getOWLObjectSomeValuesFrom(teaches, course);
        OWLObjectIntersectionOf personAndExisTeachesCourse = df.getOWLObjectIntersectionOf(person, exisTeachesCourse);
        OWLEquivalentClassesAxiom teacherEquiv = df.getOWLEquivalentClassesAxiom(teacher, personAndExisTeachesCourse);

        OWLObjectSomeValuesFrom exisTeachesThing = df.getOWLObjectSomeValuesFrom(teaches, df.getOWLThing());
        OWLSubClassOfAxiom exisTeachesThingSubClassOfPerson = df.getOWLSubClassOfAxiom(exisTeachesThing, person);

        OWLObjectSomeValuesFrom exisAttendsCourse = df.getOWLObjectSomeValuesFrom(attends, course);
        OWLObjectIntersectionOf personAndExisAttendsCourse = df.getOWLObjectIntersectionOf(person, exisAttendsCourse);
        OWLEquivalentClassesAxiom studentEquiv = df.getOWLEquivalentClassesAxiom(student, personAndExisAttendsCourse);

        OWLObjectSomeValuesFrom exisAttendsThing = df.getOWLObjectSomeValuesFrom(attends, df.getOWLThing());
        OWLSubClassOfAxiom exisAttendsThingSubClassOfPerson = df.getOWLSubClassOfAxiom(exisAttendsThing, person);

        universityOntology.addAxioms(
            courseAndPersonSubClassOfNothing,
            UGCSubClassOfCourse,
            PGCSubClassOfCourse,
            teacherEquiv,
            exisTeachesThingSubClassOfPerson,
            studentEquiv,
            exisAttendsThingSubClassOfPerson
        );

        return universityOntology;
    }

    public static OWLOntology createOWLOntologyAWithoutCR6Inferences() {
        OWLOntology ontology = OntologyUtils.createEmptyOWLOntology();
        OWLOntologyManager owlOntologyManager = ontology.getOWLOntologyManager();
        OWLDataFactory df = owlOntologyManager.getOWLDataFactory();
        IRI iri = owlOntologyManager.getOntologyDocumentIRI(ontology);

        OWLClass A = df.getOWLClass(iri + "#A");
        OWLClass B = df.getOWLClass(iri + "#B");
        OWLClass C = df.getOWLClass(iri + "#C");
        OWLClass D = df.getOWLClass(iri + "#D");

        OWLObjectProperty r = df.getOWLObjectProperty(iri + "#r");

        OWLObjectSomeValuesFrom rA = df.getOWLObjectSomeValuesFrom(r, A);
        OWLObjectSomeValuesFrom rB = df.getOWLObjectSomeValuesFrom(r, B);
        OWLObjectSomeValuesFrom rC = df.getOWLObjectSomeValuesFrom(r, C);
        OWLObjectSomeValuesFrom rrB = df.getOWLObjectSomeValuesFrom(r, rB);

        OWLObjectIntersectionOf AandB = df.getOWLObjectIntersectionOf(A, B);
        OWLObjectIntersectionOf AandC = df.getOWLObjectIntersectionOf(A, C);
        OWLObjectIntersectionOf CandD = df.getOWLObjectIntersectionOf(C, D);
        OWLObjectIntersectionOf BandrC = df.getOWLObjectIntersectionOf(B, rC);
        OWLObjectIntersectionOf BandrB = df.getOWLObjectIntersectionOf(B, rB);
        OWLObjectIntersectionOf rAandB = df.getOWLObjectIntersectionOf(rA, B);
        OWLObjectIntersectionOf rrBandD = df.getOWLObjectIntersectionOf(rrB, D);
        OWLObjectSomeValuesFrom rexistsAandB = df.getOWLObjectSomeValuesFrom(r, AandB);

        OWLSubClassOfAxiom AsubclassofBandrC = df.getOWLSubClassOfAxiom(A, BandrC);
        OWLSubClassOfAxiom BandrBsubclassofCandD = df.getOWLSubClassOfAxiom(BandrB, CandD);
        OWLSubClassOfAxiom CsubclassofrAandB = df.getOWLSubClassOfAxiom(C, rAandB);
        OWLSubClassOfAxiom rrBandDsubclassofrexistsAandB = df.getOWLSubClassOfAxiom(rrBandD, rexistsAandB);

        OWLNamedIndividual individual = df.getOWLNamedIndividual(iri + "#IO");
        OWLObjectOneOf io = df.getOWLObjectOneOf(individual);
        OWLSubClassOfAxiom IOsubclassofaxiomC = df.getOWLSubClassOfAxiom(io, C);

        ontology.add(
            AsubclassofBandrC,
            BandrBsubclassofCandD,
            CsubclassofrAandB,
            rrBandDsubclassofrexistsAandB,
            IOsubclassofaxiomC
        );

        return ontology;
    }

    public static OWLOntology createOWLOntologyAWithoutCR6InferencesModified() {
        OWLOntology ontology = OntologyUtils.createEmptyOWLOntology();
        OWLOntologyManager owlOntologyManager = ontology.getOWLOntologyManager();
        OWLDataFactory df = owlOntologyManager.getOWLDataFactory();
        IRI iri = owlOntologyManager.getOntologyDocumentIRI(ontology);

        OWLClass A = df.getOWLClass(iri + "#A");
        OWLClass B = df.getOWLClass(iri + "#B");
        OWLClass C = df.getOWLClass(iri + "#C");
        OWLClass D = df.getOWLClass(iri + "#D");

        OWLObjectProperty r = df.getOWLObjectProperty(iri + "#r");

        OWLObjectSomeValuesFrom rA = df.getOWLObjectSomeValuesFrom(r, A);
        OWLObjectSomeValuesFrom rB = df.getOWLObjectSomeValuesFrom(r, B);
        OWLObjectSomeValuesFrom rC = df.getOWLObjectSomeValuesFrom(r, C);
        OWLObjectSomeValuesFrom rrB = df.getOWLObjectSomeValuesFrom(r, rB);

        OWLObjectIntersectionOf AandB = df.getOWLObjectIntersectionOf(A, B);
        OWLObjectIntersectionOf AandC = df.getOWLObjectIntersectionOf(A, C);
        OWLObjectIntersectionOf CandD = df.getOWLObjectIntersectionOf(C, D);
        OWLObjectIntersectionOf BandrC = df.getOWLObjectIntersectionOf(B, rC);
        OWLObjectIntersectionOf BandrB = df.getOWLObjectIntersectionOf(B, rB);
        OWLObjectIntersectionOf rAandB = df.getOWLObjectIntersectionOf(rA, B);
        OWLObjectIntersectionOf rrBandD = df.getOWLObjectIntersectionOf(rrB, D);
        OWLObjectSomeValuesFrom rexistsAandB = df.getOWLObjectSomeValuesFrom(r, AandB);
        OWLObjectSomeValuesFrom rexistsAandC = df.getOWLObjectSomeValuesFrom(r, AandC);

        OWLSubClassOfAxiom AsubclassofBandrC = df.getOWLSubClassOfAxiom(A, BandrC);
        OWLSubClassOfAxiom BandrBsubclassofCandD = df.getOWLSubClassOfAxiom(BandrB, CandD);
        OWLSubClassOfAxiom CsubclassofrAandB = df.getOWLSubClassOfAxiom(C, rAandB);
        OWLSubClassOfAxiom DsubclassofrexistsAandC = df.getOWLSubClassOfAxiom(D, rexistsAandC);
        OWLSubClassOfAxiom rrBandDsubclassofrexistsAandB = df.getOWLSubClassOfAxiom(rrBandD, rexistsAandB);

        OWLNamedIndividual individual = df.getOWLNamedIndividual(iri + "#IO");
        OWLObjectOneOf io = df.getOWLObjectOneOf(individual);
        OWLSubClassOfAxiom IOsubclassofaxiomC = df.getOWLSubClassOfAxiom(io, C);

        ontology.add(
            AsubclassofBandrC,
            BandrBsubclassofCandD,
            CsubclassofrAandB,
            rrBandDsubclassofrexistsAandB,
            IOsubclassofaxiomC,
            DsubclassofrexistsAandC
        );

        return ontology;
    }

    public static OWLOntology createOWLOntologyAWithAllInferences() {
        OWLOntology ontology = OntologyUtils.createEmptyOWLOntology();
        OWLOntologyManager owlOntologyManager = ontology.getOWLOntologyManager();
        OWLDataFactory df = owlOntologyManager.getOWLDataFactory();
        IRI iri = owlOntologyManager.getOntologyDocumentIRI(ontology);

        OWLClass A = df.getOWLClass(iri + "#A");
        OWLClass B = df.getOWLClass(iri + "#B");
        OWLClass C = df.getOWLClass(iri + "#C");
        OWLClass D = df.getOWLClass(iri + "#D");

        OWLObjectProperty r = df.getOWLObjectProperty(iri + "#r");

        OWLObjectSomeValuesFrom rA = df.getOWLObjectSomeValuesFrom(r, A);
        OWLObjectSomeValuesFrom rB = df.getOWLObjectSomeValuesFrom(r, B);
        OWLObjectSomeValuesFrom rC = df.getOWLObjectSomeValuesFrom(r, C);
        OWLObjectSomeValuesFrom rrB = df.getOWLObjectSomeValuesFrom(r, rB);

        OWLObjectIntersectionOf AandB = df.getOWLObjectIntersectionOf(A, B);
        OWLObjectIntersectionOf CandD = df.getOWLObjectIntersectionOf(C, D);
        OWLObjectIntersectionOf BandrC = df.getOWLObjectIntersectionOf(B, rC);
        OWLObjectIntersectionOf BandrB = df.getOWLObjectIntersectionOf(B, rB);
        OWLObjectIntersectionOf rAandB = df.getOWLObjectIntersectionOf(rA, B);
        OWLObjectIntersectionOf rrBandD = df.getOWLObjectIntersectionOf(rrB, D);
        OWLObjectSomeValuesFrom rexistsAandB = df.getOWLObjectSomeValuesFrom(r, AandB);

        OWLSubClassOfAxiom AsubclassofBandrC = df.getOWLSubClassOfAxiom(A, BandrC);
        OWLSubClassOfAxiom BandrBsubclassofCandD = df.getOWLSubClassOfAxiom(BandrB, CandD);
        OWLSubClassOfAxiom CsubclassofrAandB = df.getOWLSubClassOfAxiom(C, rAandB);
        OWLSubClassOfAxiom rrBandDsubclassofrexistsAandB = df.getOWLSubClassOfAxiom(rrBandD, rexistsAandB);

        OWLNamedIndividual individual = df.getOWLNamedIndividual(iri + "#IO");
        OWLObjectOneOf io = df.getOWLObjectOneOf(individual);
        OWLSubClassOfAxiom IOsubclassofaxiomC = df.getOWLSubClassOfAxiom(io, C);
        OWLSubClassOfAxiom CsubclassofIO = df.getOWLSubClassOfAxiom(C, io);
        OWLSubClassOfAxiom AsubclassofIO = df.getOWLSubClassOfAxiom(A, io);

        ontology.add(
            AsubclassofBandrC,
            BandrBsubclassofCandD,
            CsubclassofrAandB,
            rrBandDsubclassofrexistsAandB,
            IOsubclassofaxiomC,
            CsubclassofIO,
            AsubclassofIO
        );

        return ontology;
    }
}
