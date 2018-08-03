package com.ctrip.ops.sysdev.render;

/**
 * Created by liujia on 17/2/10.
 */

import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.HashMap;

public class TestFieldRender {
    @Test
    public void testFieldRender() {
        FieldRender render = new FieldRender("[a][b]");
        HashMap event = new HashMap() {{
            this.put("test1", "ab");
            this.put("a", new HashMap() {{
                this.put("b", 123);
                this.put("b2", "hello");
            }});
        }};

        Object rst = render.render(event);
        Assert.assertEquals(123, rst);
    }
}
