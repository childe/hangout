package com.ctrip.ops.sysdev.test;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import com.ctrip.ops.sysdev.filters.Convert;
import org.testng.Assert;
import org.testng.annotations.Test;
import org.yaml.snakeyaml.Yaml;

public class TestConvertFilter {
    @Test
    @SuppressWarnings({"rawtypes", "unchecked", "serial"})
    public void testConvertFilter() throws UnsupportedEncodingException {
        String c = String.format("%s\n%s\n%s\n%s\n%s\n%s\n%s",
                "        fields:",
                "            cs_bytes: ",
                "                to: integer",
                "                remove_if_fail: true",
                "            time_taken: ",
                "                to: float",
                "                setto_if_fail: 0.0");
        Yaml yaml = new Yaml();
        Map config = (Map) yaml.load(c);
        Assert.assertNotNull(config);

        Convert convertFilter = new Convert(config);

        // Match
        Map event = new HashMap();
        event.put("cs_bytes", "12");
        event.put("time_taken", "11.11");

        event = convertFilter.process(event);
        Assert.assertEquals(event.get("cs_bytes"), 12);
        Assert.assertEquals(event.get("time_taken"), 11.11F);
        Assert.assertNull(event.get("tags"));

        // remove_if_fail
        event = new HashMap();
        event.put("cs_bytes", "12.2");

        event = convertFilter.process(event);
        Assert.assertNull(event.get("cs_bytes"));
        Assert.assertEquals(((ArrayList) event.get("tags")).get(0),
                "convertfail");

        // setto_if_fail
        event = new HashMap();
        event.put("time_taken", "12.2a");

        event = convertFilter.process(event);
        Assert.assertEquals(event.get("time_taken"), 0.0);
        Assert.assertEquals(((ArrayList) event.get("tags")).get(0),
                "convertfail");

        // set tag
        c = String.format("%s\n%s\n%s\n%s\n%s\n%s",
                "fields:",
                "  cs_bytes: ",
                "    to: integer",
                "    remove_if_fail: true",
                "  time_taken: ",
                "    to: float");
        yaml = new Yaml();
        config = (Map) yaml.load(c);

        convertFilter = new Convert(config);
        event = new HashMap();
        event.put("time_taken", "12.2a");

        event = convertFilter.process(event);
        Assert.assertEquals(((ArrayList) event.get("tags")).get(0),
                "convertfail");

        // NOT set tag
        c = String.format("%s\n%s\n%s\n%s\n%s\n%s\n%s",
                "tag_on_failure: null",
                "fields:",
                "  cs_bytes: ",
                "    to: integer",
                "    remove_if_fail: true",
                "  time_taken: ",
                "    to: float");
        yaml = new Yaml();
        config = (Map) yaml.load(c);

        convertFilter = new Convert(config);
        event = new HashMap();
        event.put("time_taken", "12.2a");

        event = convertFilter.process(event);
        Assert.assertNull((ArrayList) event.get("tags"));

        // test multi level field
        c = String.format("%s\n%s\n%s\n%s\n%s",
                "tag_on_failure: null",
                "fields:",
                "  '[metric][a]': ",
                "    to: integer",
                "    remove_if_fail: true");
        yaml = new Yaml();
        config = (Map) yaml.load(c);

        convertFilter = new Convert(config);
        event = new HashMap();
        event.put("metric", new HashMap() {{
            this.put("a", "1200");
        }});

        event = convertFilter.process(event);
        Assert.assertNull((ArrayList) event.get("tags"));
        Assert.assertEquals(1200, ((Map) event.get("metric")).get("a"));

        // test infinity
        c = String.format("%s\n%s\n%s\n%s",
                "tag_on_failure: null",
                "fields:",
                "  time_taken: ",
                "    to: double");
        yaml = new Yaml();
        config = (Map) yaml.load(c);

        convertFilter = new Convert(config);
        event = new HashMap();
        event.put("time_taken", "723E095012");
        event = convertFilter.process(event);
        Assert.assertEquals(event.get("time_taken"), Double.POSITIVE_INFINITY);

        c = String.format("%s\n%s\n%s\n%s\n%s",
                "tag_on_failure: null",
                "fields:",
                "  time_taken: ",
                "    to: double",
                "    allow_infinity: false"
        );
        yaml = new Yaml();
        config = (Map) yaml.load(c);

        convertFilter = new Convert(config);
        event = new HashMap();
        event.put("time_taken", "723E095012");
        event = convertFilter.process(event);
        Assert.assertEquals(event.get("time_taken"), "723E095012");


        c = String.format("%s\n%s\n%s\n%s\n%s\n%s",
                "tag_on_failure: null",
                "fields:",
                "  time_taken: ",
                "    to: double",
                "    allow_infinity: false",
                "    remove_if_fail: true"
        );
        yaml = new Yaml();
        config = (Map) yaml.load(c);

        convertFilter = new Convert(config);
        event = new HashMap();
        event.put("time_taken", "723E095012");
        event = convertFilter.process(event);
        Assert.assertNull(event.get("time_taken"));
    }
}
