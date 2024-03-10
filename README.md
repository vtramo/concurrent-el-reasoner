# Concurrent Classification of EL++ Ontologies
This project implements a Reasoner for a simplified version of the EL++ logic, with a focus on computing the 
classification of a TBox (subsumption hierarchy). The Reasoner is developed in Java, utilizing the [owlapi](https://github.com/owlcs/owlapi).

This project is based on the following articles:
- [Pushing the EL Envelope Further](https://lat.inf.tu-dresden.de/research/papers/2005/BaaderBrandtLutz-IJCAI-05.pdf)
- [Concurrent Classification of EL Ontologies](https://iccl.inf.tu-dresden.de/w/images/c/c3/Kazakov-Kroetzsch-Simancik_concurrent-el-reasoning_ISWC2011.pdf)
- [ELK Reasoner: Architecture and Evaluation](https://ceur-ws.org/Vol-858/ore2012_paper10.pdf)

## Usage example (working in progress)
```java
public static void main(String[] args) {
    
    OWLOntology ontology;
    
    // TBox normalisation
    OntologyNormaliser ontologyNormaliser = new OntologyNormaliser(ontology);
    OWLOntology normalisedOntology = ontologyNormaliser.createNormalisedOntology();
    
    // Ontology indexing
    OntologyIndexer ontologyIndexer = new OntologyIndexer(normalisedOntology);
    OntologyIndex ontologyIndex = ontologyIndexer.buildIndex();
    
    // Ontology saturation process
    OntologySaturationProcess ontologySaturationProcess = new OntologySaturationProcess(normalisedOntology, ontologyIndex);
    SaturationResult saturationResult = ontologySaturationProcess.saturate();
    
    // Build subsumption hierarchy
    SubsumptionHierarchyProcess subsumptionHierarchyProcess = new SubsumptionHierarchyProcess();
    SubsumptionHierarchy subsumptionHierarchy = subsumptionHierarchyProcess.buildHierarchy(saturationResult);
    
}
```
- **TODO**: The reasoner will be compatible with owl api (using OWLReasoner interface)