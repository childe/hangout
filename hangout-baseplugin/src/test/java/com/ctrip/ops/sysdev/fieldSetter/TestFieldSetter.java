package com.ctrip.ops.sysdev.fieldSetter;

/**
 * Created by liujia on 17/2/10.
 */

import com.ctrip.ops.sysdev.fieldSetter.FieldSetter;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.HashMap;
import java.util.Map;

public class TestFieldSetter {
    @Test
    public void testFieldSetter() {
        FieldSetter fieldSetter = FieldSetter.getFieldSetter("[a][b]");
        HashMap event = new HashMap() {{
            this.put("test1", "ab");
            this.put("a", new HashMap() {{
                this.put("b", 1);
                this.put("b2", "hello");
            }});
        }};

        fieldSetter.setField(event, 456);
        Assert.assertEquals(event.get("test1"), "ab");
        Assert.assertEquals(((Map) event.get("a")).get("b2"), "hello");
        Assert.assertEquals(((Map) event.get("a")).get("b"), 456);

        // delete a map
        fieldSetter = FieldSetter.getFieldSetter("a");
        event = new HashMap() {{
            this.put("test1", "ab");
            this.put("a", new HashMap() {{
                this.put("b", 2);
                this.put("b2", "hello");
            }});
        }};

        fieldSetter.setField(event, 123);
        Assert.assertEquals(event.get("test1"), "ab");
        Assert.assertEquals(event.get("a"), 123);

        //more than 2 level
        fieldSetter = FieldSetter.getFieldSetter("[a][b][c]");
        event = new HashMap() {{
            this.put("test1", "ab");
        }};

        fieldSetter.setField(event, 123);
        Assert.assertEquals(event.get("test1"), "ab");
        Map a = (Map) event.get("a");
        Map b = (Map) a.get("b");
        Assert.assertEquals(b.get("c"), 123);
    }
}
