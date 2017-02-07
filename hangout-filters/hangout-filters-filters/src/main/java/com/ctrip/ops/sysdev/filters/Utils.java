package com.ctrip.ops.sysdev.filters;

import com.ctrip.ops.sysdev.baseplugin.BaseFilter;
import org.apache.log4j.Logger;

import java.lang.reflect.Constructor;
import java.util.List;
import java.util.Iterator;
import java.util.Map;


@SuppressWarnings("ALL")
public class Utils {
    private static final Logger logger = Logger.getLogger(Utils.class
            .getName());

    static public BaseFilter[] createFilterProcessors(List<Map> filters) {
        BaseFilter[] filterProcessors;

        if (filters != null) {
            filterProcessors = new BaseFilter[filters.size()];

            int idx = 0;
            for (Map filter : filters) {
                Iterator<Map.Entry<String, Map>> filterIT = filter.entrySet()
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
        return filterProcessors;
    }
}
