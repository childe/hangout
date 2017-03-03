package com.ctrip.ops.sysdev.fieldDeleter;

import java.util.Map;
import java.util.regex.Pattern;

/**
 * Created by liujia on 17/2/23.
 */
public interface FieldDeleter {
    Pattern p = Pattern.compile("\\[\\S+\\]+");

    public Map delete(Map event);

    public static FieldDeleter getFieldDeleter(String field) {
        if (p.matcher(field).matches()) {
            return new MultiLevelDeleter(field);
        }
        return new OneLevelDeleter(field);
    }
}
