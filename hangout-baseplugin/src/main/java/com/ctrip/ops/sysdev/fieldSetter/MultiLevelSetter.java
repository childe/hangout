package com.ctrip.ops.sysdev.fieldSetter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by liujia on 17/2/23.
 */
public class MultiLevelSetter implements FieldSetter {
    private ArrayList<String> fields = new ArrayList();
    final Pattern p = Pattern.compile("\\[(\\S+?)\\]+");

    public MultiLevelSetter(String template) {
        Matcher m = p.matcher(template);
        while (m.find()) {
            String a = m.group();
            this.fields.add(a.substring(1, a.length() - 1));
        }
    }

    @Override
    public void setField(Map event, String field, Object value) {

    }

    @Override
    public void setField(Map event, Object value) {
        if (this.fields.size() == 0)
            return;

        Map current = event;
        for (int i = 0; i < this.fields.size() - 1; i++) {
            String field = this.fields.get(i);
            if (current.containsKey(field)) {
                current = (Map) current.get(field);
            } else {
                Map next = new HashMap<>();
                current.put(field, next);
                current = next;
            }
        }
        current.put(this.fields.get(this.fields.size() - 1), value);
    }
}
