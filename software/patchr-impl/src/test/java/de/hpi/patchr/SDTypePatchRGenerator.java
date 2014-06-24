package de.hpi.patchr;

import java.io.FileNotFoundException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.junit.Test;

import com.hp.hpl.jena.util.FileManager;
import com.hp.hpl.jena.vocabulary.RDF;

import de.hpi.patchr.api.Dataset;
import de.hpi.patchr.api.Patch;
import de.hpi.patchr.utils.PatchrUtils;

public class SDTypePatchRGenerator {

	@Test
	public void test() throws ClassNotFoundException, SQLException, FileNotFoundException {
		String baseUri = "http://example.org/";
		String agent = "http://example.org/agent/SDType";
		String performer = "http://example.org/agent/magnus";
		String datasetUri = "http://dbpedia.org";
		String endpointUri = "http://dbpedia.org/sparql";
		String graphUri = "http://dbpedia.org";
		String dataFolder = "/Users/magnus/Datasets/Patchr/SDType/";

		Dataset dataset = new Dataset(datasetUri, endpointUri, graphUri);

		PatchFactory g = new PatchFactory(baseUri, agent, performer, dataset);
		g.setModel(FileManager.get().loadModel(dataFolder + "SDTypePatches.ttl"));

		Class.forName("com.mysql.jdbc.Driver");
//		Connection conn = DriverManager.getConnection("jdbc:mysql://kashyyyk.yovisto.com/dstype?user=dstype&password=dstype");
		Connection conn = DriverManager.getConnection("jdbc:mysql://127.0.0.1:3307/dstype?user=dstype&password=dstype");

		Statement stmt = conn.createStatement();
		ResultSet rs = stmt.executeQuery("SELECT r.resource, t.type, rts.score"
				+ " FROM (SELECT * FROM `en_resulting_types`"
				+ "  WHERE `resource` LIKE '0%' AND score >= 0.1) AS rts"
				+ " JOIN `en_dbpedia_type_to_md5` AS t ON (rts.`type` = t.`type_md5`)"
				+ " JOIN `en_dbpedia_resource_to_md5` AS r ON (rts.`resource` = r.`resource_md5`)"
				//+ " LIMIT 10"
				);
		while (rs.next()) {
			String resource = rs.getString("resource");
			String type = rs.getString("type");
			double score = rs.getDouble("score");

			g.addPatchRequest(Patch.UPDATE_ACTION.insert, g.getModel().createResource(resource), RDF.type, g.getModel().createResource(type), score);
		}

		g.print();
		PatchrUtils.writeModelToFile(g.getModel(), "TURTLE", dataFolder + "SDTypePatches.ttl", true);
	}
}
