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

import org.json.JSONArray;
import org.json.JSONObject;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;

import static java.util.Collections.singletonList;

/**
 *
 * @author Vihanga Liyanage <vihanga@wso2.com>
 */
public class GenerateTest {

    @Test
    public void testSimpleProduct() throws Exception {
        String model = "{\"services\":[{\"type\":\"database\",\"profiles\":[]}],\"links\":[]}";
        List<String> dirNames = Generate.getKnowledgeBaseNames(new JSONObject(model), 0);
        Assert.assertEquals(dirNames, singletonList("database"));
    }

    @Test
    public void testProductWithProfile() throws Exception {
        String model = "{\"services\":[{\"type\":\"database\",\"profiles\":[]},{\"type\":\"wso2am\",\"profiles\""
                + ":[\"publisher\"]},{\"type\":\"wso2am\",\"profiles\":[\"store\"]}],\"links\":[]}";
        List<String> dirNames = Generate.getKnowledgeBaseNames(new JSONObject(model), 2);
        Assert.assertEquals(dirNames, singletonList("wso2am_store"));
    }

    @Test
    public void testLink() throws Exception {
        String model = "{\"services\":[{\"type\":\"database\",\"profiles\":[]},{\"type\":\"wso2am\",\"profiles\""
                + ":[]}],\"links\":[{\"source\":0,\"target\":1}]}";
        JSONObject modelJson = new JSONObject(model);

        List<String> dirNames = Generate.getKnowledgeBaseNames(modelJson, 0);
        Assert.assertEquals(dirNames, Arrays.asList("database", "database,wso2am"));

    }

    @Test
    public void testProductWithProfiles() throws Exception {
        String model = "{\"services\":[{\"type\":\"database\",\"profiles\":[]},{\"type\":\"wso2am\",\"profiles\":[\""
                + "publisher\",\"store\"]}],\"links\":[]}";
        List<String> dirNames = Generate.getKnowledgeBaseNames(new JSONObject(model), 1);
        Assert.assertEquals(dirNames, Arrays.asList("wso2am_publisher_store"));
    }

    @Test
    public void testLinkWithProfile() throws Exception {
        String model = "{\"services\":[{\"type\":\"database\",\"profiles\":[]},{\"type\":\"wso2am\",\"profiles\":[\""
                + "publisher\"]}],\"links\":[{\"source\":0,\"target\":1}]}";
        JSONObject modelJson = new JSONObject(model);
        List<String> dirNames;

//        dirNames = Generate.toKnowledgeBaseNames(0, modelJson);
//        Assert.assertEquals(dirNames, Arrays.asList("database", "database,wso2am_publisher"));

        dirNames = Generate.getKnowledgeBaseNames(modelJson, 1);
        Assert.assertEquals(dirNames, Arrays.asList("wso2am_publisher", "wso2am_publisher,database"));
    }

    @Test
    public void testLinksWithProfile() throws Exception {
        String model = "{\"services\":[{\"type\":\"database\",\"profiles\":[]},{\"type\":\"wso2am\",\"profiles\":[\""
                + "publisher\"]},{\"type\":\"wso2am\",\"profiles\":[\"store\"]}],\"links\":[{\"source\":0,\""
                + "target\":1},{\"source\":0,\"target\":2},{\"source\":1,\"target\":2}]}";
        JSONObject modelJson = new JSONObject(model);
        List<String> dirNames;

        dirNames = Generate.getKnowledgeBaseNames(modelJson, 0);
        Assert.assertEquals(dirNames, Arrays.asList("database", "database,wso2am_publisher", "database,wso2am_store"));

        dirNames = Generate.getKnowledgeBaseNames(modelJson, 1);
        Assert.assertEquals(dirNames, Arrays.asList("wso2am_publisher", "wso2am_publisher,database", "wso2am_publisher,wso2am_store"));
    }

