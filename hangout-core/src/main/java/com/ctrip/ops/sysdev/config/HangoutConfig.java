package com.ctrip.ops.sysdev.config;

import com.ctrip.ops.sysdev.exception.YamlConfigException;
import com.google.common.collect.ImmutableList;
import jdk.nashorn.internal.ir.annotations.Immutable;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.extern.log4j.Log4j2;
import ninja.leaping.configurate.ConfigurationNode;
import ninja.leaping.configurate.loader.ConfigurationLoader;
import ninja.leaping.configurate.yaml.YAMLConfigurationLoader;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;


@Getter
@Setter
@ToString
@Log4j2
public class HangoutConfig {

    private static HangoutConfig hangoutConfig = new HangoutConfig();
    private static final String HTTP = "http://";
    private static final String HTTPS = "https://";

    private ConfigurationLoader loader;
    private List<Map<String, Map>> inputConfigs;
    private List<Map<String, Map>> filterConfigs;
    private List<Map<String, Map>> outputConfigs;
    private List<Map<String, Map>> metricsConfigs;
    private ConfigurationNode rootNode;

    private HangoutConfig() {}

    public static HangoutConfig getConfigInstance() {
        return hangoutConfig;
    }

    @SuppressWarnings("NestedMethodCall")
    public void parse(String filename) throws YamlConfigException {
        try {
            if (filename.startsWith(HangoutConfig.HTTP) || filename.startsWith(HangoutConfig.HTTPS)) {
                loader = YAMLConfigurationLoader.builder().setURL(new URL(filename)).build();
            } else {
                loader = YAMLConfigurationLoader.builder().setFile(new File(filename)).build();
            }

            rootNode = loader.load();
        } catch (java.io.IOException e) {
            e.printStackTrace();
        }

        Function<Object, Map<String, Map>> f = new Function() {
            @Override
            public Map<String, Map> apply(Object o) {
                return (HashMap<String, Map>)o;
            }
        };

        inputConfigs = rootNode.getNode("inputs").getList(f).stream().collect(Collectors.toList());
        outputConfigs = rootNode.getNode("outputs").getList(f).stream().collect(Collectors.toList());
        filterConfigs = rootNode.getNode("filters").getList(f).stream().collect(Collectors.toList());
        metricsConfigs= rootNode.getNode("metrics").getList(f).stream().collect(Collectors.toList());

        if (inputConfigs.size() == 0 || outputConfigs.size() == 0) {
            String msg = "Error: No inputs or outputs!";
            log.error(msg);
            throw new YamlConfigException(msg);
        }
    }
}
