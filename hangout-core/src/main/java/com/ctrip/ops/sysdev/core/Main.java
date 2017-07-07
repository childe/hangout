package com.ctrip.ops.sysdev.core;


import com.ctrip.ops.sysdev.baseplugin.BaseInput;
import com.ctrip.ops.sysdev.baseplugin.BaseMetric;
import com.ctrip.ops.sysdev.config.CommandLineValues;
import com.ctrip.ops.sysdev.config.HangoutConfig;
import com.ctrip.ops.sysdev.log.LogSetter;

import org.apache.log4j.Logger;

import java.lang.reflect.Constructor;
import java.util.*;

public class Main {
    private static LogSetter logSetter = new LogSetter();

    private static final Logger logger = Logger.getLogger(Main.class.getName());

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
        logger.debug(configs);

        final List<HashMap<String, Map>> inputConfigs = (ArrayList<HashMap<String, Map>>) configs.get("inputs");
        final List<HashMap<String, Map>> filterConfigs = (ArrayList<HashMap<String, Map>>) configs.get("filters");
        final List<HashMap<String, Map>> outputConfigs = (ArrayList<HashMap<String, Map>>) configs.get("outputs");
        final List<HashMap<String, Map>> metricsConfigs = (ArrayList<HashMap<String, Map>>) configs.get("metrics");

        if (metricsConfigs != null) {
            metricsConfigs.forEach(metric -> {
                metric.forEach((metricType, metricConfig) -> {
                    logger.info("begin to build metric " + metricType);

                    Class<?> metricClass = null;

                    List<String> classNames = Arrays.asList("com.ctrip.ops.sysdev.metrics." + metricType, metricType);
                    boolean tryCtrip = true;
                    for (String className : classNames) {
                        try {
                            metricClass = Class.forName(className);
                            Constructor<?> ctor = metricClass.getConstructor(Map.class);
                            BaseMetric metricInstance = (BaseMetric) ctor.newInstance(metricConfig);
                            logger.info("build metric " + metricType + " done");

                            metricInstance.register();
                            logger.info("metric" + metricType + " started");

                            break;
                        } catch (ClassNotFoundException e) {
                            if (tryCtrip == true) {
                                logger.info("maybe a third party metric plugin. try to build " + metricType);
                                tryCtrip = false;
                                continue;
                            } else {
                                logger.error(e);
                                System.exit(-1);
                            }
                        } catch (Exception e) {
                            logger.error(e);
                            System.exit(-1);
                        }
                    }
                });
            });
        }

        // for input in all_inputs, Go through every input and emit immediately
        inputConfigs.forEach(
                input -> {
                    input.forEach((inputType, inputConfig) -> {
                        logger.info("begin to build input " + inputType);

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

                                logger.info("build input " + inputType + " done");
                                //Start working,guy.
                                inputInstance.emit();
                                logger.info("input" + inputType + " started");
                                break;
                            } catch (ClassNotFoundException e) {
                                if (tryCtrip == true) {
                                    logger.info("maybe a third party input plugin. try to build " + inputType);
                                    tryCtrip = false;
                                    continue;
                                } else {
                                    logger.error(e);
                                    System.exit(-1);
                                }
                            } catch (Exception e) {
                                logger.error(e);
                                System.exit(-1);
                            }
                        }
                    });
                });
    }
}

