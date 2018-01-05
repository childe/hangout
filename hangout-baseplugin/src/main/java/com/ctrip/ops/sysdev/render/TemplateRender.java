package com.ctrip.ops.sysdev.render;

import java.io.IOException;
import java.util.Map;
import java.util.regex.Pattern;


public interface TemplateRender {
    Pattern p = Pattern.compile("\\[\\S+\\]+");

    public Object render(Map event);

    static public TemplateRender getRender(Object template) throws IOException {
        if (!String.class.isAssignableFrom(template.getClass())) {
            return new DirectRender(template);
        }
        if (p.matcher((String) template).matches()) {
            return new FieldRender((String) template);
        }

        return new FreeMarkerRender((String) template, (String) template);

    }


    static public TemplateRender getRender(String template, boolean ignoreOneLevelRender) throws IOException {
        if (ignoreOneLevelRender == true) {
            return getRender(template);
        }

        if (!String.class.isAssignableFrom(template.getClass())) {
            return new DirectRender(template);
        }
        if (p.matcher(template).matches()) {
            return new FieldRender(template);
        }
        if (template.contains("$")) {
            return new FreeMarkerRender(template, template);
        }
        return new OneLevelRender(template);
    }


    static public TemplateRender getRender(String template, String timezone) throws IOException {
        Pattern p = Pattern.compile("\\%\\{\\+.*?\\}");
        if (p.matcher(template).find()) {
            return new DateFormatter(template, timezone);
        }
        return getRender(template);
    }
}