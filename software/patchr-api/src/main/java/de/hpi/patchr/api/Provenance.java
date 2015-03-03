package de.hpi.patchr.api;

import java.util.Calendar;

import org.joda.time.DateTime;

import com.hp.hpl.jena.datatypes.xsd.XSDDatatype;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.vocabulary.RDFS;

import de.hpi.patchr.vocab.PatchrOntology;
import de.hpi.patchr.vocab.ProvOntology;

public class Provenance {

    public static final double DEFAULT_CONFIDENCE = .5;
    public static final double CONFIDENCE_SURE = .95;
    public static final double CONFIDENCE_HIGH = .75;
    public static final double CONFIDENCE_MEDIUM = .5;
    public static final double CONFIDENCE_LOW = .25;
    public static final double CONFIDENCE_UNSURE = .05;

    private String actor;
    private String performer;
    private Double confidence;
    private DateTime time;
    private String comment;

    @Deprecated
    public Provenance(String actor, String performer) {
        this(actor, performer, DEFAULT_CONFIDENCE);
    }

    public Provenance(String actor, String performer, double confidence) {
        this(actor, performer, null, confidence, new DateTime(Calendar.getInstance().getTime()));
    }

    public Provenance(String actor, String performer, String comment, double confidence) {
        this(actor, performer, comment, confidence, new DateTime(Calendar.getInstance().getTime()));
    }

    public Provenance(String actor, String performer, String comment, double confidence, DateTime time) {
        this.actor = actor;
        this.performer = performer;
        this.comment = comment;
        this.confidence = confidence;
        this.time = time;
    }

    public Resource getAsResource(Model model) {
        // TODO: do we need a URI or is blank node fine?
        Resource provenance = model.createResource(ProvOntology.Activity);

        provenance.addProperty(ProvOntology.atTime, model.createTypedLiteral(time, XSDDatatype.XSDdateTime));

        if (provenance != null)
            provenance.addProperty(PatchrOntology.confidence, model.createTypedLiteral(confidence, XSDDatatype.XSDdouble));
        if (performer != null)
            provenance.addProperty(ProvOntology.actedOnBehalfOf, model.createResource(performer));
        if (actor != null)
            provenance.addProperty(ProvOntology.wasAssociatedWith, model.createResource(actor));
        if (comment != null)
            provenance.addProperty(RDFS.comment, model.createLiteral(comment));

        return provenance;
    }


    public String getActor() {
        return actor;
    }

    public String getPerformer() {
        return performer;
    }

    public Double getConfidence() {
        return confidence;
    }

    public DateTime getTime() {
        return time;
    }

    public String getComment() {
        return comment;
    }
}
