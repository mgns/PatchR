package de.hpi.patchr.io;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;

/**
 * @author magnus
 */
public abstract class DataReader {

    public Model read() throws Exception {
        try {
            Model model = ModelFactory.createDefaultModel();
            read(model);
            return model;
        } catch (Exception e) {
            throw new Exception(e);
        }
    }

    public abstract void read(Model model) throws Exception;
}