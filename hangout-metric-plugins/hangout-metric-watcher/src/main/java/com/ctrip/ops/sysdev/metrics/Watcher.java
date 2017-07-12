package com.ctrip.ops.sysdev.metrics;


import com.codahale.metrics.health.HealthCheckRegistry;
import com.codahale.metrics.servlets.AdminServlet;
import com.codahale.metrics.servlets.HealthCheckServlet;
import com.codahale.metrics.servlets.MetricsServlet;
import com.ctrip.ops.sysdev.baseplugin.BaseMetric;
import io.undertow.Handlers;
import io.undertow.Undertow;
import io.undertow.server.handlers.PathHandler;
import io.undertow.servlet.Servlets;
import io.undertow.servlet.api.DeploymentInfo;
import io.undertow.servlet.api.DeploymentManager;
import lombok.extern.log4j.Log4j2;


import javax.servlet.ServletException;
import java.util.Map;

import com.ctrip.ops.sysdev.metric.Metric;

/**
 * Created by liujia on 17/7/4.
 */
@Log4j2
public class Watcher extends BaseMetric {


    private final String host;
    private final int port;
    private HealthCheckRegistry healthCheckRegistry = new HealthCheckRegistry();

    public Watcher(Map config) {
        super(config);

        this.host = (String) config.get("host");
        this.port = (Integer) config.get("port");

    }

    public void register() {
        Runnable task = () -> {
            DeploymentInfo servletBuilder = Servlets.deployment()
                    .setClassLoader(Watcher.class.getClassLoader())
                    .setContextPath("/")
                    .setDeploymentName("admin.war")
                    .addServletContextAttribute(MetricsServlet.METRICS_REGISTRY, Metric.getMetricRegistry())
                    .addServletContextAttribute(HealthCheckServlet.HEALTH_CHECK_REGISTRY, this.healthCheckRegistry)
                    .addServlets(
                            Servlets.servlet("AdminServlet", AdminServlet.class).addMapping("/*")
                    );

            DeploymentManager manager = Servlets.defaultContainer().addDeployment(servletBuilder);
            manager.deploy();
            PathHandler path = null;
            try {
                path = Handlers.path().addPrefixPath("/", manager.start());
            } catch (ServletException e) {
                log.error("Admin Servlet error: ", e);
            }

            Undertow server = Undertow.builder()
                    .addHttpListener(port, host)
                    .setHandler(path)
                    .build();
            try {
                server.start();
            } catch (RuntimeException e) {
                log.error("Admin server start error: ", e);
            }
        };
        task.run();
    }
}
