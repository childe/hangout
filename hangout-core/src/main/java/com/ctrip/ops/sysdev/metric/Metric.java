package com.ctrip.ops.sysdev.metric;

import com.codahale.metrics.Meter;
import com.codahale.metrics.MetricRegistry;

/**
 * Created by liujia on 17/7/4.
 */
public  class Metric {

    private static MetricRegistry metricRegistry = new MetricRegistry();

    public static Meter setMetric(String metricName) {
        return metricRegistry.meter(metricName);
    }

    public static MetricRegistry getMetricRegistry()
    {
        return metricRegistry;
    }
}
