package de.hpi.patchr.vocab;

import com.hp.hpl.jena.ontology.DatatypeProperty;
import com.hp.hpl.jena.ontology.OntModel;

public enum ProvenanceDatatypeProperties {

	completedAt,
	performedAt;
	
	String uri;

	ProvenanceDatatypeProperties() {
		this.uri = "http://purl.org/net/provenance/ns#" + name();
	}

	public String getUri() {
		return uri;
	}

	@Override
	public String toString() {
		return "prv:" + name();
	}

	public DatatypeProperty getDatatypeProperty(OntModel model) {
		return model.createDatatypeProperty(getUri());
	}

}
