package com.ctrip.ops.sysdev.filters;

import com.ctrip.ops.sysdev.utils.Utils;
import com.ctrip.ops.sysdev.baseplugin.BaseFilter;
import lombok.extern.log4j.Log4j2;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Log4j2
public class Filters extends BaseFilter {
    public Filters(Map config) {
        super(config);
    }

    protected List<BaseFilter> filterProcessors;

    protected void prepare() {
        ArrayList<Map> filters = (ArrayList<Map>) config.get("filters");

        this.filterProcessors = Utils.createFilterProcessors(filters);
        for (BaseFilter filterProcessor : filterProcessors) {
            if (filterProcessor.processExtraEventsFunc == true) {
                this.processExtraEventsFunc = true;
            }
        }
    }

    @Override
    protected Map filter(Map event) {
        if (this.processExtraEventsFunc == true) {
            //will prcess the event in filterExtraEvents
            return event;
        }
        if (this.filterProcessors != null) {
            for (BaseFilter bf : filterProcessors) {
                if (event == null) {
                    break;
                }
                event = bf.process(event);
            }
        }
        return event;
    }

    @Override
    protected List<Map<String, Object>> filterExtraEvents(Map event) {
        ArrayList<Map<String, Object>> events = new ArrayList<Map<String, Object>>();
        events.add(event);
        for (BaseFilter bf : filterProcessors) {
            if (events == null) {
                break;
            }
            for (int i = 0; i < events.size(); i++) {
                events.set(i, bf.process(events.get(i)));
            }
            if (bf.processExtraEventsFunc == true) {
                int originEventSize = events.size();
                for (int i = 0; i < originEventSize; i++) {
                    List rst = bf.processExtraEvents(events.get(i));
                    if (rst != null) {
                        events.addAll(rst);
                    }
                }
            }
        }
        return events;
    }
}
