package com.ctrip.ops.sysdev.filters;

import java.io.IOException;

import java.util.Map;

import com.ctrip.ops.sysdev.baseplugin.BaseFilter;
import com.ctrip.ops.sysdev.fieldSetter.FieldSetter;
import com.ctrip.ops.sysdev.render.TemplateRender;
import org.apache.log4j.Logger;

@SuppressWarnings("ALL")
public class Replace extends BaseFilter {
    private static final Logger logger = Logger.getLogger(Replace.class
            .getName());

    public Replace(Map config) {
        super(config);
    }

    private TemplateRender templateRender;
    private TemplateRender srcTemplateRender;
    private FieldSetter fieldSetter;

    protected void prepare() {
        String src = (String) config.get("src");
        this.fieldSetter = FieldSetter.getFieldSetter(src);
        try {
            this.srcTemplateRender = TemplateRender.getRender(src, false);
        } catch (IOException e) {
            logger.fatal("could NOT build tempalte render from " + src);
            System.exit(1);
        }

        String value = (String) config.get("value");
        try {
            this.templateRender = TemplateRender.getRender(value);
        } catch (IOException e) {
            logger.fatal("could NOT build tempalte render from " + value);
            System.exit(1);
        }
    }

    @Override
    protected Map filter(final Map event) {
        if (this.srcTemplateRender.render(event) != null) {
            this.fieldSetter.setField(event, this.templateRender.render(event));
        }
        return event;
    }
}
