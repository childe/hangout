package com.ctrip.ops.sysdev.test;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.ctrip.ops.sysdev.filters.Remove;
import org.testng.Assert;
import org.testng.annotations.Test;
import org.yaml.snakeyaml.Yaml;

public class TestRemoveFilter {
    @Test
    public void testRemoveFilter() {
        String c = String.format("%s\n%s\n%s\n%s",
                "fields:",
                "  - gender",
                "  - '[name][first]'",
                "  - '[value][list][value1]'"
        );
        Yaml yaml = new Yaml();
        Map config = (Map) yaml.load(c);
        Assert.assertNotNull(config);

        Remove removeFilter = new Remove(config);

        // Match
        Map event = new HashMap();
        event.put("nothing", 11.11);
        event.put("gender", "male");
        event.put("name", new HashMap() {{
            this.put("first", "a");
            this.put("last", "b");
        }});
        event.put("value", new HashMap() {{
            this.put("list", Arrays.asList(1, 2));
        }});

        event = removeFilter.process(event);
        Assert.assertEquals(event.get("nothing"), 11.11);
        Assert.assertNull(event.get("gender"));
        Assert.assertNull(((Map) event.get("name")).get("first"));
        Assert.assertEquals(((Map) event.get("name")).get("last"), "b");
        Assert.assertEquals(((List) ((Map) event.get("value")).get("list")).size(), 2);
        Assert.assertEquals(((List) ((Map) event.get("value")).get("list")).get(0), 1);
        Assert.assertEquals(((List) ((Map) event.get("value")).get("list")).get(1), 2);
    }
}
