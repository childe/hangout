package com.ctrip.ops.sysdev.test;

import java.util.HashMap;
import java.util.Map;

import com.ctrip.ops.sysdev.filters.Lowercase;
import org.testng.Assert;
import org.testng.annotations.Test;
import org.yaml.snakeyaml.Yaml;

public class TestLowercaseFilter {
    @Test
    public void testLowercaseFilter() {
        String c = String.format("%s\n%s\n%s\n%s",
                "fields:",
                "    - name",
                "    - '[metric][value]'",
                "    - '[metric][value2]'"
        );

        Yaml yaml = new Yaml();
        Map config = (Map) yaml.load(c);
        Assert.assertNotNull(config);

        Lowercase lowercaseFilter = new Lowercase(config);

        // Match
        Map event = new HashMap();
        event.put("name", "Liujia");
        event.put("metric", new HashMap() {{
            this.put("value", "Hello");
            this.put("value3", 10);
        }});

        event = lowercaseFilter.process(event);
        Assert.assertEquals(event.get("name"), "liujia");
        Assert.assertEquals(((Map) event.get("metric")).get("value"), "hello");
        Assert.assertNull(((Map) event.get("metric")).get("value2"));
        Assert.assertEquals(((Map) event.get("metric")).get("value3"),10);
    }
}
