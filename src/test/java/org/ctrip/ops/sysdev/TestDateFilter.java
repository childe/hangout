package org.ctrip.ops.sysdev;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.testng.Assert;
import org.testng.annotations.Test;
import org.yaml.snakeyaml.Yaml;
import org.joda.time.DateTime;
import org.ctrip.ops.sysdev.filters.Date;

public class TestDateFilter {
	@Test
	@SuppressWarnings({ "rawtypes", "unchecked", "serial" })
	public void testDateFilter() throws UnsupportedEncodingException {
		String c = "src: logtime\n" + "formats:\n"
				+ "  - 'ISO8601'\n  - 'YYYY/MM/dd HH:mm.ss.SSS Z'\n"
				+ "tag_on_failure: 'datefail'\n" + "remove_fields: ['logtime']";
		Yaml yaml = new Yaml();
		Map config = (Map) yaml.load(c);
		Assert.assertNotNull(config);

		Date datefilter = new Date(config);

		Map event = new HashMap();

		// Message MATCH format
		DateTime now = DateTime.now();
		event.put("logtime", now.toString());
		event = datefilter.process(event);
		Assert.assertNull(event.get("tags"));
		Assert.assertEquals(event.get("@timestamp").toString(), now.toString());

		// Message Not match format
		event.put("logtime", "abcd");
		event = datefilter.process(event);
		Assert.assertEquals(((ArrayList) event.get("tags")).get(0), "datefail");

		// Message Not match format , NOT add tag
		c = "src: logtime\n" + "formats:\n"
				+ "  - 'ISO8601'\n  - 'YYYY/MM/dd HH:mm.ss.SSS Z'\n"
				+ "tag_on_failure: null\n" + "remove_fields: ['logtime']";
		yaml = new Yaml();
		config = (Map) yaml.load(c);

		datefilter = new Date(config);
		event = new HashMap();
		event.put("logtime", "abcd");
		event = datefilter.process(event);
		Assert.assertNull(event.get("tags"));
	}
}
