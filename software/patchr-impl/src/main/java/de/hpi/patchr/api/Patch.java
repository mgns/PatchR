package de.hpi.patchr.api;

import java.util.Random;

import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.rdf.model.Resource;

import de.hpi.patchr.vocab.PatchrDatatypeProperties;
import de.hpi.patchr.vocab.PatchrObjectProperties;
import de.hpi.patchr.vocab.PatchrOntClasses;

public class Patch {

	public static enum UPDATE_ACTION {insert, delete};
	public static enum UPDATE_STATUS {active, resolved};
	
	private final OntModel model;
	private String prefix;

	private Resource patch;
	
	private Resource update;

	private Resource dataset;
	private Resource provenance;
	
	public Patch(OntModel inputModel, String prefix, Resource dataset, Resource provenance) {
		this.model = inputModel;
		this.prefix = prefix;
		
		this.dataset = dataset;
		this.provenance = provenance;
		
        String uri = generateURI();
		patch = model.createIndividual(uri, PatchrOntClasses.Patch.getOntClass(model));
		patch.addProperty(PatchrDatatypeProperties.status.getDatatypeProperty(model), UPDATE_STATUS.active.name());
		
		if (dataset != null)
			patch.addProperty(PatchrObjectProperties.appliesTo.getObjectProperty(model), dataset);
		if (provenance != null)
			patch.addProperty(PatchrObjectProperties.provenance.getObjectProperty(model), provenance);
		
	}
	
	private String generateURI() {
		StringBuilder uri = new StringBuilder();
		uri.append(prefix);
		uri.append("Patch");
		Random r = new Random(0);
		uri.append(r.nextInt());
		
		return uri.toString();
	}

	public OntModel getModel() {
        return model;
    }

	@Override
	public String toString() {
		return toString();
	}
	
}
