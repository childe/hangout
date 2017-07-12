package com.ctrip.ops.sysdev.metrics;

import com.codahale.metrics.MetricFilter;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.MetricSet;
import com.codahale.metrics.graphite.GraphiteReporter;
import com.codahale.metrics.graphite.PickledGraphite;
import com.ctrip.ops.sysdev.baseplugin.BaseMetric;
import lombok.extern.log4j.Log4j2;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import com.ctrip.ops.sysdev.metric.Metric;

/**
 * Created by liujia on 17/7/4.
 */

@Log4j2
public class Graphit extends BaseMetric {

    private final String host;
    private final int port;
    private String prefix;
    private HashMap<String, ArrayList<String>> metrics;
    private final MetricRegistry metricRegistry = new MetricRegistry();

    public Graphit(Map config) {
        super(config);

        this.host = (String) config.get("host");
        this.port = (Integer) config.get("port");
        this.prefix = (String) config.get("prefix");
        this.metrics = (HashMap<String, ArrayList<String>>) config.get("metrics");
    }

    public void register() {
        this.metricRegistry.registerAll(Metric.getMetricRegistry());

        this.metrics.forEach((metricType, metricNames) -> {
            Class<?> metricClass = null;
            try {
                metricClass = Class.forName(metricType);
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
                log.error(e);
                System.exit(1);
            }

            Constructor<?> ctor = null;
            try {
                ctor = metricClass.getConstructor();
            } catch (NoSuchMethodException e) {
                e.printStackTrace();
                log.error(e);
                System.exit(1);
            }

            final MetricSet metricInstance;
            try {
                metricInstance = (MetricSet) ctor.newInstance();

                if (metricNames.size() == 0) {
                    metricRegistry.registerAll(metricInstance);
                } else {
                    metricNames.forEach(name -> {
                        metricRegistry.register(name, metricInstance);
                    });
                }
            } catch (InstantiationException e) {
                e.printStackTrace();
                log.error(e);
                System.exit(1);
            } catch (IllegalAccessException e) {
                e.printStackTrace();
                log.error(e);
                System.exit(1);
            } catch (InvocationTargetException e) {
                e.printStackTrace();
                log.error(e);
                System.exit(1);
            }

        });

        String localHostName = "localhost";
        try {
            localHostName = InetAddress.getLocalHost().getHostName().replace('.', '_');
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }

        if ("".equals(this.prefix)) {
            this.prefix = localHostName;
        } else {
            this.prefix += "." + localHostName;
        }

        PickledGraphite pickledGraphite = new PickledGraphite(new InetSocketAddress(host, port));
        GraphiteReporter reporter = GraphiteReporter.forRegistry(metricRegistry)
                .prefixedWith(this.prefix)
                .convertRatesTo(TimeUnit.SECONDS)
                .convertDurationsTo(TimeUnit.MILLISECONDS)
                .filter(MetricFilter.ALL)
                .build(pickledGraphite);
        reporter.start(1, TimeUnit.MINUTES);
    }
}
