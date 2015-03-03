package de.hpi.patchr.api;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Resource;
import de.hpi.patchr.utils.PrefixService;
import de.hpi.patchr.vocab.VoidOntology;
import org.aksw.jena_sparql_api.cache.h2.CacheUtilsH2;
import org.aksw.jena_sparql_api.core.QueryExecutionFactory;
import org.aksw.jena_sparql_api.delay.core.QueryExecutionFactoryDelay;
import org.aksw.jena_sparql_api.http.QueryExecutionFactoryHttp;
import org.aksw.jena_sparql_api.pagination.core.QueryExecutionFactoryPaginated;
import org.apache.log4j.Logger;

public class Dataset {

    public Logger L = Logger.getLogger(getClass());

    private final String sparqlEndpoint;
    // serialization of sparqlGraph is currently not supported
    private final String sparqlGraph;

    private final String uri;

    private QueryExecutionFactory queryExecutionFactory;

    /**
     * @param uri
     * @param sparqlEndpoint
     * @param sparqlGraph    named graph or null for default graph
     */
    public Dataset(String uri, String sparqlEndpoint, String sparqlGraph) {
        this.uri = uri;
        this.sparqlEndpoint = sparqlEndpoint;
        this.sparqlGraph = sparqlGraph;
    }

    public Resource getAsResource(Model model) {
        // Model model = ModelFactory.createDefaultModel();
        Resource r = model.createResource(uri, VoidOntology.Dataset);

        r.addProperty(VoidOntology.sparqlEndpoint, this.sparqlEndpoint);
        r.addProperty(model.createProperty(PrefixService.getPrefix("foaf") + "homepage"), this.uri);

        // TODO describe the named graph using
        // http://www.w3.org/TR/void/#sparql-sd
        // TODO add version description

        return r;
    }

    public QueryExecutionFactory getQueryExecutionFactory() {
        // default is with caching
        return getQueryExecutionFactory(true);
    }

    public QueryExecutionFactory getQueryExecutionFactory(boolean cacheOn) {
        if (queryExecutionFactory != null)
            return queryExecutionFactory;

        // Create a query execution
        queryExecutionFactory = new QueryExecutionFactoryHttp(sparqlEndpoint, sparqlGraph);

        // Add delay in order to be nice to the remote server (delay in
        // milliseconds)
        queryExecutionFactory = new QueryExecutionFactoryDelay(queryExecutionFactory, 10);

        if (cacheOn) {
            QueryExecutionFactory qefBackup = queryExecutionFactory;

            try {
                // Set up a cache
                // Cache entries are valid for 7 days
                long timeToLive = 7l * 24l * 60l * 60l * 1000l;

                // This creates a 'cache' folder, with a database file named 'sparql.db'
                QueryExecutionFactory _qef = CacheUtilsH2.createQueryExecutionFactory(queryExecutionFactory, "./cache/sparql/" + getUri().replace(":", "").replace("//", "/"), false, timeToLive);
                queryExecutionFactory = _qef;
            } catch (Exception e) {
                L.error(e);
                // Try to create cache, if fails continue...
                queryExecutionFactory = qefBackup;
            }
        }

        // Add pagination
        queryExecutionFactory = new QueryExecutionFactoryPaginated(queryExecutionFactory, 1000);

        return queryExecutionFactory;
    }

    public QueryExecutionFactory getDescribeQueryExecutionFactory() {
        // Create a query execution
        QueryExecutionFactory describeQueryExecutionFactory = new QueryExecutionFactoryHttp(sparqlEndpoint, sparqlGraph);

        return describeQueryExecutionFactory;
    }

    public String getSparqlEndpoint() {
        return sparqlEndpoint;
    }

    public String getSparqlGraph() {
        return sparqlGraph;
    }

    public String getUri() {
        return uri;
    }

}
