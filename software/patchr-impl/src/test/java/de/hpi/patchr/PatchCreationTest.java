package de.hpi.patchr;

import java.io.IOException;

import org.junit.Test;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.ResourceFactory;
import com.hp.hpl.jena.vocabulary.RDF;

import de.hpi.patchr.api.Dataset;
import de.hpi.patchr.api.InvalidUpdateInstructionException;
import de.hpi.patchr.api.Patch;
import de.hpi.patchr.api.Provenance;
import de.hpi.patchr.api.UpdateInstruction;
import de.hpi.patchr.vocab.PatchrOntology;

public class PatchCreationTest {

	@Test
	public void createPatch() throws IOException {
		Model model = ModelFactory.createDefaultModel();
		String prefix = "http://example.org/";
		Dataset dataset = new Dataset("http://dbpedia.org", "http://dbpedia.org/sparql", "http://dbpedia.org");
		Provenance provenance = new Provenance();

		try {
			UpdateInstruction update = new UpdateInstruction(dataset.getSparqlGraph(), Patch.UPDATE_ACTION.insert, ResourceFactory.createStatement(ResourceFactory.createResource("http://example.org/Patch-1"), RDF.type, PatchrOntology.Patch));
			Patch patch = new Patch(prefix, null, null, dataset, null, Patch.UPDATE_STATUS.active, update, provenance);
			patch.getAsResource(model);
		} catch (InvalidUpdateInstructionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		model.write(System.out, "TURTLE");
	}

}
