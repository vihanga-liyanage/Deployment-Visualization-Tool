package org.wso2.deployment;

import org.json.JSONArray;
import org.json.JSONObject;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static java.util.Collections.singletonList;

public class GenerateTest {

    @Test
    public void testSimpleProduct() throws Exception {
        String model = "{\"services\":[{\"type\":\"database\",\"profiles\":[]}],\"links\":[]}";
        List<String> dirNames = Generate.toKnowledgeBaseNames(0, new JSONObject(model));
        Assert.assertEquals(dirNames, singletonList("database"));
    }

    @Test
    public void testProductWithProfile() throws Exception {
        String model = "{\"services\":[{\"type\":\"database\",\"profiles\":[]},{\"type\":\"wso2am\",\"profiles\""
                + ":[\"publisher\"]},{\"type\":\"wso2am\",\"profiles\":[\"store\"]}],\"links\":[]}";
        List<String> dirNames = Generate.toKnowledgeBaseNames(2, new JSONObject(model));
        Assert.assertEquals(dirNames, singletonList("wso2am_store"));
    }

    @Test
    public void testLink() throws Exception {
        String model = "{\"services\":[{\"type\":\"database\",\"profiles\":[]},{\"type\":\"wso2am\",\"profiles\""
                + ":[]}],\"links\":[{\"source\":0,\"target\":1}]}";
        JSONObject modelJson = new JSONObject(model);

        List<String> dirNames = Generate.toKnowledgeBaseNames(0, modelJson);
        Assert.assertEquals(dirNames, Arrays.asList("database", "database,wso2am"));

    }

    @Test
    public void testProductWithProfiles() throws Exception {
        String model = "{\"services\":[{\"type\":\"database\",\"profiles\":[]},{\"type\":\"wso2am\",\"profiles\":[\""
                + "publisher\",\"store\"]}],\"links\":[]}";
        List<String> dirNames = Generate.toKnowledgeBaseNames(1, new JSONObject(model));
        Assert.assertEquals(dirNames, Collections.emptyList());
    }

    @Test
    public void testLinkWithProfile() throws Exception {
        String model = "{\"services\":[{\"type\":\"database\",\"profiles\":[]},{\"type\":\"wso2am\",\"profiles\":[\""
                + "publisher\"]}],\"links\":[{\"source\":0,\"target\":1}]}";
        JSONObject modelJson = new JSONObject(model);
        List<String> dirNames;

//        dirNames = Generate.toKnowledgeBaseNames(0, modelJson);
//        Assert.assertEquals(dirNames, Arrays.asList("database", "database,wso2am_publisher"));

        dirNames = Generate.toKnowledgeBaseNames(1, modelJson);
        Assert.assertEquals(dirNames, Arrays.asList("wso2am_publisher", "wso2am_publisher,database"));
    }

    @Test
    public void testLinksWithProfile() throws Exception {
        String model = "{\"services\":[{\"type\":\"database\",\"profiles\":[]},{\"type\":\"wso2am\",\"profiles\":[\""
                + "publisher\"]},{\"type\":\"wso2am\",\"profiles\":[\"store\"]}],\"links\":[{\"source\":0,\""
                + "target\":1},{\"source\":0,\"target\":2},{\"source\":1,\"target\":2}]}";
        JSONObject modelJson = new JSONObject(model);
        List<String> dirNames;

        dirNames = Generate.toKnowledgeBaseNames(0, modelJson);
        Assert.assertEquals(dirNames, Arrays.asList("database", "database,wso2am_publisher", "database,wso2am_store"));

        dirNames = Generate.toKnowledgeBaseNames(1, modelJson);
        Assert.assertEquals(dirNames, Arrays.asList("wso2am_publisher", "wso2am_publisher,database", "wso2am_publisher,wso2am_store"));
    }

    @Test
    public void testLinkWithProfiles() throws Exception {
        String model = "{\"services\":[{\"type\":\"database\",\"profiles\":[]},{\"type\":\"wso2am\",\"profiles\""
                + ":[\"publisher\",\"store\"]}],\"links\":[{\"source\":0,\"target\":1}]}";
        JSONObject modelJson = new JSONObject(model);
        List<String> dirNames;

        dirNames = Generate.toKnowledgeBaseNames(0, modelJson);
        Assert.assertEquals(dirNames, Arrays.asList("database", "database,wso2am_publisher", "database,wso2am_store"));
    }

    @Test
    public void testForwardLinksWithProfiles() throws Exception {
        String model = "{\"services\":[{\"type\":\"wso2am\",\"profiles\":[\"publisher\",\"store\"]},{\"type\":\"wso2"
                + "am\",\"profiles\":[\"keymanager\",\"traffic-manager\"]}],\"links\":[{\"source\":0,\"target\":1}]}";
        JSONObject modelJson = new JSONObject(model);
        List<String> dirNames;

        dirNames = Generate.toKnowledgeBaseNames(0, modelJson);
        Assert.assertEquals(dirNames, Arrays.asList("wso2am_publisher,wso2am_keymanager", "wso2am_publisher,wso2am_traffic-manager",
                "wso2am_store,wso2am_keymanager", "wso2am_store,wso2am_traffic-manager"));
    }

    @Test
    public void testBackwardLinksWithProfiles() throws Exception {
        String model = "{\"services\":[{\"type\":\"wso2am\",\"profiles\":[\"publisher\",\"store\"]},{\"type\":\"wso2"
                + "am\",\"profiles\":[\"keymanager\",\"traffic-manager\"]}],\"links\":[{\"source\":0,\"target\":1}]}";
        JSONObject modelJson = new JSONObject(model);
        List<String> dirNames;

        dirNames = Generate.toKnowledgeBaseNames(1, modelJson);
        Assert.assertEquals(dirNames, Arrays.asList("wso2am_keymanager,wso2am_publisher",
                "wso2am_keymanager,wso2am_store", "wso2am_traffic-manager,wso2am_publisher",
                "wso2am_traffic-manager,wso2am_store"));
    }

    @Test
    public void testSkipLoadBalancer() throws Exception {
        String model = "{\"services\":[{\"type\":\"wso2am\",\"profiles\":[\"publisher\"]},{\"type\":\"load-balancer\""
                + ",\"profiles\":[]}],\"links\":[{\"source\":1,\"target\":0}]}";
        JSONObject modelJson = new JSONObject(model);
        List<String> dirNames;

        dirNames = Generate.toKnowledgeBaseNames(0, modelJson);
        Assert.assertEquals(dirNames, Arrays.asList("wso2am_publisher"));
    }

    @Test
    public void testReadJSONModel() throws Exception {
        String modelPath = "src/resources/model1.json";
        JSONObject model = Generate.getJSONModel(modelPath);

        JSONArray services = model.getJSONArray("services");
        Assert.assertNotNull(services);
    }

    @Test
    public void testgetAllKnowledgeBaseNames() throws Exception {
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

        JSONObject test = Generate.getJSONFromXML(xml);
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

        JSONObject test = Generate.getJSONFromXML(xml);
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

        JSONObject test = Generate.getJSONFromXML(xml);
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

        JSONObject test = Generate.getJSONFromXML(xml);
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

        JSONObject test = Generate.getJSONFromXML(xml);
        Assert.assertEquals(test.toString(), resultJSON.toString());
    }
}
