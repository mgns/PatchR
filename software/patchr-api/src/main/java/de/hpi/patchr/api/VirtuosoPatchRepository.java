package de.hpi.patchr.api;

import com.hp.hpl.jena.query.*;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import de.hpi.patchr.PatchUtils;
import de.hpi.patchr.io.DataReader;
import de.hpi.patchr.io.DataWriter;
import de.hpi.patchr.io.VirtuosoWriter;
import de.hpi.patchr.utils.PrefixService;

import java.util.ArrayList;
import java.util.List;

public class VirtuosoPatchRepository extends PatchRepository {

    private Dataset store;
    private DataReader reader;
    private DataWriter writer;

    public VirtuosoPatchRepository(String uri, String endpoint, String graph) {
        super(uri);

        this.store = new Dataset(uri, endpoint, graph);
        this.writer = new VirtuosoWriter(endpoint, graph, "jdbc:virtuoso://localhost:1111/charset=UTF-8", "dba", "dba");
    }

    public VirtuosoPatchRepository(String uri, String endpoint, String graph, String virtJdbcUrl, String virtUser, String virtPassword) {
        this(uri, endpoint, graph);
        //this.writer = new VirtuosoWriter(endpoint, graph, "jdbc:virtuoso://localhost:1111/charset=UTF-8", "dba", "dba");
        this.writer = new VirtuosoWriter(endpoint, graph, virtJdbcUrl, virtUser, virtPassword);
    }

    public VirtuosoPatchRepository(String uri, String endpoint, String graph, DataWriter writer) {
        this(uri, endpoint, graph);
        this.writer = writer;
    }

