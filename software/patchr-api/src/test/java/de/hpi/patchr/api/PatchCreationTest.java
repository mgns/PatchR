package de.hpi.patchr.api;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.ResourceFactory;
import com.hp.hpl.jena.vocabulary.RDF;
import de.hpi.patchr.CommonAgentOnDatasetPatchFactory;
import de.hpi.patchr.exceptions.InvalidUpdateInstructionException;
import de.hpi.patchr.utils.PatchrUtils;
import de.hpi.patchr.vocab.PatchrOntology;
import org.junit.Test;

import java.io.FileNotFoundException;
import java.io.IOException;

public class PatchCreationTest {

    @Test
    public void createPatch() throws IOException {
        Model model = ModelFactory.createDefaultModel();
        String prefix = "http://example.org/";
        Dataset dataset = new Dataset("http://example.org", "http://localhost:8890/sparql", "http://example.org");
        Provenance provenance = new Provenance("http://example.org/Mr.X", "http://example.org/Mr.Y", Provenance.DEFAULT_CONFIDENCE);

        try {
            UpdateInstruction update = new UpdateInstruction(dataset.getSparqlGraph(), null, ResourceFactory.createStatement(ResourceFactory.createResource("http://example.org/Patch-1"), RDF.type, PatchrOntology.Patch));
            Patch patch = new Patch(prefix, null, null, dataset, Patch.patchStatus.OPEN, update, provenance);
            patch.getAsResource(model);
        } catch (InvalidUpdateInstructionException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        model.write(System.out, "TURTLE");
    }

    @Test
    public void testPatchFactory() throws FileNotFoundException {
        Dataset dataset = new Dataset("http://example.org", "http://localhost:8890/sparql", "http://example.org");

        CommonAgentOnDatasetPatchFactory g = new CommonAgentOnDatasetPatchFactory("http://example.org", "http://magnus.13mm.de/", null, dataset);
        Model model = ModelFactory.createDefaultModel();

        try {
            g.createPatch(model.createResource("http://dbpedia.org/resource/Oregon"), null, null, model.createProperty("http://dbpedia.org/ontology/language"), model.createResource("http://dbpedia.org/resource/English_language")).getAsResource(model);
            g.createPatch(model.createResource("http://dbpedia.org/resource/Oregon"), null, null, model.createProperty("http://dbpedia.org/ontology/language"), model.createResource("http://dbpedia.org/resource/English_language")).getAsResource(model);
            g.createPatch(model.createResource("http://dbpedia.org/resource/Oregon"), model.createProperty("http://dbpedia.org/ontology/language"), model.createResource("http://dbpedia.org/resource/De_juress"), null, null).getAsResource(model);
        } catch (InvalidUpdateInstructionException e) {
            e.printStackTrace();
        }

        model.write(System.out, "TURTLE");
        PatchrUtils.writeModelToFile(model, "TURTLE", "/tmp/patches.ttl", true);
    }

}
