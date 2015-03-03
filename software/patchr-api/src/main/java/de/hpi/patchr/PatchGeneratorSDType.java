package de.hpi.patchr;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import de.hpi.patchr.api.*;
import org.apache.log4j.Logger;

import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.rdf.model.ResourceFactory;
import com.hp.hpl.jena.vocabulary.RDF;

import de.hpi.patchr.api.VirtuosoPatchRepository;
import de.hpi.patchr.exceptions.InvalidUpdateInstructionException;

public class PatchGeneratorSDType {

	private Logger L = Logger.getLogger(getClass());

	private String prefix;
	private VirtuosoPatchRepository repo;
	private Dataset targetDataset;

	private String actor;
	private String performer;


	public static void main(String[] args) {
		PatchGeneratorSDType pgsdt = new PatchGeneratorSDType();
		pgsdt.loadTypes();
	}

	public PatchGeneratorSDType() {
		this.prefix = "http://patchr.semanticmultimedia.org/";
		this.repo = new VirtuosoPatchRepository(prefix, "http://localhost:8890/sparql/", "http://patchr.semanticmultimedia.org");
		this.targetDataset = new Dataset("http://dbpedia.org", "http://dbpedia.org/sparql", "http://dbpedia.org");
		this.actor = "http://patchr.semanticmultimedia.org/SDType";
		this.performer = "http://patchr.semanticmultimedia.org/SDType";
	}

	public void loadTypes() {
		L.info("Start loading.");
		
		Connection con = null;
		Statement st = null;
		ResultSet rs = null;

		String url = "jdbc:mysql://localhost:3306/dstype";
		String user = "root";
		String password = "start123";

		int count = 5910000;//822000
		int pagesize = 1000;
		
		try {
			con = DriverManager.getConnection(url, user, password);
			st = con.createStatement();
			// score 0.1: 3979458
			// score 0.2: 3496751
			// score 0.3: 3185971
			
			while (true) {
				
				//rs = st.executeQuery("SELECT resource, type, score FROM en_nt_resulting_types_readable WHERE resource IN (SELECT resource FROM en_nt_dbpedia_page_id) AND score > 0.3 AND type != 'http://www.w3.org/2002/07/owl#Thing' LIMIT " + pagesize + " OFFSET " + count);
				rs = st.executeQuery("SELECT resource, type, score FROM en_nt_resulting_types_readable_only_page_id WHERE score > 0.3 LIMIT " + pagesize + " OFFSET " + count);
	
				while (rs.next()) {
					count++;

					double score = rs.getDouble("score");
					String resource = rs.getString("resource");
					String type = rs.getString("type");
					
/*					if (type.equals("http://www.w3.org/2002/07/owl#Thing")) {
						L.info("Skip " + resource + " a owl:Thing");
						continue;
					}

					if (!includeResource(resource)){
						L.info("Skip " + resource + " without pageID");
						continue;
					}
*/				
					L.info(resource + " a " + type + " : " + score);
					
					Provenance provenance = new Provenance(actor, performer, score);
					try {
						UpdateInstruction update = new UpdateInstruction(
								targetDataset.getSparqlGraph(),
								null,
								ResourceFactory.createStatement(ResourceFactory.createResource(resource), RDF.type, ResourceFactory.createResource(type)));
						Patch patch = new Patch(prefix, null, null, targetDataset, Patch.patchStatus.OPEN, update, provenance);
						repo.submitPatch(patch);
					} catch (InvalidUpdateInstructionException ex) {
						L.error(ex);
					}
				}

				L.info("Done:" + count);

				if (count % pagesize != 0) {
					break;
				}
			}
		} catch (SQLException ex) {
			L.error(ex);
		} finally {
			try {
				if (rs != null) {
					rs.close();
				}
				if (st != null) {
					st.close();
				}
				if (con != null) {
					con.close();
				}
			} catch (SQLException ex) {
				L.error(ex);
			}
		}
	}

	private boolean includeResource(String resource) {
		QueryExecution qe = targetDataset.getQueryExecutionFactory().createQueryExecution("ASK {<" + resource + "> <http://dbpedia.org/ontology/wikiPageID> ?pageID }");
		return qe.execAsk();
	}
}
