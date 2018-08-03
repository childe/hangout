package com.ctrip.ops.sysdev.fieldDeleter;

import com.ctrip.ops.sysdev.fieldSetter.FieldSetter;

import java.util.Map;

/**
 * Created by liujia on 17/2/23.
 */
public class OneLevelDeleter implements FieldDeleter {
    private String field;

    public OneLevelDeleter(String field) {
        this.field = field;
    }

    @Override
    public Map delete(Map event) {
        event.remove(field);
        return event;
    }
}
