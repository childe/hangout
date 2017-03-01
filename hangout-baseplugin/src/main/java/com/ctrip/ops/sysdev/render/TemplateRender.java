package com.ctrip.ops.sysdev.render;

import java.io.IOException;
import java.util.Map;
import java.util.regex.Pattern;

public interface TemplateRender {
    Pattern p = Pattern.compile("\\[\\S+\\]+");

    public Object render(Map event);

    static public TemplateRender getRender(String template) throws IOException {
        if (p.matcher(template).matches()) {
            return new FieldRender(template);
        }

        return new FreeMarkerRender(template, template);

    }


    static public TemplateRender getRender(String template, boolean ignoreOneLevelRender) throws IOException {
        if (ignoreOneLevelRender == true) {
            return getRender(template);
        }

        if (p.matcher(template).matches()) {
            return new FieldRender(template);
        }
        if (template.contains("$")) {
            return new FreeMarkerRender(template, template);
        }
        return new OneLevelRender(template);
    }
}