    @Test
    public void testLinkWithProfiles() throws Exception {
        String model = "{\"services\":[{\"type\":\"database\",\"profiles\":[]},{\"type\":\"wso2am\",\"profiles\""
                + ":[\"publisher\",\"store\"]}],\"links\":[{\"source\":0,\"target\":1}]}";
        JSONObject modelJson = new JSONObject(model);
        List<String> dirNames;

        dirNames = Generate.getKnowledgeBaseNames(modelJson, 0);
        Assert.assertEquals(dirNames, Arrays.asList("database", "database,wso2am_publisher_store"));
    }

    @Test
    public void testForwardLinksWithProfiles() throws Exception {
        String model = "{\"services\":[{\"type\":\"wso2am\",\"profiles\":[\"publisher\",\"store\"]},{\"type\":\"wso2"
                + "am\",\"profiles\":[\"keymanager\",\"traffic-manager\"]}],\"links\":[{\"source\":0,\"target\":1}]}";
        JSONObject modelJson = new JSONObject(model);
        List<String> dirNames;

        dirNames = Generate.getKnowledgeBaseNames(modelJson, 0);
        Assert.assertEquals(dirNames, Arrays.asList("wso2am_publisher_store", 
                "wso2am_publisher_store,wso2am_keymanager_traffic-manager"));
    }

    @Test
    public void testBackwardLinksWithProfiles() throws Exception {
        String model = "{\"services\":[{\"type\":\"wso2am\",\"profiles\":[\"publisher\",\"store\"]},{\"type\":\"wso2"
                + "am\",\"profiles\":[\"keymanager\",\"traffic-manager\"]}],\"links\":[{\"source\":0,\"target\":1}]}";
        JSONObject modelJson = new JSONObject(model);
        List<String> dirNames;

        dirNames = Generate.getKnowledgeBaseNames(modelJson, 1);
        Assert.assertEquals(dirNames, Arrays.asList("wso2am_keymanager_traffic-manager",
                "wso2am_keymanager_traffic-manager,wso2am_publisher_store"));
    }

    @Test
    public void testSkipLoadBalancer() throws Exception {
        String model = "{\"services\":[{\"type\":\"wso2am\",\"profiles\":[\"publisher\"]},{\"type\":\"load-balancer\""
                + ",\"profiles\":[]}],\"links\":[{\"source\":1,\"target\":0}]}";
        JSONObject modelJson = new JSONObject(model);
        List<String> dirNames;

        dirNames = Generate.getKnowledgeBaseNames(modelJson, 0);
        Assert.assertEquals(dirNames, Arrays.asList("wso2am_publisher"));
    }

    @Test
    public void testReadJSONModel() throws Exception {
        String modelPath = "src/resources/model.json";
        JSONObject model = Generate.readJSONFile(modelPath);

        JSONArray services = model.getJSONArray("services");
        Assert.assertNotNull(services);
    }

    @Test
    public void testGetAllKnowledgeBaseNames() throws Exception {
        String modelPath = "src/resources/model.json";
        List<String> dirNames;

        dirNames = Generate.getAllKnowledgeBaseNames(modelPath);
        Assert.assertNotNull(dirNames);
    }

    //Testing getJSONFromXML
    @Test
    public void testIdentifyBasicService() throws Exception {
        String xml = "<mxGraphModel>\n" + "  <root>\n"
                + "    <Diagram label=\"My Diagram\" href=\"http://www.jgraph.com/\" id=\"0\">\n" + "      <mxCell/>\n"
                + "    </Diagram>\n" + "    <Layer label=\"Default Layer\" id=\"1\">\n"
                + "      <mxCell parent=\"0\"/>\n" + "    </Layer>\n" + "    <Image label=\"publisher\" href=\"\" id=\"34\">\n"
                + "      <mxCell style=\"wso2am\" vertex=\"1\" parent=\"1\">\n"
                + "        <mxGeometry x=\"140\" y=\"240\" width=\"100\" height=\"100\" as=\"geometry\"/>\n"
                + "      </mxCell>\n" + "    </Image>\n" + "  </root>\n" + "</mxGraphModel>";

        String resultJSONStr = "{\"services\":[{\"type\":\"wso2am\",\"profiles\":[\"publisher\"]}],\"links\":[]}";
        JSONObject resultJSON = new JSONObject(resultJSONStr);

        JSONObject test = Generate.getJSONFromXML(xml, false);
        Assert.assertEquals(test.toString(), resultJSON.toString());
    }

