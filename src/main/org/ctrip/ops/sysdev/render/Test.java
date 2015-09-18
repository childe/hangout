package org.ctrip.ops.sysdev.render;

import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import freemarker.template.*;

import com.hubspot.jinjava.Jinjava;

public class Test {

	public static void main(String[] args) throws IOException,
			TemplateException {
		Jinjava jinjava = new Jinjava();
		long s = System.currentTimeMillis();
		ArrayList a = new ArrayList();
		final Map<String, Object> context = new HashMap();
		context.put(
				"message",
				"These built-ins you can only use with the loop variable of the list and items directives (and of the deprecated foreach directive). Some explanation of that follows (loopVar?index returns the 0-based index in the listable value we iterate through)");
		context.put("name", "jia.liu");
		context.put("@timestamp", 1442281327000L);
		context.put("array", new ArrayList() {
			{
				add("123");
				add("abc");
			}
		});

		Map event = new HashMap() {
			{
				put("event", context);
			}
		};

		for (int i = 0; i < 10000; i++) {
			jinjava.render("{{\"-\" in message}}", context);
		}
		System.out.println(System.currentTimeMillis() - s);

		System.out.println(jinjava.render(
				"{{\"-\" in message && \"X\" in message}}", context));

		System.out.println(jinjava.render(
				"{{\"-\" in message || \"X\" in message}}", context));

		System.out
				.println(jinjava.render("{{\"Ja-red\" == message}}", context));

		System.out
				.println(jinjava.render("{{\"Ja-red\" != message}}", context));

		System.out.println(jinjava.render("{{!message}}", context));
		System.out.println(jinjava.render("{{!name}}", context));

		System.out.println(jinjava.render("message is: {{\"@timestamp\"}}",
				context));

		System.out
				.println(jinjava.render(
						"{% set names = message|split('-', 4) %}{{names[0]}}",
						context));
	

		// jinjava
		s = System.currentTimeMillis();
		for (int i = 0; i < 10000; i++) {
			String rst = jinjava
					.render("{% if 'liu' in name %}{{name}} {%endif%}{% set a=message|split(' ') %}{{a[0]}}",
							context);
			// System.out.println(rst);

		}

		System.out.println("10000 jinjava: ");
		System.out.println(System.currentTimeMillis() - s);

		// freemarker
		Configuration cfg = new Configuration(Configuration.VERSION_2_3_22);
		StringWriter sw = new StringWriter();
		Template t = new Template(
				"template_name",
				"<#if name?contains('liu')>${@timestamp?c} </#if><#assign x=message?split(' ')>${x[0]}",
				cfg);
		s = System.currentTimeMillis();
		for (int i = 0; i < 10000; i++) {
			t.process(context, sw);
			// System.out.println(sw.toString());
		}
		System.out.println("10000 freemarker: ");
		System.out.println(System.currentTimeMillis() - s);

		
		t = new Template("",
				"i am ${name}",
				cfg);
		sw = new StringWriter();
		t.process(context, sw);
		try {
			sw.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println(sw.toString());
		
		t = new Template("",
				"i am ${testabcd}",
				cfg);
		sw = new StringWriter();
		t.process(context, sw);
		try {
			sw.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println(sw.toString());
	}

}
