package com.ctrip.ops.sysdev.baseplugin;

import com.ctrip.ops.sysdev.decoders.Decode;
import com.ctrip.ops.sysdev.decoders.JsonDecoder;
import com.ctrip.ops.sysdev.decoders.PlainDecoder;
import lombok.extern.log4j.Log4j;

import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.util.*;
import java.util.Map.Entry;
import java.util.stream.Collectors;

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
            filterProcessors = filters.stream().collect(HashMap::new, Map::putAll, Map::putAll).entrySet().stream().map((Entry<Object, Object> filter) -> {
                BaseFilter bf = null;
                String filterType = (String) filter.getKey();
                Map filterConfig = (Map) filter.getValue();
                Class<?> filterClass;
                Constructor<?> ctor = null;
                log.info("begin to build filter " + filterType);
                try {
                    filterClass = Class.forName("com.ctrip.ops.sysdev.filters." + filterType);
                    ctor = filterClass.getConstructor(Map.class);
                    log.info("build filter " + filterType + " done");
                    bf = (BaseFilter) ctor.newInstance(filterConfig);
                } catch (Exception e) {
                    e.printStackTrace();
                    System.exit(-1);
                }
                return bf;
            }).collect(Collectors.toList());
        } else {
            filterProcessors = null;
        }
        return filterProcessors;
    }

    public List<BaseOutput> createOutputProcessors() {
        outputProcessors = outputs.stream().collect(HashMap::new, Map::putAll, Map::putAll).entrySet().stream().map((Entry<Object, Object> output) -> {
            BaseOutput bo = null;
            String outputType = (String) output.getKey();
            Map outputConfig = (Map) output.getValue();
            Class<?> outputClass;
            Constructor<?> ctor = null;
            log.info("begin to build output " + outputType);
            try {
                outputClass = Class.forName("com.ctrip.ops.sysdev.outputs." + outputType);
                ctor = outputClass.getConstructor(Map.class);
                log.info("build output " + outputType + " done");
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


    public void process(String message) {
        try {
            Map<String, Object> event = this.decoder
                    .decode(message);
            this.preprocess(event);

            ArrayList<Map<String, Object>> events = new ArrayList<Map<String, Object>>();
            events.add(event);

            if (this.filterProcessors != null) {
                for (BaseFilter bf : filterProcessors) {
                    if (events == null) {
                        break;
                    }
                    for (int i = 0; i < events.size(); i++) {
                        events.set(i, bf.process(events.get(i)));
                    }
                    if (bf.processExtraEventsFunc == true) {
                        int originEventSize = events.size();
                        for(int i=0;i < events.size(); i++){
                            List rst = bf.processExtraEvents(events.get(i));
                            if (rst != null) {
                                events.addAll(rst);
                            }
                        }
                    }
                }
            }

            for (int i = 0; i < events.size(); i++) {
                events.set(i, this.postprocess(events.get(i)));
            }

            if (events != null) {
                for (BaseOutput bo : outputProcessors) {
                    for (Map<String, Object> _ : events) {
                        bo.process(event);
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
