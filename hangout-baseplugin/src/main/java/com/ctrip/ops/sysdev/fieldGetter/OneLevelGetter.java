package com.ctrip.ops.sysdev.fieldGetter;

import java.util.Map;

public class OneLevelGetter implements FieldGetter {
    private String field;

    public OneLevelGetter(String field) {
        this.field = field;
    }

    @Override
    public Object getField(Map event) {
        return event.get(this.field);
    }
}
