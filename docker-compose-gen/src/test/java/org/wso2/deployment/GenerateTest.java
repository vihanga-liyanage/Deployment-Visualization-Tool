package org.wso2.deployment;

import org.json.JSONObject;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class GenerateTest {

    @Test(expectedExceptions = Exception.class)
    public void testEmpty() throws Exception {
        List<String> dirNames = Generate.toKnowledgeBaseNames("wso2am-analytics", new JSONObject("{}"));
    }

    @Test
    public void testSimpleProduct() throws Exception {
        String model = "{\"services\":[{\"type\":\"wso2am-analytics\"}]}";
        List<String> dirNames = Generate.toKnowledgeBaseNames("wso2am-analytics", new JSONObject(model));
        Assert.assertEquals(dirNames, Collections.singletonList("wso2am-analytics"));
    }

    @Test
    public void testProductWithProfile() throws Exception {
        String model = "{\"services\":[{\"type\":\"wso2am\",\"profiles\":[\"store\"]}]}";
        List<String> dirNames = Generate.toKnowledgeBaseNames("wso2am", new JSONObject(model));
        Assert.assertEquals(dirNames, Collections.singletonList("wso2am_store"));
    }

    @Test
    public void testNonExistingProduct() throws Exception {
        String model = "{\"services\":[{\"type\":\"wso2am-analytics\"}]}";
        List<String> dirNames = Generate.toKnowledgeBaseNames("wso2as", new JSONObject(model));
        Assert.assertEquals(dirNames, Collections.emptyList());
    }


    @Test
    public void testLink() throws Exception {
        String model = "{\"services\":[{\"type\":\"database\",\"links\":[{\"serviceId\":1}]},{\"type\":\"wso2am\"}]}";
        JSONObject modelJson = new JSONObject(model);

        List<String> dirNames = Generate.toKnowledgeBaseNames("wso2am", modelJson);
        Assert.assertEquals(dirNames, Arrays.asList("wso2am", "wso2am,database"));

        dirNames = Generate.toKnowledgeBaseNames("database", modelJson);
        Assert.assertEquals(dirNames, Arrays.asList("database", "database,wso2am"));
    }
}
