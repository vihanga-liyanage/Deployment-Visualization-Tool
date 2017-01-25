/*
*  Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied.  See the License for the
* specific language governing permissions and limitations
* under the License.
*/

package org.wso2.support;

import java.io.BufferedReader;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.XML;

import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Main class to generate docker compose artifacts form model JSON
 * @author Vihanga Liyanage  <vihanga@wso2.com>
 */
public class Generate {
    
    /**
     * Extension of the diff files
     */
    private static final String DIFF = ".diff";
    
    /**
     * Directory location where the original WSO2 products can be fine
     */
    private static final String CLEAN_PRODUCT_LOCATION = "/home/vihanga/Downloads/Compare/";
    
    /**
     * Directory location of the knowledge base
     */
    private static final String KNOWLEDGE_BASE_LOCATION = "webapps/DeploymentVisualizationTool-1.0-SNAPSHOT/resources/knowledge-base/";
    
    /**
     * Directory location where the final configuration files will be created
     */
    private static final String TARGET_LOCATION = "webapps/DeploymentVisualizationTool-1.0-SNAPSHOT/out/dockerConfig/";

    /**
     * Generate and compress a complete docker configurations folder for a given graph,
     * by reading the XML generated in the  source view of the graph UI. <br>If gen is true, links in
     * the XML will be ignored and regenerated.
     * @param xmlString XML of the graph
     * @param autoGen boolean indicator to specify auto generation of links
     * @return URI to the created zip archive
     * @throws IOException
     */
    public static String getConfigFromXML(String xmlString, boolean autoGen) throws IOException {

        Logger.getLogger(Generate.class.getName()).log(Level.INFO, "\n=====================\n");
        
        Logger.getLogger(Generate.class.getName()).log(Level.INFO, System.getProperty("user.dir"));
        

        if (xmlString == null) {
            return "";
        }

        //Deleting target dir
        if (!deleteDir(TARGET_LOCATION))
            return null;

        //Get JSON model
        List<String> fileNames = new ArrayList<>();
        JSONObject model = getJSONFromXML(xmlString, autoGen);

        //Get all file names
        JSONArray services = model.getJSONArray("services");
        for (int i = 0; i < services.length(); i ++) {
            fileNames.addAll(getKnowledgeBaseNames(model, i));
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
                    Logger.getLogger(Generate.class.getName()).log(Level.INFO, "------"+fileNames.get(i));
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

                Logger.getLogger(Generate.class.getName()).log(Level.INFO, fileName);
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
                    initApplyDiff(0, Paths.get(diffDir), Paths.get(cleanDir), Paths.get(targetDir));
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
            Logger.getLogger(Generate.class.getName()).log(Level.SEVERE, null, e);
        }

        //Compressing directory and returning file path
        zip(TARGET_LOCATION);
        return "out/dockerConfig.zip";
    }
    
