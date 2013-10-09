package de.hpi.patchr.vocab;

import com.hp.hpl.jena.rdf.model.Model;

public class PatchrNamespaces {

	public static final String PAT = "http://purl.org/hpi/patchr#";
	public static final String GUO = "http://webr3.org/owl/guo#";
	public static final String PRV = "http://purl.org/net/provenance/ns#";

	public static final String XSD = "http://www.w3.org/2001/XMLSchema#";
	public static final String OWL = "http://www.w3.org/2002/07/owl#";
	public static final String RDFS = "http://www.w3.org/2000/01/rdf-schema#";
	public static final String RDF = "http://www.w3.org/1999/02/22-rdf-syntax-ns#";
	public static final String FOAF = "http://xmlns.com/foaf/0.1/";
	public static final String VOID = "http://rdfs.org/ns/void";
	public static final String DC = "http://purl.org/dc/terms/";
	
	public static void addPrefixes(Model model) {
		addPatPrefix(model);
		addGuoPrefix(model);
		addPrvPrefix(model);
	}

	public static void addPatPrefix(Model model) {
		model.setNsPrefix("pat", PAT);
	};

	public static void addGuoPrefix(Model model) {
		model.setNsPrefix("guo", GUO);
	};

	public static void addPrvPrefix(Model model) {
		model.setNsPrefix("prv", PRV);
	};
	
}
