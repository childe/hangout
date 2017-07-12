package com.ctrip.ops.sysdev.filters;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.util.HashMap;
import java.util.Map;

import com.ctrip.ops.sysdev.baseplugin.BaseFilter;
import com.ctrip.ops.sysdev.fieldSetter.FieldSetter;
import com.ctrip.ops.sysdev.render.TemplateRender;
import lombok.extern.log4j.Log4j2;
import org.yaml.snakeyaml.Yaml;

@Log4j2
public class Translate extends BaseFilter {

    public Translate(Map config) {
        super(config);
    }

    private FieldSetter target;
    private TemplateRender source;
    private String dictionaryPath;
    private int refreshInterval;
    private long nextLoadTime;
    private HashMap dictionary;

    private void loadDictionary() {
        log.info("begin to loadDictionary: " + this.dictionaryPath);

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
                log.error("failed to load " + dictionaryPath);
                System.exit(1);
            }
        } else {
            FileInputStream input;
            try {
                input = new FileInputStream(new File(dictionaryPath));
                dictionary = (HashMap) yaml.load(input);
            } catch (FileNotFoundException e) {
                log.error(dictionaryPath + " is not found");
                log.error(e.getMessage());
                System.exit(1);
            }
        }

        log.info("loadDictionary done: " + this.dictionaryPath);
    }

    protected void prepare() {
        String sourceField = (String) config.get("source");
        String targetField = (String) config.get("target");
        try {
            this.source = TemplateRender.getRender(sourceField, false);
        } catch (IOException e) {
            log.fatal("could NOT build template render from " + sourceField);
            System.exit(1);
        }
        this.target = FieldSetter.getFieldSetter(targetField);

        dictionaryPath = (String) config.get("dictionary_path");

        if (dictionaryPath == null) {
            log.fatal("dictionary_path must be inclued in config");
            System.exit(1);
        }

        loadDictionary();

        if (config.containsKey("refresh_interval")) {
            this.refreshInterval = (Integer) config.get("refresh_interval") * 1000;
        } else {
            this.refreshInterval = 300 * 1000;
        }
        nextLoadTime = System.currentTimeMillis() + refreshInterval * 1000;
    }

    @Override
    protected Map filter(final Map event) {
        if (System.currentTimeMillis() >= nextLoadTime) {
            loadDictionary();
            nextLoadTime += refreshInterval;
        }
        if (this.dictionary == null) {
            log.debug("dictionary is null, return without any change");
            return event;
        }
        Object t = dictionary.get(this.source.render(event));
        if (t != null) {
            this.target.setField(event, t);
        }
        return event;
    }
}
