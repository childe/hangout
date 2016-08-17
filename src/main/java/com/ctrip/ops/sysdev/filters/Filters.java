package com.ctrip.ops.sysdev.filters;

import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.Map;

public class Filters extends BaseFilter {
    private static final Logger logger = Logger.getLogger(Filters.class.getName());

    public Filters(Map config) {
        super(config);
    }

    protected BaseFilter[] filterProcessors;

    protected void prepare() {
        ArrayList<Map> filters = (ArrayList<Map>) config.get("filters");

        this.filterProcessors = Utils.createFilterProcessors(filters);
    }

    ;

    @Override
    protected Map filter(Map event) {
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
}
