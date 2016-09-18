package com.ctrip.ops.sysdev;

import com.ctrip.ops.sysdev.filters.GeoIP2;

import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.File;
import java.io.UnsupportedEncodingException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class TestGeoIP2 {
    @Test
    public void testGeoIP2() throws Exception {
        File f = new File("/tmp/GeoLite2-City.mmdb");
        if (!f.exists() || f.isDirectory()) {
            throw new Exception("please put /tmp/GeoLite2-City.mmdb and then test");
        }

        HashMap config = new HashMap() {
            {
                put("source", "clientip");
                put("database", "/tmp/GeoLite2-City.mmdb");
            }
        };

        GeoIP2 geoip2Filter = new GeoIP2(config);

        Map event = new HashMap();
        event.put("clientip", "61.240.136.69");
        event = geoip2Filter.process(event);
        Assert.assertEquals(((Map) event.get("geoip")).get("country_name"), "China");

        event = new HashMap();
        event.put("clientip", "10.10.10.10");
        event = geoip2Filter.process(event);
        System.out.println(event);
        Assert.assertNull(event.get("geoip"));
        Assert.assertEquals(((ArrayList) event.get("tags")).get(0), "geoipfail");
    }
}
