package org.wso2.deployment;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
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
    static List<String> toKnowledgeBaseNames(int serviceID, JSONObject model) {
        List<String> fileNames = new ArrayList<>();
        JSONArray services = model.getJSONArray("services");

//        for (int i = 0; i < services.length(); i++) {
//            JSONObject service = services.getJSONObject(i);
//            String type = getType(service);
//            JSONArray profiles = service.optJSONArray("profiles");
//            if (type.equals(product)) {
//                if (profiles != null) {
//                    if (profiles.length() == 1) {
//                        fileNames.add(withProfile(type, profiles.getString(0)));
//                    }
//                } else {
//                    fileNames.add(type);
//                }
//            }
//            JSONArray links = service.optJSONArray("links");
//            if (links != null) {
//                for (int j = 0; j < links.length(); j++) {
//                    JSONObject link = links.getJSONObject(i);
//                    int linkIndex = link.getInt("serviceId");
//                    JSONObject linkedService = services.getJSONObject(linkIndex);
//                    String linkedType = getType(linkedService);
//                    if (type.equals(product)) {
//                        fileNames.add(type + "," + linkedType);
//                    } else if (linkedType.equals(product)) {
//                        fileNames.add(linkedType + "," + type);
//                    }
//                }
//
//            }
//        }

        JSONObject service = services.getJSONObject(serviceID);
        String type = getType(service);
        JSONArray profiles = service.optJSONArray("profiles");
        if (profiles != null) {
            if (profiles.length() == 1) {
                fileNames.add(withProfile(type, profiles.getString(0)));
            }
        } else {
            fileNames.add(type);
        }

        JSONArray links = model.getJSONArray("links");
        if (links != null) {
            for (int j = 0; j < links.length(); j++) {
                JSONObject link = links.getJSONObject(j);
                int sourceID = link.getInt("source");
                int targetID = link.getInt("target");
                if (sourceID == serviceID) {
                    JSONObject linkedService = services.getJSONObject(targetID);
                    String linkedType = getType(linkedService);
                    fileNames.add(type + "," + linkedType);
                }



            }

        }

        Collections.sort(fileNames);
        return fileNames;
    }

    private static String withProfile(String type, String profile) {
        return type + "_" + profile;
    }

    private static String getType(JSONObject service) {
        return service.getString("type");
    }
}
