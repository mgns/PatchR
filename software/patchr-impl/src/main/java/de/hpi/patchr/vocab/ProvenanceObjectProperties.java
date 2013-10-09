package de.hpi.patchr.vocab;

import com.hp.hpl.jena.ontology.ObjectProperty;
import com.hp.hpl.jena.ontology.OntModel;

public enum ProvenanceObjectProperties {

	containedBy,
	deployedSoftware,
	serializedBy,
	performedBy,
	operatedBy,
	yieldedBy,
	involvedActor,
	employedArtifact,
	createdBy,
	usedData,
	usedGuideline,
	precededBy,
	retrievedBy,
	accessedResource,
	accessedService,
	usedBy;
	
	String uri;

	ProvenanceObjectProperties() {
		this.uri = "http://purl.org/net/provenance/ns#" + name();
	}

	public String getUri() {
		return uri;
	}

	@Override
	public String toString() {
		return "prv:" + name();
	}

	public ObjectProperty getObjectProperty(OntModel model) {
		return model.createObjectProperty(getUri());
	}

}
