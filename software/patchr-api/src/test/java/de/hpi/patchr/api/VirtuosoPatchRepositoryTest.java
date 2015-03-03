package de.hpi.patchr.api;

import com.google.common.base.Joiner;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.ResourceFactory;
import com.hp.hpl.jena.vocabulary.RDF;
import de.hpi.patchr.exceptions.InvalidUpdateInstructionException;
import de.hpi.patchr.io.VirtuosoWriter;
import de.hpi.patchr.vocab.PatchrOntology;
import org.junit.Assume;
import org.junit.Test;

import java.util.List;

public class VirtuosoPatchRepositoryTest {

    @Test
    public void testVirtuosoWriter() throws Exception {
        System.out.println("Start");

        VirtuosoWriter writer = new VirtuosoWriter("http://localhost:8890/sparql/", "http://example.org", "jdbc:virtuoso://localhost:1111/charset=UTF-8", "dba", "dba");

        Model model = ModelFactory.createDefaultModel();
        model.add(model.createResource(), RDF.type, PatchrOntology.Patch);

        writer.write(model);

        writer.clearGraph();
    }

    @Test
    public void testWriting() {
        String prefix = "http://patchr.s16a.org/";

        VirtuosoPatchRepository repo = new VirtuosoPatchRepository(prefix, "http://localhost:8890/sparql/", "http://patchr.s16a.org");

        Dataset dataset = new Dataset("http://dbpedia.org", "http://dbpedia.org/sparql", "http://dbpedia.org");
        Provenance provenance = new Provenance("http://example.org/Mr.X", "http://example.org/Mr.Y", Provenance.DEFAULT_CONFIDENCE);

        try {
            UpdateInstruction update = new UpdateInstruction(dataset.getSparqlGraph(), null, ResourceFactory.createStatement(ResourceFactory.createResource("http://example.org/Patch-1"), RDF.type, PatchrOntology.Patch));
            Patch patch = new Patch(prefix, null, null, dataset, Patch.patchStatus.OPEN, update, provenance);
            repo.submitPatch(patch);
        } catch (InvalidUpdateInstructionException e) {
            e.printStackTrace();
        }

        try {
            UpdateInstruction update = new UpdateInstruction(dataset.getSparqlGraph(), null, ResourceFactory.createStatement(ResourceFactory.createResource("http://example.org/Patch-1"), RDF.type, PatchrOntology.Patch));
            Patch patch = new Patch(prefix, null, null, dataset, Patch.patchStatus.OPEN, update, provenance);
            String existingPatch = repo.findPatchId(new Patch(prefix, null, null, dataset, Patch.patchStatus.OPEN, update, provenance));

            Assume.assumeNotNull(existingPatch);
        } catch (InvalidUpdateInstructionException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testGetPatches() {
        String prefix = "http://patchr.s16a.org/";
        VirtuosoPatchRepository repo = new VirtuosoPatchRepository(prefix, "http://localhost:8890/sparql/", "http://patchr.s16a.org");

        List<Dataset> datasets = repo.getDatasets();

        for (Dataset dataset : datasets) {
            List<Patch> patches = repo.getPatches(dataset);

            System.out.println(dataset.getUri());

            for (Patch patch : patches) {
                if (patch == null) {
                    System.out.println("Patch is null");
                    continue;
                }
                UpdateInstruction update = patch.getUpdate();

                try {
                    System.out.println(" delete " + Joiner.on(", ").join(update.getDeleteStatements()));
                    System.out.println(" insert " + Joiner.on(", ").join(update.getInsertStatements()));
                } catch (InvalidUpdateInstructionException e) {
                    e.printStackTrace();
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
        }
    }
}
