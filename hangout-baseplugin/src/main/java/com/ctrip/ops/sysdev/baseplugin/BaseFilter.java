package com.ctrip.ops.sysdev.baseplugin;

import com.ctrip.ops.sysdev.render.FreeMarkerRender;
import com.ctrip.ops.sysdev.render.TemplateRender;
import lombok.extern.log4j.Log4j;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;


@Log4j
public class BaseFilter {

    protected Map config;
    protected TemplateRender render;
    protected String tagOnFailure;
    protected List<String> removeFields;
    private List<TemplateRender> IF;

    public BaseFilter(Map config) {
        this.config = config;

        if (this.config.containsKey("if")) {
            IF = new ArrayList<TemplateRender>();
            for (String c : (List<String>) this.config.get("if")) {
                try {
                    IF.add(new FreeMarkerRender(c, c));
                } catch (IOException e) {
                    log.fatal(e.getMessage());
                    System.exit(1);
                }
            }
        } else {
            IF = null;
        }

        if (config.containsKey("tag_on_failure")) {
            this.tagOnFailure = (String) config.get("tag_on_failure");
        } else {
            this.tagOnFailure = null;
        }

        this.removeFields = (ArrayList<String>) this.config.get("remove_fields");

        this.prepare();
    }

    protected void prepare() {
    }

    public Map process(Map event) {
        boolean isSuccess = true;
        if (this.IF != null) {
            for (TemplateRender render : this.IF) {
                if (!render.render(event).equals("true")) {
                    isSuccess = false;
                    break;
                }
            }
        }
        if (isSuccess == true) {
            event = this.filter(event);
        }

        return event;
    }

    ;

    protected Map filter(Map event) {
        return null;
    }

    public void postProcess(Map event, boolean ifSuccess) {
        if (ifSuccess == false) {
            if (this.tagOnFailure == null || this.tagOnFailure.length() <= 0) {
                return;
            }
            if (!event.containsKey("tags")) {
                event.put("tags", new ArrayList<String>(Arrays.asList(this.tagOnFailure)));
            } else {
                Object tags = event.get("tags");
                if (tags.getClass() == ArrayList.class
                        && ((ArrayList) tags).indexOf(this.tagOnFailure) == -1) {
                    ((ArrayList) tags).add(this.tagOnFailure);
                }
            }
        } else if (this.removeFields != null) {
            for (String f : this.removeFields) {
                event.remove(f);
            }
        }
    }
}
