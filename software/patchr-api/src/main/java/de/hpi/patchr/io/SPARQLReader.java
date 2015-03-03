package de.hpi.patchr.io;

import com.hp.hpl.jena.rdf.model.Model;

/**
 * @author magnus
 */
public class SPARQLReader extends DataReader {

    private final String endpoint;
    private final String graph;
    private final String sparqlQuery;

    public SPARQLReader(String endpoint, String graph, String sparqlQuery) {
        this.endpoint = endpoint;
        this.graph = graph;
        this.sparqlQuery = sparqlQuery;
    }

    @Override
    public void read(Model model) throws Exception {
        //TODO implement
    }
}
