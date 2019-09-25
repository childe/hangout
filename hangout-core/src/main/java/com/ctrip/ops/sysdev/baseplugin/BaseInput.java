package com.ctrip.ops.sysdev.baseplugin;

import com.ctrip.ops.sysdev.utils.Utils;
import com.ctrip.ops.sysdev.decoders.Decode;
import com.ctrip.ops.sysdev.decoders.JsonDecoder;
import com.ctrip.ops.sysdev.decoders.PlainDecoder;
import lombok.extern.log4j.Log4j2;


import java.lang.reflect.Constructor;
import java.util.*;

import java.util.Map.Entry;

@Log4j2
public abstract class BaseInput extends Base {
    protected Map<String, Object> config;
    protected Decode decoder;

    public BaseFilter nextFilter;
    public List<BaseOutput> outputs;

    public BaseInput(Map config, ArrayList<Map> filters, ArrayList<Map> outputs)
            throws Exception {
        super(config);

        this.nextFilter = null;
        this.outputs = new ArrayList<BaseOutput>();

        this.config = config;
        this.createDecoder();

        this.prepare();

        this.registerShutdownHookForSelf();
    }

    protected abstract void prepare();

    public abstract void emit();


    protected Map<String, Object> preprocess(Map<String, Object> event) {
        return event;
    }

    protected Map<String, Object> postprocess(Map<String, Object> event) {
        return event;
    }

    // any input plugin should create decoder when init
    public void createDecoder() {
        String codec = (String) this.config.get("codec");
        if (codec != null && codec.equalsIgnoreCase("plain")) {
            decoder = new PlainDecoder();
        } else {
            decoder = new JsonDecoder();
        }
    }

    public void process(String message) {
        try {
            Map<String, Object> event = this.decoder
                    .decode(message);
            if (this.config.containsKey("type")) {
                event.put("type", this.config.get("type"));
            }
            event = this.preprocess(event);

            if (this.nextFilter != null) {
                event = this.nextFilter.process(event);
            } else {
                for (BaseOutput o : this.outputs
                ) {
                    o.process(event);
                }
            }
        } catch (Exception e) {
            log.error("process event failed:" + message);
            e.printStackTrace();
            log.error(e);
        } catch (Error e) {
            log.error("process event failed:" + message);
            e.printStackTrace();
            log.error(e);
        } finally {
            if (this.enableMeter == true) {
                this.meter.mark();
            }
        }
    }

    public abstract void shutdown();

    private void registerShutdownHookForSelf() {
        final Object inputClass = this;
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            log.info("start to shutdown " + inputClass.getClass().getName());
            shutdown();
        }));
    }

    private void registerShutdownHook(final List<BaseOutput> bos) {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            log.info("start to shutdown all output plugin");
            for (BaseOutput bo : bos) {
                bo.shutdown();
            }
        }));
    }
}
