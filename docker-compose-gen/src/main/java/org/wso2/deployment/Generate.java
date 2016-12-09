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

        //Resolve self object
        JSONObject service = services.getJSONObject(serviceID);
        String type = getType(service);
        JSONArray profiles = service.optJSONArray("profiles");
        String serviceName = type;
        if (profiles != null) {
            if (profiles.length() == 0) {
                fileNames.add(serviceName);
            } else if (profiles.length() == 1) {
                serviceName = withProfile(type, profiles.getString(0));
                fileNames.add(serviceName);
            }
        }


        //Resolve links
        JSONArray links = model.getJSONArray("links");
        if (links != null) {
            for (int j = 0; j < links.length(); j++) {
                JSONObject link = links.getJSONObject(j);
                int sourceID = link.getInt("source");
                int targetID = link.getInt("target");

                if (sourceID == serviceID) {
                    //links start from this service
                    JSONObject linkedService = services.getJSONObject(targetID);
                    String linkedType = getType(linkedService);

                    JSONArray linkedServiceProfiles = linkedService.optJSONArray("profiles");
                    if (profiles.length() > 1) {
                        //If source service had more than one profile
                        for (int p = 0; p < profiles.length(); p++) {
                            serviceName = withProfile(type, profiles.getString(p));

                            if (linkedServiceProfiles != null) {
                                if (linkedServiceProfiles.length() == 0) {
                                    fileNames.add(serviceName + "," + linkedType);
                                } else {
                                    for (int k = 0; k < linkedServiceProfiles.length(); k++) {
                                        fileNames.add(serviceName + "," + withProfile(linkedType, linkedServiceProfiles.getString(k)));
                                    }
                                }
                            }
                        }
                    } else {
                        if (linkedServiceProfiles != null) {
                            if (linkedServiceProfiles.length() == 0) {
                                fileNames.add(serviceName + "," + linkedType);
                            } else {
                                for (int k = 0; k < linkedServiceProfiles.length(); k++) {
                                    fileNames.add(serviceName + "," + withProfile(linkedType, linkedServiceProfiles.getString(k)));
                                }
                            }
                        }
                    }



                }

                if (targetID == serviceID) {
                    //links end from this service
                    JSONObject linkedService = services.getJSONObject(sourceID);
                    String linkedType = getType(linkedService);

                    JSONArray linkedServiceProfiles = linkedService.optJSONArray("profiles");
                    if (linkedServiceProfiles != null) {
                        if (linkedServiceProfiles.length() == 0) {
                            fileNames.add(serviceName + "," + linkedType);
                        } else {
                            for (int k = 0; k < linkedServiceProfiles.length(); k++) {
                                fileNames.add(serviceName + "," + withProfile(linkedType, linkedServiceProfiles.getString(k)));
                            }
                        }
                    }
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
