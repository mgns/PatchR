package de.hpi.patchr.api;

import java.util.HashSet;
import java.util.Set;

import com.hp.hpl.jena.datatypes.xsd.XSDDatatype;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;

import de.hpi.patchr.utils.PatchrUtils;
import de.hpi.patchr.vocab.PatchrOntology;

/**
 * @author magnus
 */
public class Patch {

	public static enum UPDATE_ACTION {
		insert, delete
	};

	public static enum UPDATE_STATUS {
		active, resolved
	};

	private String prefix;
	private String id;
	private String comment;
	private Dataset dataset;
	private Double confidence;
	private UPDATE_STATUS status;
	private UpdateInstruction update;
	private Provenance provenance;

	private Set<String> proposers;
	private Set<String> protesters;

	private Patch() {
		proposers = new HashSet<String>();
		protesters = new HashSet<String>();
	}

	public Patch(String prefix, String id, String comment, Dataset dataset, Double confidence, Patch.UPDATE_STATUS status, UpdateInstruction update, Provenance provenance) {
		this();

		this.prefix = prefix;
		this.id = id;
		this.comment = comment;
		this.dataset = dataset;
		this.confidence = confidence;
		this.status = status;
		this.update = update;
		this.provenance = provenance;
	}

	public void addProposer(String actor) {
		proposers.add(actor);
	}

	public void addProtester(String actor) {
		protesters.add(actor);
	}

	public Resource getAsResource(Model model) {
		String uri = PatchrUtils.generateUri(prefix, "patch");
		Resource patch = model.createResource(uri, PatchrOntology.Patch);
		patch.addProperty(PatchrOntology.status, status.name());
		patch.addProperty(PatchrOntology.appliesTo, dataset.getAsResource(model));
		patch.addProperty(PatchrOntology.update, update.getAsResource(model));

		if (comment != null)
			patch.addProperty(PatchrOntology.comment, comment);
		if (confidence != null)
			patch.addProperty(PatchrOntology.confidence, model.createTypedLiteral(confidence, XSDDatatype.XSDdouble));
		if (provenance != null)
			patch.addProperty(PatchrOntology.provenance, provenance.getAsResource(model));
		// TODO which one is better?
		// patch.addProperty(ProvOntology.wasGeneratedBy,
		// provenance.getAsResource(model));

		for (String p : proposers) {
			patch.addProperty(PatchrOntology.advocate, model.createResource(p));
		}
		for (String p : protesters) {
			patch.addProperty(PatchrOntology.criticiser, model.createResource(p));
		}

		return patch;
	}

	@Override
	public String toString() {
		return toString();
	}

}
