package com.ctrip.ops.sysdev.baseplugin;

import lombok.extern.log4j.Log4j2;

import java.lang.reflect.Constructor;
import java.util.*;

import static com.ctrip.ops.sysdev.utils.Utils.createFilterProcessors;

@Log4j2
public class TopologyBuilder {

    List<HashMap<String, Map>> inputConfigs;
    List<HashMap<String, Map>> filterConfigs;
    List<HashMap<String, Map>> outputConfigs;

    public TopologyBuilder(List<HashMap<String, Map>> inputConfigs,
                           List<HashMap<String, Map>> filterConfigs,
                           List<HashMap<String, Map>> outputConfigs) {
        this.inputConfigs = inputConfigs;
        this.filterConfigs = filterConfigs;
        this.outputConfigs = outputConfigs;
    }

    private List<BaseInput> buildInputs() {
        List<BaseInput> inputs = new ArrayList<BaseInput>();
        inputConfigs.forEach(
                input -> {
                    input.forEach((inputType, inputConfig) -> {
                        log.info("begin to build input " + inputType);

                        Class<?> inputClass = null;

                        List<String> classNames = Arrays.asList("com.ctrip.ops.sysdev.inputs." + inputType, inputType);
                        boolean tryCtrip = true;
                        for (String className : classNames) {
                            try {
                                inputClass = Class.forName(className);
                                Constructor<?> ctor = inputClass.getConstructor(
                                        Map.class,
                                        ArrayList.class,
                                        ArrayList.class);
                                BaseInput inputInstance = (BaseInput) ctor.newInstance(
                                        inputConfig,
                                        filterConfigs,
                                        outputConfigs);

                                log.info("build input " + inputType + " done");
                                inputs.add(inputInstance);
                                break;
                            } catch (ClassNotFoundException e) {
                                if (tryCtrip == true) {
                                    log.info("maybe a third party input plugin. try to build " + inputType);
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
        return inputs;
    }

    private List<BaseFilter> buildFilters() {
        List<BaseFilter> filterProcessors = new ArrayList();
        if (filterConfigs != null) {
            filterConfigs.stream().forEach((Map filterMap) -> {
                filterMap.entrySet().stream().forEach(entry -> {
                    Map.Entry<String, Map> filter = (Map.Entry<String, Map>) entry;
                    String filterType = filter.getKey();
                    Map filterConfig = filter.getValue();

                    log.info("begin to build filter " + filterType);

                    Class<?> filterClass;
                    Constructor<?> ctor = null;
                    List<String> classNames = Arrays.asList("com.ctrip.ops.sysdev.filters." + filterType, filterType);
                    boolean tryCtrip = true;
                    for (String className : classNames) {
                        try {
                            filterClass = Class.forName(className);
                            ctor = filterClass.getConstructor(Map.class);
                            log.info("build filter " + filterType + " done");
                            filterProcessors.add((BaseFilter) ctor.newInstance(filterConfig));
                            break;
                        } catch (ClassNotFoundException e) {
                            if (tryCtrip == true) {
                                log.info("maybe a third party output plugin. try to build " + filterType);
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
        }

        return filterProcessors;
    }


    private List<BaseOutput> buildOutputs() {
        List<BaseOutput> outputProcessors = new ArrayList<BaseOutput>();
        if (outputConfigs != null) {

            outputConfigs.stream().forEach((Map outputMap) -> {
                outputMap.entrySet().stream().forEach(entry -> {
                    Map.Entry<String, Map> output = (Map.Entry<String, Map>) entry;
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

        return outputProcessors;
    }

    private void setDestToInput(BaseInput input, List<BaseFilter> filters, List<BaseOutput> outputs) {
        if (input.config.get("to") == null) {
            if (this.filterConfigs.size() != 0) {
                input.nextFilters.add(filters.get(0));
            } else {
                input.nextOutputs.addAll(outputs);
            }
            return;
        }

        for (String dest : (ArrayList<String>) input.config.get("to")) {
            for (BaseFilter f : filters) {
                if (f.config.containsKey("id")) {
                    String ID = (String) f.config.get("id");
                    if (ID.equals(dest)) {
                        input.nextFilters.add(f);
                    }
                }
            }
            for (BaseOutput o : outputs) {
                if (o.config.containsKey("id")) {
                    String ID = (String) o.config.get("id");
                    if (ID.equals(dest)) {
                        input.nextOutputs.add(o);
                    }
                }
            }
        }
    }

    private void setDestToFilter(BaseFilter filter, int i, List<BaseFilter> filters, List<BaseOutput> outputs) {
        if (filter.config.get("to") == null) {
            if (filters.size() == i + 1) {
                filter.nextOutputs.addAll(outputs);
            } else {
                filter.nextFilters.add(filters.get(i + 1));
            }
            return;
        }

        for (String dest : (ArrayList<String>) filter.config.get("to")) {
            for (BaseFilter f : filters) {
                if (f.config.containsKey("id")) {
                    String ID = (String) f.config.get("id");
                    if (ID.equals(dest)) {
                        filter.nextFilters.add(f);
                    }
                }
            }
            for (BaseOutput o : outputs) {
                if (o.config.containsKey("id")) {
                    String ID = (String) o.config.get("id");
                    if (ID.equals(dest)) {
                        filter.nextOutputs.add(o);
                    }
                }
            }
        }
    }

    public List<BaseInput> buildTopology() {
        List<BaseInput> inputs = this.buildInputs();
        List<BaseFilter> filters = this.buildFilters();
        List<BaseOutput> outputs = this.buildOutputs();

        for (BaseInput input :
                inputs
        ) {
            setDestToInput(input, filters, outputs);
            log.info("input " + input.getClass().getName() + " filters and outputs:");
            log.debug(input.nextFilters);
            log.debug(input.nextOutputs);
        }

        for (int i = 0; i < filters.size(); i++) {
            BaseFilter filter = filters.get(i);
            setDestToFilter(filters.get(i), i, filters, outputs);
            log.info("filter " + filter.getClass().getName() + " filters and outputs:");
            log.debug(filter.nextFilters);
            log.debug(filter.nextOutputs);
        }

        return inputs;
    }
}
