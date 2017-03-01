package com.ctrip.ops.sysdev.test;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.ctrip.ops.sysdev.filters.FlatMetric;
import org.testng.Assert;
import org.testng.annotations.Test;
import org.yaml.snakeyaml.Yaml;


public class TestFlatMetric {
    private ArrayList<Map<String, Object>> process(FlatMetric filter, ArrayList<Map<String, Object>> events) {
        for (int i = 0; i < events.size(); i++) {
            Map rst = filter.process(events.get(i));
            if (rst != null) {
                events.set(i, rst);
            }
        }
        int originEventSize = events.size();
        for (int i = 0; i < originEventSize; i++) {
            List rst = filter.processExtraEvents(events.get(i));
            if (rst != null) {
                events.addAll(rst);
            }
        }
        return events;
    }

    @Test
    public void testFlatMetric() {
        String c = String
                .format("%s\n%s\n%s\n",
                        "key: app",
                        "value: dbname",
                        "windowSize: 1");
        Yaml yaml = new Yaml();
        Map config = (Map) yaml.load(c);
        Assert.assertNotNull(config);

        FlatMetric filter = new FlatMetric(config);

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
        System.out.println(events);

        Assert.assertEquals(events.size(), 5);
        for (Map<String, Object> e : events) {
            if (e.containsKey("app1") && e.containsKey("db1")) {
                Assert.assertEquals(e.get("count"), 1);
            }
            if (e.containsKey("app1") && e.containsKey("db2")) {
                Assert.assertEquals(e.get("count"), 1);
            }
            if (e.containsKey("app2") && e.containsKey("db1")) {
                Assert.assertEquals(e.get("count"), 1);
            }
            if (e.containsKey("app2") && e.containsKey("db3")) {
                Assert.assertEquals(e.get("count"), 2);
            }
        }
    }
}
