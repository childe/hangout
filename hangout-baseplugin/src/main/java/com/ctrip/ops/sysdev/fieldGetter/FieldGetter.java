package com.ctrip.ops.sysdev.fieldGetter;

import java.util.Map;
import java.util.regex.Pattern;

/**
 * Created by huochen on 2017/11/6.
 */
public interface FieldGetter {
    Pattern p = Pattern.compile("\\[\\S+\\]+");

    public Object getField(Map event);

//    public void getField(Map event, Object value);

    public static FieldGetter getFieldSetter(String field) {
        if (p.matcher(field).matches()) {
            return new MultiLevelGetter(field);
        }
        return new OneLevelGetter(field);
    }
}
