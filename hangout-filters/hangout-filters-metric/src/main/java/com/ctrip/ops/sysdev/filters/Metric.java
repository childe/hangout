package com.ctrip.ops.sysdev.filters;

import java.util.*;

import com.ctrip.ops.sysdev.baseplugin.BaseFilter;

import lombok.extern.log4j.Log4j2;

@Log4j2
public class Metric extends BaseFilter {


    int windowSize;
    String key;
    String value;
    Map<String, Object> metric;
    Map<String, Object> metricToEmit;
    long lastEmitTime;

    public Metric(Map config) {
        super(config);
    }

    protected void prepare() {
        this.key = (String) config.get("key");
        this.value = (String) config.get("value");
        this.windowSize = (int) config.get("windowSize") * 1000;
        this.processExtraEventsFunc = true;
        this.metric = new HashMap();
        this.metricToEmit = new HashMap();

        this.lastEmitTime = System.currentTimeMillis();
    }

    @Override
    protected Map filter(final Map event) {
        if (System.currentTimeMillis() >= this.windowSize + this.lastEmitTime) {
            this.metricToEmit = this.metric;
            this.metric = new HashMap();
        }
        if (event.containsKey(this.key) && event.containsKey(this.value)) {
            String keyValue = event.get(this.key).toString();
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
        if (metricToEmit.size() == 0) {
            return null;
        }
        List<Map<String, Object>> events = new ArrayList<Map<String, Object>>();
        this.metricToEmit.put("@timestamp", this.lastEmitTime);
        this.postProcess(this.metricToEmit, true);
        events.add(this.metricToEmit);
        this.metricToEmit = new HashMap();
        this.lastEmitTime = System.currentTimeMillis();
        return events;
    }
}
