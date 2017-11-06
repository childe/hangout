package com.ctrip.ops.sysdev.fieldGetter;

import java.util.Map;

/**
 * Created by huochen on 2017/11/6.
 */
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
