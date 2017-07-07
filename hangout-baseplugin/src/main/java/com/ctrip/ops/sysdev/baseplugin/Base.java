package com.ctrip.ops.sysdev.baseplugin;

import com.codahale.metrics.Meter;
import com.ctrip.ops.sysdev.exception.YamlConfigException;
import com.ctrip.ops.sysdev.metric.Metric;

import java.util.Map;

/**
 * Created by gnuhpc on 2017/2/14.
 */
public class Base {

    protected boolean enableMeter = false;
    protected Meter meter;
    
    public Base(Map config) {
        if (config.containsKey("meter_name")) {
            this.enableMeter = true;
            meter = Metric.setMetric((String) config.get("meter_name"));
        }
    }

    /**
     * Get specified config from configurations, default config can also be set, isMust indicates whether this config is a must or not.
     *
     * @param config
     * @param key
     * @param defaultConfig
     * @param isMust
     * @param <T>
     * @return
     */
    public <T> T getConfig(Map config, String key, T defaultConfig, boolean isMust) throws YamlConfigException {
        if (config.containsKey(key)) {
            return (T) config.get(key);
        } else {
            if (!isMust) {
                return defaultConfig;
            } else {
                throw new YamlConfigException(key + " must be specified");
            }
        }
    }
}