    @Test
    public void testIdentifyServices() throws Exception {
        String xml = "<mxGraphModel><root><Diagram label=\"My Diagram\" href=\"http://www.jgraph.com/\" id=\"0\">"
                + "<mxCell/></Diagram><Layer label=\"Default Layer\" id=\"1\"><mxCell parent=\"0\"/></Layer>"
                + "<Image label=\"publisher\" href=\"\" id=\"34\"><mxCell style=\"wso2am\" vertex=\"1\" parent=\"1\">"
                + "</mxCell></Image><Image label=\"\" href=\"\" id=\"35\"><mxCell style=\"database\" vertex=\"1\""
                + " parent=\"1\"></mxCell></Image>"
                + "</root></mxGraphModel>";

        String resultJSONStr = "{\"services\":[{\"type\":\"wso2am\",\"profiles"
                + "\":[\"publisher\"]},{\"type\":\"database\",\"profiles\":[]}],\"links\":[]}";

        JSONObject resultJSON = new JSONObject(resultJSONStr);

        JSONObject test = Generate.getJSONFromXML(xml, false);
        Assert.assertEquals(test.toString(), resultJSON.toString());
    }

    @Test
    public void testIdentifyServiceWithProfiles() throws Exception {
        String xml = "<mxGraphModel>\n" + "  <root>\n"
                + "    <Diagram label=\"My Diagram\" href=\"http://www.jgraph.com/\" id=\"0\">\n" + "      <mxCell/>\n"
                + "    </Diagram>\n" + "    <Layer label=\"Default Layer\" id=\"1\">\n"
                + "      <mxCell parent=\"0\"/>\n" + "    </Layer>\n" + "    <Image label=\"publisher/store\" href=\"\" id=\"34\">\n"
                + "      <mxCell style=\"wso2am\" vertex=\"1\" parent=\"1\">\n"
                + "        <mxGeometry x=\"140\" y=\"240\" width=\"100\" height=\"100\" as=\"geometry\"/>\n"
                + "      </mxCell>\n" + "    </Image>\n" + "  </root>\n" + "</mxGraphModel>";

        String resultJSONStr = "{\"services\":[{\"type\":\"wso2am\",\"profiles\":[\"publisher\",\"store\"]}],\"links\":[]}";
        JSONObject resultJSON = new JSONObject(resultJSONStr);

        JSONObject test = Generate.getJSONFromXML(xml, false);
        Assert.assertEquals(test.toString(), resultJSON.toString());

        //Extended test with multiple profiles
        xml = "<mxGraphModel><root><Diagram id=\"0\"><mxCell/></Diagram><Layer id=\"1\"><mxCell parent=\"0\"/>"
                + "</Layer><Image label=\"publisher/store\" id=\"3\"><mxCell style=\"wso2am\" vertex=\"1\" parent=\"1\">"
                + "</mxCell></Image><Image label=\"keymanager/traffic-manager\" id=\"4\">"
                + "<mxCell style=\"wso2am\" vertex=\"1\" parent=\"1\"></mxCell></Image><Connector id=\"5\">"
                + "<mxCell edge=\"1\" parent=\"1\" source=\"3\" target=\"4\"></mxCell></Connector></root></mxGraphModel>";

        resultJSONStr = "{\"services\":[{\"type\":\"wso2am\",\"profiles\":[\"publisher\",\"store\"]},{\"type\":\"wso2am\""
                + ",\"profiles\":[\"keymanager\",\"traffic-manager\"]}],\"links\":[{\"source\":0,\"target\":1}]}";
        resultJSON = new JSONObject(resultJSONStr);

        test = Generate.getJSONFromXML(xml, false);
        Assert.assertEquals(test.toString(), resultJSON.toString());
    }

