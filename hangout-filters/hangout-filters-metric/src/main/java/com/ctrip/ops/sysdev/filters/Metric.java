package com.ctrip.ops.sysdev.filters;

import java.util.*;

import com.ctrip.ops.sysdev.baseplugin.BaseFilter;

import org.apache.log4j.Logger;

public class Metric extends BaseFilter {
    private static final Logger logger = Logger.getLogger(Metric.class.getName());

    private int windowSize;
    private String key;
    private String value;
    private Map<String, Object> metric;
    private long lastEmitTime;

    public Metric(Map config) {
        super(config);
    }

    protected void prepare() {
        this.key = (String) config.get("key");
        this.value = (String) config.get("value");
        this.windowSize = (int) config.get("windowSize") * 1000;
        this.processExtraEventsFunc = true;
        this.metric = new HashMap<String, Object>();

        this.lastEmitTime = System.currentTimeMillis();
    }

    @Override
    protected Map filter(final Map event) {
        if (event.containsKey(this.key) && event.containsKey(this.value)) {
            String keyValue = (String) event.get(this.key);
            Object valueValue = event.get(this.value);
            HashMap set = (HashMap) this.metric.get(keyValue);
            if (set == null) {
                set = new HashMap();
                set.put(valueValue, 1);
            } else {
                if (set.containsKey(valueValue)) {
                    int count = (int) set.get(valueValue);
                    set.put(valueValue, count + 1);
                } else {
                    set.put(valueValue, 1);
                }
            }
            this.metric.put(keyValue, set);
        }

        return event;
    }

    @Override
    public List<Map<String, Object>> filterExtraEvents(Map event) {
        if (System.currentTimeMillis() < this.windowSize + this.lastEmitTime) {
            return null;
        }
        List<Map<String, Object>> events = new ArrayList<Map<String, Object>>();
        this.metric.put("@timestamp", this.lastEmitTime);
        this.postProcess(this.metric, true);
        events.add(this.metric);
        this.lastEmitTime = System.currentTimeMillis();
        return events;
    }
}
