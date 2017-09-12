package com.ctrip.ops.sysdev.filters;

import java.util.Map;

import com.ctrip.ops.sysdev.baseplugin.BaseFilter;
import com.ctrip.ops.sysdev.fieldSetter.FieldSetter;
import com.ctrip.ops.sysdev.render.TemplateRender;
import lombok.extern.log4j.Log4j2;
import org.json.simple.JSONValue;

@Log4j2
public class Json extends BaseFilter {

    public Json(Map config) {
        super(config);
    }

    private String field;
    private TemplateRender templateRender;
    private FieldSetter fieldSetter;

    protected void prepare() {
        if (!config.containsKey("field")) {
            log.error("no field configured in Json");
            System.exit(1);
        }
        this.field = (String) config.get("field");

        String target = (String) config.get("target");
        if (target == null || target.equals("")) {
            this.fieldSetter = null;
        } else {
            this.fieldSetter = FieldSetter.getFieldSetter(target);
        }

        if (config.containsKey("tag_on_failure")) {
            this.tagOnFailure = (String) config.get("tag_on_failure");
        } else {
            this.tagOnFailure = "jsonfail";
        }

        try {
            this.templateRender = TemplateRender.getRender(field, false);
        } catch (Exception e) {
            log.error("could not render template: " + field);
            System.exit(1);
        }
    }

    @Override
    protected Map filter(final Map event) {
        Object obj = null;
        boolean success = false;

        Object o = this.templateRender.render(event);
        if (o != null) {
            try {
                obj = JSONValue
                        .parseWithException((String) o);
                success = true;
            } catch (Exception e) {
                log.debug("failed to json parse field: " + this.field);
            }
        }

        if (obj != null) {
            if (this.fieldSetter == null) {
                try {
                    event.putAll((Map) obj);
                } catch (Exception e) {
                    log.warn(this.field + " is not a map, you should set a target to save it");
                    success = false;
                }
            } else {
                this.fieldSetter.setField(event, obj);
            }
        }

        this.postProcess(event, success);
        return event;
    }
}
