package de.hpi.patchr.vocab;

import com.hp.hpl.jena.ontology.DatatypeProperty;
import com.hp.hpl.jena.ontology.ObjectProperty;
import com.hp.hpl.jena.ontology.OntModel;

public enum PatchrDatatypeProperties {

	comment, status;

	String uri;

	PatchrDatatypeProperties() {
		this.uri = "http://purl.org/hpi/patchr#" + name();
	}

	public String getUri() {
		return uri;
	}

	@Override
	public String toString() {
		return "pat:" + name();
	}

	public DatatypeProperty getDatatypeProperty(OntModel model) {
		return model.createDatatypeProperty(getUri());
	}
}
