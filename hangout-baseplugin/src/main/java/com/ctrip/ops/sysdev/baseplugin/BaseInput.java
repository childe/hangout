package com.ctrip.ops.sysdev.baseplugin;

import com.ctrip.ops.sysdev.utils.Utils;
import com.ctrip.ops.sysdev.decoders.Decode;
import com.ctrip.ops.sysdev.decoders.JsonDecoder;
import com.ctrip.ops.sysdev.decoders.PlainDecoder;
import lombok.extern.log4j.Log4j2;


import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import java.util.Map.Entry;

@Log4j2
public abstract class BaseInput extends Base {
    protected Map<String, Object> config;
    protected Decode decoder;
    protected List<BaseFilter> filterProcessors;
    protected List<BaseOutput> outputProcessors;
    protected ArrayList<Map> filters;
    protected ArrayList<Map> outputs;

    public BaseInput(Map config, ArrayList<Map> filters, ArrayList<Map> outputs)
            throws Exception {
        super(config);

        this.config = config;
        this.filters = filters;
        this.outputs = outputs;
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

    // some input plugin like kafka has more than one thread, and each thread must own their filter/output instance.
    // so we should call createFilterProcessors and return filters in each thread.
    public List<BaseFilter> createFilterProcessors() {
        this.filterProcessors = Utils.createFilterProcessors(this.filters);
        return this.filterProcessors;
    }

    // some input plugin like kafka has more than one thread, and each thread must own their filter/output instance.
    // so we should call createFilterProcessors and return filters in each thread.
    public List<BaseOutput> createOutputProcessors() {

        if (outputs != null) {
            outputProcessors = new ArrayList<>();
            outputs.stream().forEach((Map outputMap) -> {
                outputMap.entrySet().stream().forEach(entry -> {
                    Entry<String, Map> output = (Entry<String, Map>) entry;
                    String outputType = output.getKey();
                    Map outputConfig = output.getValue();

                    log.info("begin to build output " + outputType);

                    Class<?> outputClass;
                    Constructor<?> ctor = null;
                    List<String> classNames = Arrays.asList("com.ctrip.ops.sysdev.outputs." + outputType, outputType);
                    boolean tryCtrip = true;

                    for (String className : classNames) {
                        try {
                            outputClass = Class.forName(className);
                            ctor = outputClass.getConstructor(Map.class);
                            log.info("build output " + outputType + " done");
                            outputProcessors.add((BaseOutput) ctor.newInstance(outputConfig));
                            break;
                        } catch (ClassNotFoundException e) {
                            if (tryCtrip == true) {
                                log.info("maybe a third party output plugin. try to build " + outputType);
                                tryCtrip = false;
                                continue;
                            } else {
                                log.error(e);
                                System.exit(1);
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                            System.exit(1);
                        }
                    }
                });
            });
        } else {
            log.error("Error: At least One output should be set.");
            System.exit(1);
        }


        this.registerShutdownHook(outputProcessors);
        return outputProcessors;
    }

    public void process(String message, List<BaseFilter> filterProcessors, List<BaseOutput> outputProcessors) {
        try {
            Map<String, Object> event = this.decoder
                    .decode(message);
            this.preprocess(event);

            ArrayList<Map<String, Object>> events = new ArrayList();
            events.add(event);

            if (filterProcessors != null) {
                for (BaseFilter bf : filterProcessors) {
                    for (int i = 0; i < events.size(); i++) {
                        Map rst = bf.process(events.get(i));
                        events.set(i, rst);
                    }
                    if (bf.processExtraEventsFunc == true) {
                        int originEventSize = events.size();
                        for (int i = 0; i < originEventSize; i++) {
                            List rst = bf.processExtraEvents(events.get(i));
                            if (rst != null) {
                                events.addAll(rst);
                            }
                        }
                    }
                }
            }

//            for (int i = 0; i < events.size(); i++) {
//                events.set(i, this.postprocess(events.get(i)));
//            }

            if (events != null) {
                for (BaseOutput bo : outputProcessors) {
                    for (Map<String, Object> theevent : events) {
                        if (theevent != null) {
                            bo.process(theevent);
                        }
                    }
                }
            }
        } catch (OutOfMemoryError e) {
            e.printStackTrace();
            log.error(e);
            System.exit(1);
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

    public void process(String message, List<BaseFilter> filterProcessors) {
        this.process(message, filterProcessors, this.outputProcessors);
    }

    public void process(String message) {
        this.process(message, this.filterProcessors, this.outputProcessors);
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
