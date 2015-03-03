package de.hpi.patchr;

import de.hpi.patchr.api.Dataset;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by magnus on 16.01.15.
 */
public class DatasetProvider {

    private Map<String, Dataset> datasetMap;

    public DatasetProvider() {
        datasetMap = new HashMap<String, Dataset>();
    }

    public void addDataset(String uri, Dataset dataset) {
        datasetMap.put(uri, dataset);
    }

    public Dataset getDataset(String uri) {
        return datasetMap.get(uri);
    }
}
