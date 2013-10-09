package de.hpi.patchr.vocab;

import com.hp.hpl.jena.ontology.OntClass;
import com.hp.hpl.jena.ontology.OntModel;

public enum ProvenanceOntClasses {

	DataItem,
	File,
	Immutable,
	HumanAgent,
	DataCreation,
	CreationGuideline,
	DataAccess,
	DataProvidingService,
	DataPublisher,
	Actor,
	HumanActor,
	NonHumanActor,
	Execution,
	Artifact;
	
	String uri;

	ProvenanceOntClasses() {
		this.uri = "http://purl.org/net/provenance/ns#" + name();
	}

	public String getUri() {
		return uri;
	}

	@Override
	public String toString() {
		return "prv:" + name();
	}

	public OntClass getOntClass(OntModel model) {
		return model.createClass(getUri());
	}
	
}
