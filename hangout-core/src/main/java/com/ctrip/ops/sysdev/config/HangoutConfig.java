package com.ctrip.ops.sysdev.config;


import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.Map;

import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.error.YAMLException;


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
            System.err.println("Error: No inputs or outputs!");
            throw new Exception();
        }

        return configs;
    }


    /**
     * Get specified config from configurations, default config can also be set, isMust indicates whether this config is a must or not.
     * @param config
     * @param key
     * @param defaultConfig
     * @param isMust
     * @param <T>
     * @return
     */
}