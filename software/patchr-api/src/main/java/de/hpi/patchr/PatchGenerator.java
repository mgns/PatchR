package de.hpi.patchr;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import com.hp.hpl.jena.util.FileManager;
import de.hpi.patchr.api.Dataset;
import de.hpi.patchr.api.Patch;
import de.hpi.patchr.exceptions.InvalidUpdateInstructionException;
import de.hpi.patchr.utils.PatchrUtils;
import org.apache.commons.cli.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;

public class PatchGenerator {

    private static Logger L = LoggerFactory.getLogger(PatchGenerator.class);

    private static final Options cliOptions = new Options();

    static {
        cliOptions.addOption("h", "help", false, "show this help message");
        cliOptions.addOption("d", "dataset-uri", true, "the URI of the dataset (required)");
        cliOptions.addOption("e", "endpoint", true, "the endpoint to run the tests on (required)");
        cliOptions.addOption("g", "graph", true, "the graphs to use (separate multiple graphs with ',' (no whitespaces) (defaults to '')");
        cliOptions.addOption("a", "agent", true, "the URI of the agent (defaults to 'http://example.org/agent')");
        cliOptions.addOption("b", "base", true, "the base URI for generated patches (defaults to 'http://example.org/')");
        cliOptions.addOption("f", "data-folder", true, "the location of the data folder (defaults to './data/'");
    }

    /**
     * @param args
     * @throws ParseException
     * @throws IOException
     */
    public static void main(String[] args) throws ParseException, IOException {
        /* <cliStuff> */
        CommandLineParser cliParser = new GnuParser();
        CommandLine commandLine = cliParser.parse(cliOptions, args);

        if (commandLine.hasOption("h") || !commandLine.hasOption("d") || !commandLine.hasOption("e") || !commandLine.hasOption("g")) {

            if (!commandLine.hasOption("h"))
                System.out.println("\nError: Required arguments are missing.");

            HelpFormatter formatter = new HelpFormatter();
            formatter.printHelp("patchgenerator", cliOptions);
            System.exit(1);
        }

        String datasetUri = commandLine.getOptionValue("d");
        String endpointUri = commandLine.getOptionValue("e");
        String graphUri = commandLine.getOptionValue("g", "");
        String agent = commandLine.getOptionValue("a", "http://example.org/agent");
        String baseUri = commandLine.getOptionValue("b", "http://example.org/");
        String dataFolder = commandLine.getOptionValue("f", "./data/");
        /* </cliStuff> */

        if (!PatchrUtils.fileExists(dataFolder)) {
            L.error("Path: " + new File(dataFolder).getCanonicalPath() + " does not exists, use -f argument");
            System.exit(1);
        }

        Dataset dataset = new Dataset(datasetUri, endpointUri, graphUri);

        CommonAgentOnDatasetPatchFactory g = new CommonAgentOnDatasetPatchFactory(baseUri, agent, null, dataset);
        Model model = ModelFactory.createDefaultModel();
        // g.createPatchRequest(Action.insert,
        // g.getModel().createResource("http://dbpedia.org/resource/Oregon"),
        // g.getModel().createProperty("http://dbpedia.org/ontology/language"),
        // g.getModel().createResource("http://dbpedia.org/resource/English_language"));
        // (g.createPatchRequest(Action.delete,
        // g.model.createResource("http://dbpedia.org/resource/Oregon"),
        // g.model.createProperty("http://dbpedia.org/ontology/language"),
        // g.model.createResource("http://dbpedia.org/resource/De_jure"));
        // g.createPatchRequest(Action.delete,
        // g.getModel().createResource("http://dbpedia.org/resource/Oregon"),
        // g.getModel().createProperty("http://dbpedia.org/ontology/language"),
        // g.getModel().createResource("http://dbpedia.org/resource/De_juress"));

        if (PatchrUtils.fileExists(dataFolder + "patches.ttl")) {
            model.add(FileManager.get().loadModel(dataFolder + "patches.ttl"));
        }

        int count = 0;
        if (PatchrUtils.fileExists(dataFolder + "delete.n3")) {
            Model deleteModel = FileManager.get().loadModel(dataFolder + "delete.n3");
            for (StmtIterator i = deleteModel.listStatements(); i.hasNext(); ) {
                L.info("Delete " + ++count);
                Statement s = i.next();
                try {
                    Patch patch = g.createPatch(s.getSubject().asResource(), s.getPredicate(), s.getObject(), null, null);
                    patch.getAsResource(model);
                } catch (InvalidUpdateInstructionException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        }
        count = 0;
        if (PatchrUtils.fileExists(dataFolder + "insert.n3")) {
            Model m = FileManager.get().loadModel(dataFolder + "insert.n3");
            for (StmtIterator i = m.listStatements(); i.hasNext(); ) {
                L.info("Insert " + ++count);
                Statement s = i.next();
                try {
                    Patch patch = g.createPatch(s.getSubject().asResource(), null, null, s.getPredicate(), s.getObject());
                    patch.getAsResource(model);
                } catch (InvalidUpdateInstructionException e) {
                    e.printStackTrace();
                }
            }
        }

        model.write(System.out, "TURTLE");
        PatchrUtils.writeModelToFile(model, "TURTLE", dataFolder + "patches.ttl", true);
    }
}
