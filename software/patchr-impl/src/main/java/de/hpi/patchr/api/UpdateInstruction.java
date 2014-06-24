package de.hpi.patchr.api;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;

import de.hpi.patchr.api.Patch.UPDATE_ACTION;
import de.hpi.patchr.vocab.GuoOntology;

/**
 * Simple UpdateInstruction works for a set of statements having the same subject.
 * 
 * @author magnus
 */
public class UpdateInstruction {

	private String graph;
	private UPDATE_ACTION action;
	private Collection<Statement> statements;

	public UpdateInstruction(String graph, Patch.UPDATE_ACTION action, Collection<Statement> statements) throws InvalidUpdateInstructionException {
		this.graph = graph;
		this.action = action;
		this.statements = statements;

		if (!isValid()) {
			throw (new InvalidUpdateInstructionException("UpdateInstruction is not valid."));
		}
	}

	public UpdateInstruction(String graph, Patch.UPDATE_ACTION action, Statement statement) throws InvalidUpdateInstructionException {
		this(graph, action, (new ArrayList<Statement>(Arrays.asList(statement))));
	}
	
	private boolean isValid() {
		Resource subject = null;
		for (Statement s : statements) {
			if (subject == null)
				subject = s.getSubject();
			else if (!subject.equals(s.getSubject()))
				return false;
		}
		return true;
	}

	public Resource getAsResource(Model model) {
		Resource update = model.createResource(GuoOntology.UpdateInstruction);
		update.addProperty(GuoOntology.target_graph, model.createResource(graph));
		update.addProperty(GuoOntology.target_subject, statements.iterator().next().getSubject());

		Resource triplesNode = model.createResource();
		for (Statement s : statements) {
			triplesNode.addProperty(s.getPredicate(), s.getObject());
		}
		if (action == Patch.UPDATE_ACTION.delete) {
			update.addProperty(GuoOntology.delete, triplesNode);
		} else if (action == Patch.UPDATE_ACTION.insert) {
			update.addProperty(GuoOntology.insert, triplesNode);
		}

		return update;
	}

}
