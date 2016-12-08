package org.wso2.deployment;

import org.json.JSONObject;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class GenerateTest {

//    @Test(expectedExceptions = Exception.class)
//    public void testEmpty() throws Exception {
//        List<String> dirNames = Generate.toKnowledgeBaseNames(0, new JSONObject("{}"));
//    }

    @Test
    public void testSimpleProduct() throws Exception {
        String model = "{\"services\":[{\"type\":\"database\"}],\"links\":[]}";
        List<String> dirNames = Generate.toKnowledgeBaseNames(0, new JSONObject(model));
        Assert.assertEquals(dirNames, Collections.singletonList("database"));
    }

    @Test
    public void testProductWithProfile() throws Exception {
        String model = "{\"services\":[{\"type\":\"database\",\"profiles\":[]},{\"type\":\"wso2am\",\"profiles\":[\"publisher\"]},{\"type\":\"wso2am\",\"profiles\":[\"store\"]}],\"links\":[{\"source\":0,\"target\":1},{\"source\":0,\"target\":2},{\"source\":1,\"target\":2}]}";
        List<String> dirNames = Generate.toKnowledgeBaseNames(2, new JSONObject(model));
        Assert.assertEquals(dirNames, Collections.singletonList("wso2am_store"));
    }

//    @Test
//    public void testNonExistingProduct() throws Exception {
//        String model = "{\"services\":[{\"type\":\"wso2am-analytics\"}]}";
//        List<String> dirNames = Generate.toKnowledgeBaseNames(1, new JSONObject(model));
//        Assert.assertEquals(dirNames, Collections.emptyList());
//    }
//
    @Test
    public void testLink() throws Exception {
        String model = "{\"services\":[{\"type\":\"database\",\"profiles\":[]},{\"type\":\"wso2am\",\"profiles\":[]}],\"links\":[{\"source\":0,\"target\":1}]}";
        JSONObject modelJson = new JSONObject(model);

        List<String> dirNames = Generate.toKnowledgeBaseNames(0, modelJson);
        Assert.assertEquals(dirNames, Arrays.asList("database,wso2am"));

    }
//
//    @Test
//    public void testProductWithProfiles() throws Exception {
//        String model = "{\"services\":[{\"type\":\"wso2am\",\"profiless\":[\"store\",\"publisher\"]}]}";
//        List<String> dirNames = Generate.toKnowledgeBaseNames("wso2am", new JSONObject(model));
//        Assert.assertEquals(dirNames, Collections.emptyList());
//    }
//
//    @Test
//    public void testLinkWithProfile() throws Exception {
//        String model = "{\"services\":[{\"type\":\"database\",\"links\":[{\"serviceId\":1}]},{\"type\":\"wso2am\",\"profiless\":[\"store\",\"publisher\"]}]}";
//        JSONObject modelJson = new JSONObject(model);
//
//        List<String> dirNames = Generate.toKnowledgeBaseNames("wso2am", modelJson);
//        Assert.assertEquals(dirNames, Arrays.asList("wso2am", "wso2am,database"));
//
////        dirNames = Generate.toKnowledgeBaseNames("database", modelJson);
////        Assert.assertEquals(dirNames, Arrays.asList("database", "database,wso2am"));
//    }

}
