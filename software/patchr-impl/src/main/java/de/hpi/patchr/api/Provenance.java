package de.hpi.patchr.api;

import java.util.Calendar;
import java.util.Date;

import org.joda.time.DateTime;

import com.hp.hpl.jena.datatypes.xsd.XSDDatatype;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Resource;

import de.hpi.patchr.vocab.PatchrOntology;
import de.hpi.patchr.vocab.ProvOntology;

public class Provenance {

	private static final double DEFAULT_CONFIDENCE = 1.;
	
	private String actor;
	private String performer;
	private Double confidence;

	public Provenance() {
	}
	
	@Deprecated
	public Provenance(String actor, String performer) {
		this(actor, performer, DEFAULT_CONFIDENCE);
	}
	
	public Provenance(String actor, String performer, double confidence) {
		this.actor = actor;
		this.performer = performer;
		this.confidence = confidence;
	}
	
	public Resource getAsResource(Model model) {
		Resource provenance = model.createResource(ProvOntology.Activity);

		Date now = Calendar.getInstance().getTime();
		provenance.addProperty(ProvOntology.atTime, model.createTypedLiteral(new DateTime(now), XSDDatatype.XSDdateTime));
		
		provenance.addProperty(PatchrOntology.confidence, model.createTypedLiteral(confidence, XSDDatatype.XSDdouble));
		
		if (performer != null)
			provenance.addProperty(ProvOntology.wasAssociatedWith, model.createResource(performer));
		if (actor != null)
			provenance.addProperty(ProvOntology.wasAssociatedWith, model.createResource(actor));
		
		return provenance;
	}

}
