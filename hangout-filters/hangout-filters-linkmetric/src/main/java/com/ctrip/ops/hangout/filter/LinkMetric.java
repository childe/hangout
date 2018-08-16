package com.ctrip.ops.hangout.filter;


import com.ctrip.ops.sysdev.baseplugin.BaseOutput;
import lombok.extern.log4j.Log4j2;
import org.joda.time.DateTime;

import java.util.*;

import com.ctrip.ops.sysdev.baseplugin.BaseFilter;

@Log4j2
public class LinkMetric extends BaseFilter {
    int reserveWindow;
    int batchWindow;
    String timestamp;
    String fieldsLink;
    String[] fields;

    Map<Long, Object> metric;
    Map<Long, Object> metricToEmit;
    long lastEmitTime;

    public LinkMetric(Map config) {
        super(config);
    }

    protected void prepare() {
        this.processExtraEventsFunc = true;

        if (!config.containsKey("fieldsLink")) {
            log.fatal("fieldsLink must be included in config");
            System.exit(4);
        }
        this.fieldsLink = (String) config.get("fieldsLink");

        if (!config.containsKey("timestamp")) {
            this.timestamp = null;
        }
        this.timestamp = (String) config.get("timestamp");

        this.fields = this.fieldsLink.split("->");
        if (this.fields.length <= 1) {
            log.fatal("fieldsLink should contain at least 2 fields");
            System.exit(4);
        }

        if (!config.containsKey("reserveWindow")) {
            log.fatal("reserveWindow must be included in config");
            System.exit(4);
        }
        this.reserveWindow = (int) config.get("reserveWindow") * 1000;

        if (!config.containsKey("batchWindow")) {
            log.fatal("batchWindow must be included in config");
            System.exit(4);
        }
        this.batchWindow = (int) config.get("batchWindow") * 1000;

        this.metric = new HashMap();
        this.metricToEmit = new HashMap();

        this.lastEmitTime = System.currentTimeMillis();
    }

    @Override
    protected Map filter(final Map event) {
        if (System.currentTimeMillis() >= this.batchWindow + this.lastEmitTime) {
            this.metricToEmit = this.metric;
            this.metric = new HashMap();

            this.metricToEmit.forEach((timestamp, s) -> {
                this.metricToEvents((Map) s, 0).forEach((Map<String, Object> e) -> {
                    e.put(this.timestamp, timestamp);
                    this.postProcess(e, true);
                    if (this.nextFilter != null) {
                        e = this.nextFilter.process(e);
                    } else {
                        for (BaseOutput o : this.outputs
                        ) {
                            o.process(e);
                        }
                    }
                });
            });

            this.metricToEmit.clear();
            this.lastEmitTime = System.currentTimeMillis();
        }

        long timestamp = System.currentTimeMillis();
        if (this.timestamp != null) {
            Object o = event.get(this.timestamp);
            if (o != null && o.getClass() == DateTime.class) {
                timestamp = ((DateTime) o).getMillis();
            } else {
                log.debug("timestamp is not instaceof Datetime. use currentTimeMillis");
            }
        }

        if (System.currentTimeMillis() >= this.reserveWindow + timestamp) {
            return event;
        }

        timestamp -= timestamp % this.batchWindow;

        Map set;
        if (this.metric.containsKey(timestamp)) {
            set = (Map) this.metric.get(timestamp);
        } else {
            set = new HashMap<>();
            this.metric.put(timestamp, set);
        }
        for (String field : this.fields) {
            if (event.containsKey(field)) {
                String v = event.get(field).toString();
                if (set.containsKey(v)) {
                    set = (Map) set.get(v);
                    set.put("count", 1 + (long) set.get("count"));
                } else {
                    Map o = new HashMap<String, Object>() {{
                        this.put("count", 1l);
                    }};
                    set.put(v, o);
                    set = o;
                }
            }
        }

        return event;
    }

    private List<Map<String, Object>> metricToEvents(Map metric, int level) {
        String field = this.fields[level];
        List<Map<String, Object>> rst = new ArrayList<>();
        if (level + 1 == this.fields.length) {
            metric.forEach((k, v) -> {
                if (k.toString().contentEquals("count")) {
                    return;
                }
                Map<String, Object> event = new HashMap();
                event.put(field, k);
                event.put("count", ((Map) v).get("count"));
                rst.add(event);
            });
            return rst;
        }
        metric.forEach((k, set) -> {
            if (k.toString().contentEquals("count")) {
                return;
            }
            this.metricToEvents((Map) set, level + 1).forEach((Map<String, Object> e) -> {
                Map<String, Object> event = new HashMap();
                event.put(field, k);
                event.putAll(e);
                rst.add(event);
            });
        });

        return rst;
    }

    public List<Map<String, Object>> filterExtraEvents(Map event) {

        if (metricToEmit.size() == 0) {
            return null;
        }
        List<Map<String, Object>> events = new ArrayList();

        this.metricToEmit.forEach((timestamp, s) -> {
            this.metricToEvents((Map) s, 0).forEach((Map<String, Object> e) -> {
                e.put(this.timestamp, timestamp);
                this.postProcess(e, true);
                events.add(e);
            });
        });

        this.metricToEmit.clear();
        this.lastEmitTime = System.currentTimeMillis();

        return events;
    }

}
