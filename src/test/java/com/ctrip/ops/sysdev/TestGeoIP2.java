package com.ctrip.ops.sysdev;

import com.ctrip.ops.sysdev.filters.GeoIP2;
import org.json.simple.JSONValue;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.HashMap;
import java.util.Map;

public class TestGeoIP2 {
	@Test
	public void testGsub() throws UnsupportedEncodingException {
		String s = "61.240.136.69";
		HashMap config = new HashMap() {
			{
				put("source", "clientip");
			}
		};

		GeoIP2 geoip2Filter = new GeoIP2(config);

		Map event = new HashMap();
		event.put("clientip", s);
		event = geoip2Filter.process(event);
		System.out.println(event);
	}
}
