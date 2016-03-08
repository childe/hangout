package com.ctrip.ops.sysdev;

import java.util.HashMap;
import java.util.Map;

import org.testng.Assert;
import org.testng.annotations.Test;
import org.json.simple.JSONValue;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

public class TestJsonSimple {
	@Test
	public void testtoJSONString() {
		HashMap<String, Object> event = new HashMap<String, Object>();
		DateTimeFormatter formatter = DateTimeFormat
				.forPattern("YYYY-MM-dd HH:mm:ss.SSS");
		DateTime now = formatter.parseDateTime("2015-12-15 16:23:45.001");
		event.put("@timestamp", now);
		String s = JSONValue.toJSONString(event);
		Assert.assertEquals(
				"{\"@timestamp\":\"2015-12-15T16:23:45.001+08:00\"}", s);
	}

	public static void main(String[] args) {
		HashMap<String, Object> event = new HashMap<String, Object>();
		DateTimeFormatter formatter = DateTimeFormat
				.forPattern("YYYY-MM-dd HH:mm:ss.SSS");
		DateTime now = formatter.parseDateTime("2015-12-15 16:23:45.001");
		event.put("@timestamp", now);
		String s = JSONValue.toJSONString(event);
		System.out.println(s);
	}
}
