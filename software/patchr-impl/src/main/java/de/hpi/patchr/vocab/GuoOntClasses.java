package de.hpi.patchr.vocab;

import com.hp.hpl.jena.ontology.OntClass;
import com.hp.hpl.jena.ontology.OntModel;

public enum GuoOntClasses {

	UpdateInstruction;
	
	String uri;

	GuoOntClasses() {
		this.uri = "http://webr3.org/owl/guo#" + name();
	}

	public String getUri() {
		return uri;
	}

	@Override
	public String toString() {
		return "guo:" + name();
	}

	public OntClass getOntClass(OntModel model) {
		return model.createClass(getUri());
	}

}
