package com.ctrip.ops.sysdev.test;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.ctrip.ops.sysdev.filters.Metric;
import org.testng.Assert;
import org.testng.annotations.Test;
import org.yaml.snakeyaml.Yaml;


public class TestMetric {
    private ArrayList<Map<String, Object>> process(Metric filter, ArrayList<Map<String, Object>> events) {
        return events;
    }

    @Test
    public void testMetric() {
        String c = String
                .format("%s\n%s\n%s\n",
                        "key: app",
                        "value: dbname",
                        "windowSize: 1");
        Yaml yaml = new Yaml();
        Map config = (Map) yaml.load(c);
        Assert.assertNotNull(config);

        Metric filter = new Metric(config);

        Map<String, Object> event = new HashMap();
        event.put("app", "app1");
        event.put("dbname", "db1");

        ArrayList<Map<String, Object>> events = new ArrayList();
        events.add(event);
        process(filter, events);

        event = new HashMap();
        event.put("app", "app1");
        event.put("dbname", "db2");
        events = new ArrayList();
        events.add(event);
        process(filter, events);

        event = new HashMap();
        event.put("app", "app2");
        event.put("dbname", "db1");
        events = new ArrayList();
        events.add(event);
        process(filter, events);

        event = new HashMap();
        event.put("app", "app2");
        event.put("dbname", "db3");
        events = new ArrayList();
        events.add(event);
        process(filter, events);

        event = new HashMap();
        event.put("app", "app2");
        event.put("dbname", "db3");
        events = new ArrayList();
        events.add(event);
        process(filter, events);

        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        events = new ArrayList();
        events.add(event);
        events = process(filter, events);

        Assert.assertEquals(events.size(), 2);
        Map app1 = (Map) events.get(1).get("app1");
        Map app2 = (Map) events.get(1).get("app2");
        Assert.assertEquals(app1.get("db1"), 1);
        Assert.assertEquals(app1.get("db2"), 1);
        Assert.assertEquals(app2.get("db1"), 1);
        Assert.assertEquals(app2.get("db2"), null);
        Assert.assertEquals(app2.get("db3"), 2);
    }
}