    @Test
    public void testIdentifyLink() throws Exception {
        String xml = "<mxGraphModel>\n" + "    <root>\n"
                + "        <Diagram label=\"My Diagram\" href=\"http://www.jgraph.com/\" id=\"0\">\n"
                + "            <mxCell/>\n" + "        </Diagram>\n"
                + "        <Layer label=\"Default Layer\" id=\"1\">\n" + "            <mxCell parent=\"0\"/>\n"
                + "        </Layer>\n" + "        <Image label=\"publisher\" href=\"\" id=\"34\">\n"
                + "            <mxCell style=\"wso2am\" vertex=\"1\" parent=\"1\">\n"
                + "                <mxGeometry x=\"440\" y=\"310\" width=\"100\" height=\"100\" as=\"geometry\"/>\n"
                + "            </mxCell>\n" + "        </Image>\n" + "        <Image label=\"\" href=\"\" id=\"35\">\n"
                + "            <mxCell style=\"database\" vertex=\"1\" parent=\"1\">\n"
                + "                <mxGeometry x=\"70\" y=\"90\" width=\"100\" height=\"100\" as=\"geometry\"/>\n"
                + "            </mxCell>\n" + "        </Image>\n"
                + "        <Connector label=\"\" href=\"\" id=\"36\">\n"
                + "            <mxCell edge=\"1\" parent=\"1\" source=\"35\" target=\"34\">\n"
                + "                <mxGeometry relative=\"1\" as=\"geometry\"/>\n" + "            </mxCell>\n"
                + "        </Connector>\n" + "    </root>\n" + "</mxGraphModel>";

        String resultJSONStr = "{\"services\":[{\"type\":\"wso2am\",\"profiles\":[\"publisher\"]},{\"type\":\"database\""
                + ",\"profiles\":[]}],\"links\":[{\"source\":0,\"target\":1}]}";

        JSONObject resultJSON = new JSONObject(resultJSONStr);

        JSONObject test = Generate.getJSONFromXML(xml, false);
        Assert.assertEquals(test.toString(), resultJSON.toString());
    }

    @Test
    public void testIdentifyLinks() throws Exception {
        String xml = "<mxGraphModel><root><Image label=\"\" id=\"2\"><mxCell style=\"database\" ></mxCell></Image>"
                + "<Image label=\"publisher\" id=\"3\"><mxCell style=\"wso2am\" ></mxCell></Image><Image label=\"store\""
                + " href=\"\" id=\"4\"><mxCell style=\"wso2am\" ></mxCell></Image><Connector label=\"\" id=\"5\">"
                + "<mxCell  source=\"2\" target=\"4\"></mxCell></Connector><Connector label=\"\"  id=\"6\"><mxCell "
                + "source=\"2\" target=\"3\"></mxCell></Connector><Connector label=\"\" href=\"\" id=\"7\"><mxCell "
                + "source=\"3\" target=\"4\"></mxCell></Connector></root></mxGraphModel>";

        String resultJSONStr = "{\"services\":[{\"type\":\"database\",\"profiles\":[]},{\"type\":\"wso2am\",\"profiles\""
                + ":[\"publisher\"]},{\"type\":\"wso2am\",\"profiles\":[\"store\"]}],\"links\":[{\"source\":0,\"target\":2}"
                + ",{\"source\":0,\"target\":1},{\"source\":1,\"target\":2}]}";

        JSONObject resultJSON = new JSONObject(resultJSONStr);

        JSONObject test = Generate.getJSONFromXML(xml, false);
        Assert.assertEquals(test.toString(), resultJSON.toString());
    }

    //Testing getXMLFromJSON
    @Test
    public void testXMLIdentifyBasicService() throws Exception {
        String modelStr = "{\"services\":[{\"type\":\"database\",\"profiles\":[]}],\"links\":[]}";
        JSONObject model = new JSONObject(modelStr);

        String resultXML = "<mxGraphModel><root><Diagram id=\"0\"><mxCell/></Diagram><Layer id=\"1\">"
                + "<mxCell parent=\"0\"/></Layer><Image label=\"\" id=\"2\">"
                + "<mxCell style=\"database\" vertex=\"1\" parent=\"1\">"
                + "<mxGeometry x=\"0\" y=\"0\" width=\"100\" height=\"100\" as=\"geometry\"/>"
                + "</mxCell></Image></root></mxGraphModel>";
        String test = Generate.getXMLFromJSON(model);

        Assert.assertEquals(resultXML, test);
    }

