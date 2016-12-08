package org.wso2.deployment;

import org.json.JSONObject;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.Collections;
import java.util.List;

public class GenerateTest {

    @Test(expectedExceptions = Exception.class)
    public void testEmpty() throws Exception {
        List<String> dirNames = Generate.toKnowledgeBaseNames(new JSONObject("{}"));
    }

    @Test
    public void testJustProfile() throws Exception {
        String model = "{\"services\":[{\"type\":\"wso2am-analytics\"}]}";
        List<String> dirNames = Generate.toKnowledgeBaseNames(new JSONObject(model));
        Assert.assertEquals(dirNames, Collections.singletonList("wso2am-analytics"));
    }
}
