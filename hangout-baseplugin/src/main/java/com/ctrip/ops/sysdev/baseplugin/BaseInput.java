package com.ctrip.ops.sysdev.baseplugin;

import com.ctrip.ops.sysdev.decoders.Decode;
import com.ctrip.ops.sysdev.decoders.JsonDecoder;
import com.ctrip.ops.sysdev.decoders.PlainDecoder;
import lombok.extern.log4j.Log4j;


import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import java.util.Map.Entry;

@Log4j
public abstract class BaseInput {
    protected Map<String, Object> config;
    protected Decode decoder;
    protected List<BaseFilter> filterProcessors;
    protected List<BaseOutput> outputProcessors;
    protected ArrayList<Map> filters;
    protected ArrayList<Map> outputs;

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


    protected Map<String, Object> preprocess(Map<String, Object> event) {
        return event;
    }

    protected Map<String, Object> postprocess(Map<String, Object> event) {
        return event;
    }


    //Apply decoder, filters, output in sequences
    public void applyProcessor(String message) {
        Map event = applyDecoder(message);
        processAfterDecode(event);
        event = applyFilters(event);
        if (event != null)
            applyOutputs(event);
    }

    public void processAfterDecode(Map event) {
        return;
    }

    //Apply decoder
    public Map<String, Object> applyDecoder(String message) {
        return this.decoder.decode(message);
    }

    //Apply Filters
    public Map<String, Object> applyFilters(Map<String, Object> event) {
        if (this.filterProcessors != null) {
            for (BaseFilter bf : filterProcessors) {
                if (event == null) {
                    break;
                }
                event = bf.process(event);
            }
        }
        return event;
    }

    //Apply Outputs
    public void applyOutputs(Map<String, Object> event) {
        outputProcessors.stream().forEach(outputProcessor -> outputProcessor.process(event));
    }

    public void createProcessors() {
        createDecoder();
        createFilterProcessors();
        createOutputProcessors();
    }

    public void createDecoder() {
        String codec = (String) this.config.get("codec");
        if (codec != null && codec.equalsIgnoreCase("plain")) {
            decoder = new PlainDecoder();
        } else {
            decoder = new JsonDecoder();
        }
    }

    public List<BaseFilter> createFilterProcessors() {
        if (filters != null) {
            filterProcessors = new ArrayList<>();

            filters.stream().forEach((Map filterMap) -> {
                filterMap.entrySet().stream().forEach(entry -> {
                    Entry<String, Map> filter = (Entry<String, Map>) entry;
                    String filterType = filter.getKey();
                    Map filterConfig = filter.getValue();
                    Class<?> filterClass;
                    Constructor<?> ctor = null;
                    log.info("begin to build filter " + filterType);
                    try {
                        filterClass = Class.forName("com.ctrip.ops.sysdev.filters." + filterType);
                        ctor = filterClass.getConstructor(Map.class);
                        log.info("build filter " + filterType + " done");
                        this.filterProcessors.add((BaseFilter) ctor.newInstance(filterConfig));
                    } catch (Exception e) {
                        e.printStackTrace();
                        System.exit(-1);
                    }
                });
            });
        }

        return filterProcessors;
    }

    public List<BaseOutput> createOutputProcessors() {

        if (outputs != null) {
            outputProcessors = new ArrayList<>();
            outputs.stream().forEach((Map outputMap) -> {
                outputMap.entrySet().stream().forEach(entry -> {
                    Entry<String, Map> output = (Entry<String, Map>) entry;
                    String outputType = output.getKey();
                    Map outputConfig = output.getValue();
                    Class<?> outputClass;
                    Constructor<?> ctor = null;
                    log.info("begin to build output " + outputType);
                    try {
                        outputClass = Class.forName("com.ctrip.ops.sysdev.outputs." + outputType);
                        ctor = outputClass.getConstructor(Map.class);
                        log.info("build output " + outputType + " done");
                        outputProcessors.add((BaseOutput) ctor.newInstance(outputConfig));
                    } catch (Exception e) {
                        e.printStackTrace();
                        System.exit(-1);
                    }
                });
            });
        } else {
            log.error("Error: At least One output should be set.");
            System.exit(-1);
        }


        this.registerShutdownHook(outputProcessors);
        return outputProcessors;
    }


    public void process(String message) {
        try {
            Map<String, Object> event = this.decoder
                    .decode(message);
            this.preprocess(event);

            ArrayList<Map<String, Object>> events = new ArrayList<Map<String, Object>>();
            events.add(event);

            if (this.filterProcessors != null) {
                for (BaseFilter bf : filterProcessors) {
                    for (int i = 0; i < events.size(); i++) {
                        Map rst = bf.process(events.get(i));
                        if (rst != null) {
                            events.set(i, rst);
                        }
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
        } catch (Exception e) {
            log.error("process event failed:" + message);
            e.printStackTrace();
            log.error(e);
        }
    }

    public void shutdown() {
    }


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
