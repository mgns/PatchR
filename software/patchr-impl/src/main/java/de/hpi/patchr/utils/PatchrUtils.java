package de.hpi.patchr.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.UUID;

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

	/**
	 * @param prefix
	 * @param cl
	 * @return
	 */
	public static String generateUri(String prefix, String cl) {
		StringBuilder sb = new StringBuilder(prefix);
		if (cl != null)
			sb.append(cl + "-");
		else
			sb.append("uuid-");
		sb.append(UUID.randomUUID());
		return sb.toString();
	}

	/**
	 * @param prefix
	 * @param cl
	 * @return
	 */
	public static String generateUri(String prefix, String cl, String name) {
		StringBuilder sb = new StringBuilder(prefix);
		if (cl != null)
			sb.append(cl + "-");
		else
			sb.append("uuid-");
		try {
			// sb.append(UUID.fromString(String.format("%040x", new
			// BigInteger(1, name.getBytes("utf8")))));
			byte[] bytesOfMessage = name.getBytes("UTF-8");

			MessageDigest sha1 = MessageDigest.getInstance("SHA-1");
			sb.append(hexEncode(sha1.digest(bytesOfMessage)));
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
		return sb.toString();
	}

	static private String hexEncode(byte[] aInput) {
		StringBuilder result = new StringBuilder();
		char[] digits = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f' };
		for (int idx = 0; idx < aInput.length; ++idx) {
			byte b = aInput[idx];
			result.append(digits[(b & 0xf0) >> 4]);
			result.append(digits[b & 0x0f]);
		}
		return result.toString();
	}
}
