package saturation.context;

import org.semanticweb.owlapi.model.OWLSubClassOfAxiom;

import java.util.Collection;

public interface ContextAllocator {

    Collection<Context> initContexts();
    Collection<Context> getContexts(OWLSubClassOfAxiom axiom);

}
