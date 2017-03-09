package com.ctrip.ops.sysdev.core;

import com.codahale.metrics.servlets.AdminServlet;
import com.codahale.metrics.servlets.HealthCheckServlet;
import com.codahale.metrics.servlets.MetricsServlet;
import com.ctrip.ops.sysdev.baseplugin.BaseInput;
import com.ctrip.ops.sysdev.config.CommandLineValues;
import com.ctrip.ops.sysdev.config.HangoutConfig;
import com.ctrip.ops.sysdev.log.LogSetter;
import com.ctrip.ops.sysdev.watcher.Watcher;
import io.undertow.Handlers;
import io.undertow.Undertow;
import io.undertow.server.handlers.PathHandler;
import io.undertow.servlet.Servlets;
import io.undertow.servlet.api.DeploymentInfo;
import io.undertow.servlet.api.DeploymentManager;
import lombok.extern.log4j.Log4j;

import javax.servlet.ServletException;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.*;

@Log4j
public class Main {
    private static LogSetter logSetter = new LogSetter();

    public static void main(String[] args) {

        //Parse CommandLine arguments
        CommandLineValues cm = new CommandLineValues(args);
        cm.parseCmd();

        //Set Log Level and appenders
        logSetter.initLogger(cm);

        // parse configure file
        Map configs = null;
        try {
            configs = HangoutConfig.parse(cm.getConfigFile());
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(-1);
        }
        log.debug(configs);

        final List<HashMap<String, Map>> inputConfigs = (ArrayList<HashMap<String, Map>>) configs.get("inputs");
        final List<HashMap<String, Map>> filterConfigs = (ArrayList<HashMap<String, Map>>) configs.get("filters");
        final List<HashMap<String, Map>> outputConfigs = (ArrayList<HashMap<String, Map>>) configs.get("outputs");
        final HashMap<String, Object> commonsConfigs = (HashMap<String, Object>) configs.get("commons");

        runAdminServer(commonsConfigs);

        // for input in all_inputs, Go through every input and emit immediately
        inputConfigs.forEach(
                input -> {
                    input.forEach((inputType, inputConfig) -> {
                        log.info("begin to build input " + inputType);

                        Class<?> inputClass = null;

                        List<String> classNames = Arrays.asList("com.ctrip.ops.sysdev.inputs." + inputType, inputType);
                        boolean tryCtrip = true;
                        for (String className : classNames) {
                            try {
                                inputClass = Class.forName(className);
                                //Get Constructor for each input
                                Constructor<?> ctor = inputClass.getConstructor(
                                        Map.class,
                                        ArrayList.class,
                                        ArrayList.class);
                                //instantiate the input,prepare() and registerShutdownHookForSelf() are called here.
                                BaseInput inputInstance = (BaseInput) ctor.newInstance(
                                        inputConfig,
                                        filterConfigs,
                                        outputConfigs);

                                log.info("build input " + inputType + " done");
                                //Start working,guy.
                                inputInstance.emit();
                                log.info("input" + inputType + " started");
                                break;
                            } catch (ClassNotFoundException e) {
                                if (tryCtrip == true) {
                                    log.info("maybe a third party input plugin. try to build " + inputType);
                                    tryCtrip = false;
                                    continue;
                                } else {
                                    log.error(e);
                                    System.exit(-1);
                                }
                            } catch (Exception e) {
                                log.error(e);
                                System.exit(-1);
                            }
                        }
                    });
                });
    }

    private static void runAdminServer(HashMap<String, Object> configs) {
        if (configs == null) {
            return;
        }
        Watcher watcher = Watcher.getWatcher();
        boolean isAdminListen = configs.containsKey("admin_listen") ? (boolean) configs.get("admin_listen") : false;
        if (isAdminListen) {
            Runnable task = () -> {
                int adminPort;
                String address = null, adminHost = null;
                adminPort = configs.containsKey("admin_port") ? (int) configs.get("admin_port") : 8080;
                adminHost = configs.containsKey("admin_ip") ? (String) configs.get("admin_ip") : "0.0.0.0";

                DeploymentInfo servletBuilder = Servlets.deployment()
                        .setClassLoader(Main.class.getClassLoader())
                        .setContextPath("/")
                        .setDeploymentName("admin.war")
                        .addServletContextAttribute(MetricsServlet.METRICS_REGISTRY, watcher.getMetricRegistry())
                        .addServletContextAttribute(HealthCheckServlet.HEALTH_CHECK_REGISTRY, watcher.getHealthCheckRegistry())
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
                        .addHttpListener(adminPort, adminHost)
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
}

