package com.ctrip.ops.sysdev.filters;

import lombok.extern.log4j.Log4j2;

import java.util.*;

@Log4j2
public class FlatMetric extends Metric {
    public FlatMetric(Map config) {
        super(config);
        this.metricToEmit = new HashMap<>();
    }

    @Override
    public List<Map<String, Object>> filterExtraEvents(Map event) {
        if (metricToEmit.size() == 0) {
            return null;
        }
        List<Map<String, Object>> events = new ArrayList<Map<String, Object>>();
        Iterator<Map.Entry<String, Object>> it = this.metricToEmit.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<String, Object> o = it.next();
//            Map.Entry<String, Map<Object, Integer>> o = it.next();
            final Object keyValue = o.getKey();
            final Map ValueValue = (Map) o.getValue();
            Iterator<Map.Entry<Object, Integer>> vvit = ValueValue.entrySet().iterator();
            while (vvit.hasNext()) {
                Map.Entry<Object, Integer> vvitentry = vvit.next();
                HashMap e = new HashMap<String, Object>() {{
                    this.put(key, keyValue);
                    this.put(value, vvitentry.getKey());
                    this.put("count", vvitentry.getValue());
                    this.put("@timestamp", lastEmitTime);
                }};
                this.postProcess(e, true);
                events.add(e);
            }

        }
        this.metricToEmit.clear();
        this.lastEmitTime = System.currentTimeMillis();

        return events;
    }
}
