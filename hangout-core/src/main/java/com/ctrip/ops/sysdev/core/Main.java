package com.ctrip.ops.sysdev.core;

import com.ctrip.ops.sysdev.baseplugin.BaseInput;
import com.ctrip.ops.sysdev.config.CommandLineValues;
import com.ctrip.ops.sysdev.config.HangoutConfig;
import com.ctrip.ops.sysdev.log.LogSetter;
import lombok.extern.log4j.Log4j;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@SuppressWarnings("ALL")
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

        // for input in all_inputs
        final List<HashMap<String, Map>> inputConfigs = (ArrayList<HashMap<String, Map>>) configs.get("inputs");
        final List<HashMap<String, Map>> filterConfigs= (ArrayList<HashMap<String, Map>>) configs.get("filters");
        final List<HashMap<String, Map>> outputConfigs= (ArrayList<HashMap<String, Map>>) configs.get("outputs");

        //Go through every input and emit immediately
        inputConfigs.forEach(
                input -> {
                    input.forEach((inputType, inputConfig) -> {
                        Class<?> inputClass = null;
                        try {
                            log.info("begin to build input " + inputType);
                            inputClass = Class.forName("com.ctrip.ops.sysdev.inputs." + inputType);
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
                        } catch (Exception e) {
                            log.error(e);
                            e.printStackTrace();
                            System.exit(-1);
                        }
                    });
                });
    }
}

