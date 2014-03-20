package de.hpi.patchr;

import java.io.FileNotFoundException;

import org.aksw.jena_sparql_api.core.QueryExecutionFactory;
import org.apache.log4j.Logger;

import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.ResourceFactory;

import de.hpi.patchr.api.Dataset;
import de.hpi.patchr.api.InvalidUpdateException;
import de.hpi.patchr.api.InvalidUpdateInstructionException;
import de.hpi.patchr.api.Patch;
import de.hpi.patchr.api.Provenance;
import de.hpi.patchr.api.UpdateInstruction;
import de.hpi.patchr.utils.PatchrUtils;
import de.hpi.patchr.utils.PrefixService;

/**
 * A PatchFactory encapsulates the creation of a model of patch requests with a
 * common agent and dataset.
 * 
 * @author magnus
 */
public class PatchFactory {

	private static Logger L = Logger.getLogger(PatchFactory.class);

	private Model model;

	private String prefix;

	private RDFNode performer;
	private RDFNode actor;

	private Dataset dataset;

	/**
	 * @param args
	 * @throws FileNotFoundException
	 */
	public static void main(String[] args) throws FileNotFoundException {
		Dataset dataset = new Dataset("http://dbpedia.org", "http://dbpedia.org/sparql", "http://dbpedia.org");

		/*
		 * QueryExecutionFactory qef = dataset.getQueryExecutionFactory();
		 * QueryExecution qe = qef.createQueryExecution(
		 * "SELECT ?p ?o WHERE { <http://dbpedia.org/resource/Oregon> ?p ?o . }"
		 * ); ResultSet rset = qe.execSelect(); while (rset.hasNext()) {
		 * QuerySolution row = rset.next(); L.info(rset.getRowNumber() + ": " +
		 * row.get("p") + "\t" + row.get("o")); }
		 */
		PatchFactory g = new PatchFactory("http://example.org/patches/", "http://magnus.13mm.de/", null, dataset);
		g.addPatchRequest(Patch.UPDATE_ACTION.insert, g.model.createResource("http://dbpedia.org/resource/Oregon"), g.model.createProperty("http://dbpedia.org/ontology/language"), g.model.createResource("http://dbpedia.org/resource/English_language"));
		g.addPatchRequest(Patch.UPDATE_ACTION.insert, g.model.createResource("http://dbpedia.org/resource/Oregon"), g.model.createProperty("http://dbpedia.org/ontology/language"), g.model.createResource("http://dbpedia.org/resource/English_language"));
		// (g.createPatchRequest(Action.delete,
		// g.model.createResource("http://dbpedia.org/resource/Oregon"),
		// g.model.createProperty("http://dbpedia.org/ontology/language"),
		// g.model.createResource("http://dbpedia.org/resource/De_jure"));
		g.addPatchRequest(Patch.UPDATE_ACTION.delete, g.model.createResource("http://dbpedia.org/resource/Oregon"), g.model.createProperty("http://dbpedia.org/ontology/language"), g.model.createResource("http://dbpedia.org/resource/De_juress"));

		g.print();
		PatchrUtils.writeModelToFile(g.getModel(), "TURTLE", "/tmp/patches.ttl", true);
	}

	public PatchFactory(String prefix) {
		this.prefix = prefix;
		this.model = ModelFactory.createDefaultModel();

		PrefixService.addPrefixes(model);
	}

	public PatchFactory(String prefix, String actorUri, String performerUri) {
		this(prefix);

		this.actor = actorUri == null ? null : model.createResource(actorUri);
		this.performer = performerUri == null ? null : model.createResource(performerUri);
	}

	public PatchFactory(String prefix, String actorUri, String performerUri, Dataset dataset) {
		this(prefix, actorUri, performerUri);

		this.dataset = dataset;
	}

	/**
	 * @param model
	 */
	public void setModel(Model model) {
		this.model = model;
	}
	
	/**
	 * @return
	 */
	public Model getModel() {
		return this.model;
	}

	/**
	 * 
	 */
	public void print() {
		System.out.println("### Model ###");
		model.write(System.out, "TURTLE");
	}

	/**
	 * @param prefix
	 * @param uri
	 */
	public void addPrefix(String prefix, String uri) {
		PrefixService.addPrefix(prefix, uri);
	}

	/**
	 * @param action
	 * @param subject
	 * @param property
	 * @param object
	 */
	private Model createPatchRequest(Patch.UPDATE_ACTION action, Resource subject, Property predicate, RDFNode object) {
		Model p_model = ModelFactory.createDefaultModel();

		try {
			UpdateInstruction update = new UpdateInstruction(dataset.getSparqlGraph(), action, ResourceFactory.createStatement(subject, predicate, object));
			Provenance prov = new Provenance(actor.asResource().getURI(), actor.asResource().getURI());
			Patch patch = new Patch(prefix, null, null, dataset, 1., Patch.UPDATE_STATUS.active, update, prov);
			patch.addProposer(actor.asResource().getURI());

			// TODO check if patch exists
	
			patch.getAsResource(p_model);
		} catch (InvalidUpdateInstructionException e) {
			e.printStackTrace();
		}
		
		return p_model;
	}

	public void addPatchRequest(Patch.UPDATE_ACTION action, Resource subject, Property property, RDFNode object) {
		Model patchModel = createPatchRequest(action, subject, property, object);

		if (modelContains(patchModel)) {
			L.info("Patch exists.");
		} else {
			this.model.add(patchModel);
		}
	}

	private boolean modelContains(Model patchModel) {
		return this.model.containsAll(patchModel);
	}

	private void validateInsertion(Resource subject, Property property, RDFNode rdfNode) throws InvalidUpdateException {
		if (tripleExists(subject, property, rdfNode)) {
			throw new InvalidUpdateException("Triple <" + subject + "> <" + property + "> <" + rdfNode + "> already exists.");
		}
		L.info("Insertion of triple <" + subject + "> <" + property + "> <" + rdfNode + "> validated.");
	}

	private void validateDeletion(Resource subject, Property property, RDFNode rdfNode) throws InvalidUpdateException {
		if (!tripleExists(subject, property, rdfNode)) {
			throw new InvalidUpdateException("Triple <" + subject + "> <" + property + "> <" + rdfNode + "> does not exist.");
		}
		L.info("Deletion of triple <" + subject + "> <" + property + "> <" + rdfNode + "> validated.");
	}

	private boolean tripleExists(Resource subject, Property property, RDFNode rdfNode) {
		// Gives a NPE
		// String queryString = "ASK { <" + subject + "> <" + property + "> <" +
		// object + "> . }";
		// L.info(queryString);
		//
		// QueryExecutionFactory qef = dataset.getQueryExecutionFactory();
		// QueryExecution qe = qef.createQueryExecution(queryString);
		//
		// return qe.execAsk();

		// TODO that is a workaround
		String queryString = "SELECT ?o WHERE { <" + subject + "> <" + property + "> ?o . FILTER (?o = <" + rdfNode + ">) }";
		L.info(queryString);

		QueryExecutionFactory qef = dataset.getQueryExecutionFactory();
		QueryExecution qe = qef.createQueryExecution(queryString);

		return qe.execSelect().hasNext();
	}

}
