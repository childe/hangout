package com.ctrip.ops.hangout.filter.test;


import java.util.ArrayList;

import org.joda.time.DateTime;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.ctrip.ops.hangout.filter.LinkMetric;
import org.testng.Assert;
import org.testng.annotations.Test;


public class TestLinkMetric {
    private ArrayList<Map<String, Object>> process(LinkMetric filter, ArrayList<Map<String, Object>> events) {
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
    public void testMetric() {
        Map config = new HashMap<>();
        config.put("reserveWindow", 10);
        config.put("batchWindow", 2);
        config.put("timestamp", "@timestamp");
        config.put("fieldsLink", "cip->sip->url");

        LinkMetric filter = new LinkMetric(config);

        DateTime now = DateTime.now();

        Map<String, Object> event = new HashMap();
        event.put("cip", "127.0.0.1");
        event.put("sip", "10.10.10.11");
        event.put("url", "/");
        event.put("@timestamp", now);
        filter.process(event);
        filter.processExtraEvents(event);

        event = new HashMap();
        event.put("cip", "127.0.0.1");
        event.put("sip", "10.10.10.11");
        event.put("url", "/hello");
        event.put("@timestamp", now);
        filter.process(event);
        filter.processExtraEvents(event);

        event = new HashMap();
        event.put("cip", "127.0.0.1");
        event.put("sip", "10.10.10.11");
        event.put("url", "/hello");
        event.put("@timestamp", now);
        filter.process(event);
        filter.processExtraEvents(event);

        event = new HashMap();
        event.put("cip", "127.0.0.1");
        event.put("sip", "10.10.10.12");
        event.put("url", "/hello");
        event.put("@timestamp", now);
        filter.process(event);
        filter.processExtraEvents(event);

        event = new HashMap();
        event.put("cip", "127.0.0.2");
        event.put("sip", "10.10.10.12");
        event.put("url", "/hello");
        event.put("@timestamp", now);
        filter.process(event);
        filter.processExtraEvents(event);

        try {
            Thread.sleep(2500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        event = new HashMap();
        filter.process(event);

        List events = filter.processExtraEvents(event);

        events.forEach(e -> {
            System.out.println(e);
        });
        Assert.assertEquals(events.size(), 4);
    }
}
