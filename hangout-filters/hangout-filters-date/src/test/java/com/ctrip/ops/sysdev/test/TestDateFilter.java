package com.ctrip.ops.sysdev.test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.ctrip.ops.sysdev.filters.Date;
import org.joda.time.DateTime;
import org.testng.Assert;
import org.testng.annotations.Test;
import org.yaml.snakeyaml.Yaml;

public class TestDateFilter {
    @Test
    public void testAddFilter() {
        // success, remove src field
        String c = String.format("%s\n%s\n%s\n%s\n%s",
                "src: logtime",
                "formats:",
                "    - 'ISO8601'",
                "    - 'YYYY.MM.dd HH:mm:ssZ'",
                "remove_fields: ['logtime']"
        );
        Yaml yaml = new Yaml();
        Map config = (Map) yaml.load(c);
        Assert.assertNotNull(config);

        Date dateFilter = new Date(config);

        Map event = new HashMap();
        event.put("logtime", "2010-03-02T19:59:26+0800");
        event.put("nothing", "11.11");
        event.put("extra", new HashMap() {{
            this.put("value", 10);
        }});

        event = dateFilter.process(event);
        Assert.assertEquals(((DateTime) event.get("@timestamp")).getYear(), 2010);
        Assert.assertEquals(event.get("nothing"), "11.11");
        Assert.assertEquals(((Map) event.get("extra")).get("value"), 10);
        Assert.assertNull(event.get("tags"));
        Assert.assertNull(event.get("logtime"));


        // success, remove src field, addyear
        c = String.format("%s\n%s\n%s\n%s\n%s\n%s",
                "src: logtime",
                "add_year: true",
                "formats:",
                "    - 'ISO8601'",
                "    - 'YYYYMM.dd HH:mm:ssZ'",
                "remove_fields: ['logtime']"
        );
        yaml = new Yaml();
        config = (Map) yaml.load(c);
        Assert.assertNotNull(config);

        dateFilter = new Date(config);

        event = new HashMap();
        event.put("logtime", "03.02 19:59:26+0800");
        event.put("nothing", "11.11");
        event.put("extra", new HashMap() {{
            this.put("value", 10);
        }});

        event = dateFilter.process(event);
        Assert.assertEquals(((DateTime) event.get("@timestamp")).getYear(), DateTime.now().getYear());
        Assert.assertEquals(event.get("nothing"), "11.11");
        Assert.assertEquals(((Map) event.get("extra")).get("value"), 10);
        Assert.assertNull(event.get("tags"));
        Assert.assertNull(event.get("logtime"));


        // success, remove src field, addyear, multilevel target
        c = String.format("%s\n%s\n%s\n%s\n%s\n%s",
                "src: ${extra.value}",
                "add_year: true",
                "formats:",
                "    - 'ISO8601'",
                "    - 'YYYYMM.dd HH:mm:ssZ'",
                "remove_fields: ['logtime']"
        );
        yaml = new Yaml();
        config = (Map) yaml.load(c);
        Assert.assertNotNull(config);

        dateFilter = new Date(config);

        event = new HashMap();
        event.put("logtime", "03.02 19:59:26+0800");
        event.put("nothing", "11.11");
        event.put("extra", new HashMap() {{
            this.put("value", "03.02 19:59:26+0800");
        }});

        event = dateFilter.process(event);
        Assert.assertEquals(((DateTime) event.get("@timestamp")).getYear(), DateTime.now().getYear());
        Assert.assertEquals(event.get("nothing"), "11.11");
        Assert.assertNull(event.get("tags"));
        Assert.assertNull(event.get("logtime"));


        // success, remove src field, addyear, multilevel target
        c = String.format("%s\n%s\n%s\n%s\n%s\n%s",
                "src: '[extra][value]'",
                "add_year: true",
                "formats:",
                "    - 'ISO8601'",
                "    - 'YYYYMM.dd HH:mm:ssZ'",
                "remove_fields: ['logtime']"
        );
        yaml = new Yaml();
        config = (Map) yaml.load(c);
        Assert.assertNotNull(config);

        dateFilter = new Date(config);

        event = new HashMap();
        event.put("logtime", "03.02 19:59:26+0800");
        event.put("nothing", "11.11");
        event.put("extra", new HashMap() {{
            this.put("value", "03.02 19:59:26+0800");
        }});

        event = dateFilter.process(event);
        Assert.assertEquals(((DateTime) event.get("@timestamp")).getYear(), DateTime.now().getYear());
        Assert.assertEquals(event.get("nothing"), "11.11");
        Assert.assertNull(event.get("tags"));
        Assert.assertNull(event.get("logtime"));


        // success, remove src field, multilevel target
        c = String.format("%s\n%s\n%s\n%s\n%s",
                "src: '[extra][value]'",
                "target: '[extra][value2]'",
                "formats:",
                "    - 'ISO8601'",
                "    - 'YYYY.MM.dd HH:mm:ssZ'"
        );
        yaml = new Yaml();
        config = (Map) yaml.load(c);
        Assert.assertNotNull(config);

        dateFilter = new Date(config);

        event = new HashMap();
        event.put("logtime", "03.02 19:59:26+0800");
        event.put("nothing", "11.11");
        event.put("extra", new HashMap() {{
            this.put("value", "2010.03.02 19:59:26+0800");
        }});

        event = dateFilter.process(event);
        Assert.assertEquals(((DateTime) ((Map) event.get("extra")).get("value2")).getYear(), 2010);
        Assert.assertEquals(event.get("nothing"), "11.11");
        Assert.assertNull(event.get("tags"));
        Assert.assertNull(event.get("@timestamp"));


        // fail
        c = String.format("%s\n%s\n%s\n%s\n%s",
                "src: '[extra][value]'",
                "target: '[extra][value2]'",
                "formats:",
                "    - 'YYYYMMddHHmmssZ'",
                "remove_fields: ['logtime']"
        );
        yaml = new Yaml();
        config = (Map) yaml.load(c);
        Assert.assertNotNull(config);

        dateFilter = new Date(config);

        event = new HashMap();
        event.put("logtime", "03.02 19:59:26+0800");
        event.put("nothing", "11.11");
        event.put("extra", new HashMap() {{
            this.put("value", "2010.03.02 19:59:26+0800");
        }});

        event = dateFilter.process(event);
        Assert.assertNull(((Map) event.get("extra")).get("value2"));
        Assert.assertEquals(event.get("nothing"), "11.11");
        Assert.assertEquals(((List) event.get("tags")).get(0), "datefail");
        Assert.assertNull(event.get("@timestamp"));
    }
}
