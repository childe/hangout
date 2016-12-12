package com.ctrip.ops.sysdev.config;


import org.apache.commons.io.FileUtils;
import org.yaml.snakeyaml.Yaml;

import java.io.File;
import java.io.IOException;
import java.util.Map;

public class HangoutConfig {

    public static Map parse(File filename) throws IOException {
        Yaml yaml = new Yaml();
        String configContent;
        configContent = FileUtils.readFileToString(filename, "UTF-8");
        return (Map) yaml.load(configContent);
    }
}

