package de.hpi.patchr.vocab;

import com.hp.hpl.jena.ontology.ObjectProperty;
import com.hp.hpl.jena.ontology.OntModel;

public enum GuoObjectProperties {

	delete,
	insert,
	merge,
	target_graph,
	target_subject;

	String uri;

	GuoObjectProperties() {
		this.uri = "http://webr3.org/owl/guo#" + name();
	}

	public String getUri() {
		return uri;
	}

	@Override
	public String toString() {
		return "guo:" + name();
	}

	public ObjectProperty getObjectProperty(OntModel model) {
		return model.createObjectProperty(getUri());
	}

}
