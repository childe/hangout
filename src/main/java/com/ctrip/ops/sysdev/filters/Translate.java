package com.ctrip.ops.sysdev.filters;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;
import org.yaml.snakeyaml.Yaml;

public class Translate extends BaseFilter {
    private static final Logger logger = Logger.getLogger(Translate.class
            .getName());

    public Translate(Map config) {
        super(config);
    }

    private String target;
    private String source;
    private String dictionaryPath;
    private int refreshInterval;
    private long nextLoadTime;
    private HashMap dictionary;

    private void loadDictionary() {
        logger.info("begin to loadDictionary: " + this.dictionaryPath);

        if (dictionaryPath == null) {
            dictionary = null;
            logger.warn("dictionary_path is null");
            return;
        }
        Yaml yaml = new Yaml();

        if (dictionaryPath.startsWith("http://") || dictionaryPath.startsWith("https://")) {
            URL httpUrl;
            URLConnection connection;
            try {
                httpUrl = new URL(dictionaryPath);
                connection = httpUrl.openConnection();
                connection.connect();
                dictionary = (HashMap) yaml.load(connection.getInputStream());
            } catch (IOException e) {
                e.printStackTrace();
                logger.error("failed to load " + dictionaryPath);
                System.exit(1);
            }
        } else {
            FileInputStream input;
            try {
                input = new FileInputStream(new File(dictionaryPath));
                dictionary = (HashMap) yaml.load(input);
            } catch (FileNotFoundException e) {
                logger.error(dictionaryPath + " is not found");
                logger.error(e.getMessage());
                System.exit(1);
            }
        }

        logger.info("loadDictionary done: " + this.dictionaryPath);
    }

    protected void prepare() {
        target = (String) config.get("target");
        source = (String) config.get("source");

        dictionaryPath = (String) config.get("dictionary_path");

        loadDictionary();

        if (config.containsKey("refresh_interval")) {
            this.refreshInterval = (int) config.get("refresh_interval") * 1000;
        } else {
            this.refreshInterval = 300 * 1000;
        }
        nextLoadTime = System.currentTimeMillis() + refreshInterval * 1000;
    }

    ;

    @Override
    protected Map filter(final Map event) {
        if (dictionary == null || !event.containsKey(this.source)) {
            return event;
        }
        if (System.currentTimeMillis() >= nextLoadTime) {
            loadDictionary();
            nextLoadTime += refreshInterval;
        }
        Object t = dictionary.get(event.get(source));
        if (t != null) {
            event.put(target, t);
        }
        return event;
    }
}
