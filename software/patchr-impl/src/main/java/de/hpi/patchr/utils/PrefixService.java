package de.hpi.patchr.utils;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * @author magnus
 */
public class PrefixService {

	final private static Map<String, String> prefixes = new HashMap<String, String>();

    private PrefixService() {
    }

    public static void addPrefix(String prefix, String uri) {
        prefixes.put(prefix, uri);
    }

    public static String getPrefix(String id) {
        return prefixes.get(id);
    }

    public static Set<String> getPrefixList() {
        return prefixes.keySet();
    }

    public static Map<String, String> getPrefixMap() {
        return prefixes;
    }
}
