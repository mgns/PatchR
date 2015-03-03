package de.hpi.patchr;

import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.ResourceFactory;
import de.hpi.patchr.api.*;
import de.hpi.patchr.exceptions.InvalidUpdateInstructionException;
import org.apache.log4j.Logger;
import org.joda.time.DateTime;

public class PatchGeneratorWhoKnows {

    private Logger L = Logger.getLogger(getClass());

    private String prefix;
    private VirtuosoPatchRepository repo;
    private Dataset targetDataset;

    private String performer;


    public static void main(String[] args) {
        PatchGeneratorWhoKnows pgwk = new PatchGeneratorWhoKnows();
        pgwk.loadTypes();
    }

    public PatchGeneratorWhoKnows() {
        this.prefix = "http://patchr.semanticmultimedia.org/";
        this.repo = new VirtuosoPatchRepository(prefix, "http://localhost:8890/sparql/", "http://patchr.semanticmultimedia.org");
        this.targetDataset = new Dataset("http://dbpedia.org", "http://dbpedia.org/sparql", "http://dbpedia.org");
        this.performer = prefix + "WhoKnows";
    }

    public void loadTypes() {
        L.info("Start loading.");

        Dataset sourceDataset = new Dataset("http://example.org/", "http://localhost:8890/sparql/", "http://example.org/");

        int count = 0;
        int pagesize = 1000;

        QueryExecution qe = null;

        try {
            String query = "SELECT ?patch ?action ?comment ?subject ?p ?o ?actor ?time WHERE {"
                    + "?patch a <http://purl.org/hpi/patchr#Patch> ;"
                    + "  <http://purl.org/hpi/patchr#comment> ?comment ;"
                    + "  <http://purl.org/hpi/patchr#hasUpdate> ?update ;"
                    + "  <http://purl.org/hpi/patchr#hasProvenance> ?prov ."
                    + "?update <http://webr3.org/owl/guo#target_subject> ?subject ;"
                    + "  ?action ?subgraph ."
                    + "?subgraph ?p ?o ."
                    + "?prov <http://purl.org/net/provenance/ns#involvedActor> ?actor ;"
                    + "  <http://purl.org/net/provenance/ns#performedAt> ?time ."
                    + "FILTER (?action = <http://webr3.org/owl/guo#delete> || ?action = <http://webr3.org/owl/guo#insert>)"
                    + "}";

            while (true) {

                qe = sourceDataset.getQueryExecutionFactory().createQueryExecution(query + " LIMIT " + pagesize + " OFFSET " + count);
                ResultSet rs = qe.execSelect();

                while (rs.hasNext()) {

                    count++;

                    QuerySolution qs = rs.next();

                    String actor = qs.getResource("actor").getLocalName();
                    String action = qs.getResource("action").getLocalName();
                    String subject = qs.getResource("subject").getURI();
                    String p = qs.getResource("p").getURI();
                    String o = qs.getResource("o").getURI();
                    String comment = qs.getLiteral("comment").getString();
                    String time = qs.getLiteral("time").getString();

                    L.info(count + " actor   " + actor);
                    L.info(count + " action    " + action);
                    L.info(count + " subject " + subject);
                    L.info(count + " p       " + p);
                    L.info(count + " o       " + o);
                    L.info(count + " comment   " + comment);
                    L.info(count + " time      " + time);

/*					if (type.equals("http://www.w3.org/2002/07/owl#Thing")) {
                        L.info("Skip " + resource + " a owl:Thing");
						continue;
					}

					if (!includeResource(resource)){
						L.info("Skip " + resource + " without pageID");
						continue;
					}
*/

                    Provenance provenance = new Provenance(performer + "/" + actor, performer, comment, Provenance.CONFIDENCE_MEDIUM, new DateTime(time));
                    try {
                        UpdateInstruction update = new UpdateInstruction(targetDataset.getSparqlGraph(),
                                action.equals("delete") ? ResourceFactory.createStatement(ResourceFactory.createResource(subject), ResourceFactory.createProperty(p), ResourceFactory.createResource(o)) : null,
                                action.equals("insert") ? ResourceFactory.createStatement(ResourceFactory.createResource(subject), ResourceFactory.createProperty(p), ResourceFactory.createResource(o)) : null);
                        Patch patch = new Patch(prefix, null, null, targetDataset, Patch.patchStatus.OPEN, update, provenance);
                        repo.submitPatch(patch);
                    } catch (InvalidUpdateInstructionException ex) {
                        L.error(ex);
                    }
                }

                L.info("Done:" + count);

                if (count == 0 || count % pagesize != 0) {
                    break;
                }
            }
        } finally {
            if (qe != null) {
                qe.close();
            }
        }
    }

}
