package com.ctrip.ops.sysdev.render;

import freemarker.template.Configuration;
import freemarker.template.Template;
import lombok.extern.log4j.Log4j2;

import java.io.IOException;
import java.io.StringWriter;
import java.util.Map;

@Log4j2
public class FreeMarkerRender implements TemplateRender {
    private Template t;

    public FreeMarkerRender(String template, String templateName)
            throws IOException {
        Configuration cfg = new Configuration(Configuration.VERSION_2_3_22);
        this.t = new Template(templateName, template, cfg);
    }

    public Object render(Map event) {
        StringWriter sw = new StringWriter();
        try {
            t.process(event, sw);
        } catch (Exception e) {
            log.error(e.getMessage());
            log.debug(event);
            return null;
        }
        try {
            sw.close();
        } catch (IOException e) {
            log.error(e.getMessage());
        }
        return sw.toString();
    }
}
