package de.hpi.patchr;

import java.io.FileWriter;
import java.util.Calendar;
import java.util.Date;
import java.util.UUID;

import org.apache.log4j.Logger;
import org.joda.time.DateTime;

import com.hp.hpl.jena.datatypes.xsd.XSDDatatype;
import com.hp.hpl.jena.ontology.Individual;
import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;

import de.hpi.patchr.vocab.GuoObjectProperties;
import de.hpi.patchr.vocab.GuoOntClasses;
import de.hpi.patchr.vocab.PatchrNamespaces;
import de.hpi.patchr.vocab.PatchrObjectProperties;
import de.hpi.patchr.vocab.PatchrOntClasses;
import de.hpi.patchr.vocab.ProvenanceDatatypeProperties;
import de.hpi.patchr.vocab.ProvenanceObjectProperties;
import de.hpi.patchr.vocab.ProvenanceOntClasses;

public class Generate {

	private static Logger L = Logger.getLogger(Generate.class.getSimpleName());
	
	private OntModel model;
	
	private String prefix;
	
	private RDFNode performer;
	private RDFNode actor;
	
	private RDFNode targetGraph;
	private RDFNode dataSet;
	
	public static enum Action {insert, delete};
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		FileWriter out = null;
		
		Generate g = new Generate("http://example.org/patches/", "http://magnus.13mm.de/", null);
		g.createPatchRequestIndividual(Action.insert, g.model.createResource("http://dbpedia.org/resource/Oregon"), g.model.createProperty("http://dbpedia.org/ontology/language"), g.model.createResource("http://dbpedia.org/resource/English_language"));
		
		g.print();
	}

	public Generate(String prefix) {
		this.prefix = prefix;
		this.model = ModelFactory.createOntologyModel();

		PatchrNamespaces.addPrefixes(model);
	}
	
	public Generate(String prefix, String actorUri, String performerUri) {
		this(prefix);
		
		this.actor = actorUri == null ? null : model.createResource(actorUri);
		this.performer = performerUri == null ? null : model.createResource(performerUri);
	}
	
	public void print() {
		model.write(System.out, "TURTLE");
	}
	
	public void addPrefix(String prefix, String namespace) {
		
	}
	
	private Individual createPatchRequestIndividual() {
        String uri = generateUri(prefix, "patch");
        Individual patch = model.createIndividual(uri, PatchrOntClasses.Patch.getOntClass(model));

        Individual provenance = model.createIndividual(generateUri(prefix, "provenance"), ProvenanceOntClasses.DataCreation.getOntClass(model));
        if (performer != null)
        	provenance.addProperty(ProvenanceObjectProperties.performedBy.getObjectProperty(model), performer);
        if (actor != null)
        	provenance.addProperty(ProvenanceObjectProperties.involvedActor.getObjectProperty(model), actor);
        Date now = Calendar.getInstance().getTime();
        provenance.addProperty(ProvenanceDatatypeProperties.performedAt.getDatatypeProperty(model), model.createTypedLiteral(new DateTime(now), XSDDatatype.XSDdateTime));
        patch.addProperty(PatchrObjectProperties.provenance.getObjectProperty(model), provenance);
        
        return patch;
    }
	
	public Individual createPatchRequestIndividual(Action action, Resource subject, Property property, Resource object) {
		Individual patch = createPatchRequestIndividual();
		
        Individual update = model.createIndividual(generateUri(prefix, "update"), GuoOntClasses.UpdateInstruction.getOntClass(model));
        update.addProperty(GuoObjectProperties.target_subject.getObjectProperty(model), subject);
        if (targetGraph != null)
        	update.addProperty(GuoObjectProperties.target_graph.getObjectProperty(model), targetGraph);
        
        Resource bnode = model.createResource().addProperty(property, object);
        if (action.equals(Action.insert)) {
        	update.addProperty(GuoObjectProperties.insert.getObjectProperty(model), bnode);
        } else if (action.equals(Action.delete)) {
        	update.addProperty(GuoObjectProperties.delete.getObjectProperty(model), bnode);
        }
        
        patch.addProperty(PatchrObjectProperties.update.getObjectProperty(model), update);
        
		return patch;
	}
	
    public String generateUri(String prefix, String cl) {
        StringBuilder sb = new StringBuilder(prefix);
        if (cl != null)
        	sb.append(cl + "-");
        else
        	sb.append("uuid-");
        sb.append(UUID.randomUUID());
        return sb.toString();
    }

}
