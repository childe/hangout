package com.ctrip.ops.sysdev.render;

/**
 * Created by liujia on 17/2/10.
 */

import com.ctrip.ops.sysdev.fieldDeleter.FieldDeleter;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.HashMap;
import java.util.Map;

public class TestFieldDeleter {
    @Test
    public void testFieldDeleter() {
        FieldDeleter fieldDeleter = FieldDeleter.getFieldDeleter("[a][b]");
        HashMap event = new HashMap() {{
            this.put("test1", "ab");
            this.put("a", new HashMap() {{
                this.put("b", 123);
                this.put("b2", "hello");
            }});
        }};

        fieldDeleter.delete(event);
        Assert.assertEquals(event.get("test1"), "ab");
        Assert.assertEquals(((Map) event.get("a")).get("b2"), "hello");
        Assert.assertNull(((Map) event.get("a")).get("b"));
    }
}
