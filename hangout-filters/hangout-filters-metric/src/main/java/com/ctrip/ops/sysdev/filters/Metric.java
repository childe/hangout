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

    public Metric(Map config) {
        super(config);
    }

    protected void prepare() {
        this.key = (String) config.get("key");
        this.value = (String) config.get("value");
        this.windowSize = (int) config.get("windowSize");
        this.processExtraEventsFunc = true;
        this.metric = new HashMap<String, Object>();
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
        List<Map<String, Object>> events = new ArrayList<Map<String, Object>>();
        events.add(this.metric);
        return events;
    }
}
