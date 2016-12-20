package org.wso2.deployment;

import difflib.DiffUtils;
import difflib.Patch;
import difflib.PatchFailedException;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.XML;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.*;

/**
 * Main class to generate docker compose artifacts form model json
 */
public class Generate {
    public static final String DIFF = ".diff";

    public static void main(String[] args) throws IOException {
        String modelPath = "src/resources/model.json";
        if(args.length!=2){
            System.err.print("Usage: Generate CLEAN_PRODUCT_LOCATION TARGET_LOCATION");
            System.exit(25);
        }

        //Reading arguments
        String cleanProductLocation = args[0];
        String targetLocation = args[1];

        //Deleting target dir
        Path rootPath = Paths.get(targetLocation);
        try {
            Files.walkFileTree(rootPath, new SimpleFileVisitor<Path>() {
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                    Files.delete(file);
                    return FileVisitResult.CONTINUE;
                }
                @Override
                public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                    Files.delete(dir);
                    return FileVisitResult.CONTINUE;
                }
            });
            System.out.println(targetLocation + " Deleted successfully!");
        } catch(IOException e){
            System.out.println(targetLocation + " Directory not found!");
        }

        //Creating docker compose yaml file
        Path composeFile = Paths.get(targetLocation + "/docker-compose.yml");
        String line = "version: '2'\nservices:\n  svn:\n    image: docker.wso2.com/svnrepo\n";
        Files.createDirectories(Paths.get(targetLocation));
        Files.createFile(composeFile);
        Files.write(composeFile, line.getBytes());

        //process all file names
        List<String> fileNames = getAllKnowledgeBaseNames(modelPath);
        fileNames.forEach(fileName -> {
            //Ignore database, svn and load-balancer
            if (!fileName.startsWith("svn") && !fileName.startsWith("load-balancer")) {
                String diffDir = "../knowledge-base/" + fileName, product;

                System.out.println(fileName);
                //Get first service if it's a pair
                if (fileName.contains(",")) {
                    fileName = fileName.split(",")[0];
                } else {
                    //Append details to compose file
                    addToComposeFile(Paths.get(diffDir + "/dockerfilePart.yml"), fileName, composeFile);
                }

                String targetDir = targetLocation + fileName;

                //Separate product and profile
                if (fileName.contains("_")) {
                    product = fileName.split("_")[0];
                } else {
                    product = fileName;
                }

                //Setup cleanDir
                String version = "2.0.0"; //temp solution
                String cleanDir = cleanProductLocation + product + "-" + version;

                if (Files.exists(Paths.get(diffDir))) {
                    apply(0, Paths.get(diffDir), Paths.get(cleanDir), Paths.get(targetDir));
                }

            }

        });

        //Coping artifacts folder
        Path sourcePath = Paths.get(cleanProductLocation + "/artifacts");
        Path targetPath = Paths.get(targetLocation + "/artifacts/");
        Files.createDirectory(targetPath);
        try {
            Files.walkFileTree(sourcePath, new SimpleFileVisitor<Path>() {
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                    Files.copy(file, targetPath.resolve(file.getFileName()));
                    return FileVisitResult.CONTINUE;
                }
            });
        } catch(IOException e){
            e.printStackTrace();
        }

