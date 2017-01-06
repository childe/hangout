package com.ctrip.ops.sysdev.test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import com.ctrip.ops.sysdev.filters.KV;
import org.testng.Assert;
import org.testng.annotations.Test;
import org.yaml.snakeyaml.Yaml;

public class TestKV {
	@Test
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public void testGrok() {
		// General Test
		String c = String.format("%s\n%s\n%s\n%s\n%s\n%s\n%s\n%s\n%s",
		"source: msg",
		"field_split: ' '",
		"value_split: '='",
		"trim: '\t\"'",
		"trimkey: '\"'",
		"include_keys: ['a','b','xyz','12']",
		"exclude_keys: ['b','c']",
		"tag_on_failure: 'KVfail'",
		"remove_fields: ['msg']");


		Yaml yaml = new Yaml();
		Map config = (Map) yaml.load(c);
		Assert.assertNotNull(config);

		KV KVfilter = new KV(config);

		Map event = new HashMap();
		event.put("msg", "a=aaa b=bbb c=ccc xyz=\"\txyzxyz\t\" 12=\"1221\" d=ddd");

		event = KVfilter.process(event);
		Assert.assertEquals(event.get("a"), "aaa");
		Assert.assertEquals(event.get("12"), "1221");
		Assert.assertEquals(event.get("xyz"), "xyzxyz");
		Assert.assertNull(event.get("b"));
		Assert.assertNull(event.get("c"));
		Assert.assertNull(event.get("d"));
		Assert.assertNull(event.get("tags"));
		Assert.assertNull(event.get("msg"));

		// Put object to target
		c = String.format("%s\n%s\n%s\n%s\n%s\n%s\n%s\n%s\n%s\n%s",
		"source: msg",
		"target: kv",
		"field_split: ' '",
		"value_split: '='",
		"trim: '\t\"'",
		"trimkey: '\"'",
		"include_keys: ['a','b','xyz','12']",
		"exclude_keys: ['b','c']",
		"tag_on_failure: 'KVfail'",
		"remove_fields: ['msg']");


		yaml = new Yaml();
		config = (Map) yaml.load(c);
		Assert.assertNotNull(config);

		KVfilter = new KV(config);

		event = new HashMap();
		event.put("msg", "a=aaa b=bbb c=ccc xyz=\"\txyzxyz\t\" 12=\"1221\" d=ddd");

		event = KVfilter.process(event);
		Assert.assertEquals(((Map)event.get("kv")).get("a"), "aaa");
		Assert.assertEquals(((Map)event.get("kv")).get("12"), "1221");
		Assert.assertEquals(((Map)event.get("kv")).get("xyz"), "xyzxyz");
		Assert.assertNull(event.get("a"));
		Assert.assertNull(event.get("b"));
		Assert.assertNull(event.get("c"));
		Assert.assertNull(event.get("d"));
		Assert.assertNull(event.get("tags"));
		Assert.assertNull(event.get("msg"));

		// include all
		c = String.format("%s\n%s\n%s\n%s\n%s\n%s\n%s\n%s\n%s",
		"source: msg",
		"target: kv",
		"field_split: ' '",
		"value_split: '='",
		"trim: '\t\"'",
		"trimkey: '\"'",
		"exclude_keys: ['b','c']",
		"tag_on_failure: 'KVfail'",
		"remove_fields: ['msg']");


		yaml = new Yaml();
		config = (Map) yaml.load(c);
		Assert.assertNotNull(config);

		KVfilter = new KV(config);

		event = new HashMap();
		event.put("msg", "a=aaa b=bbb c=ccc xyz=\"\txyzxyz\t\" 12=\"1221\" d=ddd");

		event = KVfilter.process(event);
		Assert.assertEquals(((Map)event.get("kv")).get("a"), "aaa");
		Assert.assertEquals(((Map)event.get("kv")).get("d"), "ddd");
		Assert.assertEquals(((Map)event.get("kv")).get("12"), "1221");
		Assert.assertEquals(((Map)event.get("kv")).get("xyz"), "xyzxyz");
		Assert.assertNull(event.get("a"));
		Assert.assertNull(event.get("b"));
		Assert.assertNull(event.get("c"));
		Assert.assertNull(event.get("tags"));
		Assert.assertNull(event.get("msg"));

		// Fail
		c = String.format("%s\n%s\n%s\n%s\n%s\n%s\n%s\n%s\n%s",
		"source: msg",
		"target: kv",
		"field_split: ' '",
		"value_split: '='",
		"trim: '\t\"'",
		"trimkey: '\"'",
		"exclude_keys: ['b','c']",
		"tag_on_failure: 'KVfail'",
		"remove_fields: ['msg']");


		yaml = new Yaml();
		config = (Map) yaml.load(c);
		Assert.assertNotNull(config);

		KVfilter = new KV(config);

		event = new HashMap();
		event.put("msg", "a=aaa b=bbb c=ccc xyz=\"\txyzxyz\t\" 12=12=12 dddd 13=1313");

		event = KVfilter.process(event);
		Assert.assertEquals(((Map)event.get("kv")).get("a"), "aaa");
		Assert.assertEquals(((Map)event.get("kv")).get("12"), "12=12");
		Assert.assertEquals(((Map)event.get("kv")).get("13"), "1313");
		Assert.assertNull(((Map)event.get("kv")).get("d"));
		Assert.assertEquals(((Map)event.get("kv")).get("xyz"), "xyzxyz");
		Assert.assertNull(event.get("a"));
		Assert.assertNull(event.get("b"));
		Assert.assertNull(event.get("c"));
		Assert.assertEquals(((ArrayList) event.get("tags")).get(0), "KVfail");
		Assert.assertNotNull(event.get("msg"));

		// Fail, No tags
		c = String.format("%s\n%s\n%s\n%s\n%s\n%s\n%s\n%s\n%s",
		"source: msg",
		"target: kv",
		"field_split: ' '",
		"value_split: '='",
		"trim: '\t\"'",
		"trimkey: '\"'",
		"exclude_keys: ['b','c']",
		"tag_on_failure: null",
		"remove_fields: ['msg']");


		yaml = new Yaml();
		config = (Map) yaml.load(c);
		Assert.assertNotNull(config);

		KVfilter = new KV(config);

		event = new HashMap();
		event.put("msg", "a=aaa b=bbb c=ccc xyz=\"\txyzxyz\t\" 12=12=12 dddd 13=1313");

		event = KVfilter.process(event);
		Assert.assertEquals(((Map)event.get("kv")).get("a"), "aaa");
		Assert.assertEquals(((Map)event.get("kv")).get("12"), "12=12");
		Assert.assertEquals(((Map)event.get("kv")).get("13"), "1313");
		Assert.assertNull(((Map)event.get("kv")).get("d"));
		Assert.assertEquals(((Map)event.get("kv")).get("xyz"), "xyzxyz");
		Assert.assertNull(event.get("a"));
		Assert.assertNull(event.get("b"));
		Assert.assertNull(event.get("c"));
		Assert.assertNull(event.get("tags"));
		Assert.assertNotNull(event.get("msg"));
	}
}
