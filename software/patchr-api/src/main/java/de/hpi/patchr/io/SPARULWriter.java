package de.hpi.patchr.io;

import org.aksw.jena_sparql_api.core.QueryExecutionFactory;
import org.aksw.jena_sparql_api.model.QueryExecutionFactoryModel;

import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.rdf.model.Model;

/**
 * @author magnus
 */
public class SPARULWriter extends DataWriter {

	protected final String endpoint;
	protected final String graph;

	public SPARULWriter(String endpoint, String graph) {
		this.endpoint = endpoint;
		this.graph = graph;
	}

	@Override
	public void write(QueryExecutionFactory qef) {
		// TODO implement
	}
	
	// TODO utility method
	protected Model getModelFromQueryFactory(QueryExecutionFactory qef) throws Exception {
        if (qef instanceof QueryExecutionFactoryModel) {
            return ((QueryExecutionFactoryModel) qef).getModel();
        } else {
            QueryExecution qe = null;
            try {
                qe = qef.createQueryExecution("CONSTRUCT ?s ?p ?o WHERE { ?s ?p ?o }");
                return qe.execConstruct();
            } catch (Exception e) {
                throw e;
            } finally {
                if (qe != null) {
                    qe.close();
                }
            }
        }
    }
}
