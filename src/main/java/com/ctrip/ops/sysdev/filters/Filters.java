package com.ctrip.ops.sysdev.filters;

import org.apache.log4j.Logger;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

public class Filters extends BaseFilter {
    private static final Logger logger = Logger.getLogger(Filters.class.getName());

    public Filters(Map config) {
        super(config);
    }

    protected BaseFilter[] filterProcessors;

    protected void prepare() {
        ArrayList<Map> filters = (ArrayList<Map>) config.get("filters");

        if (filters != null) {
            filterProcessors = new BaseFilter[filters.size()];

            int idx = 0;
            for (Map filter : filters) {
                Iterator<Entry<String, Map>> filterIT = filter.entrySet()
                        .iterator();

                while (filterIT.hasNext()) {
                    Map.Entry<String, Map> filterEntry = filterIT.next();
                    String filterType = filterEntry.getKey();
                    Map filterConfig = filterEntry.getValue();

                    try {
                        logger.info("begin to build filter " + filterType);
                        Class<?> filterClass = Class
                                .forName("com.ctrip.ops.sysdev.filters."
                                        + filterType);
                        Constructor<?> ctor = filterClass
                                .getConstructor(Map.class);

                        BaseFilter filterInstance = (BaseFilter) ctor
                                .newInstance(filterConfig);
                        filterProcessors[idx] = filterInstance;
                        logger.info("build filter " + filterType + " done");
                    } catch (Exception e) {
                        logger.error(e);
                        System.exit(1);
                    }
                    idx++;
                }
            }
        } else {
            filterProcessors = null;
        }
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
