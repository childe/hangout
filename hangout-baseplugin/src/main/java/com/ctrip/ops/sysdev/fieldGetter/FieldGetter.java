package com.ctrip.ops.sysdev.fieldGetter;

import java.util.Map;
import java.util.regex.Pattern;

public interface FieldGetter {
    Pattern p = Pattern.compile("\\[\\S+\\]+");

    public Object getField(Map event);

    public static FieldGetter getFieldGetter(String field) {
        if (p.matcher(field).matches()) {
            return new MultiLevelGetter(field);
        }
        return new OneLevelGetter(field);
    }
}
