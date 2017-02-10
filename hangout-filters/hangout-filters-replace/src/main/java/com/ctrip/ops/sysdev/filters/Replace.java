package com.ctrip.ops.sysdev.filters;

import java.io.IOException;

import java.util.Map;

import com.ctrip.ops.sysdev.baseplugin.BaseFilter;
import com.ctrip.ops.sysdev.render.TemplateRender;
import org.apache.log4j.Logger;

@SuppressWarnings("ALL")
public class Replace extends BaseFilter {
    private static final Logger logger = Logger.getLogger(Replace.class
            .getName());

    public Replace(Map config) {
        super(config);
    }

    private String src;
    private String value;

    protected void prepare() {
        this.src = (String) config.get("src");
        this.value = (String) config.get("value");

        try {
            this.render = TemplateRender.getRender(value);
        } catch (IOException e) {
            logger.fatal(e.getMessage());
            System.exit(1);
        }
    }

    @Override
    protected Map filter(final Map event) {
        if (event.containsKey(this.src)) {
            event.put(this.src, render.render(event));
        }
        return event;
    }
}