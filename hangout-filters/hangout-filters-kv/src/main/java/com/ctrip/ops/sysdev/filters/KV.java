package com.ctrip.ops.sysdev.filters;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import com.ctrip.ops.sysdev.baseplugin.BaseFilter;
import lombok.extern.log4j.Log4j2;

@Log4j2
public class KV extends BaseFilter {

    private String source;
    private String target;
    private String field_split;
    private String value_split;
    private String trim;
    private String trimkey;

    private ArrayList<String> excludeKeys, includeKeys;

    @SuppressWarnings("rawtypes")
    public KV(Map config) {
        super(config);
    }

    @SuppressWarnings("unchecked")
    protected void prepare() {

        if (this.config.containsKey("source")) {
            this.source = (String) this.config.get("source");
        } else {
            this.source = "message";
        }

        if (this.config.containsKey("target")) {
            this.target = (String) this.config.get("target");
        }

        if (this.config.containsKey("field_split")) {
            this.field_split = (String) this.config.get("field_split");
        } else {
            this.field_split = " ";
        }
        if (this.config.containsKey("value_split")) {
            this.value_split = (String) this.config.get("value_split");
        } else {
            this.value_split = "=";
        }

        if (this.config.containsKey("trim")) {
            this.trim = (String) this.config.get("trim");
            this.trim = "^[" + this.trim + "]+|[" + this.trim + "]+$";
        }

        if (this.config.containsKey("trimkey")) {
            this.trimkey = (String) this.config.get("trimkey");
            this.trimkey = "^[" + this.trimkey + "]+|[" + this.trimkey + "]+$";
        }

        if (this.config.containsKey("tag_on_failure")) {
            this.tagOnFailure = (String) this.config.get("tag_on_failure");
        } else {
            this.tagOnFailure = "KVfail";
        }

        this.excludeKeys = (ArrayList<String>) this.config.get("exclude_keys");
        this.includeKeys = (ArrayList<String>) this.config.get("include_keys");

    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    @Override
    protected Map filter(Map event) {
        if (!event.containsKey(this.source)) {
            return event;
        }
        boolean success = true;

        HashMap targetObj = new HashMap();

        try {
            String sourceStr = (String) event.get(this.source);
            for (String kv : sourceStr.split(this.field_split)) {
                String[] kandv = kv.split(this.value_split, 2);
                if (kandv.length != 2) {
                    success = false;
                    continue;
                }

                String k = kandv[0];
                if (this.includeKeys != null && !this.includeKeys.contains(k)
                        || this.excludeKeys != null
                        && this.excludeKeys.contains(k)) {
                    continue;
                }

                String v = kandv[1];

                if (this.trim != null) {
                    v = v.replaceAll(this.trim, "");
                }
                if (this.trimkey != null) {
                    k = k.replaceAll(this.trimkey, "");
                }

                if (this.target != null) {
                    targetObj.put(k, v);
                } else {
                    event.put(k, v);
                }
            }

            if (this.target != null) {
                event.put(this.target, targetObj);
            }
        } catch (Exception e) {
            log.warn(event + "kv faild");
            success = false;
        }

        this.postProcess(event, success);

        return event;
    }
}
