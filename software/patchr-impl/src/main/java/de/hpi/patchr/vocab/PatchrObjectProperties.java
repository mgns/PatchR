package de.hpi.patchr.vocab;

import com.hp.hpl.jena.ontology.ObjectProperty;
import com.hp.hpl.jena.ontology.OntModel;

public enum PatchrObjectProperties {

	provenance,
	update,
	memberOf,
	appliesTo,
	advocate,
	criticiser,
	patchType;
	
	String uri;

	PatchrObjectProperties() {
		this.uri = "http://purl.org/hpi/patchr#" + name();
	}

	public String getUri() {
		return uri;
	}

	@Override
	public String toString() {
		return "pat:" + name();
	}

	public ObjectProperty getObjectProperty(OntModel model) {
		return model.createObjectProperty(getUri());
	}
	
}
