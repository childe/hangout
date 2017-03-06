package com.ctrip.ops.sysdev.utils;

import com.ctrip.ops.sysdev.baseplugin.BaseFilter;
import org.apache.log4j.Logger;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Arrays;
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

                    logger.info("begin to build filter " + filterType);

                    Class<?> filterClass;
                    Constructor<?> ctor = null;
                    List<String> classNames = Arrays.asList("com.ctrip.ops.sysdev.filters." + filterType, filterType);
                    boolean tryCtrip = true;
                    for (String className : classNames) {
                        try {
                            filterClass = Class.forName(className);
                            ctor = filterClass.getConstructor(Map.class);
                            logger.info("build filter " + filterType + " done");
                            filterProcessors.add((BaseFilter) ctor.newInstance(filterConfig));
                        } catch (ClassNotFoundException e) {
                            if (tryCtrip == true) {
                                logger.info("maybe a third party output plugin. try to build " + filterType);
                                tryCtrip = false;
                                continue;
                            } else {
                                logger.error(e);
                                System.exit(-1);
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                            System.exit(-1);
                        }
                    }
                });
            });
        }

        return filterProcessors;
    }

}
