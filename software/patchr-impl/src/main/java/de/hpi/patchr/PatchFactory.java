package de.hpi.patchr;

import java.io.FileNotFoundException;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Calendar;
import java.util.Date;
import java.util.UUID;

import org.aksw.jena_sparql_api.core.QueryExecutionFactory;
import org.apache.log4j.Logger;
import org.joda.time.DateTime;

import com.hp.hpl.jena.datatypes.xsd.XSDDatatype;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;

import de.hpi.patchr.api.Dataset;
import de.hpi.patchr.api.InvalidUpdateException;
import de.hpi.patchr.utils.PatchrUtils;
import de.hpi.patchr.utils.PrefixService;
import de.hpi.patchr.vocab.GuoOntology;
import de.hpi.patchr.vocab.PatchrOntology;
import de.hpi.patchr.vocab.ProvOntology;

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

	public static enum Action {
		insert, delete
	};

	public static enum Status {
		active, resolved
	};

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
		g.createPatchRequest(Action.insert, g.model.createResource("http://dbpedia.org/resource/Oregon"), g.model.createProperty("http://dbpedia.org/ontology/language"), g.model.createResource("http://dbpedia.org/resource/English_language"));
		g.createPatchRequest(Action.insert, g.model.createResource("http://dbpedia.org/resource/Oregon"), g.model.createProperty("http://dbpedia.org/ontology/language"), g.model.createResource("http://dbpedia.org/resource/English_language"));
		// (g.createPatchRequest(Action.delete,
		// g.model.createResource("http://dbpedia.org/resource/Oregon"),
		// g.model.createProperty("http://dbpedia.org/ontology/language"),
		// g.model.createResource("http://dbpedia.org/resource/De_jure"));
		g.createPatchRequest(Action.delete, g.model.createResource("http://dbpedia.org/resource/Oregon"), g.model.createProperty("http://dbpedia.org/ontology/language"), g.model.createResource("http://dbpedia.org/resource/De_juress"));

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
	 * @return
	 */
	private Resource createPatchRequestResource(String uri) {
		// String uri = generateUri(prefix, "patch");
		Resource patch = model.createResource(uri, PatchrOntology.Patch);

		// the provenance
		Resource provenance = model.createResource(generateUri(prefix, "prov"), ProvOntology.Activity);
		if (performer != null)
			provenance.addProperty(ProvOntology.wasAssociatedWith, performer);
		if (actor != null)
			provenance.addProperty(ProvOntology.wasAssociatedWith, actor);
		Date now = Calendar.getInstance().getTime();
		provenance.addProperty(ProvOntology.atTime, model.createTypedLiteral(new DateTime(now), XSDDatatype.XSDdateTime));
		patch.addProperty(ProvOntology.wasGeneratedBy, provenance);

		// the advocate
		if (actor != null)
			patch.addProperty(PatchrOntology.advocate, actor);

		// the dataset
		if (dataset != null)
			patch.addProperty(PatchrOntology.appliesTo, dataset.getAsResource(model));

		return patch;
	}

	/**
	 * @param action
	 * @param subject
	 * @param property
	 * @param object
	 */
	private Model createPatchRequest(Action action, Resource subject, Property property, RDFNode object) {
		Model p_model = ModelFactory.createDefaultModel();

		// used to create UUIDs
		String name = subject.getURI() + " " + property.getURI() + " " + object.toString();

		// the update
		Resource update = p_model.createResource(generateUri(prefix, "update", action + " " + name), GuoOntology.UpdateInstruction);
		Resource bnode = p_model.createResource().addProperty(property, object);

		if (action.equals(Action.insert)) {
			update.addProperty(GuoOntology.insert, bnode);
		} else if (action.equals(Action.delete)) {
			update.addProperty(GuoOntology.delete, bnode);
		}
		update.addProperty(GuoOntology.target_subject, subject);

		// the dataset graph
		if (dataset != null)
			update.addProperty(GuoOntology.target_graph, p_model.createResource(this.dataset.getSparqlGraph()));

		// TODO check if patch exists

		// the patch
		Resource patch = createPatchRequestResource(generateUri(prefix, "patch", name));
		patch.addProperty(PatchrOntology.update, update);
		patch.addProperty(PatchrOntology.status, Status.active.name());

		return p_model;
	}

	public void addPatchRequest(Action action, Resource subject, Property property, RDFNode object) {
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

	/**
	 * @param prefix
	 * @param cl
	 * @return
	 */
	public String generateUri(String prefix, String cl) {
		StringBuilder sb = new StringBuilder(prefix);
		if (cl != null)
			sb.append(cl + "-");
		else
			sb.append("uuid-");
		sb.append(UUID.randomUUID());
		return sb.toString();
	}

	/**
	 * @param prefix
	 * @param cl
	 * @return
	 */
	public String generateUri(String prefix, String cl, String name) {
		StringBuilder sb = new StringBuilder(prefix);
		if (cl != null)
			sb.append(cl + "-");
		else
			sb.append("uuid-");
		try {
			// sb.append(UUID.fromString(String.format("%040x", new
			// BigInteger(1, name.getBytes("utf8")))));
			byte[] bytesOfMessage = name.getBytes("UTF-8");

			MessageDigest sha1 = MessageDigest.getInstance("SHA-1");
			sb.append(hexEncode(sha1.digest(bytesOfMessage)));
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
		return sb.toString();
	}

	static private String hexEncode(byte[] aInput) {
		StringBuilder result = new StringBuilder();
		char[] digits = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f' };
		for (int idx = 0; idx < aInput.length; ++idx) {
			byte b = aInput[idx];
			result.append(digits[(b & 0xf0) >> 4]);
			result.append(digits[b & 0x0f]);
		}
		return result.toString();
	}
}
