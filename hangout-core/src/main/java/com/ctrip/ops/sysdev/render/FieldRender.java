package com.ctrip.ops.sysdev.render;

import lombok.extern.log4j.Log4j2;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Log4j2
public class FieldRender implements TemplateRender {
    private ArrayList<String> fields = new ArrayList();
    final Pattern p = Pattern.compile("\\[(\\S+?)\\]+");

    public FieldRender(String template) {
        Matcher m = p.matcher(template);
        while (m.find()) {
            String a = m.group();
            this.fields.add(a.substring(1, a.length() - 1));
        }
    }

    public Object render(Map event) {
        if (this.fields.size() == 0)
            return null;

        Object current = event;
        try {
            for (String field : this.fields) {
                if (List.class.isAssignableFrom(current.getClass())) {
                    int i = Integer.parseInt(field);
                    current = ((List) current).get(i);
                } else if (Map.class.isAssignableFrom(current.getClass())) {
                    current = ((Map) current).get(field);
                } else {
                    log.debug("render error: current object is not list or map");
                    return null;
                }
            }
            return current;
        } catch (Exception e) {
            log.debug("render error: " + e.toString());
            return null;
        }
    }
}
