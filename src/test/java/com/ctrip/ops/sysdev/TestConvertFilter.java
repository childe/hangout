package com.ctrip.ops.sysdev;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.testng.Assert;
import org.testng.annotations.Test;
import org.yaml.snakeyaml.Yaml;
import com.ctrip.ops.sysdev.filters.Convert;

;

public class TestConvertFilter {
	@Test
	@SuppressWarnings({ "rawtypes", "unchecked", "serial" })
	public void testDateFilter() throws UnsupportedEncodingException {
		String c = String.format("%s\n%s\n%s\n%s\n%s\n%s\n%s",
				"        fields:", "            cs_bytes: ",
				"                to: integer",
				"                remove_if_fail: true",
				"            time_taken: ", "                to: float",
				"                setto_if_fail: 0.0");
		Yaml yaml = new Yaml();
		Map config = (Map) yaml.load(c);
		Assert.assertNotNull(config);

		Convert convertfilter = new Convert(config);

		// Match
		Map event = new HashMap();
		event.put("cs_bytes", "12");
		event.put("time_taken", "11.11");

		event = convertfilter.process(event);
		Assert.assertEquals(event.get("cs_bytes"), 12);
		Assert.assertEquals(event.get("time_taken"), 11.11F);
		Assert.assertNull(event.get("tags"));

		// remove_if_fail
		event = new HashMap();
		event.put("cs_bytes", "12.2");

		event = convertfilter.process(event);
		Assert.assertNull(event.get("cs_bytes"));
		Assert.assertEquals(((ArrayList) event.get("tags")).get(0),
				"convertfail");

		// setto_if_fail
		event = new HashMap();
		event.put("time_taken", "12.2a");

		event = convertfilter.process(event);
		Assert.assertEquals(event.get("time_taken"), 0.0);
		Assert.assertEquals(((ArrayList) event.get("tags")).get(0),
				"convertfail");

		// set tag
		c = String.format("%s\n%s\n%s\n%s\n%s\n%s",
				"fields:",
				"  cs_bytes: ",
				"    to: integer",
				"    remove_if_fail: true",
				"  time_taken: ",
				"    to: float");
		yaml = new Yaml();
		config = (Map) yaml.load(c);

		convertfilter = new Convert(config);
		event = new HashMap();
		event.put("time_taken", "12.2a");

		event = convertfilter.process(event);
		Assert.assertEquals(((ArrayList) event.get("tags")).get(0),
				"convertfail");

		// NOT set tag
		c = String.format("%s\n%s\n%s\n%s\n%s\n%s\n%s",
				"tag_on_failure: null",
				"fields:",
				"  cs_bytes: ",
				"    to: integer",
				"    remove_if_fail: true",
				"  time_taken: ",
				"    to: float");
		yaml = new Yaml();
		config = (Map) yaml.load(c);

		convertfilter = new Convert(config);
		event = new HashMap();
		event.put("time_taken", "12.2a");

		event = convertfilter.process(event);
		Assert.assertNull((ArrayList) event.get("tags"));
	}
}
