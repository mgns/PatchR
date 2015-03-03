package de.hpi.patchr;

import com.hp.hpl.jena.datatypes.xsd.XSDDatatype;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.vocabulary.RDF;
import de.hpi.patchr.api.Patch;
import de.hpi.patchr.vocab.GuoOntology;
import de.hpi.patchr.vocab.PatchrOntology;
import de.hpi.patchr.vocab.ProvOntology;
import de.hpi.patchr.vocab.VoidOntology;
import org.joda.time.DateTime;
import org.junit.Assert;
import org.junit.Test;

import java.util.Collection;
import java.util.Date;

/**
 * Created by magnus on 03.03.15.
 */
public class PatchUtilsTest {


    @Test
    public void instantiatePatchFromEmptyModelTest() {
        Model model = ModelFactory.createDefaultModel();
        Collection<Patch> patches = PatchUtils.instantiatePatchesFromModel(model);
        Assert.assertTrue(patches.isEmpty());
    }

    @Test
    public void getPatchFromModelTest() {
        Patch patch = null;

        Model model = ModelFactory.createDefaultModel();

        model.add(model.createResource("http://example.org/patch-1"), RDF.type, PatchrOntology.Patch);
        patch = PatchUtils.getPatch(model, "http://example.org/patch-1");
        Assert.assertNull(patch);

        model.add(model.getResource("http://example.org/patch-1"), PatchrOntology.appliesTo, model.createResource("http://example.org/dataset"));
        patch = PatchUtils.getPatch(model, "http://example.org/patch-1");
        Assert.assertNotNull(patch);
        Assert.assertEquals("http://example.org/", patch.getPrefix());
        Assert.assertEquals("patch-1", patch.getId());
        Assert.assertEquals(Patch.patchStatus.OPEN, patch.getStatus());
        Assert.assertEquals(0., patch.getAggregatedConfidence(), 0.);
        Assert.assertTrue(patch.getProvenance().isEmpty());
        Assert.assertNull(patch.getComment());
        Assert.assertNull(patch.getUpdate());
        Assert.assertNull(patch.getDataset());

        // SELECT ?datasetURI ?sparqlEndpoint ?sparqlGraph WHERE {
        // <http://example.org/patch-1> pat:appliesTo ?datasetURI .
        // ?datasetURI void:sparqlEndpoint ?sparqlEndpoint .
        // OPTIONAL { ?datasetURI void:sparqlGraph ?sparqlGraph . }
        // }

        model.add(model.getResource("http://example.org/dataset"), VoidOntology.sparqlEndpoint, model.createResource("http://example.org/dataset/sparql"));
        patch = PatchUtils.getPatch(model, "http://example.org/patch-1");
        Assert.assertNotNull(patch);
        Assert.assertNotNull(patch.getDataset());
        Assert.assertEquals("http://example.org/dataset/sparql", patch.getDataset().getSparqlEndpoint());

        // SELECT DISTINCT ?confidence ?time ?actor ?performer ?comment WHERE {   <http://example.org/patch-1> prov:wasGeneratedBy ?provenance .   ?provenance pat:confidence ?confidence ; prov:atTime ?time ; prov:wasAssociatedWith ?actor .   OPTIONAL { ?provenance prov:actedOnBehalfOf ?performer . }   OPTIONAL { ?provenance rdfs:comment ?comment . } }

        Resource prov = model.createResource();
        model.add(model.getResource("http://example.org/patch-1"), ProvOntology.wasGeneratedBy, prov);
        model.add(prov, PatchrOntology.confidence, model.createTypedLiteral(new Double(.13)));
        model.add(prov, ProvOntology.atTime, model.createTypedLiteral(DateTime.now().toString(), XSDDatatype.XSDdateTime));
        model.add(prov, ProvOntology.wasAssociatedWith, model.createResource("http://example.com/actor-x"));
        patch = PatchUtils.getPatch(model, "http://example.org/patch-1");
        Assert.assertNotNull(patch);
        Assert.assertFalse(patch.getProvenance().isEmpty());
        Assert.assertEquals(1, patch.getProvenance().size());
        Assert.assertEquals(.13, patch.getAggregatedConfidence(), 0.);

        // SELECT ?graph ?subject ?deleteGraph ?insertGraph WHERE {
        // <http://example.org/patch-1> pat:update ?update .
        // ?update guo:target_graph ?graph ;
        // guo:target_subject ?subject .
        // OPTIONAL { ?update guo:delete ?deleteGraph . }
        // OPTIONAL { ?update guo:insert ?insertGraph . }
        // }

        Resource update = model.createResource();
        model.add(model.getResource("http://example.org/patch-1"), PatchrOntology.update, update);
        model.add(update, GuoOntology.target_graph, model.getResource("http://example.org/graph"));
        model.add(update, GuoOntology.target_subject, model.getResource("http://example.org/subject"));
        Resource updateGraph = model.createResource();
        model.add(update, GuoOntology.delete, updateGraph);
        model.add(updateGraph, model.createProperty("http://example.org/deleteProperty"), model.createResource("http://example.org/deleteObject"));
        patch = PatchUtils.getPatch(model, "http://example.org/patch-1");

        Assert.assertNotNull(patch.getUpdate());
    }

    @Test
    public void getPatchFromModelMinimalTest() {
        Collection<Patch> patches = null;

        Model model = ModelFactory.createDefaultModel();

        Resource patchResource = model.createResource();
        Resource datasetResource = model.createResource("http://example.org");
        Resource provResource = model.createResource();
        Resource updateResource = model.createResource();
        Resource updateGraphResource = model.createResource();

        // type
        model.add(patchResource, RDF.type, PatchrOntology.Patch);
        // a known dataset
        model.add(patchResource, PatchrOntology.appliesTo, datasetResource);
        // prov
        model.add(patchResource, ProvOntology.wasGeneratedBy, provResource);
        model.add(provResource, PatchrOntology.confidence, model.createTypedLiteral(new Double(.13)));
        // update
        model.add(patchResource, PatchrOntology.update, updateResource);
        model.add(updateResource, GuoOntology.target_graph, model.getResource("http://example.org/graph"));
        model.add(updateResource, GuoOntology.target_subject, model.getResource("http://example.org/subject"));
        model.add(updateResource, GuoOntology.delete, updateGraphResource);
        model.add(updateGraphResource, model.createProperty("http://example.org/deleteProperty"), model.createResource("http://example.org/deleteObject"));
        patches = PatchUtils.instantiatePatchesFromModel(model);

        Assert.assertFalse(patches.isEmpty());
        Assert.assertEquals(1, patches.size());
        for (Patch patch : patches) {
            Assert.assertNotNull(patch);
            Assert.assertNotNull(patch.getUpdate());
        }
    }
}
