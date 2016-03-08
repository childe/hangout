package com.ctrip.ops.sysdev.configs;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.util.Map;

import org.yaml.snakeyaml.Yaml;

@SuppressWarnings("rawtypes")
public class HangoutConfig {
    private static final String HTTP = "http://";
    private static final String HTTPS = "https://";
//    private static final Logger logger = Logger.getLogger(HangoutConfig.class.getName());

    @SuppressWarnings("NestedMethodCall")
    public static Map parse(String filename) throws IOException {
        Yaml yaml = new Yaml();
        if (HangoutConfig.HTTP.startsWith(filename) || filename.startsWith(HangoutConfig.HTTPS)) {
            URL httpUrl;
            URLConnection connection;
            httpUrl = new URL(filename);
            connection = httpUrl.openConnection();
            connection.connect();
            return (Map) yaml.load(connection.getInputStream());
        } else {
            FileInputStream input = new FileInputStream(new File(filename));
            return (Map) yaml.load(input);
        }
    }
}
