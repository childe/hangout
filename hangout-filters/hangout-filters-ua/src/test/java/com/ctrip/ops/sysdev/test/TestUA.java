package com.ctrip.ops.sysdev.test;

import java.util.HashMap;
import java.util.Map;

import com.ctrip.ops.sysdev.filters.UA;
import org.testng.Assert;
import org.testng.annotations.Test;
import org.yaml.snakeyaml.Yaml;

public class TestUA {
	@Test
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public void testUA() {
		// General Test
		String c = String.format("%s\n", "source: ua");

		Yaml yaml = new Yaml();
		Map config = (Map) yaml.load(c);
		Assert.assertNotNull(config);

		UA UAfilter = new UA(config);

		Map event = new HashMap();
		event.put(
				"ua",
				"Mozilla/5.0 (iPhone; CPU iPhone OS 5_1_1 like Mac OS X) AppleWebKit/534.46 (KHTML, like Gecko) Version/5.1 Mobile/9B206 Safari/7534.48.3");

		event = UAfilter.process(event);
		Assert.assertEquals(event.get("userAgent_family"), "Mobile Safari");
		Assert.assertEquals(event.get("userAgent_major"), "5");
		Assert.assertEquals(event.get("userAgent_minor"), "1");
		Assert.assertEquals(event.get("os_family"), "iOS");
		Assert.assertEquals(event.get("os_major"), "5");
		Assert.assertEquals(event.get("os_minor"), "1");
		Assert.assertEquals(event.get("device_family"), "iPhone");
		Assert.assertNull(event.get("tags"));
		Assert.assertNotNull(event.get("ua"));

		// could NOT parse
		event = new HashMap();
		event.put("ua", "whatisthis");

		event = UAfilter.process(event);
		Assert.assertEquals(event.get("userAgent_family"), "Other");
		Assert.assertNull(event.get("userAgent_major"));
		Assert.assertNull(event.get("userAgent_minor"));
		Assert.assertEquals(event.get("os_family"), "Other");
		Assert.assertNull(event.get("os_major"));
		Assert.assertNull(event.get("os_minor"));
		Assert.assertEquals(event.get("device_family"), "Other");
		Assert.assertNotNull(event.get("ua"));
	}
}
