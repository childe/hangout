package com.ctrip.ops.sysdev.baseplugin;

import com.ctrip.ops.sysdev.render.FreeMarkerRender;
import com.ctrip.ops.sysdev.render.TemplateRender;
import lombok.extern.log4j.Log4j;

import java.io.IOException;
import java.util.*;


@Log4j
public class BaseFilter extends Base {

    protected Map config;
    protected TemplateRender render;
    protected String tagOnFailure;
    protected List<String> removeFields;
    protected Map<String, Object> addFields;
    private List<TemplateRender> IF;
    public boolean processExtraEventsFunc;


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
        this.addFields = (Map<String, Object>) this.config.get("add_fields");

        this.prepare();
    }

    protected void prepare() {
    }

    public boolean needProcess(Map event) {
        if (this.IF != null) {
            for (TemplateRender render : this.IF) {
                if (!render.render(event).equals("true")) {
                    return false;
                }
            }
        }
        return true;
    }

    public Map process(Map event) {
        if (event == null) {
            return null;
        }

        if (this.needProcess(event) == true) {
            event = this.filter(event);
        }

        return event;
    }

    public List<Map<String, Object>> processExtraEvents(Map event) {
        if (this.processExtraEventsFunc == false || event == null || this.needProcess(event) == false) {
            return null;
        }

        return this.filterExtraEvents(event);
    }

    protected Map filter(Map event) {
        return event;
    }

    protected List<Map<String, Object>> filterExtraEvents(Map event) {
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
        } else {
            if (this.removeFields != null) {
                for (String f : this.removeFields) {
                    event.remove(f);
                }
            }

            if (this.addFields != null) {
                Iterator<Map.Entry<String, Object>> it = this.addFields.entrySet().iterator();
                while (it.hasNext()) {
                    Map.Entry<String, Object> entry = it.next();
                    String field = entry.getKey();
                    Object value = entry.getValue();
                    event.put(field, value);
                }
            }
        }
    }
}
