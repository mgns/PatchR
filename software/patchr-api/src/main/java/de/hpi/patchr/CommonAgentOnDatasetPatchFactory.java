package de.hpi.patchr;

import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.rdf.model.*;
import de.hpi.patchr.api.Dataset;
import de.hpi.patchr.api.Patch;
import de.hpi.patchr.api.Provenance;
import de.hpi.patchr.api.UpdateInstruction;
import de.hpi.patchr.exceptions.InvalidUpdateException;
import de.hpi.patchr.exceptions.InvalidUpdateInstructionException;
import org.aksw.jena_sparql_api.core.QueryExecutionFactory;
import org.apache.log4j.Logger;

/**
 * A CommonAgentOnDatasetPatchFactory encapsulates the creation of a model of patch requests with a
 * common agent and dataset.
 *
 * @author magnus
 */
public class CommonAgentOnDatasetPatchFactory extends PatchFactory {

    private static Logger L = Logger.getLogger(CommonAgentOnDatasetPatchFactory.class);

    private String performerUri;
    private String actorUri;
    private Dataset dataset;

    public CommonAgentOnDatasetPatchFactory(String prefix) {
        super(prefix);
    }

    public CommonAgentOnDatasetPatchFactory(String prefix, String actorUri, String performerUri) {
        this(prefix);

        this.actorUri = actorUri;
        this.performerUri = performerUri;
    }

    public CommonAgentOnDatasetPatchFactory(String prefix, String actorUri, String performerUri, Dataset dataset) {
        this(prefix, actorUri, performerUri);

        this.dataset = dataset;
    }

    /**
     * @param subject
     * @param deleteProperty
     * @param deleteObject
     * @param insertProperty
     * @param insertObject
     * @param confidence
     * @throws InvalidUpdateInstructionException
     */
    public Patch createPatch(Resource subject, Property deleteProperty, RDFNode deleteObject, Property insertProperty, RDFNode insertObject, double confidence) throws InvalidUpdateInstructionException {

        UpdateInstruction update = new UpdateInstruction(
                dataset.getSparqlGraph(),
                (deleteProperty != null && deleteObject != null ? ResourceFactory.createStatement(subject, deleteProperty, deleteObject) : null),
                (insertProperty != null && insertObject != null ? ResourceFactory.createStatement(subject, insertProperty, insertObject) : null));
        Provenance prov = new Provenance(actorUri, performerUri, confidence);
        Patch patch = new Patch(prefix, null, null, dataset, Patch.patchStatus.OPEN, update, prov);

        return patch;
    }

    public Patch createPatch(Resource subject, Property deleteProperty, RDFNode deleteObject, Property insertProperty, RDFNode insertObject) throws InvalidUpdateInstructionException {
        return createPatch(subject, deleteProperty, deleteObject, insertProperty, insertObject, DEFAULT_CONFIDENCE);
    }

    /*
     * 	public void addPatchRequest(Patch.updateAction action, Resource subject, Property property, RDFNode object) {
            addPatchRequest(action, subject, property, object, DEFAULT_CONFIDENCE);
        }

        public void addPatchRequest(Patch.updateAction action, Resource subject, Property property, RDFNode object, double confidence) {
            Model patchModel = createPatch(action, subject, property, object, confidence);

            // TODO: check for equivalent patches and add provenance if necessary
            // TODO: check for patches of the same actor and update confidence if
            // necessary
            if (modelContains(patchModel)) {
                L.info("Patch exists.");
            } else {
                this.model.add(patchModel);
            }
        }
    */
    public boolean validatePatch(Patch patch) {

        try {
            for (Statement s : patch.getUpdate().getDeleteStatements()) {
                validateDeletion(s.getSubject(), s.getPredicate(), s.getObject());
            }
            for (Statement s : patch.getUpdate().getInsertStatements()) {
                validateInsertion(s.getSubject(), s.getPredicate(), s.getObject());
            }
        } catch (InvalidUpdateInstructionException e) {
            L.error(e);
            return false;
        } catch (InvalidUpdateException e) {
            L.warn(e);
            return false;
        }

        return true;
    }

    private void validateInsertion(Resource subject, Property property, RDFNode rdfNode) throws InvalidUpdateException {
        if (tripleExists(subject, property, rdfNode)) {
            throw new InvalidUpdateException("Triple <" + subject + "> <" + property + "> <" + rdfNode + "> already exists.");
        }
        L.info("Insertion of triple <" + subject + "> <" + property + "> <" + rdfNode + "> validated.");
    }

    private void validateDeletion(Resource subject, Property property, RDFNode rdfNode) throws InvalidUpdateException {
        if (!tripleExists(subject, property, rdfNode)) {
            throw new InvalidUpdateException("Triple <" + subject + "> <" + property + "> <" + rdfNode + "> does not exist.");
        }
        L.info("Deletion of triple <" + subject + "> <" + property + "> <" + rdfNode + "> validated.");
    }

    private boolean tripleExists(Resource subject, Property property, RDFNode rdfNode) {
        // Gives a NPE
        // String queryString = "ASK { <" + subject + "> <" + property + "> <" +
        // object + "> . }";
        // L.info(queryString);
        //
        // QueryExecutionFactory qef = dataset.getQueryExecutionFactory();
        // QueryExecution qe = qef.createQueryExecution(queryString);
        //
        // return qe.execAsk();

        // TODO that is a workaround
        String queryString = "SELECT ?o WHERE { <" + subject + "> <" + property + "> ?o . FILTER (?o = <" + rdfNode + ">) }";
        L.info(queryString);

        QueryExecutionFactory qef = dataset.getQueryExecutionFactory();
        QueryExecution qe = qef.createQueryExecution(queryString);

        return qe.execSelect().hasNext();
    }

}
