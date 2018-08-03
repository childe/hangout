package com.ctrip.ops.sysdev.json;

/**
 * Created by liujia on 17/2/10.
 */

import org.joda.time.DateTime;
import org.json.simple.JSONValue;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.HashMap;

public class TestJson {
    @Test
    public void testJsonParse() {

    }

    @Test
    public void testJsonDump() {
        HashMap event = new HashMap<String, Object>() {
            {
                put("message", "hello json");
                put("@timestamp", DateTime.parse("2017-03-02T11:15:54+0800"));
            }
        };

        String rst = JSONValue.toJSONString(event);
        Assert.assertEquals(rst, "{\"@timestamp\":\"2017-03-02T11:15:54.000+08:00\",\"message\":\"hello json\"}");

        event.clear();
        event.put("message", "this is \"hello\" world");
        rst = JSONValue.toJSONString(event);
        Assert.assertEquals(rst, "{\"message\":\"this is \\\"hello\\\" world\"}");
    }
}