    /**
     * Delete a given directory recursively 
     * @param targetLocation Directory location to delete
     */
    private static boolean deleteDir(String targetLocation) {
        Path rootPath = Paths.get(targetLocation).getParent();
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
            Logger.getLogger(Generate.class.getName()).log(Level.INFO, rootPath + " Deleted successfully!");
            return true;
        } catch(IOException e){
            Logger.getLogger(Generate.class.getName()).log(Level.SEVERE, rootPath + " Directory not found!", e);
            return false;
        }
    }
    
    /**
     * Process a given mxGraph graph XML string and generate the JSON model<br>
     * If autoGen is passed true, links in the XML will be ignored and 
     * consider n^2 connections to build the JSON model
     * @param xmlString XML of the graph
     * @param autoGen boolean indicator to auto generate links
     */
    static JSONObject getJSONFromXML(String xmlString, boolean autoGen) {
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
        if (autoGen) {
            
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
        } else {
            
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
        }
        result.put("services", services);
        result.put("links", links);
        return result;
    }

    /**
     * Process image object retrieved from the graph XML and return a service
     * @param image JSON object of XML graph image
     * @param serviceMap Map to store service IDs and their model indexes
     * @param serviceID Model ID of the service to be created
     */
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

    /**
     * Process connector object retrieved from the graph XML and return a link
     * @param connector JSON object of XML graph connector
     * @param serviceMap Map to store service IDs and their model indexes
     */
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

    /**
     * Return a set of knowledge-base folders for a specific service.
     * @param model JSON object with services and links
     * @param serviceID Integer id of the service
     * @return list of dir names that can be looked up in KB
     */
    static List<String> getKnowledgeBaseNames(JSONObject model, int serviceID) {
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
                    //Using a buffer for better performance 
                    StringBuffer buf = new StringBuffer();
                    for (int k = 0; k < profiles.length(); k++) {
                        buf.append("_" + profiles.getString(k));
                    }
                    serviceName += buf.toString();
                }
            }
            fileNames.add(serviceName);

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
     * Call the applyDiff on each file of a diff directory by traveling recursively
     * @param level Integer level to initiate recursive call
     * @param diffDir Path to diff directory
     * @param cleanDir Path to clean directory
     * @param targetDir Path to target directory
     */
    private static void initApplyDiff(int level, Path diffDir, Path cleanDir, Path targetDir) {
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
                    initApplyDiff(level + 1, file, subCleanDir, targetDir.resolve(fileName));
                } else {
                    String fileNameStr = fileName.toString();
                    if (fileNameStr.endsWith(DIFF)) {
                        String fileNameWithoutDiff = fileNameStr.substring(0, fileNameStr.length() - DIFF.length());
                        applyDiff(file, cleanDir.resolve(fileNameWithoutDiff), targetDir.resolve(fileNameWithoutDiff));

                    } else if (!fileNameStr.endsWith("gitignore") && !fileNameStr.endsWith(".yml")){
                        try {
                            Files.createDirectories(targetDir);
                            Files.copy(file, targetDir.resolve(fileNameStr));
                            Logger.getLogger(Generate.class.getName()).log(Level.INFO, "|\tcp " + file + " " + targetDir.resolve(fileNameStr));
                        } catch (IOException e) {
                            Logger.getLogger(Generate.class.getName()).log(Level.SEVERE, null, e);
                        }
                    }
                }
            });

        } catch (IOException e) {
            Logger.getLogger(Generate.class.getName()).log(Level.SEVERE, null, e);
        }
    }

    /**
     * Apply a diff to a single file using linux patch command
     * @param diffFile Path to diff file
     * @param cleanFile Path to clean file
     * @param targetDir Path to target directory
     */
    private static void applyDiff(Path diffFile, Path cleanFile, Path targetDir) {
        try {
            Files.createDirectories(targetDir.getParent());
            if (!Files.isRegularFile(targetDir)) {
                Files.copy(cleanFile, targetDir);
            }
            Logger.getLogger(Generate.class.getName()).log(Level.INFO, "|\tpatch -f " + targetDir + " < " + diffFile);
            Process process = new ProcessBuilder("patch", "-f", targetDir.toString(), diffFile.toString()).start();

            process.waitFor();
        } catch (IOException | InterruptedException e) {
            Logger.getLogger(Generate.class.getName()).log(Level.SEVERE, null, e);
        }
    }

    /**
     * Append new entry to docker compose yml file
     * @param partFile Path to the compose file part relevant to the service
     * @param dirname string to replace the $dirname in compose part file. i.e docker service name
     * @param composeFile File to append the part
     */
    public static void addToComposeFile(Path partFile, String dirname, Path composeFile){

        try {
            List<String> lines = Files.readAllLines(partFile);
            for (String line : lines) {
                if (line.contains("$dirname")) {
                    line = line.replace("$dirname", dirname);
                }
                line += "\n";
                Files.write(composeFile, line.getBytes(), StandardOpenOption.APPEND);

            }
        } catch (IOException e) {
            Logger.getLogger(Generate.class.getName()).log(Level.SEVERE, null, e);
        }
    }

    /**
     * Compress a given folder in .zip format and return the URI
     * @param targetLocation path to the folder
     * @return URI to the created zip archive
     */
    private static String zip(String targetLocation) {
        
        String parent = Paths.get(targetLocation).getParent().toString();
        String zipFile = "dockerConfig.zip";
        String zipFileURI = parent + zipFile;

        try {
            Process process = null;
            Logger.getLogger(Generate.class.getName()).log(Level.INFO, "zip -r " + "../" + zipFile + " " + targetLocation);
            ProcessBuilder p = new ProcessBuilder("zip", "-r", "../" + zipFile, ".");
            
            //Set command executing directory
            p.directory(new File(targetLocation));
            process = p.start();
            
            process.waitFor();
        } catch (IOException | InterruptedException e) {
            Logger.getLogger(Generate.class.getName()).log(Level.SEVERE, null, e);
        }

        return zipFileURI;
    }
    
    /**
     * Get a JSON model and return a complete set of knowledge-base folders
     * @param modelPath path to the JSON model file
     * @return created JSON object by reading the file
     */
    static List<String> getAllKnowledgeBaseNames(String modelPath) {
        List<String> fileNames = new ArrayList<>();
        JSONObject model = readJSONFile(modelPath);
        JSONArray services = model.getJSONArray("services");
        for (int i = 0; i < services.length(); i ++) {
            fileNames.addAll(getKnowledgeBaseNames(model, i));
        }
        return fileNames;
    }

    /**
     * Read a .json file and return a json object
     * @param modelPath path to the JSON model file
     * @return created JSON object by reading the file
     */
    static JSONObject readJSONFile(String modelPath){
        String modelStr = null;
        try {
            modelStr = new String(Files.readAllBytes(Paths.get(modelPath)));
        } catch (IOException e) {
            Logger.getLogger(Generate.class.getName()).log(Level.SEVERE, null, e);
        }

        return new JSONObject(modelStr);
    }
    
    /*
    * Reverse engineering code
    */
    
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
}
