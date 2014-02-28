package de.hpi.patchr;

import java.io.IOException;

import org.junit.Test;

import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.vocabulary.RDF;

import de.hpi.patchr.api.Patch;
import de.hpi.patchr.vocab.PatchrOntClasses;
import de.hpi.patchr.vocab.PatchrOntology;
import de.hpi.rdf.Document;
import de.hpi.rdf.Jrdf;

public class PatchCreationTest {

	@Test
	public void createPatch() throws IOException {
        OntModel model = ModelFactory.createOntologyModel();
        String prefix = "http://example.org/";
        Resource dataset = model.createIndividual("http://dbpedia.org/void.ttl#DBpedia", null);
        Resource provenance = model.createResource();
        
        Patch patch = new Patch(model, prefix, dataset, provenance);
		
		model.write(System.out, "TURTLE");
	}

	@Test
	public void createPatch2() throws IOException {

		Document patchDescription = Jrdf.newDocument();
		Resource patch = Jrdf.resource();
		
		patchDescription.addTriple(patch, RDF.type, PatchrOntClasses.Patch);
	}

}
