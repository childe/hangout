package com.ctrip.ops.sysdev.watcher;

import com.codahale.metrics.Meter;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.health.HealthCheckRegistry;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.extern.log4j.Log4j;

/**
 * Created by gnuhpc on 2017/2/16.
 */
@Log4j
@Data
public class Watcher {
    private static Watcher instance;
    private MetricRegistry metricRegistry = new MetricRegistry();
    private HealthCheckRegistry healthCheckRegistry = new HealthCheckRegistry();

    private Watcher() {
    }

    public static Watcher getWatcher() {
        if (instance == null) {
            synchronized (Watcher.class) {
                if (instance == null) {
                    instance = new Watcher();
                }
            }
        }
        return instance;
    }

    public Meter setMetric(String metricName) {
        return metricRegistry.meter(metricName);
    }

}
