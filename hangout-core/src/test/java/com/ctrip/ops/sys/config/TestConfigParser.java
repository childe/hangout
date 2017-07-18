package com.ctrip.ops.sys.config;

import ninja.leaping.configurate.ConfigurationNode;
import ninja.leaping.configurate.loader.ConfigurationLoader;
import ninja.leaping.configurate.objectmapping.ObjectMappingException;
import ninja.leaping.configurate.yaml.YAMLConfigurationLoader;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import static junit.framework.TestCase.assertEquals;

/**
 * Created by gnuhpc on 2017/7/18.
 */
public class TestConfigParser {
    private ConfigurationNode rootNode;

    @Before
    public void testLoadConfig() throws IOException {
        ConfigurationLoader loader = YAMLConfigurationLoader.builder().setFile(new File("src/test/resources/example.yml")).build();
        rootNode = loader.load();
    }

    @Test
    public void testConfigParse() throws ObjectMappingException {
        List<HashMap<String, Map>> inputConfigs = rootNode.getNode("inputs").getList(
                (Function<Object, HashMap<String, Map>>) obj -> (HashMap<String, Map>) obj
        );
        List<HashMap<String, Map>> outputConfigs = rootNode.getNode("outputs").getList(
                (Function<Object, HashMap<String, Map>>) obj -> (HashMap<String, Map>) obj
        );
        List<HashMap<String, Map>> filterConfigs = rootNode.getNode("filters").getList(
                (Function<Object, HashMap<String, Map>>) obj -> (HashMap<String, Map>) obj
        );

        List<HashMap<String, Map>> metricsConfigs = rootNode.getNode("metrics").getList(
                (Function<Object, HashMap<String, Map>>) obj -> (HashMap<String, Map>) obj
        );
        return;
    }
}