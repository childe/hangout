package com.ctrip.ops.sysdev.render;

import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.HashMap;

/**
 * Created by liujia on 18/7/11.
 */
public class TestDateFormatter {
    @Test
    public void testDateFormatter() {
        DateFormatter render = new DateFormatter("access-%{+YYYY.MM.dd}", "UTC");
        HashMap event = new HashMap() {{
            this.put("@timestamp", "2006-01-02T15:03:04.000");
        }};

        Object rst = render.render(event);
        Assert.assertEquals(rst, "access-2006.01.02");


        render = new DateFormatter("access-%{+YYYY.MM.dd}", "Asia/Shanghai");
        event = new HashMap() {{
            this.put("@timestamp", "2006-01-02T00:03:04.000");
        }};

        rst = render.render(event);
        Assert.assertEquals(rst, "access-2006.01.02");


        render = new DateFormatter("access-%{+YYYY.MM.dd}", "UTC");
        event = new HashMap() {{
            this.put("@timestamp", "2006-01-02T00:03:04.000");
        }};

        rst = render.render(event);
        Assert.assertEquals(rst, "access-2006.01.01");


        render = new DateFormatter("access-%{appname}-%{+YYYY.MM.dd}", "UTC");
        event = new HashMap() {{
            this.put("@timestamp", "2006-01-02T00:03:04.000");
            this.put("appname", "home");
        }};

        rst = render.render(event);
        Assert.assertEquals(rst, "access-home-2006.01.01");



        render = new DateFormatter("access-%{appname}-%{+YYYY.MM.dd}", "UTC");
        event = new HashMap() {{
            this.put("@timestamp", "2006-01-02T00:03:04.000");
        }};

        rst = render.render(event);
        Assert.assertEquals(rst, "access-%{appname}-2006.01.01");
    }
}

