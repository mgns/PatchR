package de.hpi.patchr.api;

import java.util.Random;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Resource;

import de.hpi.patchr.vocab.PatchrOntology;

public class Patch {

	public static enum UPDATE_ACTION {insert, delete};
	public static enum UPDATE_STATUS {active, resolved};
	
	private final Model model;
	private String prefix;

	private Resource patch;
	
	private Resource update;

	private Resource dataset;
	private Resource provenance;
	
	public Patch(Model inputModel, String prefix, Resource dataset, Resource provenance) {
		this.model = inputModel;
		this.prefix = prefix;
		
		this.dataset = dataset;
		this.provenance = provenance;
		
        String uri = generateURI();
		patch = model.createResource(uri, PatchrOntology.Patch);
		patch.addProperty(PatchrOntology.status, UPDATE_STATUS.active.name());
		
		if (dataset != null)
			patch.addProperty(PatchrOntology.appliesTo, dataset);
		if (provenance != null)
			patch.addProperty(PatchrOntology.provenance, provenance);
		
	}
	
	private String generateURI() {
		StringBuilder uri = new StringBuilder();
		uri.append(prefix);
		uri.append("Patch");
		Random r = new Random(0);
		uri.append(r.nextInt());
		
		return uri.toString();
	}

	public Model getModel() {
        return model;
    }

	@Override
	public String toString() {
		return toString();
	}
	
}
