package indexing;

import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLObjectPropertyExpression;

public record RoleAndFiller(OWLObjectPropertyExpression role, OWLClassExpression filler) {}