    @Test
    public void testXMLIdentifyServices() throws Exception {
        String modelStr = "{\"services\":[{\"type\":\"database\",\"profiles\":[]},{\"type\":\"wso2am\",\"profiles\":[]}],\"links\":[]}";
        JSONObject model = new JSONObject(modelStr);

        String resultXML = "<mxGraphModel><root><Diagram id=\"0\"><mxCell/></Diagram><Layer id=\"1\"><mxCell parent=\"0\"/>"
                + "</Layer><Image label=\"\" id=\"2\"><mxCell style=\"database\" vertex=\"1\" parent=\"1\">"
                + "<mxGeometry x=\"0\" y=\"0\" width=\"100\" height=\"100\" as=\"geometry\"/></mxCell></Image>"
                + "<Image label=\"\" id=\"3\"><mxCell style=\"wso2am\" vertex=\"1\" parent=\"1\">"
                + "<mxGeometry x=\"0\" y=\"0\" width=\"100\" height=\"100\" as=\"geometry\"/></mxCell></Image></root></mxGraphModel>";
        String test = Generate.getXMLFromJSON(model);

        Assert.assertEquals(resultXML, test);
    }

    @Test
    public void testXMLIdentifyServiceWithProfile() throws Exception {
        String modelStr = "{\"services\":[{\"type\":\"wso2am\",\"profiles\":[\"publisher\"]}],\"links\":[]}";
        JSONObject model = new JSONObject(modelStr);

        String resultXML = "<mxGraphModel><root><Diagram id=\"0\"><mxCell/></Diagram><Layer id=\"1\"><mxCell parent=\"0\"/>"
                + "</Layer><Image label=\"publisher\" id=\"2\"><mxCell style=\"wso2am\" vertex=\"1\" parent=\"1\">"
                + "<mxGeometry x=\"0\" y=\"0\" width=\"100\" height=\"100\" as=\"geometry\"/></mxCell></Image></root></mxGraphModel>";
        String test = Generate.getXMLFromJSON(model);

        Assert.assertEquals(resultXML, test);
    }

    @Test
    public void testXMLIdentifyLink() throws Exception {
        String modelStr = "{\"services\":[{\"type\":\"wso2am\",\"profiles\":[\"publisher\"]},{\"type\":\"wso2am\",\"profiles\":"
                + "[\"store\"]}],\"links\":[{\"source\":0,\"target\":1}]}";
        JSONObject model = new JSONObject(modelStr);

        String resultXML = "<mxGraphModel><root><Diagram id=\"0\"><mxCell/></Diagram><Layer id=\"1\"><mxCell parent=\"0\"/>"
                + "</Layer><Image label=\"publisher\" id=\"2\"><mxCell style=\"wso2am\" vertex=\"1\" parent=\"1\">"
                + "<mxGeometry x=\"0\" y=\"0\" width=\"100\" height=\"100\" as=\"geometry\"/></mxCell></Image>"
                + "<Image label=\"store\" id=\"3\"><mxCell style=\"wso2am\" vertex=\"1\" parent=\"1\">"
                + "<mxGeometry x=\"0\" y=\"0\" width=\"100\" height=\"100\" as=\"geometry\"/></mxCell></Image>"
                + "<Connector id=\"4\"><mxCell edge=\"1\" parent=\"1\" source=\"2\" target=\"3\">"
                + "<mxGeometry relative=\"1\" as=\"geometry\"/></mxCell></Connector></root></mxGraphModel>";
        String test = Generate.getXMLFromJSON(model);

        Assert.assertEquals(test, resultXML);
    }