    public String submitPatch(Patch patch) {
        // check for patch existence
        String patchId = findPatchId(patch);

        if (patchId == null) {
            Model model = ModelFactory.createDefaultModel();
            patch.getAsResource(model);
            try {
                writer.write(model);
            } catch (Exception e) {
                e.printStackTrace();
            }

            patchId = patch.getId();
        } else {
            Patch existingPatch = new Patch(patch.getPrefix(), patchId, null, null, null, null, patch.getProvenance());

            Model model = ModelFactory.createDefaultModel();
            existingPatch.getAsResource(model);
            try {
                writer.write(model);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return patchId;
    }

    /**
     * Find patch in repository and return its id (prefix + id = uri).
     *
     * @param patch
     * @return the patch id if found, else null
     */
    public String findPatchId(Patch patch) {
        QueryExecution qe = this.store.getQueryExecutionFactory(false).createQueryExecution(getPatchSelectQuery(patch));
        ResultSet rs = qe.execSelect();

        if (!rs.hasNext()) {
            return null;
        }
        while (rs.hasNext()) {
            QuerySolution r = rs.nextSolution();
            String uri = r.get("patch").asResource().getURI();

            // assuming common prefix
            String prefix = patch.getPrefix();
            String id = null;
            if (uri.startsWith(prefix))
                return uri.replace(prefix, "");
        }

        return null;
    }

    private Query getPatchSelectQuery(Patch patch) {
        String xx = PrefixService.getSparqlPrefixDecl() + "SELECT ?patch WHERE " + getPatchExistenceWhereClause(patch);
        return QueryFactory.create(xx);
    }

    private Query getPatchExistenceAskQuery(Patch patch) {
        return QueryFactory.create(PrefixService.getSparqlPrefixDecl() + "ASK WHERE " + getPatchExistenceWhereClause(patch));
    }

    /**
     * Makes a patch matching where by
     * * same dataset (pat:appliesTo)
     * * same target subject (guo:target_subject)
     * * same update action (guo:insert vs guo:delete)
     * * same update graph (guo:insert or guo:delete)
     * Variable patch holds patch URI.
     *
     * @param patch
     * @return
     */
    private String getPatchExistenceWhereClause(Patch patch) {
        String whereClause = "";

        //    try {
        // TODO
        whereClause = "{"
                + "?patch a pat:Patch ;\n"
                + "  pat:appliesTo <" + patch.getDataset().getUri() + "> ;\n"
                + "  pat:update ?update .\n"
                + "?update guo:target_subject <" + patch.getUpdate().getSubject() + "> ;\n"
                //        + "  <" + patch.getUpdate().getActionProperty().getURI() + "> " + patch.getUpdate().getUpdateGraphAsBNode() + " .\n"
                + "}";
        //    } catch (InvalidUpdateInstructionException e) {
        //        L.error(e);
        //    }

        return whereClause;
    }

    public List<Dataset> getDatasets() {
        String queryString = PrefixService.getSparqlPrefixDecl() + "SELECT DISTINCT ?dataset ?sparqlEndpoint WHERE {\n"
                + "?dataset a void:Dataset ;\n"
                + "  void:sparqlEndpoint ?sparqlEndpoint .\n"
                + "}";

        QueryExecution qe = this.store.getQueryExecutionFactory(false).createQueryExecution(queryString);
        ResultSet rs = qe.execSelect();

        List<Dataset> result = new ArrayList<Dataset>();

        if (!rs.hasNext()) {
            return null;
        }
        while (rs.hasNext()) {
            QuerySolution r = rs.nextSolution();
            String uri = r.get("dataset").asResource().getURI();
            String sparqlEndpoint = r.get("sparqlEndpoint").toString();

            Dataset dataset = new Dataset(uri, sparqlEndpoint, null);

            result.add(dataset);
        }

        return result;
    }

    public List<Patch> getPatches(Dataset dataset) {
        String queryString = PrefixService.getSparqlPrefixDecl() + "SELECT DISTINCT ?patch WHERE { "
                + "  ?patch a pat:Patch ; "
                + "pat:appliesTo %%DATASETURI%% . "
                + "}";

        L.info(queryString.replaceAll("%%DATASETURI%%", "<" + dataset.getUri() + ">"));

        List<Patch> patches = new ArrayList<Patch>();
        QueryExecution qe = null;
        try {
            qe = this.store.getQueryExecutionFactory(false).createQueryExecution(queryString.replaceAll("%%DATASETURI%%", "<" + dataset.getUri() + ">"));
            ResultSet results = qe.execSelect();

            while (results.hasNext()) {
                QuerySolution qs = results.nextSolution();
                String uri = qs.get("patch").toString();

                String id = uri.replaceFirst(prefix + "[/]?", "");
                patches.add(getPatch(id));
            }
        } finally {
            if (qe != null) {
                qe.close();
            }
        }

        return patches;
    }

    public Patch getPatch(String id) {
        String uri = prefix.endsWith("/") ? prefix + id : prefix + "/" + id;

        String queryString = PrefixService.getSparqlPrefixDecl()
                + "DESCRIBE %%PATCHURI%% ?update ?prov ?deleteGraph ?insertGraph ?dataset WHERE { "
                + "  %%PATCHURI%% a pat:Patch ; "
                + "pat:update ?update ; "
                + "pat:appliesTo ?dataset ; "
                + "prov:wasGeneratedBy ?prov . "
                + "  OPTIONAL { ?update guo:delete ?deleteGraph . } "
                + "  OPTIONAL { ?update guo:insert ?insertGraph . } "
                + "}";

        L.info(queryString.replaceAll("%%PATCHURI%%", "<" + uri + ">"));

        Patch patch = null;
        QueryExecution qe = null;
        try {
            qe = this.store.getDescribeQueryExecutionFactory().createQueryExecution(queryString.replaceAll("%%PATCHURI%%", "<" + uri + ">"));
            Model describeModel = qe.execDescribe();

            patch = PatchUtils.getPatch(describeModel, uri);
        } finally {
            if (qe != null) {
                qe.close();
            }
        }

        return patch;
    }

    @Override
    public String toString() {
        return "VirtuosoPatchRepository " + this.store.getUri();
    }

}
