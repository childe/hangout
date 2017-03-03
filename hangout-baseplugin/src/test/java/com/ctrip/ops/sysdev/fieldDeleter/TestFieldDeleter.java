package com.ctrip.ops.sysdev.fieldDeleter;

/**
 * Created by liujia on 17/2/10.
 */

import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
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

        // delete a map
        fieldDeleter = FieldDeleter.getFieldDeleter("a");
        event = new HashMap() {{
            this.put("test1", "ab");
            this.put("a", new HashMap() {{
                this.put("b", 123);
                this.put("b2", "hello");
            }});
        }};

        fieldDeleter.delete(event);
        Assert.assertEquals(event.get("test1"), "ab");
        Assert.assertNull(event.get("a"));


        // NOT contain
        fieldDeleter = FieldDeleter.getFieldDeleter("[a][b]");
        event = new HashMap() {{
            this.put("test1", "ab");
            this.put("a", Arrays.asList(1, 2));
        }};

        fieldDeleter.delete(event);
        Assert.assertEquals(event.get("test1"), "ab");
        Assert.assertEquals(((List) event.get("a")).size(), 2);
        Assert.assertEquals(((List) event.get("a")).get(0), 1);
        Assert.assertEquals(((List) event.get("a")).get(1), 2);

        // NOT map
        fieldDeleter = FieldDeleter.getFieldDeleter("[a][b][c]");
        event = new HashMap() {{
            this.put("test1", "ab");
            this.put("a", new HashMap() {{
                this.put("b", Arrays.asList(1, 2));
            }});
        }};

        fieldDeleter.delete(event);
        Assert.assertEquals(event.get("test1"), "ab");
        Assert.assertEquals(((Map) event.get("a")).size(), 1);
        Map a = (Map) event.get("a");
        List b = (List) a.get("b");
        Assert.assertEquals(b.size(), 2);
        Assert.assertEquals(b.get(0), 1);
        Assert.assertEquals(b.get(1), 2);

        // [a][b][c]
        fieldDeleter = FieldDeleter.getFieldDeleter("[a][b][c]");
        event = new HashMap() {{
            this.put("test1", "ab");
            this.put("a", new HashMap() {{
                this.put("b", new HashMap() {{
                    this.put("c", 100);
                }});
            }});
        }};

        fieldDeleter.delete(event);
        Assert.assertEquals(event.get("test1"), "ab");
        Assert.assertEquals(((Map) event.get("a")).size(), 1);
        a = (Map) event.get("a");
        Assert.assertEquals(((Map) a.get("b")).size(), 0);
    }
}