    @Test
    public void testXMLIdentifyLinksWithColors() throws Exception {
        String modelStr = "{\"services\":[{\"type\":\"database\",\"profiles\":[]},{\"type\":\"wso2am\",\"profiles\":"
                + "[\"publisher\"]},{\"type\":\"wso2am\",\"profiles\":[\"store\"]}],\"links\":[{\"source\":0,\"target\""
                + ":1},{\"source\":1,\"target\":2}]}";
        JSONObject model = new JSONObject(modelStr);

        String resultXML = "<mxGraphModel><root><Diagram id=\"0\"><mxCell/></Diagram><Layer id=\"1\"><mxCell parent=\"0\"/>"
                + "</Layer><Image label=\"\" id=\"2\"><mxCell style=\"database\" vertex=\"1\" parent=\"1\">"
                + "<mxGeometry x=\"0\" y=\"0\" width=\"100\" height=\"100\" as=\"geometry\"/></mxCell></Image>"
                + "<Image label=\"publisher\" id=\"3\"><mxCell style=\"wso2am\" vertex=\"1\" parent=\"1\">"
                + "<mxGeometry x=\"0\" y=\"0\" width=\"100\" height=\"100\" as=\"geometry\"/></mxCell></Image>"
                + "<Image label=\"store\" id=\"4\"><mxCell style=\"wso2am\" vertex=\"1\" parent=\"1\">"
                + "<mxGeometry x=\"0\" y=\"0\" width=\"100\" height=\"100\" as=\"geometry\"/></mxCell></Image>"
                + "<Connector id=\"5\"><mxCell edge=\"1\" parent=\"1\" source=\"2\" target=\"3\" style=\"strokeColor=#e900ff;"
                + "startArrow=classic;\"><mxGeometry relative=\"1\" as=\"geometry\"/></mxCell></Connector><Connector id=\"6\">"
                + "<mxCell edge=\"1\" parent=\"1\" source=\"3\" target=\"4\"><mxGeometry relative=\"1\" as=\"geometry\"/>"
                + "</mxCell></Connector></root></mxGraphModel>";
        String test = Generate.getXMLFromJSON(model);

        Assert.assertEquals(test, resultXML);
    }

    @Test
    public void testXMLIdentifyLinks() throws Exception {
        String modelStr = "{\"services\":[{\"type\":\"database\",\"profiles\":[]},{\"type\":\"wso2am\",\"profiles\":"
                + "[\"publisher\"]},{\"type\":\"wso2am\",\"profiles\":[\"store\"]}],\"links\":[{\"source\":0,\"target\":1}"
                + ",{\"source\":0,\"target\":2},{\"source\":1,\"target\":2}]}";
        JSONObject model = new JSONObject(modelStr);

        String resultXML = "<mxGraphModel><root><Diagram id=\"0\"><mxCell/></Diagram><Layer id=\"1\"><mxCell parent=\"0\"/>"
                + "</Layer><Image label=\"\" id=\"2\"><mxCell style=\"database\" vertex=\"1\" parent=\"1\">"
                + "<mxGeometry x=\"0\" y=\"0\" width=\"100\" height=\"100\" as=\"geometry\"/></mxCell></Image>"
                + "<Image label=\"publisher\" id=\"3\"><mxCell style=\"wso2am\" vertex=\"1\" parent=\"1\">"
                + "<mxGeometry x=\"0\" y=\"0\" width=\"100\" height=\"100\" as=\"geometry\"/></mxCell></Image>"
                + "<Image label=\"store\" id=\"4\"><mxCell style=\"wso2am\" vertex=\"1\" parent=\"1\">"
                + "<mxGeometry x=\"0\" y=\"0\" width=\"100\" height=\"100\" as=\"geometry\"/></mxCell></Image>"
                + "<Connector id=\"5\"><mxCell edge=\"1\" parent=\"1\" source=\"2\" target=\"3\" "
                + "style=\"strokeColor=#e900ff;startArrow=classic;\"><mxGeometry relative=\"1\" as=\"geometry\"/>"
                + "</mxCell></Connector><Connector id=\"6\"><mxCell edge=\"1\" parent=\"1\" source=\"2\" target=\"4\" "
                + "style=\"strokeColor=#e900ff;startArrow=classic;\"><mxGeometry relative=\"1\" as=\"geometry\"/>"
                + "</mxCell></Connector><Connector id=\"7\"><mxCell edge=\"1\" parent=\"1\" source=\"3\" target=\"4\">"
                + "<mxGeometry relative=\"1\" as=\"geometry\"/></mxCell></Connector></root></mxGraphModel>";
        String test = Generate.getXMLFromJSON(model);

        Assert.assertEquals(test, resultXML);
    }

