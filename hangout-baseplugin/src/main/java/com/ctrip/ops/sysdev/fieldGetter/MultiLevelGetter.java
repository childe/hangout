package com.ctrip.ops.sysdev.fieldGetter;

import java.util.ArrayList;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MultiLevelGetter implements FieldGetter {
    private ArrayList<String> fields = new ArrayList();
    final Pattern p = Pattern.compile("\\[(\\S+?)\\]+");

    public MultiLevelGetter(String template) {
        Matcher m = p.matcher(template);
        while (m.find()) {
            String a = m.group();
            this.fields.add(a.substring(1, a.length() - 1));
        }
    }

    @Override
    public Object getField(Map event) {
        if (this.fields.size() == 0)
            return "";

        Map current = event;
        for (int i = 0; i < this.fields.size() - 1; i++) {
            String field = this.fields.get(i);
            if (current.containsKey(field)) {
                current = (Map) current.get(field);
            } else {
                return null;
            }
        }
        return current.get(this.fields.get(this.fields.size() - 1));
    }
}
