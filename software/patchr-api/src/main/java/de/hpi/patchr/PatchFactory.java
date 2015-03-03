package de.hpi.patchr;

import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.ResourceFactory;
import de.hpi.patchr.api.Dataset;
import de.hpi.patchr.api.Patch;
import de.hpi.patchr.api.Provenance;
import de.hpi.patchr.api.UpdateInstruction;
import de.hpi.patchr.exceptions.InvalidUpdateInstructionException;
import de.hpi.patchr.utils.PrefixService;
import org.apache.log4j.Logger;

import java.util.HashSet;
import java.util.Set;

/**
 * A PatchFactory encapsulates the creation of a model of patch requests.
 * <p/>
 * Generates new IDs, so don't use it to re-create patches.
 *
 * @author magnus
 */
public class PatchFactory {

    private static Logger L = Logger.getLogger(PatchFactory.class);

    public static final double DEFAULT_CONFIDENCE = .5;

    protected String prefix;

    public PatchFactory(String prefix) {
        this.prefix = prefix;
    }

    /**
     * @param prefix
     * @param uri
     */
    public void addPrefix(String prefix, String uri) {
        PrefixService.addPrefix(prefix, uri);
    }

    /**
     * @param dataset
     * @param provenanceSet
     * @param update
     * @throws InvalidUpdateInstructionException
     */
    public Patch createPatch(Dataset dataset, Set<Provenance> provenanceSet, UpdateInstruction update, Patch.patchStatus status) throws InvalidUpdateInstructionException {

        Patch patch = new Patch(prefix, null, null, dataset, status, update, provenanceSet);

        return patch;
    }

    /**
     * @param dataset
     * @param provenance
     * @param update
     * @throws InvalidUpdateInstructionException
     */
    public Patch createPatch(Dataset dataset, Provenance provenance, UpdateInstruction update, Patch.patchStatus status) throws InvalidUpdateInstructionException {

        Set<Provenance> provenanceSet = new HashSet<Provenance>();
        provenanceSet.add(provenance);

        return createPatch(dataset, provenanceSet, update, status);
    }

    /**
     * @param dataset
     * @param provenanceSet
     * @param update
     * @throws InvalidUpdateInstructionException
     */
    public Patch createPatch(Dataset dataset, Set<Provenance> provenanceSet, UpdateInstruction update) throws InvalidUpdateInstructionException {

        return createPatch(dataset, provenanceSet, update, Patch.patchStatus.OPEN);
    }

    /**
     * @param dataset
     * @param provenance
     * @param update
     * @throws InvalidUpdateInstructionException
     */
    public Patch createPatch(Dataset dataset, Provenance provenance, UpdateInstruction update) throws InvalidUpdateInstructionException {

        return createPatch(dataset, provenance, update, Patch.patchStatus.OPEN);
    }

    /**
     * @param dataset
     * @param provenance
     * @param subject
     * @param deleteProperty
     * @param deleteObject
     * @param insertProperty
     * @param insertObject
     * @throws InvalidUpdateInstructionException
     */
    public Patch createPatch(Dataset dataset, Provenance provenance, Resource subject, Property deleteProperty, RDFNode deleteObject, Property insertProperty, RDFNode insertObject) throws InvalidUpdateInstructionException {

        UpdateInstruction update = new UpdateInstruction(dataset.getSparqlGraph(), ResourceFactory.createStatement(subject, deleteProperty, deleteObject), ResourceFactory.createStatement(subject, insertProperty, insertObject));
        Patch patch = new Patch(prefix, null, null, dataset, Patch.patchStatus.OPEN, update, provenance);

        return patch;
    }
}
