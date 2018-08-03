package com.ctrip.ops.sysdev.baseplugin;

/**
 * Created by liujia on 17/2/10.
 */

import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;


public class TestBaseFilter {
    @Test
    public void testBaseFilter() {
        HashMap config = new HashMap();
        config.put("remove_fields", new ArrayList(Arrays.asList("test1", "[a][b]")));
        config.put("add_fields", new HashMap() {{
            this.put("ok", true);
            this.put("[extra][value]", 11.11);
            this.put("[extra][metric][value]", 10);
        }});
        BaseFilter bi = new BaseFilter(config);

        HashMap event = new HashMap() {{
            this.put("test1", "ab");
            this.put("a", new HashMap() {{
                this.put("b", 213);
                this.put("b2", "hello");
            }});
        }};

        bi.postProcess(event, true);
        System.out.println(event);
        Assert.assertNull(event.get("test1"));
        Assert.assertNull(((Map) event.get("a")).get("b"));
        Assert.assertEquals(((Map) event.get("a")).get("b2"), "hello");
        Assert.assertTrue((Boolean) event.get("ok"));
        Map extra = (Map) event.get("extra");
        Map metric = (Map) extra.get("metric");
        Assert.assertEquals(extra.size(), 2);
        Assert.assertEquals(extra.get("value"), 11.11);
        Assert.assertEquals(metric.size(), 1);
        Assert.assertEquals(metric.get("value"), 10);
    }
}
