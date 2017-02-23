package com.ctrip.ops.sysdev.Utils;

import com.ctrip.ops.sysdev.baseplugin.BaseFilter;
import org.apache.log4j.Logger;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;


@SuppressWarnings("ALL")
public class Utils {
    private static final Logger logger = Logger.getLogger(Utils.class
            .getName());

    public static List<BaseFilter> createFilterProcessors(List<Map> filters) {
        List<BaseFilter> filterProcessors = new ArrayList();

        if (filters != null) {
            filters.stream().forEach((Map filterMap) -> {
                filterMap.entrySet().stream().forEach(entry -> {
                    Map.Entry<String, Map> filter = (Map.Entry<String, Map>) entry;
                    String filterType = filter.getKey();
                    Map filterConfig = filter.getValue();
                    Class<?> filterClass;
                    Constructor<?> ctor = null;
                    logger.info("begin to build filter " + filterType);
                    try {
                        filterClass = Class.forName("com.ctrip.ops.sysdev.filters." + filterType);
                        ctor = filterClass.getConstructor(Map.class);
                        logger.info("build filter " + filterType + " done");
                        filterProcessors.add((BaseFilter) ctor.newInstance(filterConfig));
                    } catch (Exception e) {
                        e.printStackTrace();
                        System.exit(-1);
                    }
                });
            });
        }

        return filterProcessors;
    }

}
