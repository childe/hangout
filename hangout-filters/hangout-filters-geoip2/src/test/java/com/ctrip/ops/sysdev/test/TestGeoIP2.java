package com.ctrip.ops.sysdev.test;

import com.ctrip.ops.sysdev.filters.GeoIP2;

import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.File;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

@SuppressWarnings("ALL")
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
        Assert.assertEquals(((Map) event.get("geoip")).get("country_code"), "CN");
        Assert.assertEquals(((Map) event.get("geoip")).get("country_isocode"), "CN");
        Assert.assertNull(((Map) event.get("geoip")).get("subdivision_name"));
        Assert.assertNull(((Map) event.get("geoip")).get("city_name"));

        event = new HashMap();
        event.put("clientip", "115.239.211.112");
        event = geoip2Filter.process(event);
        Assert.assertEquals(((Map) event.get("geoip")).get("country_name"), "China");
        Assert.assertEquals(((Map) event.get("geoip")).get("country_code"), "CN");
        Assert.assertEquals(((Map) event.get("geoip")).get("country_isocode"), "CN");
        Assert.assertEquals(((Map) event.get("geoip")).get("city_name"), "Hangzhou");
        Assert.assertEquals(((Map) event.get("geoip")).get("subdivision_name"), "Zhejiang");
        Assert.assertEquals(((Map) event.get("geoip")).get("longitude"), 120.1614);
        Assert.assertEquals(((Map) event.get("geoip")).get("latitude"), 30.2936);

        event = new HashMap();
        event.put("clientip", "10.10.10.10");
        event = geoip2Filter.process(event);
        Assert.assertNull(event.get("geoip"));
        Assert.assertEquals(((ArrayList) event.get("tags")).get(0), "geoipfail");

        config = new HashMap() {
            {
                put("source", "clientip");
                put("database", "/tmp/GeoLite2-City.mmdb");
                put("country_code", false);
                put("latitude", false);
                put("longitude", false);
            }
        };
        geoip2Filter = new GeoIP2(config);
        event = new HashMap();
        event.put("clientip", "115.239.211.112");
        event = geoip2Filter.process(event);

        Assert.assertEquals(((Map) event.get("geoip")).get("country_name"), "China");
        Assert.assertEquals(((Map) event.get("geoip")).get("country_isocode"), "CN");
        Assert.assertEquals(((Map) event.get("geoip")).get("city_name"), "Hangzhou");
        Assert.assertEquals(((Map) event.get("geoip")).get("subdivision_name"), "Zhejiang");
        Assert.assertNull(((Map) event.get("geoip")).get("country_code"));
        Assert.assertNull(((Map) event.get("geoip")).get("latitude"));
        Assert.assertNull(((Map) event.get("geoip")).get("longitude"));
        Assert.assertEquals(((double[]) ((Map) event.get("geoip")).get("location"))[0], 120.1614);
        Assert.assertEquals(((double[]) ((Map) event.get("geoip")).get("location"))[1], 30.2936);

        f = new File("/tmp/GeoLite2-Country.mmdb");
        if (f.exists() && !f.isDirectory()) {
            config = new HashMap() {
                {
                    put("source", "clientip");
                    put("database", "/tmp/GeoLite2-Country.mmdb");
                }
            };
            geoip2Filter = new GeoIP2(config);

            event = new HashMap();
            event.put("clientip", "115.239.211.112");
            event = geoip2Filter.process(event);
            Assert.assertEquals(((Map) event.get("geoip")).get("country_name"), "China");
            Assert.assertEquals(((Map) event.get("geoip")).get("country_code"), "CN");
            Assert.assertEquals(((Map) event.get("geoip")).get("country_isocode"), "CN");
            Assert.assertNull(((Map) event.get("geoip")).get("city_name"));
        }
    }
}
