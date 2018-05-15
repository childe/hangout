package com.ctrip.ops.sysdev.filters;

import com.ctrip.ops.sysdev.utils.Utils;
import com.ctrip.ops.sysdev.baseplugin.BaseFilter;
import lombok.extern.log4j.Log4j2;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Stack;

@Log4j2
public class Filters extends BaseFilter {
    public Filters(Map config) {
        super(config);
    }

    protected List<BaseFilter> filterProcessors;
    private Map<String, Object> event;

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
            this.event = event;
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
    protected void filterExtraEvents(Stack<Map<String, Object>> to_st) {
        Stack<Map<String, Object>> from_st = new Stack<Map<String, Object>>();
        from_st.push(event);

        for (BaseFilter bf : filterProcessors) {
            while (!from_st.empty()) {
                Map rst = bf.process(from_st.pop());
                if (rst != null) {
                    to_st.push(rst);
                }
            }
            if (bf.processExtraEventsFunc == true) {
                bf.processExtraEvents(to_st);
            }
        }
        return;
    }
}
