package com.ctrip.ops.sysdev.test;

import java.util.HashMap;
import java.util.Map;

import com.ctrip.ops.sysdev.filters.Add;
import org.testng.Assert;
import org.testng.annotations.Test;
import org.yaml.snakeyaml.Yaml;

public class TestAddFilter {
    @Test
    public void testConvertFilter() {
        String c = String.format("%s\n%s\n%s\n%s\n%s\n%s\n%s",
                "        fields:",
                "            nick: '${name}-badboy'",
                "            gender: male"
        );
        Yaml yaml = new Yaml();
        Map config = (Map) yaml.load(c);
        Assert.assertNotNull(config);

        Add convertFilter = new Add(config);

        // Match
        Map event = new HashMap();
        event.put("cs_bytes", "12");
        event.put("time_taken", "11.11");

        event = convertFilter.process(event);
        Assert.assertEquals(event.get("cs_bytes"), 12);
        Assert.assertEquals(event.get("time_taken"), 11.11F);
        Assert.assertNull(event.get("tags"));

    }
}
