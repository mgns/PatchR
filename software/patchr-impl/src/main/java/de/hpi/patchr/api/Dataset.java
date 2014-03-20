package de.hpi.patchr.api;

import org.aksw.jena_sparql_api.cache.core.QueryExecutionFactoryCacheEx;
import org.aksw.jena_sparql_api.cache.extra.CacheCoreEx;
import org.aksw.jena_sparql_api.cache.extra.CacheCoreH2;
import org.aksw.jena_sparql_api.cache.extra.CacheEx;
import org.aksw.jena_sparql_api.cache.extra.CacheExImpl;
import org.aksw.jena_sparql_api.core.QueryExecutionFactory;
import org.aksw.jena_sparql_api.delay.core.QueryExecutionFactoryDelay;
import org.aksw.jena_sparql_api.http.QueryExecutionFactoryHttp;
import org.aksw.jena_sparql_api.pagination.core.QueryExecutionFactoryPaginated;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Resource;

import de.hpi.patchr.utils.PrefixService;
import de.hpi.patchr.vocab.VoidOntology;

public class Dataset {

	private final String sparqlEndpoint;
	private final String sparqlGraph;

	private final String uri;

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
		// Create a query execution
		QueryExecutionFactory qef = new QueryExecutionFactoryHttp(sparqlEndpoint, sparqlGraph);

		// Add delay in order to be nice to the remote server (delay in
		// milliseconds)
		qef = new QueryExecutionFactoryDelay(qef, 7000);

		QueryExecutionFactory qefBackup = qef;

		try {
			// Set up a cache
			// Cache entries are valid for 7 days
			long timeToLive = 7l * 24l * 60l * 60l * 1000l;

			// This creates a 'cache' folder, with a database file named
			// 'sparql.db'
			// Technical note: the cacheBackend's purpose is to only deal with
			// streams,
			// whereas the frontend interfaces with higher level classes - i.e.
			// ResultSet and Model
			CacheCoreEx cacheBackend = CacheCoreH2.create(getUri(), timeToLive, true);
			CacheEx cacheFrontend = new CacheExImpl(cacheBackend);
			qef = new QueryExecutionFactoryCacheEx(qef, cacheFrontend);
		} catch (Exception e) {
			// Try to create cache, if fails continue...
			qef = qefBackup;
		}

		// Add pagination
		qef = new QueryExecutionFactoryPaginated(qef, 900);

		return qef;
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
