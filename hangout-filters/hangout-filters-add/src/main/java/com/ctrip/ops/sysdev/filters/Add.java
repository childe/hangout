package com.ctrip.ops.sysdev.filters;

import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import com.ctrip.ops.sysdev.baseplugin.BaseFilter;

import com.ctrip.ops.sysdev.fieldSetter.FieldSetter;
import com.ctrip.ops.sysdev.render.TemplateRender;
import lombok.extern.log4j.Log4j2;

@Log4j2
public class Add extends BaseFilter {
    public Add(Map config) {
        super(config);
    }

    private Map<FieldSetter, TemplateRender> f;

    protected void prepare() {
        f = new HashMap();

        Map<String, String> fields = (Map<String, String>) config.get("fields");
        Iterator<Entry<String, String>> it = fields.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<String, String> entry = it.next();

            String field = entry.getKey();
            String value = entry.getValue();

            try {
                this.f.put(FieldSetter.getFieldSetter(field), TemplateRender.getRender(value));
            } catch (IOException e) {
                log.fatal(e.getMessage());
                System.exit(1);
            }
        }
    }

    @Override
    protected Map filter(final Map event) {
        Iterator<Entry<FieldSetter, TemplateRender>> it = f.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<FieldSetter, TemplateRender> entry = it.next();

            FieldSetter fieldSetter = entry.getKey();
            TemplateRender render = entry.getValue();
            fieldSetter.setField(event, render.render(event));
        }
        return event;
    }
}
