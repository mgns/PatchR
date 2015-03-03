package de.hpi.patchr.api;

import com.hp.hpl.jena.query.*;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import de.hpi.patchr.exceptions.InvalidUpdateInstructionException;
import de.hpi.patchr.utils.PrefixService;

import java.util.List;

/**
 * Created by magnus on 21.01.15.
 */
public class JenaPatchRepository extends PatchRepository {

    private Model store;

    public JenaPatchRepository(String prefix) {
        super(prefix);

        this.store = ModelFactory.createDefaultModel();
    }

    @Override
    public String submitPatch(Patch patch) {
        return null;
    }

    @Override
    public String findPatchId(Patch patch) {
        QueryExecution qe = QueryExecutionFactory.create(getPatchSelectQuery(patch), store);
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

        return null;    }

    private Query getPatchSelectQuery(Patch patch) {
        String q = PrefixService.getSparqlPrefixDecl() + "SELECT ?patch WHERE " + getPatchExistenceWhereClause(patch);
        return QueryFactory.create(q);
    }

    private String getPatchExistenceWhereClause(Patch patch) {
        String whereClause = "";

        try {
            whereClause = "{\n"
                + "?patch a pat:Patch ;\n"
                + "  pat:appliesTo <" + patch.getDataset().getUri() + "> ;\n"
                + "  pat:update ?update .\n"
                + "?update guo:target_subject <" + patch.getUpdate().getSubject() + "> ;\n"
                + (patch.getUpdate().isDeleteAction() ? "  guo:delete " + patch.getUpdate().getDeleteGraphAsBNode() + " .\n" : "")
                + (patch.getUpdate().isInsertAction() ? "  guo:insert " + patch.getUpdate().getInsertGraphAsBNode() + " .\n" : "")
                + "}";
        } catch (InvalidUpdateInstructionException e) {
            L.error(e);
        }

        return whereClause;
    }

    @Override
    public List<Dataset> getDatasets() {
        return null;
    }

    @Override
    public List<Patch> getPatches(Dataset dataset) {
        return null;
    }

    @Override
    public Patch getPatch(String id) {
        return null;
    }
}
