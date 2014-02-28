package de.hpi.patchr.utils;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.Map;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;

public class PatchrUtils {

	public static void fillPrefixService(String filename) {

		Model prefixModel = ModelFactory.createDefaultModel();
		try {
			prefixModel.read(new FileInputStream(filename), null, "TURTLE");
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		
		// Update Prefix Service
		Map<String, String> prefixes = prefixModel.getNsPrefixMap();
		for (String key : prefixes.keySet()) {
			PrefixService.addPrefix(key, prefixes.get(key));
		}
	}

}
