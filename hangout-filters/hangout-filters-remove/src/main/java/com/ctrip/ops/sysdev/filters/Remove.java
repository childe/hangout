package com.ctrip.ops.sysdev.filters;

import com.ctrip.ops.sysdev.baseplugin.BaseFilter;
import com.ctrip.ops.sysdev.fieldDeleter.FieldDeleter;

import java.util.ArrayList;
import java.util.Map;

@SuppressWarnings("ALL")
public class Remove extends BaseFilter {
    public Remove(Map config) {
        super(config);
    }

    private ArrayList<FieldDeleter> fields;

    protected void prepare() {

        this.fields = new ArrayList<>();
        for (String field : (ArrayList<String>) config.get("fields")) {
            this.fields.add(FieldDeleter.getFieldDeleter(field));
        }
    }

    @Override
    protected Map filter(final Map event) {
        for (FieldDeleter t : this.fields) {
            t.delete(event);
        }

        return event;
    }
}
