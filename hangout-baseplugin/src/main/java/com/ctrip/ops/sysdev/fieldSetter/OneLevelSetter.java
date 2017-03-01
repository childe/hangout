package com.ctrip.ops.sysdev.fieldSetter;

import java.util.Map;

/**
 * Created by liujia on 17/2/23.
 */
public class OneLevelSetter implements FieldSetter {
    private String field;

    public OneLevelSetter(String field) {
        this.field = field;
    }

    @Override
    public void setField(Map event, String field, Object value) {
        event.put(field, value);
    }

    @Override
    public void setField(Map event, Object value) {
        event.put(this.field, value);
    }
}
