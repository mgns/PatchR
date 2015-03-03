package de.hpi.patchr.io;

import com.hp.hpl.jena.rdf.model.Model;
import org.aksw.jena_sparql_api.core.QueryExecutionFactory;
import virtuoso.jena.driver.VirtGraph;
import virtuoso.jena.driver.VirtuosoUpdateFactory;
import virtuoso.jena.driver.VirtuosoUpdateRequest;

import java.io.ByteArrayOutputStream;

public class VirtuosoWriter extends SPARULWriter {

    private VirtGraph virtGraph;

    public VirtuosoWriter(String endpoint, String graph) {
        super(endpoint, graph);

        this.virtGraph = new VirtGraph(graph);
    }

    public VirtuosoWriter(String endpoint, String graph, String virtJdbcUrl, String virtUser, String virtPassword) {
        super(endpoint, graph);

        this.virtGraph = new VirtGraph(graph, virtJdbcUrl, virtUser, virtPassword);
    }

    @Override
    public void write(QueryExecutionFactory qef) {

        try {
            Model model = getModelFromQueryFactory(qef);

            if (model.isEmpty())
                return;

            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            model.getWriter("TURTLE").write(model, bos, null);
            byte[] modelAsByteArray = bos.toByteArray();
            String modelAsString = new String(modelAsByteArray);

            String query = "INSERT INTO GRAPH <" + this.graph + "> {" + modelAsString + "}";
            executeUpdate(query);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void clearGraph() {
        String query = "CLEAR GRAPH <" + this.graph + ">";
        executeUpdate(query);
    }

    private void executeUpdate(String query) {
        VirtuosoUpdateRequest virtUpdateRequest = VirtuosoUpdateFactory.create(query, virtGraph);
        virtUpdateRequest.exec();
    }

}
