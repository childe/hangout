package com.ctrip.ops.sysdev.test;

import java.util.HashMap;
import java.util.Map;

import com.ctrip.ops.sysdev.filters.Add;
import org.testng.Assert;
import org.testng.annotations.Test;
import org.yaml.snakeyaml.Yaml;

public class TestAddFilter {
    @Test
    public void testAddFilter() {
        String c = String.format("%s\n%s\n%s\n%s\n%s\n%s\n%s",
                "        fields:",
                "            nick: '${name}-badboy'",
                "            gender: 'male'",
                "            '[metric][value1]': '10'",
                "            '[metric][value2]': ${name}",
                "            '[metric][value3]': '[extra][value]'",
                "            '[metric][value4]': '[extra][value2]'"
        );
        Yaml yaml = new Yaml();
        Map config = (Map) yaml.load(c);
        Assert.assertNotNull(config);

        Add addFilter = new Add(config);

        // Match
        Map event = new HashMap();
        event.put("name", "liujia");
        event.put("nothing", "11.11");
        event.put("extra", new HashMap() {{
            this.put("value", 10);
        }});

        event = addFilter.process(event);
        Assert.assertEquals(event.get("name"), "liujia");
        Assert.assertEquals(event.get("nick"), "liujia-badboy");
        Assert.assertEquals(event.get("gender"), "male");
        Assert.assertEquals(((Map) event.get("metric")).get("value1"), "10");
        Assert.assertEquals(((Map) event.get("metric")).get("value2"), "liujia");
        Assert.assertEquals(((Map) event.get("metric")).get("value3"), 10);
        Assert.assertNull(((Map) event.get("metric")).get("value4"));
    }
}
