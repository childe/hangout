package com.ctrip.ops.sysdev.inputs;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.log4j.Logger;
import com.ctrip.ops.sysdev.outputs.BaseOutput;
import com.ctrip.ops.sysdev.decoder.IDecode;
import com.ctrip.ops.sysdev.decoder.JsonDecoder;
import com.ctrip.ops.sysdev.decoder.PlainDecoder;
import com.ctrip.ops.sysdev.filters.BaseFilter;
import com.ctrip.ops.sysdev.filters.Utils;

public abstract class BaseInput {
    private static final Logger logger = Logger.getLogger(BaseInput.class
            .getName());

    protected Map<String, Object> config;
    protected IDecode decoder;
    protected BaseFilter[] filterProcessors;
    protected BaseOutput[] outputProcessors;
    protected ArrayList<Map> filters;
    protected ArrayList<Map> outputs;

    public BaseFilter[] createFilterProcessors() {
        return Utils.createFilterProcessors(filters);
    }

    public BaseOutput[] createOutputProcessors() {
        outputProcessors = new BaseOutput[outputs.size()];
        int idx = 0;
        for (Map output : outputs) {
            Iterator<Entry<String, Map>> outputIT = output.entrySet()
                    .iterator();

            while (outputIT.hasNext()) {
                Map.Entry<String, Map> outputEntry = outputIT.next();
                String outputType = outputEntry.getKey();
                Map outputConfig = outputEntry.getValue();
                Class<?> outputClass;
                try {
                    logger.info("begin to build output " + outputType);
                    outputClass = Class.forName("com.ctrip.ops.sysdev.outputs."
                            + outputType);
                    Constructor<?> ctor = outputClass.getConstructor(Map.class);

                    outputProcessors[idx] = (BaseOutput) ctor
                            .newInstance(outputConfig);
                    logger.info("build output " + outputType + " done");
                    idx++;
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
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

    public void shutdown(){}

    protected void registerShutdownHook() {
        final Object inputclass = this;
        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                logger.info("start to shutdown " + inputclass.getClass().getName());
                shutdown();

                logger.info("start to shutdown all output plugin");
                for (BaseOutput bo : outputProcessors) {
                    bo.shutdown();
                }
            }
        });
    }

    public BaseInput(Map config, ArrayList<Map> filters, ArrayList<Map> outputs)
            throws Exception {
        this.config = config;
        this.filters = filters;
        this.outputs = outputs;

        this.prepare();

        this.registerShutdownHook();
    }

    protected abstract void prepare();

    public abstract void emit();
}
