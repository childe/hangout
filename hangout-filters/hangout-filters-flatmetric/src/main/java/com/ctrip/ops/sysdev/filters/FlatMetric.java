package com.ctrip.ops.sysdev.filters;

import java.util.*;

import com.ctrip.ops.sysdev.baseplugin.BaseFilter;

import org.apache.log4j.Logger;

public class FlatMetric extends BaseFilter {
    private static final Logger logger = Logger.getLogger(FlatMetric.class.getName());

    private int windowSize;
    private String key;
    private String value;
    private Map<Object, Map<Object, Integer>> metric;
    private long lastEmitTime;

    public FlatMetric(Map config) {
        super(config);
    }

    protected void prepare() {
        this.key = (String) config.get("key");
        this.value = (String) config.get("value");
        this.windowSize = (int) config.get("windowSize") * 1000;
        this.processExtraEventsFunc = true;
        this.metric = new HashMap();
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
        Iterator<Map.Entry<Object, Map<Object, Integer>>> it = this.metric.entrySet().iterator();
        this.lastEmitTime = System.currentTimeMillis();
        while (it.hasNext()) {
            Map.Entry<Object, Map<Object, Integer>> o = it.next();
            final Object keyValue = o.getKey();
            final Map ValueValue = (Map) o.getValue();
            Iterator<Map.Entry<Object, Integer>> vvit = ValueValue.entrySet().iterator();
            while (vvit.hasNext()) {
                Map.Entry<Object, Integer> vvitentry = vvit.next();
                events.add(new HashMap<String, Object>() {{
                    this.put(key, keyValue);
                    this.put(value, vvitentry.getKey());
                    this.put("count", vvitentry.getValue());
                    this.put("@timestamp", lastEmitTime);
                }});
            }

        }
        this.metric = new HashMap();

        return events;
    }
}
