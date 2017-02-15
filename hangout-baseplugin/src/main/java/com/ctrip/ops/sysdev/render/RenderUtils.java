package com.ctrip.ops.sysdev.render;

import lombok.extern.log4j.Log4j;

import java.io.IOException;
import java.util.Map;

/**
 * Created by gnuhpc on 2017/2/11.
 */
@Log4j
public class RenderUtils {
    public static TemplateRender esConfigRender(Map config, String key, TemplateRender defaultRender) {
        if (config.containsKey(key)) {
            try {
                return new FreeMarkerRender(
                        (String) config.get(key),
                        (String) config.get(key));
            } catch (IOException e) {
                log.fatal(e.getMessage());
                System.exit(1);
            }
        }

        return defaultRender;
    }
}
