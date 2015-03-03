package de.hpi.patchr;

import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.*;
import de.hpi.patchr.api.*;
import de.hpi.patchr.exceptions.InvalidUpdateInstructionException;
import de.hpi.patchr.utils.PrefixService;
import org.aksw.jena_sparql_api.core.QueryExecutionFactory;
import org.aksw.jena_sparql_api.model.QueryExecutionFactoryModel;
import org.joda.time.DateTime;

import java.util.ArrayList;
import java.util.Collection;

/**
 * Created by magnus on 26.01.15.
 */
public final class PatchUtils {

    private PatchUtils() {
    }

    public static Collection<Patch> instantiatePatchesFromModel(Model model) {
        QueryExecutionFactory queryFactory = new QueryExecutionFactoryModel(model);

        return instantiatePatchesFromModel(queryFactory);
    }

    /**
     * Takes a QEF and tries to instantiate all defined patches in that QEF
     *
     * @param queryFactory the query factory
     * @return the collection
     */
    public static Collection<Patch> instantiatePatchesFromModel(QueryExecutionFactory queryFactory) {

        final String sparqlSelectPatches = PrefixService.getSparqlPrefixDecl() +
                "SELECT DISTINCT ?patch WHERE {\n" +
                "  ?patch a pat:Patch .\n" +
                "}";

        Collection<String> patchURIs = new ArrayList<>();

        QueryExecution qe = null;
        try {
            qe = queryFactory.createQueryExecution(sparqlSelectPatches);
            ResultSet results = qe.execSelect();

            while (results.hasNext()) {
                QuerySolution qs = results.next();
                Resource patch = qs.get("patch").asResource();

                if (patch.isURIResource())
                    patchURIs.add(patch.getURI());
                else if (patch.isAnon())
                    patchURIs.add("_:B" + patch.getId().toString());
            }
        } finally {
            if (qe != null) {
                qe.close();
            }
        }

        Collection<Patch> patches = new ArrayList<>();
        for (String patchURI : patchURIs) {
            patches.add(getPatch(queryFactory, patchURI));
        }

        return patches;
    }

    public static Patch getPatch(Model model, String patchURI) {
        QueryExecutionFactory queryFactory = new QueryExecutionFactoryModel(model);
        return getPatch(queryFactory, patchURI);
    }

    /**
     * Given a QueryExecutionFactory and a patch URI, it instantiates a new Patch
     *
     * @param queryFactory the query factory
     * @param patchURI     the patch URI
     * @return the patch object or null if patch not found
     */
    public static Patch getPatch(QueryExecutionFactory queryFactory, String patchURI) {
        final String sparqlSelect = PrefixService.getSparqlPrefixDecl() +
                "SELECT DISTINCT ?id ?desc ?datasetURI ?status WHERE { " +
                "  %%PATCHURI%% a pat:Patch ; " +
                "pat:appliesTo ?datasetURI . " +
                "  OPTIONAL { %%PATCHURI%% pat:status ?status . } " +
                "  OPTIONAL { %%PATCHURI%% dcterms:identifier ?id . } " +
                "  OPTIONAL { %%PATCHURI%% dcterms:description ?desc . } " +
                "}";

        PatchRepository.L.info(sparqlSelect.replaceAll("%%PATCHURI%%", "<" + patchURI + ">"));

        Patch patch = null;
        QueryExecution qe = null;
        try {
            qe = queryFactory.createQueryExecution(sparqlSelect.replaceAll("%%PATCHURI%%", "<" + patchURI + ">"));
            ResultSet results = qe.execSelect();

            if (results.hasNext()) {
                QuerySolution qs = results.next();

                String datasetURI = qs.get("datasetURI").toString();
                Patch.patchStatus status = Patch.patchStatus.OPEN;
                //TODO valueOf(qs.get("status").toString().replace(PatchrOntology.NS, ""));
                // OPTIONAL
                String id = qs.get("id") == null ? null : qs.get("id").toString();
                String desc = qs.get("desc") == null ? null : qs.get("desc").toString();

                //TODO bad fix
                if (id == null) {
                    String[] ids = patchURI.split("/");
                    id = ids[ids.length - 1];
                }
                String prefix = getPatchPrefix(patchURI, id);

                // Get patch provenances
                Collection<Provenance> provenances = getPatchProvenances(queryFactory, patchURI);

                // Get patch update
                UpdateInstruction update = getPatchUpdate(queryFactory, patchURI);

                // Get patch dataset
                Dataset dataset = getPatchDataset(queryFactory, patchURI);

                patch = new Patch(prefix, id, desc, dataset, status, update, provenances);

                // if not valid OR if multiple results returns something is wrong
                //if (!patch.isValid() || results.hasNext()) {
                if (results.hasNext()) {
                    throw new IllegalArgumentException("Patch " + patchURI + " not valid: ambiguous.");
                }
            }
        } finally {
            if (qe != null) {
                qe.close();
            }
        }

        return patch;
    }