//        String xmlPath = "src/resources/test.xml";
//        String TEST_XML_STRING = null;
//        int PRETTY_PRINT_INDENT_FACTOR = 4;
//        try {
//            TEST_XML_STRING = new String(Files.readAllBytes(Paths.get(xmlPath)));
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//
//        try {
//            JSONObject xmlJSONObj = XML.toJSONObject(TEST_XML_STRING);
//
//            String jsonPrettyPrintString = xmlJSONObj.toString(PRETTY_PRINT_INDENT_FACTOR);
//            System.out.println(jsonPrettyPrintString);
//
//        } catch (JSONException e) {
//            System.out.println(e.toString());
//        }

    }

    static List<String> getAllKnowledgeBaseNames(String modelPath) {
        List<String> fileNames = new ArrayList<>();
        JSONObject model = getJSONModel(modelPath);
        JSONArray services = model.getJSONArray("services");
        for (int i = 0; i < services.length(); i ++) {
            fileNames.addAll(toKnowledgeBaseNames(i, model));
        }
//        print(fileNames);
        return fileNames;
    }

    /**
     * @param modelPath path to the JSON model file
     * @return created JSON object by reading the file
     */
    static JSONObject getJSONModel(String modelPath){
        String modelStr = null;
        try {
            modelStr = new String(Files.readAllBytes(Paths.get(modelPath)));
        } catch (IOException e) {
            e.printStackTrace();
        }

        return new JSONObject(modelStr);
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
        String type = service.getString("type");
        JSONArray profiles = service.optJSONArray("profiles");
        String serviceName = type;
        if (!"load-balancer".equals(type)) {
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
            if (null != links) {
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
                        String linkedType = linkedService.getString("type");
                        if (!"load-balancer".equals(linkedType)) {
                            JSONArray linkedServiceProfiles = linkedService.optJSONArray("profiles");

                            //If original service had more than one profile
                            if (profiles.length() > 1) {
                                for (int p = 0; p < profiles.length(); p++) {
                                    String tempServiceName = withProfile(type, profiles.getString(p));

                                    addLinks(linkedServiceProfiles, tempServiceName, fileNames, linkedType);
                                }
                            } else {
                                addLinks(linkedServiceProfiles, serviceName, fileNames, linkedType);
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

    private static void print(List<String> fileNames) {
        for (int i = 0; i < fileNames.size(); i ++) {
            System.out.println(fileNames.get(i));
        }
    }

    public static void apply(int level, Path diffDir, Path cleanDir, Path targetDir) {
        try {
            Files.list(diffDir).forEach(file -> {
                int count = diffDir.getNameCount();
                Path fileName = file.getFileName();
                if (Files.isDirectory(file)) {
                    Path subCleanDir;
                    if(level==0 && fileName.toString().equals("carbon")){
                        subCleanDir = cleanDir;
                    }else{
                        subCleanDir= cleanDir.resolve(fileName);
                    }
                    apply(level + 1, file, subCleanDir, targetDir.resolve(fileName));
                } else {
                    String fileNameStr = fileName.toString();
                    if (fileNameStr.endsWith(DIFF)) {
                        String fileNameWithoutDiff = fileNameStr.substring(0, fileNameStr.length() - DIFF.length());
                        applyDiffNative(file, cleanDir.resolve(fileNameWithoutDiff), targetDir.resolve(fileNameWithoutDiff));

                    } else if (!fileNameStr.endsWith("gitignore") && !fileNameStr.endsWith(".yml")){
                        try {
                            Files.createDirectories(targetDir);
                            Files.copy(file, targetDir.resolve(fileNameStr));
                            System.out.println("cp " + file + " " + targetDir.resolve(fileNameStr));
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
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
            System.out.println("patch -f " + targetDir + " < " + diffFile);
            Process process = new ProcessBuilder("patch", "-f", targetDir.toString(), diffFile.toString()).start();

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

    static JSONObject getJSONFromXML(String xmlString) {
        JSONObject temp = XML.toJSONObject(xmlString);
//        System.out.println(temp.toString(4));

        JSONObject result = new JSONObject();

        JSONObject mxGraphModel = temp.getJSONObject("mxGraphModel");
        JSONObject root = mxGraphModel.getJSONObject("root");

        Map<Integer, Integer> serviceMap = new HashMap<>();

        //Resolve images, i.e services
        JSONArray services = new JSONArray();
        if (root.get("Image") instanceof JSONArray) {
            //More than one images exists
            JSONArray images = root.getJSONArray("Image");

            for (int i = 0; i < images.length(); i ++) {
                JSONObject image = images.getJSONObject(i);

                //Adding new service into services array
                services.put(processService((JSONObject)image, serviceMap, i));
            }

        } else {
            //Only one image exists
            JSONObject image = root.getJSONObject("Image");

            //Adding new service into services array
            services.put(processService((JSONObject)image, serviceMap, 0));

        }

        //Resolve connectors, i.e links
        JSONArray links = new JSONArray();
        if (root.has("Connector")) {
            if (root.get("Connector") instanceof JSONArray) {
                //More than one images exists
                JSONArray connectors = root.getJSONArray("Connector");

                for (int i = 0; i < connectors.length(); i++) {
                    JSONObject connector = connectors.getJSONObject(i);

                    //Adding new service into services array
                    links.put(processLink((JSONObject) connector, serviceMap));
                }

            } else {
                //Only one image exists
                JSONObject connector = root.getJSONObject("Connector");

                //Adding new service into services array
                links.put(processLink((JSONObject) connector, serviceMap));

            }
        }
        result.put("services", services);
        result.put("links", links);
//        System.out.println(serviceMap);
        return result;
    }

    private static JSONObject processService(JSONObject image, Map serviceMap, int serviceID) {
        //Constructing new service
        JSONObject mxCell = image.getJSONObject("mxCell");
        String type = mxCell.getString("style");
        String profileStr = image.getString("label");
        JSONObject service = new JSONObject();
        service.put("type", type);

        if (profileStr.contains("/")) {
            //If more than one profiles exists
            JSONArray profiles = new JSONArray();
            String[] profileArray = profileStr.split("/");
            for (String profile : profileArray) {
                profiles.put(profile);
            }
            service.put("profiles", profiles);
        } else {
            service.put("profiles", new JSONArray("[" + profileStr + "]"));
        }

        //Add service ids to map
        int imageID = image.getInt("id");
        serviceMap.put(imageID, serviceID);

        return service;
    }

    private static JSONObject processLink(JSONObject connector, Map serviceMap) {
        JSONObject mxCell = connector.getJSONObject("mxCell");
        int source = mxCell.getInt("source");
        int target = mxCell.getInt("target");

        //Swap if source is greater than target
        if (source > target) {
            int temp = source;
            source = target;
            target = temp;
        }

        JSONObject link = new JSONObject();
        link.put("source", serviceMap.get(source));
        link.put("target", serviceMap.get(target));

        return link;
    }

    static String getXMLFromJSON(JSONObject model) {
        String xml = "<mxGraphModel><root><Diagram id=\"0\"><mxCell/></Diagram><Layer id=\"1\"><mxCell parent=\"0\"/></Layer>";
        int elementID = 2;
        Map<Integer, String> serviceMap = new HashMap<>();

        //Resolve services
        JSONArray services = model.getJSONArray("services");
        for (int i=0; i<services.length(); i++) {
            JSONObject service = services.getJSONObject(i);
            String type = service.getString("type");

            //Add id and profile
            JSONArray profiles = service.optJSONArray("profiles");
            if (profiles.length() < 1) {
                xml += "<Image label=\"\" id=\"" + elementID + "\">";
                //Adding services to map
                serviceMap.put(elementID, type);

            } else if (profiles.length() == 1) {
                xml += "<Image label=\"" + profiles.get(0) + "\" id=\"" + elementID + "\">";
                //Adding services to map
                serviceMap.put(elementID, type + "_" + profiles.get(0));

            } else {
                xml += "<Image label=\"";
                for (int j=0; j<profiles.length(); j++) {
                    xml += profiles.get(j) + "/";
                }
                xml += "\" id=\"" + elementID + "\">";
                //Adding services to map
                serviceMap.put(elementID, type);
            }

            //Add style, i.e type
            xml += "<mxCell style=\"" + type
                    + "\" vertex=\"1\" parent=\"1\"><mxGeometry x=\"0\" y=\"0\" width=\"100\" "
                    + "height=\"100\" as=\"geometry\"/></mxCell></Image>";

            elementID ++;
        }

        //Resolve links
        JSONArray links = model.getJSONArray("links");
        for (int i=0; i<links.length(); i++) {
            JSONObject link = links.getJSONObject(i);
            int source = link.getInt("source") + 2;
            int target = link.getInt("target") + 2;
            xml += "<Connector id=\"" + elementID
                    + "\"><mxCell edge=\"1\" parent=\"1\" source=\"" + source + "\" target=\"" + target + "\"";

            //Adding different colors to links based on source
            if (serviceMap.get(source).contains("database")){
                xml += " style=\"strokeColor=#e900ff;startArrow=classic;\"";
            } else if (serviceMap.get(source).contains("wso2am-analytics")){
                xml += " style=\"strokeColor=#3aa210;dashed=1;\"";
            } else if (serviceMap.get(source).contains("svn")){
                xml += " style=\"strokeColor=#3aa210;startArrow=classic;\"";
            } else if (serviceMap.get(source).contains("traffic-manager")){
                xml += " style=\"strokeColor=#dd0009;startArrow=classic;dashed=1;\"";
            } else if (serviceMap.get(source).contains("gateway-worker")){
                xml += " style=\"strokeColor=#3aa210;dashed=1;\"";
            }
            xml += "><mxGeometry relative=\"1\" as=\"geometry\"/></mxCell></Connector>";
            elementID ++;
        }
        xml += "</root></mxGraphModel>";
        return xml;
    }

    public static void addToComposeFile(Path partFile, String dirname, Path out){

        try {
            List<String> lines = Files.readAllLines(partFile);
            for (String line : lines) {
                if (line.contains("$dirname")) {
                    line = line.replace("$dirname", dirname);
                }
                line += "\n";
                Files.write(out, line.getBytes(), StandardOpenOption.APPEND);

            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
