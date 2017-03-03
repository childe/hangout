package com.ctrip.ops.sysdev.test;

import java.util.HashMap;
import java.util.Map;

import com.ctrip.ops.sysdev.filters.Trim;
import org.testng.Assert;
import org.testng.annotations.Test;
import org.yaml.snakeyaml.Yaml;

public class TestTrimFilter {
    @Test
    public void testTrimFilter() {
        String c = String.format("%s\n%s\n%s\n%s",
                "fields:",
                "    - name",
                "    - '[metric][value]'",
                "    - '[metric][value2]'"
        );

        Yaml yaml = new Yaml();
        Map config = (Map) yaml.load(c);
        Assert.assertNotNull(config);

        Trim trimFilter = new Trim(config);

        // Match
        Map event = new HashMap();
        event.put("name", " Liuj ia  \t ");
        event.put("metric", new HashMap() {{
            this.put("value", "\t H ello");
            this.put("value3", 10);
        }});

        event = trimFilter.process(event);
        Assert.assertEquals(event.get("name"), "Liuj ia");
        Assert.assertEquals(((Map) event.get("metric")).get("value"), "H ello");
        Assert.assertNull(((Map) event.get("metric")).get("value2"));
        Assert.assertEquals(((Map) event.get("metric")).get("value3"), 10);
    }
}
