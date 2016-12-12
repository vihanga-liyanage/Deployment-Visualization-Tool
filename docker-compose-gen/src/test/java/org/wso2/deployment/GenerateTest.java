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
}
