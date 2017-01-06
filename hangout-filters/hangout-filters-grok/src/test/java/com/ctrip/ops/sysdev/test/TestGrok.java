package com.ctrip.ops.sysdev.test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import com.ctrip.ops.sysdev.filters.Grok;
import org.testng.Assert;
import org.testng.annotations.Test;
import org.yaml.snakeyaml.Yaml;

public class TestGrok {
    @Test
    @SuppressWarnings({"rawtypes", "unchecked"})
    public void testGrok() {
        String c = String
                .format("%s\n%s\n%s\n",
                        "match:",
                        "  - '^(?<logtime>\\S+) %{USER:user} (-|(?<level>\\w+)) %{DATA:msg}$'",
                        "remove_fields: ['message']");
        Yaml yaml = new Yaml();
        Map config = (Map) yaml.load(c);
        Assert.assertNotNull(config);

        Grok grokfilter = new Grok(config);

        // Match
        Map event = new HashMap();
        event.put("message",
                "2015-12-27T15:44:19+0800 childe - this is a test line");

        event = grokfilter.process(event);
        Assert.assertEquals(event.get("user"), "childe");
        Assert.assertNull(event.get("level"));
        Assert.assertNull(event.get("tags"));

        // Not Match
        event = new HashMap();
        event.put("message",
                "2015-12-27T15:44:19 0800 childe.liu - this is a test line");

        event = grokfilter.process(event);
        Assert.assertNull(event.get("user"));
        Assert.assertNull(event.get("level"));
        Assert.assertEquals(((ArrayList) event.get("tags")).get(0), "grokfail");

        // Not Match, But tags is a String
        event = new HashMap();
        event.put("message",
                "2015-12-27T15:44:19 0800 childe.liu - this is a test line");
        event.put("tags", "tag1");

        event = grokfilter.process(event);
        Assert.assertNull(event.get("user"));
        Assert.assertNull(event.get("level"));
        Assert.assertEquals(event.get("tags").getClass(), String.class);

        // Not Match, Not Tags
        c = String
                .format("%s\n%s\n%s\n",
                        "match:",
                        "  - '^(?<logtime>\\S+) %{USER:user} (-|(?<level>\\w+)) %{DATA:msg}$'",
                        "tag_on_failure: null",
                        "remove_fields: ['message']");
        yaml = new Yaml();
        config = (Map) yaml.load(c);
        Assert.assertNotNull(config);

        grokfilter = new Grok(config);

        event = new HashMap();
        event.put("message",
                "2015-12-27T15:44:19 0800 childe.liu - this is a test line");

        event = grokfilter.process(event);
        Assert.assertNull(event.get("user"));
        Assert.assertNull(event.get("level"));
        Assert.assertNull(event.get("tags"));


        // put array to a key if more than one refs
        c = String
                .format("%s\n%s\n%s\n",
                        "match:",
                        "  - '%{NOTSPACE:a} %{NOTSPACE:a}'",
                        "tag_on_failure: null",
                        "remove_fields: ['message']");
        yaml = new Yaml();
        config = (Map) yaml.load(c);
        Assert.assertNotNull(config);

        grokfilter = new Grok(config);

        event = new HashMap();
        event.put("message",
                "abc xyz");

        event = grokfilter.process(event);
        Assert.assertEquals(2, ((ArrayList) event.get("a")).size());
        Assert.assertNull(event.get("tags"));
    }
}
