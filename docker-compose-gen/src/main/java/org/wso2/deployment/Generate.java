package org.wso2.deployment;

import difflib.DiffUtils;
import difflib.Patch;
import difflib.PatchFailedException;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Main class to generate docker compose artifacts form model json
 */
public class Generate {
    public static final String DIFF = ".diff";

    public static void main(String[] args) throws IOException {
        apply(0, Paths.get("../knowledge-base/wso2am_publisher"), Paths.get("/home/manu/setups/carbon/wso2am-2.0.0/"), Paths.get("/tmp/wso2am-2.0.0"));
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
        } else {
            fileNames.add(type);
        }

        //Resolve links
        JSONArray links = model.getJSONArray("links");
        if (links != null) {
            for (int j = 0; j < links.length(); j++) {
                JSONObject link = links.getJSONObject(j);
                int sourceID = link.getInt("source");
                int targetID = link.getInt("target");
                JSONObject linkedService = null;
                if (sourceID == serviceID) {
                    //links start from this service
                    linkedService = services.getJSONObject(targetID);
                } else if (targetID == serviceID) {
                    //links end from this service
                    linkedService = services.getJSONObject(sourceID);
                }

                if (linkedService != null) {
                    String linkedType = getType(linkedService);

                    JSONArray linkedServiceProfiles = linkedService.optJSONArray("profiles");
                    if (profiles.length() > 1) {
                        //If source service had more than one profile
                        for (int p = 0; p < profiles.length(); p++) {
                            serviceName = withProfile(type, profiles.getString(p));

                            addLinks(linkedServiceProfiles, serviceName, fileNames, linkedType);
                        }

                    } else {
                        addLinks(linkedServiceProfiles, serviceName, fileNames, linkedType);
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

    private static void addLinks(JSONArray profiles, String serviceName, List<String> fileNames, String linkedType) {
        if (profiles != null) {
            if (profiles.length() == 0) {
                fileNames.add(serviceName + "," + linkedType);
            } else {
                for (int k = 0; k < profiles.length(); k++) {
                    fileNames.add(serviceName + "," + withProfile(linkedType, profiles.getString(k)));
                }
            }
        }
    }

    public static void apply(int level, Path diffDir, Path cleanDir, Path targetDir) {
        try {
            Files.list(diffDir).forEach(file -> {
                int count = diffDir.getNameCount();
                Path fileName = file.getFileName();
                if (Files.isDirectory(file)) {
                    apply(level + 1, file, cleanDir.resolve(fileName), targetDir.resolve(fileName));
                } else {
                    String fileNameStr = fileName.toString();
                    if (fileNameStr.endsWith(DIFF)) {
                        String fileNameWithoutDiff = fileNameStr.substring(0, fileNameStr.length() - DIFF.length());
                        applyDiffNative(file, cleanDir.resolve(fileNameWithoutDiff), targetDir.resolve(fileNameWithoutDiff));
                    }
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void applyDiffNative(Path diffFile, Path cleanFile, Path targetDir) {
        try {
            Files.createDirectories(targetDir.getParent());
            if (!Files.isRegularFile(targetDir)) {
                Files.copy(cleanFile, targetDir);
            }
            System.out.println("patch " + targetDir + " < " + diffFile);
            Process process = new ProcessBuilder("patch", targetDir.toString()).start();

            PrintWriter writer = new PrintWriter(process.getOutputStream());
            BufferedReader reader = Files.newBufferedReader(diffFile);
            String line;
            while ((line = reader.readLine()) != null) {
                writer.println(line);
            }
            writer.close();

            process.waitFor();
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    private static void applyDiff(Path diffFile, Path cleanFile, Path targetDir) {
        try {
            System.out.println(targetDir);
            List<String> diff = Files.readAllLines(diffFile);
            List<String> clean = Files.readAllLines(cleanFile);
            diff.add(0, "+++");
            Patch<String> patch = DiffUtils.parseUnifiedDiff(diff);
            List<String> patched = DiffUtils.patch(clean, patch);

            Files.createDirectories(targetDir.getParent());
            Files.write(targetDir, patched);
        } catch (IOException | PatchFailedException e) {
            e.printStackTrace();
        }
    }

}
