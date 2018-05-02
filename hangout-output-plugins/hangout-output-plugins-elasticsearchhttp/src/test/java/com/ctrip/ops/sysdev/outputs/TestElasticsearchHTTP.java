package com.ctrip.ops.sysdev.outputs;

import org.apache.http.HttpHost;
import org.testng.Assert;
import org.testng.annotations.Test;


/**
 * Created by liujia on 18/5/2.
 */
public class TestElasticsearchHTTP {
    @Test
    public void TestElasticsearchHTTP() {
        HttpHost host = null;

        host = HttpHost.create("");
        Assert.assertEquals(host.getSchemeName(), "http");
        Assert.assertEquals(host.getAddress(), null);
        Assert.assertEquals(host.getHostName(), "");
        Assert.assertEquals(host.getPort(), -1);

        host = HttpHost.create("192.168.0.100");
        Assert.assertEquals(host.getSchemeName(), "http");
        Assert.assertEquals(host.getAddress(), null);
        Assert.assertEquals(host.getHostName(), "192.168.0.100");
        Assert.assertEquals(host.getPort(), -1);

        host = HttpHost.create("192.168.0.100:80");
        Assert.assertEquals(host.getSchemeName(), "http");
        Assert.assertEquals(host.getAddress(), null);
        Assert.assertEquals(host.getHostName(), "192.168.0.100");
        Assert.assertEquals(host.getPort(), 80);

        host = HttpHost.create("http://192.168.0.100");
        Assert.assertEquals(host.getSchemeName(), "http");
        Assert.assertEquals(host.getAddress(), null);
        Assert.assertEquals(host.getHostName(), "192.168.0.100");
        Assert.assertEquals(host.getPort(), -1);

        host = HttpHost.create("https://192.168.0.100");
        Assert.assertEquals(host.getSchemeName(), "https");
        Assert.assertEquals(host.getAddress(), null);
        Assert.assertEquals(host.getHostName(), "192.168.0.100");
        Assert.assertEquals(host.getPort(), -1);
    }
}