    @Test
    public void testXMLsample() throws Exception {
        String modelStr = "{\"services\":[{\"type\":\"database\",\"name\":\"apim_rdbms\",\"image\":\"mysql:5.5\",\"ports\":[],"
                + "\"profiles\":[]},{\"type\":\"wso2am\",\"name\":\"publisher\",\"image\":\"docker.wso2.com/wso2am:2.0.0\",\""
                + "ports\":[\"443:9443\",\"80:9763\",\"8280:8280\",\"8243:8243\"],\"profiles\":[\"publisher\"]},{\"type\":\""
                + "wso2am\",\"name\":\"store\",\"image\":\"docker.wso2.com/wso2am:2.0.0\",\"ports\":[\"9446:9443\"],\"profiles\""
                + ":[\"store\"]},{\"type\":\"wso2am-analytics\",\"name\":\"analytics\",\"image\":\"docker.wso2.com/wso2am-ana"
                + "lytics:2.0.0\",\"ports\":[\"9448:9444\"],\"profiles\":[]},{\"type\":\"wso2am\",\"name\":\"traffic-manager\""
                + ",\"image\":\"docker.wso2.com/wso2am:2.0.0\",\"ports\":[\"9447:9443\"],\"profiles\":[\"traffic-manager\"]},"
                + "{\"type\":\"wso2am\",\"name\":\"keymanager\",\"image\":\"docker.wso2.com/wso2am:2.0.0\",\"ports\":[\"9443:"
                + "9443\"],\"profiles\":[\"keymanager\"]},{\"type\":\"wso2am\",\"name\":\"gateway-manager\",\"image\":\"docker"
                + ".wso2.com/wso2am:2.0.0\",\"ports\":[\"9444:9443\"],\"profiles\":[\"gateway-manager\"]},{\"type\":\"wso2am\""
                + ",\"name\":\"gateway-worker\",\"image\":\"docker.wso2.com/wso2am:2.0.0\",\"ports\":[\"8280:8280\",\"8243:824"
                + "3\"],\"profiles\":[\"gateway-worker\"]},{\"type\":\"load-balancer\",\"name\":\"load-balancer\",\"image\":\""
                + "\",\"ports\":[],\"profiles\":[]},{\"type\":\"svn\",\"name\":\"svn\",\"image\":\"\",\"ports\":[],\"profiles\""
                + ":[]}],\"links\":[{\"source\":0,\"target\":1},{\"source\":0,\"target\":2},{\"source\":0,\"target\":3},{\""
                + "source\":0,\"target\":4},{\"source\":0,\"target\":5},{\"source\":1,\"target\":2},{\"source\":3,\"target\":1}"
                + ",{\"source\":3,\"target\":2},{\"source\":3,\"target\":7},{\"source\":4,\"target\":7},{\"source\":6,\"target\""
                + ":7},{\"source\":8,\"target\":1},{\"source\":8,\"target\":2},{\"source\":8,\"target\":3},{\"source\":8,\"targe"
                + "t\":4},{\"source\":8,\"target\":5},{\"source\":8,\"target\":6},{\"source\":8,\"target\":7},{\"source\":9,\""
                + "target\":6},{\"source\":9,\"target\":7}]}";
        JSONObject model = new JSONObject(modelStr);

        String test = Generate.getXMLFromJSON(model);
        System.out.println(test);
    }

    @Test
    public void testaddToComposeFile() throws Exception {
        Path partFile = Paths.get("knowledge-base/database/dockerfilePart.yml");
        Path out = Paths.get("out.yml");
        Generate.addToComposeFile(partFile, "database", out);
    }
    
    @Test
    public void testinitSystem() throws Exception {
        //Generate.initSystem();
    }
}
