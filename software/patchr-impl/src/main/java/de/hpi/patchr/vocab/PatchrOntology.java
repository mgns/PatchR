package de.hpi.patchr.vocab;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.rdf.model.ModelFactory;

public class PatchrOntology {

	private static OntModel model;
	
	public static OntModel getOntModel() {
		if (model == null) {
			model = ModelFactory.createOntologyModel();
			
			InputStream in;
			try {
				in = PatchrOntology.class.getResourceAsStream("patchr.ttl");
		        model.read(in, null, "TTL");
		        in.close();
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		return model;
	}

}
