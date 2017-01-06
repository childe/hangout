package com.ctrip.ops.sysdev.core;

import com.ctrip.ops.sysdev.baseplugin.BaseInput;
import com.ctrip.ops.sysdev.config.CommandLineValues;
import com.ctrip.ops.sysdev.config.HangoutConfig;
import com.ctrip.ops.sysdev.log.*;
import lombok.extern.log4j.Log4j;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(-1);
        }
        log.debug(configs);
        //log.trace("Tracetest");


        // for input in all_inputs
        List<HashMap<String, Map>> inputs = (ArrayList<HashMap<String, Map>>) configs.get("inputs");

        //Go through every input and emit immediately
        for (HashMap<String, Map> input : inputs) {
            for (Map.Entry<String, Map> entry : input.entrySet()) {
                String inputType = entry.getKey();
                Map inputConfig = entry.getValue();
                Class<?> inputClass = null;
                try {
                    inputClass = Class
                            .forName("com.ctrip.ops.sysdev.inputs." + inputType);
                    Constructor<?> ctor = inputClass.getConstructor(Map.class,
                            ArrayList.class, ArrayList.class);
                    BaseInput inputInstance = (BaseInput) ctor.newInstance(
                            inputConfig, configs.get("filters"), configs.get("outputs"));
                    inputInstance.emit();
                } catch (InstantiationException e) {
                    e.printStackTrace();
                } catch (InvocationTargetException e) {
                    e.printStackTrace();
                } catch (NoSuchMethodException e) {
                    e.printStackTrace();
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}

