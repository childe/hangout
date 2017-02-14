package com.ctrip.ops.sysdev.baseplugin;

import org.yaml.snakeyaml.error.YAMLException;

import java.util.Map;

/**
 * Created by gnuhpc on 2017/2/14.
 */
public class Base {

 /**
     * Get specified config from configurations, default config can also be set, isMust indicates whether this config is a must or not.
     * @param config
     * @param key
     * @param defaultConfig
     * @param isMust
     * @param <T>
     * @return
     */
    public <T> T getConfig(Map config, String key, T defaultConfig, boolean isMust){
        if(config.containsKey(key)){
            return (T)config.get(key);
        }
        else{
            if(!isMust){
                return defaultConfig;
            }
            else{
                throw new YAMLException(key+" must be specified");
            }
        }
    }
}
