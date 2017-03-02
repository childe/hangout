package com.ctrip.ops.sysdev.test;

import java.util.HashMap;
import java.util.Map;

import com.ctrip.ops.sysdev.filters.Uppercase;
import org.testng.Assert;
import org.testng.annotations.Test;
import org.yaml.snakeyaml.Yaml;

public class TestUppercaseFilter {
    @Test
    public void testUppercaseFilter() {
        String c = String.format("%s\n%s\n%s\n%s",
                "fields:",
                "    - name",
                "    - '[metric][value]'",
                "    - '[metric][value2]'"
        );

        Yaml yaml = new Yaml();
        Map config = (Map) yaml.load(c);
        Assert.assertNotNull(config);

        Uppercase uppercaseFilter = new Uppercase(config);

        // Match
        Map event = new HashMap();
        event.put("name", "Liujia");
        event.put("metric", new HashMap() {{
            this.put("value", "Hello");
            this.put("value3", 10);
        }});

        event = uppercaseFilter.process(event);
        Assert.assertEquals(event.get("name"), "LIUJIA");
        Assert.assertEquals(((Map) event.get("metric")).get("value"), "HELLO");
        Assert.assertNull(((Map) event.get("metric")).get("value2"));
        Assert.assertEquals(((Map) event.get("metric")).get("value3"), 10);
    }
}
