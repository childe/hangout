package com.ctrip.ops.sysdev.config;


import com.ctrip.ops.sysdev.exception.YamlConfigException;
import org.yaml.snakeyaml.Yaml;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.Map;


@SuppressWarnings("rawtypes")
public class HangoutConfig<T> {
    private static final String HTTP = "http://";
    private static final String HTTPS = "https://";
//    private static final Logger logger = Logger.getLogger(HangoutConfig.class.getName());

    @SuppressWarnings("NestedMethodCall")
    public static Map parse(String filename) throws Exception{
        Yaml yaml = new Yaml();
        InputStream is;
        if (filename.startsWith(HangoutConfig.HTTP) || filename.startsWith(HangoutConfig.HTTPS)) {
            URL httpUrl;
            URLConnection connection;
            httpUrl = new URL(filename);
            connection = httpUrl.openConnection();
            connection.connect();
            is = connection.getInputStream();
        } else {
            is = new FileInputStream(new File(filename));
        }

        Map configs = (Map) yaml.load(is);

        if (configs.get("inputs")==null||configs.get("outputs")==null){
            throw new YamlConfigException("Error: No inputs or outputs");
        }

        return configs;
    }
}