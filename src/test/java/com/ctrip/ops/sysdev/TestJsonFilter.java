package com.ctrip.ops.sysdev;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import com.ctrip.ops.sysdev.filters.Json;
import org.testng.Assert;
import org.testng.annotations.Test;
import org.yaml.snakeyaml.Yaml;

public class TestJsonFilter {
	@Test
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public void testJsonFilter() {
		// General Test
		String c = String.format("%s\n%s", "field: message",
				"remove_fields: ['message','abcd']");

		Yaml yaml = new Yaml();
		Map config = (Map) yaml.load(c);
		Assert.assertNotNull(config);

		Json Jsonfilter = new Json(config);

		Map event = new HashMap();
		event.put("message",
				"{\"@timestamp\":\"2015-12-28T17:14:47.337+08:00\",\"a\":123}");

		event = Jsonfilter.process(event);

		Assert.assertEquals(event.get("a"), 123L);
		Assert.assertEquals(event.get("@timestamp"),
				"2015-12-28T17:14:47.337+08:00");
		Assert.assertNull(event.get("message"));
		Assert.assertNull(event.get("tags"));

		// Fail
		c = String.format("%s\n%s", "field: message",
				"remove_fields: ['message']");

		yaml = new Yaml();
		config = (Map) yaml.load(c);
		Assert.assertNotNull(config);

		Jsonfilter = new Json(config);

		event = new HashMap();
		event.put("message",
				"{\"@timestamp\":\"2015-12-28T17:14:47.337+08:00\",\"a\"=123}");

		event = Jsonfilter.process(event);

		Assert.assertNull(event.get("a"));
		Assert.assertNull(event.get("@timestamp"));
		Assert.assertNotNull(event.get("message"));
		Assert.assertEquals(((ArrayList) event.get("tags")).get(0), "jsonfail");

		// Fail, No tags
		c = String.format("%s\n%s\n%s\n", "field: message",
				"tag_on_failure: null", "remove_fields: ['message']");

		yaml = new Yaml();
		config = (Map) yaml.load(c);
		Assert.assertNotNull(config);

		Jsonfilter = new Json(config);

		event = new HashMap();
		event.put("message",
				"{\"@timestamp\":\"2015-12-28T17:14:47.337+08:00\",\"a\"=123}");

		event = Jsonfilter.process(event);

		Assert.assertNull(event.get("a"));
		Assert.assertNull(event.get("@timestamp"));
		Assert.assertNotNull(event.get("message"));
		Assert.assertNull(event.get("tags"));

		// target
		// Fail
		c = String.format("%s\n%s\n%s\n", "field: message", "target: jsonobj",
				"remove_fields: ['message']");

		yaml = new Yaml();
		config = (Map) yaml.load(c);
		Assert.assertNotNull(config);

		Jsonfilter = new Json(config);

		event = new HashMap();
		event.put("message",
				"{\"@timestamp\":\"2015-12-28T17:14:47.337+08:00\",\"a\":123}");

		event = Jsonfilter.process(event);

		Assert.assertEquals(((Map) event.get("jsonobj")).get("a"), 123L);
		Assert.assertNull(event.get("a"));
		Assert.assertNull(event.get("@timestamp"));
		Assert.assertNull(event.get("message"));
		Assert.assertNull(event.get("tags"));
	}
}
