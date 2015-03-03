package de.hpi.patchr.api;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.impl.StatementImpl;
import de.hpi.patchr.exceptions.InvalidUpdateInstructionException;
import de.hpi.patchr.vocab.GuoOntology;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

/**
 * Simple UpdateInstruction works for a set of statements having the same subject.
 *
 * @author magnus
 */
public class UpdateInstruction {

    public static String TARGET_SUBJECT_URI = "http://purl.org/hpi/patchr#targetSubjectPlaceholder";

    private String graph;
    private Resource subject;
    private Model deleteGraph;
    private Model insertGraph;

    private UpdateInstruction(String graph) {
        this.graph = graph;
    }

    public UpdateInstruction(String graph, Resource subject, Model deleteGraph, Model insertGraph) throws InvalidUpdateInstructionException {
        this(graph);

        this.subject = subject;
        this.deleteGraph = deleteGraph;
        this.insertGraph = insertGraph;
    }

    public UpdateInstruction(String graph, Resource subject, Collection<Statement> deleteStatements, Collection<Statement> insertStatements) throws InvalidUpdateInstructionException {
        this(graph);

        this.subject = subject;
        this.deleteGraph = getUpdateGraphFromStatements(subject, deleteStatements);
        this.insertGraph = getUpdateGraphFromStatements(subject, insertStatements);
    }

    public UpdateInstruction(String graph, Statement deleteStatement, Statement insertStatement) throws InvalidUpdateInstructionException {
        this(graph,
                deleteStatement != null ? deleteStatement.getSubject() : insertStatement.getSubject(),
                deleteStatement != null ? (new ArrayList<Statement>(Arrays.asList(deleteStatement))) : null,
                insertStatement != null ? (new ArrayList<Statement>(Arrays.asList(insertStatement))) : null);
    }

    private static Model getUpdateGraphFromStatements(Resource subject, Collection<Statement> statements) throws InvalidUpdateInstructionException {
        Model updateGraph = ModelFactory.createDefaultModel();

        if (statements == null || statements.isEmpty())
            return updateGraph;

        Resource node = updateGraph.createResource(TARGET_SUBJECT_URI);

        for (Statement s : statements) {
            if (subject.equals(s.getSubject())) {
                node.addProperty(s.getPredicate(), s.getObject());
            } else {
                // TODO: check for graph connectivity
                throw (new InvalidUpdateInstructionException("Complex updates are currently not allowed. Can not create updateGraph."));
            }
        }

        return updateGraph;
    }

    private static Collection<Statement> getStatementsFromUpdateGraph(Resource subject, Model updateGraph) throws InvalidUpdateInstructionException {
        List<Statement> statements = new ArrayList<Statement>();

        for (Statement s : updateGraph.listStatements().toList()) {
            if (s.getSubject().getURI().equals(TARGET_SUBJECT_URI))
                statements.add(new StatementImpl(subject, s.getPredicate(), s.getObject()));
            else
                throw new InvalidUpdateInstructionException("Complex updates are currently not allowed. Can not create updateStatements");
        }

        return statements;
    }

    private static Resource getNodeFromUpdateGraph(Model targetModel, Model updateGraph) throws InvalidUpdateInstructionException {
        if (updateGraph.isEmpty())
            return null;

        // a simple blank node :)
        Resource node = targetModel.createResource();

        for (Statement s : updateGraph.listStatements().toList()) {
            if (s.getSubject().getURI().equals(TARGET_SUBJECT_URI)) {
                node.addProperty(s.getPredicate(), s.getObject());
            } else {
                //that should currently not happen
                targetModel.add(s);
                throw (new InvalidUpdateInstructionException("Complex updates are currently not allowed. Can not create model."));
            }
        }

        return node;
    }

    public Resource getAsResource(Model model) {
        // TODO: do we need a URI or is blank node fine?
        Resource update = model.createResource(GuoOntology.UpdateInstruction);
        update.addProperty(GuoOntology.target_graph, model.createResource(graph));
        update.addProperty(GuoOntology.target_subject, this.subject);

        try {
            Resource deleteNode = getNodeFromUpdateGraph(model, this.deleteGraph);
            if (deleteNode != null)
                update.addProperty(GuoOntology.delete, deleteNode);
        } catch (InvalidUpdateInstructionException ex) {
            PatchRepository.L.error(ex);
        }
        try {
            Resource insertNode = getNodeFromUpdateGraph(model, this.insertGraph);
            if (insertNode != null)
                update.addProperty(GuoOntology.insert, insertNode);
        } catch (InvalidUpdateInstructionException ex) {
            PatchRepository.L.error(ex);
        }

        return update;
    }

    public String getGraph() {
        return graph;
    }

    public void setGraph(String graph) {
        this.graph = graph;
    }

    public boolean isDeleteAction() {
        return !this.deleteGraph.isEmpty();
    }

    public boolean isInsertAction() {
        return !this.insertGraph.isEmpty();
    }

    public boolean isChangeAction() {
        return !this.deleteGraph.isEmpty() && !this.insertGraph.isEmpty();
    }

    public Resource getSubject() {
        return subject;
    }

    public void setSubject(Resource subject) {
        this.subject = subject;
    }

    public Model getDeleteGraph() {
        return deleteGraph;
    }

    public Model getInsertGraph() {
        return insertGraph;
    }

    @Deprecated
    public String getDeleteGraphAsBNode() throws InvalidUpdateInstructionException {
        String result = "[ ";
        for (Statement s : deleteGraph.listStatements().toList()) {
            if (s.getSubject().equals(subject))
                result += "<" + s.getPredicate().getURI() + "> <" + s.getObject().asNode().toString(true) + ">";
            else
                throw new InvalidUpdateInstructionException("Complex updates are currently not allowed. Can not create Bnode.");
            // TODO only works for one statement
        }
        result += " ]";
        return result;
    }

    @Deprecated
    public String getInsertGraphAsBNode() throws InvalidUpdateInstructionException {
        String result = "[ ";
        for (Statement s : insertGraph.listStatements().toList()) {
            if (s.getSubject().equals(subject))
                result += "<" + s.getPredicate().getURI() + "> <" + s.getObject().asNode().toString(true) + ">";
            else
                throw new InvalidUpdateInstructionException("Complex updates are currently not allowed. Can not create Bnode.");
            // TODO only works for one statement
        }
        result += " ]";
        return result;
    }

    public Collection<Statement> getDeleteStatements() throws InvalidUpdateInstructionException {
        return getStatementsFromUpdateGraph(subject, deleteGraph);
    }

    public Collection<Statement> getInsertStatements() throws InvalidUpdateInstructionException {
        return getStatementsFromUpdateGraph(subject, insertGraph);
    }

}
