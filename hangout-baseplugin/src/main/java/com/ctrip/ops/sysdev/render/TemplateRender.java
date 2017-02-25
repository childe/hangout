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
        if (template.contains("$")) {
            return new FreeMarkerRender(template, template);
        }
        return new OneLevelRender(template);
    }
}
