package de.hpi.patchr;

import java.io.IOException;

import org.junit.Test;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Resource;

import de.hpi.patchr.api.Patch;

public class PatchCreationTest {

	@Test
	public void createPatch() throws IOException {
		Model model = ModelFactory.createDefaultModel();
		String prefix = "http://example.org/";
		Resource dataset = model.createResource("http://dbpedia.org/void.ttl#DBpedia");
		Resource provenance = model.createResource();

		Patch patch = new Patch(model, prefix, dataset, provenance);

		model.write(System.out, "TURTLE");
	}

}
