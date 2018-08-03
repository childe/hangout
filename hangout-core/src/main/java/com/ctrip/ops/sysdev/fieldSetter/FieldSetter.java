package com.ctrip.ops.sysdev.fieldSetter;

import java.util.Map;
import java.util.regex.Pattern;

/**
 * Created by liujia on 17/2/23.
 */
public interface FieldSetter {
    Pattern p = Pattern.compile("\\[\\S+\\]+");

    public void setField(Map event, String field, Object value);

    public void setField(Map event, Object value);

    public static FieldSetter getFieldSetter(String field) {
        if (p.matcher(field).matches()) {
            return new MultiLevelSetter(field);
        }
        return new OneLevelSetter(field);
    }
}
