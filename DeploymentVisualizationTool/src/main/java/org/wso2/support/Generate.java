package org.wso2.support;

import org.json.JSONArray;
import org.json.JSONObject;
import org.json.XML;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Main class to generate docker compose artifacts form model json
 */
public class Generate {
    public static final String DIFF = ".diff";
    public static final String CLEAN_PRODUCT_LOCATION = "/home/vihanga/Downloads/Compare/";
    public static final String KNOWLEDGE_BASE_LOCATION = "/var/www/html/Deployment-Visualization-Tool/DeploymentVisualizationTool/knowledge-base/";
    public static final String TARGET_LOCATION = "/var/www/html/Deployment-Visualization-Tool/DeploymentVisualizationTool/target/DeploymentVisualizationTool-1.0-SNAPSHOT/out/dockerConfig/";
    
    /**
     * Generate and compress a complete docker configurations folder for a given graph,
     * by reading the XML generated in the  source view of the graph UI. <br>If gen is true, links in
     * the XML will be ignored and regenerated.
     * @param xmlString XML of the graph
     * @param gen boolean indicator to specify auto generation of links
     * @return URI to the created zip archive
     */
    public static String getConfigFromXML(String xmlString, boolean gen) throws IOException {

        System.out.println("\n\n\n=====================\n=====================\n=====================\n");

        if (xmlString == null) {
            return "";
        }

        //Deleting target dir
        Path rootPath = Paths.get(TARGET_LOCATION).getParent();
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
            System.out.println(rootPath + " Deleted successfully!");
        } catch(IOException e){
            System.out.println(rootPath + " Directory not found!");
        }

        //Get JSON model
        List<String> fileNames = new ArrayList<>();
        JSONObject model;
        if (gen) {
            model = getJSONFromXMLAutoGenLinks(xmlString);
        } else {
            model = getJSONFromXML(xmlString);
        }

        //Get all file names
        JSONArray services = model.getJSONArray("services");
        for (int i = 0; i < services.length(); i ++) {
            fileNames.addAll(toKnowledgeBaseNames(model, i));
        }

        //Creating docker compose yaml file
        Path composeFile = Paths.get(TARGET_LOCATION + "/docker-compose.yml");
        String line = "version: '2'\nservices:\n";
        Files.createDirectories(Paths.get(TARGET_LOCATION));
        Files.createFile(composeFile);
        Files.write(composeFile, line.getBytes());