    private static Collection<Provenance> getPatchProvenances(QueryExecutionFactory queryFactory, String patchURI) {
        final String sparqlSelect = PrefixService.getSparqlPrefixDecl() +
                "SELECT DISTINCT ?confidence ?time ?actor ?performer ?comment WHERE { "
                + "  %%PATCHURI%% prov:wasGeneratedBy ?provenance . "
                + "  ?provenance pat:confidence ?confidence ; "
                + "prov:atTime ?time ; "
                + "prov:wasAssociatedWith ?actor . "
                + "  OPTIONAL { ?provenance prov:actedOnBehalfOf ?performer . } "
                + "  OPTIONAL { ?provenance rdfs:comment ?comment . } "
                + "}";

        PatchRepository.L.info(sparqlSelect.replaceAll("%%PATCHURI%%", "<" + patchURI + ">"));

        Collection<Provenance> provenances = new ArrayList<>();
        QueryExecution qe = null;
        try {
            qe = queryFactory.createQueryExecution(sparqlSelect.replaceAll("%%PATCHURI%%", "<" + patchURI + ">"));
            ResultSet results = qe.execSelect();

            while (results.hasNext()) {
                QuerySolution qs = results.next();

                Double confidence = qs.get("confidence").asLiteral().getDouble();
                DateTime time = DateTime.parse(qs.get("time").asLiteral().getValue().toString());
                String actor = qs.get("actor").toString();
                //OPTIONAL
                String performer = qs.get("performer") == null ? null : qs.get("performer").toString();
                String comment = qs.get("comment") == null ? null : qs.get("comment").toString();

                if (performer == null) {
                    performer = actor;
                }

                Provenance provenance = new Provenance(actor, performer, comment, confidence, time);
                provenances.add(provenance);
            }
        } finally {
            if (qe != null) {
                qe.close();
            }
        }

        return provenances;
    }

    private static UpdateInstruction getPatchUpdate(QueryExecutionFactory queryFactory, String patchURI) {
        final String sparqlSelect = PrefixService.getSparqlPrefixDecl() +
                "SELECT ?graph ?subject ?deleteGraph ?insertGraph WHERE { " +
                "  %%PATCHURI%% pat:update ?update . " +
                "  ?update guo:target_graph ?graph ; " +
                "guo:target_subject ?subject . " +
                "  OPTIONAL { ?update guo:delete ?deleteGraph . } " +
                "  OPTIONAL { ?update guo:insert ?insertGraph . } " +
                "}";

        PatchRepository.L.info(sparqlSelect.replaceAll("%%PATCHURI%%", "<" + patchURI + ">"));

        UpdateInstruction update = null;
        QueryExecution qe = null;
        try {
            qe = queryFactory.createQueryExecution(sparqlSelect.replaceAll("%%PATCHURI%%", "<" + patchURI + ">"));
            ResultSet results = qe.execSelect();

            while (results.hasNext()) {
                QuerySolution qs = results.next();

                String graph = qs.get("graph").toString();
                Resource subject = qs.get("subject").asResource();
                // OPTIONAL
                String deleteGraph = qs.get("deleteGraph") == null ? null : qs.get("deleteGraph").toString();
                String insertGraph = qs.get("insertGraph") == null ? null : qs.get("insertGraph").toString();

                if (results.hasNext()) {
                    throw new IllegalArgumentException("Patch " + patchURI + " not valid: multiple updates.");
                }
                if (deleteGraph == null && insertGraph == null) {
                    throw new IllegalArgumentException("Patch " + patchURI + " not valid: no update actions.");
                }

                Collection<Statement> deleteStatements = getPatchUpdateStatements(queryFactory, patchURI, "guo:delete");
                Collection<Statement> insertStatements = getPatchUpdateStatements(queryFactory, patchURI, "guo:insert");

                try {
                    update = new UpdateInstruction(graph, subject, deleteStatements, insertStatements);
                } catch (InvalidUpdateInstructionException ex) {
                    PatchRepository.L.error(ex);
                }
            }
        } finally {
            if (qe != null) {
                qe.close();
            }
        }

        return update;
    }

