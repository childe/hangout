package com.ctrip.ops.sysdev.cmd;

import com.ctrip.ops.sysdev.baseplugin.BaseInput;
import com.ctrip.ops.sysdev.baseplugin.BaseMetric;
import config.CommandLineValues;
import config.HangoutConfig;
import lombok.extern.log4j.Log4j2;

import java.lang.reflect.Constructor;
import java.util.*;

class InputEmitThread extends Thread {
    private BaseInput input;

    public InputEmitThread(BaseInput input) {
        this.input = input;
    }

    @Override
    public void run() {
        this.input.emit();
    }
}

/**
 * @author liujia
 */
@Log4j2
public class Main {
    public static void main(String[] args) {

        //Parse CommandLine arguments
        CommandLineValues cm = new CommandLineValues(args);
        cm.parseCmd();

        // parse configure file
        Map configs = null;
        try {
            configs = HangoutConfig.parse(cm.getConfigFile());
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }
        log.debug(configs);

        final List<HashMap<String, Map>> inputConfigs = (ArrayList<HashMap<String, Map>>) configs.get("inputs");
        final List<HashMap<String, Map>> filterConfigs = (ArrayList<HashMap<String, Map>>) configs.get("filters");
        final List<HashMap<String, Map>> outputConfigs = (ArrayList<HashMap<String, Map>>) configs.get("outputs");
        final List<HashMap<String, Map>> metricsConfigs = (ArrayList<HashMap<String, Map>>) configs.get("metrics");

        if (metricsConfigs != null) {
            metricsConfigs.forEach(metric -> {
                metric.forEach((metricType, metricConfig) -> {
                    log.info("begin to build metric " + metricType);

                    Class<?> metricClass = null;

                    List<String> classNames = Arrays.asList("com.ctrip.ops.sysdev.metrics." + metricType, metricType);
                    boolean tryCtrip = true;
                    for (String className : classNames) {
                        try {
                            metricClass = Class.forName(className);
                            Constructor<?> ctor = metricClass.getConstructor(Map.class);
                            BaseMetric metricInstance = (BaseMetric) ctor.newInstance(metricConfig);
                            log.info("build metric " + metricType + " done");

                            metricInstance.register();
                            log.info("metric" + metricType + " started");

                            break;
                        } catch (ClassNotFoundException e) {
                            if (tryCtrip == true) {
                                log.info("maybe a third party metric plugin. try to build " + metricType);
                                tryCtrip = false;
                                continue;
                            } else {
                                log.error(e);
                                System.exit(1);
                            }
                        } catch (Exception e) {
                            log.error(e);
                            System.exit(1);
                        }
                    }
                });
            });
        }

        TopologyBuilder tb = new TopologyBuilder(inputConfigs, filterConfigs, outputConfigs);
        List<BaseInput> inputs = tb.buildTopology();

        for (BaseInput input : inputs
        ) {
            InputEmitThread t = new InputEmitThread(input);
            t.start();
        }
    }
}
