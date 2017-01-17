package com.ctrip.ops.sysdev.baseplugin;

import com.ctrip.ops.sysdev.decoders.IDecode;
import com.ctrip.ops.sysdev.decoders.JsonDecoder;
import com.ctrip.ops.sysdev.decoders.PlainDecoder;
import org.apache.log4j.Logger;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@SuppressWarnings("ALL")
public abstract class BaseInput {
    private static final Logger logger = Logger.getLogger(BaseInput.class
            .getName());

    protected Map<String, Object> config;
    protected IDecode decoder;
    protected BaseFilter[] filterProcessors;
    protected List<BaseOutput> outputProcessors;
    protected ArrayList<Map> filters;
    protected ArrayList<Map> outputs;

    public BaseFilter[] createFilterProcessors() {
        return Utils.createFilterProcessors(filters);
    }

    public List<BaseOutput> createOutputProcessors() {
        outputProcessors = outputs.stream().collect(HashMap::new, Map::putAll, Map::putAll).entrySet().stream().map((Entry<Object, Object> output) -> {
            BaseOutput bo=null;
            String outputType = (String) output.getKey();
            Map outputConfig = (Map) output.getValue();
            Class<?> outputClass;
            Constructor<?> ctor = null;
            logger.info("begin to build output " + outputType);
            try {
                outputClass = Class.forName("com.ctrip.ops.sysdev.outputs." + outputType);
                ctor = outputClass.getConstructor(Map.class);
                logger.info("build output " + outputType + " done");
                bo = (BaseOutput) ctor.newInstance(outputConfig);
            } catch (Exception e) {
                e.printStackTrace();
                System.exit(-1);
            }
            return bo;
        }).collect(Collectors.toList());

        this.registerShutdownHook(outputProcessors);
        return outputProcessors;
    }

    public IDecode createDecoder() {
        String codec = (String) this.config.get("codec");
        if (codec != null && codec.equalsIgnoreCase("plain")) {
            return new PlainDecoder();
        } else {
            return new JsonDecoder();
        }
    }

    public void shutdown() {
    }

    protected void registerShutdownHookForSelf() {
        final Object inputClass = this;
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            logger.info("start to shutdown " + inputClass.getClass().getName());
            shutdown();
        }));
    }

    protected void registerShutdownHook(final List<BaseOutput> bos) {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            logger.info("start to shutdown all output plugin");
            for (BaseOutput bo : bos) {
                bo.shutdown();
            }
        }));
    }


    public BaseInput(Map config, ArrayList<Map> filters, ArrayList<Map> outputs)
            throws Exception {
        this.config = config;
        this.filters = filters;
        this.outputs = outputs;

        this.prepare();

        this.registerShutdownHookForSelf();
    }

    protected abstract void prepare();

    public abstract void emit();
}
