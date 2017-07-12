package com.ctrip.ops.sysdev.filters;

import java.util.Map;

import com.ctrip.ops.sysdev.baseplugin.BaseFilter;
import lombok.extern.log4j.Log4j2;
import org.json.simple.JSONValue;

@Log4j2
public class Json extends BaseFilter {

    public Json(Map config) {
        super(config);
    }

    private String field, target;

    protected void prepare() {
        if (!config.containsKey("field")) {
            log.error("no field configured in Json");
            System.exit(1);
        }
        this.field = (String) config.get("field");

        this.target = (String) config.get("target");
        if (this.target != null && this.target.equals("")) {
            this.target = null;
        }

        if (config.containsKey("tag_on_failure")) {
            this.tagOnFailure = (String) config.get("tag_on_failure");
        } else {
            this.tagOnFailure = "jsonfail";
        }
    }

    @Override
    protected Map filter(final Map event) {
        Object obj = null;
        boolean success = false;
        if (event.containsKey(this.field)) {
            try {
                obj = JSONValue
                        .parseWithException((String) event.get(this.field));
                success = true;
            } catch (Exception e) {
                log.debug("failed to json parse field: " + this.field);
            }
        }


        if (obj != null) {
            if (this.target == null) {
                try {
                    event.putAll((Map) obj);
                } catch (Exception e) {
                    log.warn(this.field + " is not a map, you should set a target to save it");
                    success = false;
                }
            } else {
                event.put(this.target, obj);
            }
        }

        this.postProcess(event, success);
        return event;
    }
}