        //Add svnrepo to dockerfile if needed
        for (int i=0; i<fileNames.size(); i++) {
            if (fileNames.get(i).startsWith("svnrepo")) {
                try {
                    System.out.println("------"+fileNames.get(i));
                    String svnLine = "  svnrepo:\n    image: docker.wso2.com/svnrepo\n";
                    Files.write(composeFile, svnLine.getBytes(), StandardOpenOption.APPEND);
                    break;
                } catch (IOException ex) {
                    Logger.getLogger(Generate.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }

        //process all file names
        fileNames.forEach(fileName -> {

            //Ignore svnrepo and load-balance
            if (!fileName.startsWith("svnrepo") && !fileName.startsWith("load-balancer")) {
                String diffDir = KNOWLEDGE_BASE_LOCATION + fileName, product;

                System.out.println(fileName);
                //Get first service if it's a pair
                if (fileName.contains(",")) {
                    fileName = fileName.split(",")[0];
                } else {
                    //Append details to compose file
                    addToComposeFile(Paths.get(diffDir + "/dockerfilePart.yml"), fileName, composeFile);
                }

                String targetDir = TARGET_LOCATION + fileName;

                //Separate product and profile
                if (fileName.contains("_")) {
                    product = fileName.split("_")[0];
                } else {
                    product = fileName;
                }

                //Setup cleanDir
                String cleanDir = CLEAN_PRODUCT_LOCATION + product;

                if (Files.exists(Paths.get(diffDir))) {
                    apply(0, Paths.get(diffDir), Paths.get(cleanDir), Paths.get(targetDir));
                }
            }

        });

        //Coping artifacts folder
        Path sourcePath = Paths.get(CLEAN_PRODUCT_LOCATION + "/artifacts");
        Path targetPath = Paths.get(TARGET_LOCATION + "/artifacts/");
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

        //Compressing directory and returning file path
        zip(TARGET_LOCATION);
        return "out/dockerConfig.zip";
    }
    
    /**
     * Return a complete set of knowledge-base folders by reading a
     * JSON model
     * @param modelPath path to the JSON model file
     * @return created JSON object by reading the file
     */
    static List<String> getAllKnowledgeBaseNames(String modelPath) {
        List<String> fileNames = new ArrayList<>();
        JSONObject model = getJSONModel(modelPath);
        JSONArray services = model.getJSONArray("services");
        for (int i = 0; i < services.length(); i ++) {
            fileNames.addAll(toKnowledgeBaseNames(model, i));
        }
        return fileNames;
    }

    /**
     * Read the json model file and return a json object
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
     * Return a set of knowledge-base folders for a specific service.
     * @param model JSON object with services and links
     * @param serviceID Integer id of the service
     * @return list of dir names that can be looked up in KB
     */
    static List<String> toKnowledgeBaseNames(JSONObject model, int serviceID) {
        List<String> fileNames = new ArrayList<>();
        JSONArray services = model.getJSONArray("services");
        
        JSONObject service = services.getJSONObject(serviceID);
        String type = service.getString("type");

        String serviceName = type;
        //Load balancer node will be ignored
        if (!"load-balancer".equals(type))
        {
            //Resolve self object
            JSONArray profiles = service.optJSONArray("profiles");
            if (profiles != null) {
                if (profiles.length() > 0) {
                    for (int k = 0; k < profiles.length(); k++) {
                        serviceName += "_" + profiles.getString(k);
                    }
                }
            }
            fileNames.add(serviceName);
            System.out.println(serviceName);

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
                            //Add links to fileNames list -> Eg: wso2am_publisher,database
                            System.out.println(linkedServiceProfiles);
                            if (linkedServiceProfiles != null && linkedServiceProfiles.length() > 0) {
                                for (int k = 0; k < linkedServiceProfiles.length(); k++) {
                                    linkedType += "_" + linkedServiceProfiles.getString(k);
                                }
                            }
                            fileNames.add(serviceName + "," + linkedType);
                        }
                    }
                }
            }
        }

        Collections.sort(fileNames);
        return fileNames;
    }

    /**
     * Call the applyDiffNative on each file of a diff directory by traveling recursively
     * @param level Integer level to initiate recursive call
     * @param diffDir Path to diff directory
     * @param cleanDir Path to clean directory
     * @param targetDir Path to target directory
     */
    public static void apply(int level, Path diffDir, Path cleanDir, Path targetDir) {
        try {
            Files.list(diffDir).forEach(file -> {
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

    /**
     * Apply a diff to a single file using linux patch command
     * @param diffFile Path to diff file
     * @param cleanFile Path to clean file
     * @param targetDir Path to target directory
     */
    private static void applyDiffNative(Path diffFile, Path cleanFile, Path targetDir) {
        try {
            Files.createDirectories(targetDir.getParent());
            if (!Files.isRegularFile(targetDir)) {
                Files.copy(cleanFile, targetDir);
            }
            System.out.println("\tpatch -f " + targetDir + " < " + diffFile);
            Process process = new ProcessBuilder("patch", "-f", targetDir.toString(), diffFile.toString()).start();

            process.waitFor();
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    static JSONObject getJSONFromXML(String xmlString) {
        System.out.println("Generate.getJSONFromXML...");
        JSONObject temp = XML.toJSONObject(xmlString);

        JSONObject result = new JSONObject();

        JSONObject mxGraphModel = temp.getJSONObject("mxGraphModel");
        JSONObject root = mxGraphModel.getJSONObject("root");

        //Map to store service IDs and their model indexes -> serviceID:modelIndex
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
                    JSONObject obj = processLink((JSONObject) connector, serviceMap);
                    if (obj != null) {
                        links.put(obj);
                    }
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

    /**
     * Test
     * @param xmlString XML of the graph
     * @return URI to the created zip archive
     */
    static JSONObject getJSONFromXMLAutoGenLinks(String xmlString) {
        System.out.println("Generate.getJSONFromXMLAutoGenLinks...");
        JSONObject temp = XML.toJSONObject(xmlString);

        JSONObject result = new JSONObject();

        JSONObject mxGraphModel = temp.getJSONObject("mxGraphModel");
        JSONObject root = mxGraphModel.getJSONObject("root");

        //Map to store service IDs and their model indexes -> serviceID:modelIndex
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

        Set<Integer> keys = serviceMap.keySet();
        Integer[] keyArray = keys.toArray(new Integer[keys.size()]);

        //Add a connection for each pair of services.
        for (int i=0; i<serviceMap.size(); i++) {
            for (int j=i+1; j<serviceMap.size(); j++) {
                JSONObject link = new JSONObject();
                link.put("source", serviceMap.get(keyArray[i]));
                link.put("target", serviceMap.get(keyArray[j]));
                links.put(link);
            }
        }

        result.put("services", services);
        result.put("links", links);
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
        if (mxCell.has("source") && mxCell.has("target")) {
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
        } else {
            return null;
        }
    }

    //Add new entry to docker compose file
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

    public static String getXMLFromJSON(JSONObject model) {
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
            } else if (serviceMap.get(source).contains("svnrepo")){
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

    /**
     * Compress a given folder in zip format and return the URI
     * @param targetLocation path to the folder
     * @return URI to the created zip archive
     */
    private static String zip(String targetLocation) {

        String zipFile = targetLocation.substring(0, targetLocation.length()-1) + ".zip";

        try {
            System.out.println("zip -r " + zipFile + " " + ".");
            Process process = null;
            ProcessBuilder p = new ProcessBuilder("zip", "-r", zipFile, ".");
            p.directory(new File(targetLocation));
            process = p.start();
            process.waitFor();
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }

        return zipFile;
    }
}