    private static Collection<Statement> getPatchUpdateStatements(QueryExecutionFactory queryFactory, String patchURI, String actionPropertyURI) {
        final String sparqlSelect = PrefixService.getSparqlPrefixDecl() +
                "SELECT ?subject ?p ?o WHERE { " +
                "  %%PATCHURI%% pat:update ?update . " +
                "  ?update guo:target_subject ?subject ; " +
                "%%ACTION%% ?updateGraph . " +
                "  ?updateGraph ?p ?o . " +
                "}";

        PatchRepository.L.info(sparqlSelect.replaceAll("%%PATCHURI%%", "<" + patchURI + ">").replaceAll("%%ACTION%%", actionPropertyURI));

        Collection<Statement> statements = new ArrayList<>();
        QueryExecution qe = null;
        try {
            qe = queryFactory.createQueryExecution(sparqlSelect.replaceAll("%%PATCHURI%%", "<" + patchURI + ">").replaceAll("%%ACTION%%", actionPropertyURI));
            ResultSet results = qe.execSelect();

            while (results.hasNext()) {
                QuerySolution qs = results.next();

                Resource subject = qs.get("subject").asResource();
                Property p = ResourceFactory.createProperty(qs.get("p").toString());
                RDFNode o = qs.get("o");

                statements.add(ResourceFactory.createStatement(subject, p, o));
            }
        } finally {
            if (qe != null) {
                qe.close();
            }
        }

        return statements;
    }

    private static Dataset getPatchDataset(QueryExecutionFactory queryFactory, String patchURI) {
        final String sparqlSelect = PrefixService.getSparqlPrefixDecl() +
                "SELECT ?datasetURI ?sparqlEndpoint ?sparqlGraph WHERE { " +
                "  %%PATCHURI%% pat:appliesTo ?datasetURI . " +
                "  ?datasetURI void:sparqlEndpoint ?sparqlEndpoint . " +
                "  OPTIONAL { ?datasetURI void:sparqlGraph ?sparqlGraph . } " +
                "}";

        PatchRepository.L.info(sparqlSelect.replaceAll("%%PATCHURI%%", "<" + patchURI + ">"));

        Dataset dataset = null;
        QueryExecution qe = null;
        try {
            qe = queryFactory.createQueryExecution(sparqlSelect.replaceAll("%%PATCHURI%%", "<" + patchURI + ">"));
            ResultSet results = qe.execSelect();

            if (results.hasNext()) {
                QuerySolution qs = results.next();

                String datasetURI = qs.get("datasetURI").toString();
                String sparqlEndpoint = qs.get("sparqlEndpoint").toString();
                // OPTIONAL
                String sparqlGraph = qs.get("sparqlGraph") == null ? null : qs.get("sparqlGraph").toString();

                if (sparqlGraph == null) {
                    sparqlGraph = datasetURI;
                }

                dataset = new Dataset(datasetURI, sparqlEndpoint, sparqlGraph);

                if (results.hasNext()) {
                    throw new IllegalArgumentException("Patch " + patchURI + " not valid: multiple datasets.");
                }
            }
        } finally {
            if (qe != null) {
                qe.close();
            }
        }

        return dataset;
    }

    public static String getPatchPrefix(String patchURI, String patchId) {
        if (patchURI != null && patchURI.endsWith(patchId)) {
            return patchURI.replace(patchId, "");
        }
        return PatchRepository.DEFAULT_PREFIX;
    }

}
