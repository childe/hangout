package com.ctrip.ops.sysdev.configs;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.net.URL;
import java.net.URLConnection;
import java.util.Map;

import org.yaml.snakeyaml.Yaml;

public class HangoutConfig {
//    private static final Logger logger = Logger.getLogger(HangoutConfig.class
//            .getName());

    public static Map parse(String filename) throws FileNotFoundException {
        Yaml yaml = new Yaml();
        if (filename.startsWith("http://") || filename.startsWith("https://")) {
            URL httpUrl;
            URLConnection connection;
            httpUrl = new URL(filename);
            connection = httpUrl.openConnection();
            connection.connect();
            return (Map) yaml.load(connection.getInputStream());
        } else {
            FileInputStream input;
            input = new FileInputStream(new File(filename));
            return (Map) yaml.load(input);
        }
    }
}
