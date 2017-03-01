package com.ctrip.ops.sysdev.test;

import com.ctrip.ops.sysdev.filters.Filters;
import org.testng.Assert;
import org.testng.annotations.Test;
import org.yaml.snakeyaml.Yaml;

import java.util.HashMap;
import java.util.Map;

public class TestFilters {
    @Test
    @SuppressWarnings({"rawtypes", "unchecked"})
    public void testFilters() {
        // General Test
        String c = String.format("%s\n%s\n%s\n%s\n%s\n%s\n%s\n%s\n%s\n%s\n%s\n%s\n%s\n%s\n%s\n%s\n",
                "if:",
                "  - '<#if message??>true</#if>'",
                "  - '<#if message?contains(\"liu\")>true<#elseif message?contains(\"warn\")>true</#if>'",
                "filters:",
                "    - Grok:",
                "        match:",
                "          - '^(?<logtime>\\S+) %{USERNAME:user} (-|%{LOGLEVEL:level}) %{DATA:msg}$'",
                "        remove_fields: ['message']",
                "    - Add:",
                "        fields:",
                "            test: 'abcd'",
                "    - Date:",
                "        src: logtime",
                "        formats:",
                "            - 'ISO8601'",
                "        remove_fields: ['logtime']");


        Yaml yaml = new Yaml();
        Map config = (Map) yaml.load(c);
        Assert.assertNotNull(config);

        Filters Filtersfilter = new Filters(config);

        Map event = new HashMap();
        event.put("message", "2016-06-10T13:30:33.040+08:00 username info messsage body.");
        event = Filtersfilter.process(event);
        Assert.assertEquals(event.get("message"), "2016-06-10T13:30:33.040+08:00 username info messsage body.");
        Assert.assertNull(event.get("user"));
        Assert.assertNotNull(event.get("message"));

        event = new HashMap();
        event.put("message", "2016-06-10T13:30:33.040+08:00 username warning message body.");
        event = Filtersfilter.process(event);
        Assert.assertEquals(event.get("user"), "username");
        Assert.assertEquals(event.get("level"), "warning");
        Assert.assertNull(event.get("message"));


        c = String.format("%s\n%s\n%s\n%s\n%s\n%s\n%s\n%s\n%s\n%s\n",
                "if:",
                "  - '<#if message??>true</#if>'",
                "  - '<#if message?contains(\"liu\")>true<#elseif message?contains(\"warn\")>true</#if>'",
                "filters:",
                "    - Add:",
                "        fields:",
                "            a: 'a'",
                "    - Add:",
                "        fields:",
                "            b: 'b'");

        yaml = new Yaml();
        config = (Map) yaml.load(c);
        Assert.assertNotNull(config);

        Filtersfilter = new Filters(config);

        event = new HashMap();
        event.put("message", "2016-06-10T13:30:33.040+08:00 username warn messsage body.");
        event = Filtersfilter.process(event);
        Assert.assertEquals(event.get("a"), "a");
        Assert.assertEquals(event.get("b"), "b");
    }
}
