package de.hpi.patchr.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;

/**
 * @author magnus
 */
public class PatchrUtils {

	/**
	 * @param filename
	 */
	public static void fillPrefixService(String filename) {

		Model prefixModel = ModelFactory.createDefaultModel();
		try {
			prefixModel.read(new FileInputStream(filename), null, "TURTLE");
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		
		// Update Prefix Service
		PrefixService.addPrefixes(prefixModel);
	}
	
	/**
	 * @param path
	 * @return
	 */
	public static boolean fileExists(String path) {
		File f = new File(path);

		return f.exists();
	}

	/**
	 * @param model
	 * @param lang
	 * @param path
	 * @param createParentFolders
	 * @throws FileNotFoundException
	 */
	public static void writeModelToFile(Model model, String lang, String path, boolean createParentFolders) throws FileNotFoundException {
		writeModelToFile(model, lang, new File(path), createParentFolders);
	}

	/**
	 * @param model
	 * @param lang
	 * @param file
	 * @param createParentFolders
	 * @throws FileNotFoundException
	 */
	public static void writeModelToFile(Model model, String lang, File file, boolean createParentFolders) throws FileNotFoundException {
		if (createParentFolders) {
			File parentFolder = file.getParentFile();
			if (!parentFolder.exists())
				file.getParentFile().mkdirs();
		}

		model.write(new FileOutputStream(file, true), lang);
	}

}
