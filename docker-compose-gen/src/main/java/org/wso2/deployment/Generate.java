package org.wso2.deployment;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Main class to generate docker compose artifacts form model json
 */
public class Generate {
    public static void main(String[] args) {
    }

    /**
     * @param model JSON object with links and nodes
     * @return list of dir names that can be looked up in KB
     */
    static List<String> toKnowledgeBaseNames(JSONObject model) {
        List<String> fileNames = new ArrayList<>();
        JSONArray services = model.getJSONArray("services");
        for (int i = 0; i < services.length(); i++) {
            JSONObject service = services.getJSONObject(i);
            String type = service.getString("type");
            fileNames.add(type);
        }
        return fileNames;
    }
}
