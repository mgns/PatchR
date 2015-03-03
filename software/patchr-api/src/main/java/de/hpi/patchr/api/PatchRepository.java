package de.hpi.patchr.api;

import org.apache.log4j.Logger;

import java.util.List;

public abstract class PatchRepository {

    public static Logger L = Logger.getLogger(PatchRepository.class);

    public static final String DEFAULT_PREFIX = "http://patchr.s16a.org";

    //common prefix
    protected String prefix;

    public PatchRepository(String prefix) {
        this.prefix = prefix;
    }

    /**
     * Submit patch to repository and return its id (prefix + id = uri).
     *
     * @param patch
     * @return the patch id if found, else null
     */
    public abstract String submitPatch(Patch patch);

    /**
     * Find patch in repository and return its id (prefix + id = uri).
     *
     * @param patch
     * @return the patch id if found, else null
     */
    public abstract String findPatchId(Patch patch);

    /**
     * Get all datasets contained.
     *
     * @return a list of datasets
     */
    public abstract List<Dataset> getDatasets();

    /**
     * Get all patches contained for a given dataset.
     *
     * @param dataset
     * @return a list of patches
     */
    public abstract List<Patch> getPatches(Dataset dataset);

    /**
     * Get a patch by its Id.
     *
     * @param id
     * @return a patch or null if none has this id
     */
    public abstract Patch getPatch(String id);

    public String getPrefix() {
        return prefix;
    }
}
