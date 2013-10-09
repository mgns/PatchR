package de.hpi.patchr.vocab;

import com.hp.hpl.jena.ontology.OntClass;
import com.hp.hpl.jena.ontology.OntModel;

public enum PatchrOntClasses {

	Patch,
	PatchGroup;

	String uri;

	PatchrOntClasses() {
		this.uri = "http://purl.org/hpi/patchr#" + name();
	}

	public String getUri() {
		return uri;
	}

	@Override
	public String toString() {
		return "pat:" + name();
	}

	public OntClass getOntClass(OntModel model) {
		return model.createClass(getUri());
	}
}
