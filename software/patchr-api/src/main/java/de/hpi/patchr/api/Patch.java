package de.hpi.patchr.api;

import java.util.Collection;
import java.util.HashSet;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Resource;

import de.hpi.patchr.utils.PatchrUtils;
import de.hpi.patchr.vocab.PatchrOntology;
import de.hpi.patchr.vocab.ProvOntology;

/**
 * @author magnus
 */
public class Patch {

    public static enum patchStatus {
        OBSOLETE, OPEN, POSTPONED, REJECTED, RESOLVED
    };

    private String prefix;
    private String id;
    private String comment;
    private Dataset dataset;
    private patchStatus status;
    private UpdateInstruction update;
    private Collection<Provenance> provenance;

    private Patch() {
    }

    public Patch(String prefix, String id, String comment, Dataset dataset, patchStatus status, UpdateInstruction update, Provenance provenance) {
        this();

        this.prefix = prefix;
        this.id = id == null ? PatchrUtils.generateId("patch") : id;
        this.comment = comment;
        this.dataset = dataset;
        this.status = status;
        this.update = update;
        this.provenance = new HashSet<Provenance>();
        this.addProvenance(provenance);
    }

    public Patch(String prefix, String id, String comment, Dataset dataset, patchStatus status, UpdateInstruction update, Collection<Provenance> provenance) {
        this();

        this.prefix = prefix;
        this.id = id == null ? PatchrUtils.generateId("patch") : id;
        this.comment = comment;
        this.dataset = dataset;
        this.status = status;
        this.update = update;
        this.provenance = provenance == null ? new HashSet<Provenance>() : provenance;
    }

    public Resource getAsResource(Model model) {
        //String uri = PatchrUtils.generateUri(prefix, "patch");
        Resource patch = model.createResource(this.prefix + this.id, PatchrOntology.Patch);

        if (status != null)
            patch.addProperty(PatchrOntology.status, getPatchrStatusAsResource(status));
        if (dataset != null)
            patch.addProperty(PatchrOntology.appliesTo, dataset.getAsResource(model));
        if (update != null)
            patch.addProperty(PatchrOntology.update, update.getAsResource(model));

        if (comment != null)
            patch.addProperty(PatchrOntology.comment, comment);
        if (provenance != null)
            for (Provenance prov : provenance) {
                patch.addProperty(ProvOntology.wasGeneratedBy, prov.getAsResource(model));
            }
        // TODO which one is better?
        // patch.addProperty(PatchrOntology.provenance, provenance.getAsResource(model));
        // patch.addProperty(ProvOntology.wasGeneratedBy, provenance.getAsResource(model));

/*		for (String p : proposers) {
            patch.addProperty(PatchrOntology.advocate, model.createResource(p));
		}
		for (String p : protesters) {
			patch.addProperty(PatchrOntology.criticiser, model.createResource(p));
		}
*/
        return patch;
    }

    private Resource getPatchrStatusAsResource(patchStatus status) {
        switch (status) {
            case OBSOLETE:
                return PatchrOntology.Obsolete;
            case OPEN:
                return PatchrOntology.Open;
            case POSTPONED:
                return PatchrOntology.Postponed;
            case REJECTED:
                return PatchrOntology.Rejected;
            case RESOLVED:
                return PatchrOntology.Resolved;
        }
        // default
        return PatchrOntology.Open;
    }

    public String getPrefix() {
        return prefix;
    }

    public void setPrefix(String prefix) {
        this.prefix = prefix;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public Dataset getDataset() {
        return dataset;
    }

    public void setDataset(Dataset dataset) {
        this.dataset = dataset;
    }

    public patchStatus getStatus() {
        return status;
    }

    public void setStatus(patchStatus status) {
        this.status = status;
    }

    public Collection<Provenance> getProvenance() {
        return provenance;
    }

    public void setProvenance(Collection<Provenance> provenance) {
        this.provenance = provenance;
    }

    public void addProvenance(Provenance provenance) {
        // TODO update provenance in case its the same actor
        this.provenance.add(provenance);
    }

    public UpdateInstruction getUpdate() {
        return this.update;
    }

    public void setUpdate(UpdateInstruction update) {
        this.update = update;
    }

    /*
     * cf knuth-m-2014-data: Data Cleansing Consolidation with PatchR, ESWC, 2014
     */
    public double getAggregatedConfidence() {
        double aggConf = Double.MAX_VALUE;

        for (Provenance prov : getProvenance()) {
            if (aggConf == Double.MAX_VALUE)
                aggConf = prov.getConfidence();
            else
                aggConf = 1. - ((1. - aggConf) * (1. - prov.getConfidence()));
        }

        return aggConf;
    }

    @Override
    public String toString() {
        return getAsResource(ModelFactory.createDefaultModel()).toString();
    }

}
