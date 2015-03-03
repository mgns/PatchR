package de.hpi.patchr.io;

import com.hp.hpl.jena.rdf.model.Model;
import org.aksw.jena_sparql_api.core.QueryExecutionFactory;
import org.aksw.jena_sparql_api.model.QueryExecutionFactoryModel;

/**
 * @author magnus
 */
public abstract class DataWriter {

    /**
     * Writes a model into a destination. This function delegates to {@code write(QueryExecutionFactory qef)}
     *
     * @param model the model
     * @throws Exception
     */
    public void write(Model model) throws Exception {
        write(new QueryExecutionFactoryModel(model));
    }


    /**
     * abstract class that writes a {@code QueryExecutionFactory} to a destination
     *
     * @param qef a QueryExecutionFactory
     * @throws Exception
     */
    public abstract void write(QueryExecutionFactory qef) throws Exception;
}
