package com.ctrip.ops.sysdev.fieldDeleter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by liujia on 17/2/23.
 */
public class MultiLevelDeleter implements FieldDeleter {
    private ArrayList<String> fields = new ArrayList();
    final Pattern p = Pattern.compile("\\[(\\S+?)\\]+");

    public MultiLevelDeleter(String template) {
        Matcher m = p.matcher(template);
        while (m.find()) {
            String a = m.group();
            this.fields.add(a.substring(1, a.length() - 1));
        }
    }


    @Override
    public Map delete(Map event) {
        if (this.fields.size() == 0)
            return event;

        Map current = event;
        for (int i = 0; i < this.fields.size() - 1; i++) {
            String field = this.fields.get(i);
            if (current.containsKey(field)) {
                Object t = current.get(field);
                if (!Map.class.isAssignableFrom(t.getClass())) {
                    return event;
                }
                current = (Map) t;
            } else {
                return event;
            }
        }
        current.remove(this.fields.get(this.fields.size() - 1));
        return event;
    }
}
