package org.ctrip.ops.sysdev.utils.jinfilter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import com.hubspot.jinjava.objects.date.PyishDate;
import com.hubspot.jinjava.interpret.Context;
import com.hubspot.jinjava.interpret.JinjavaInterpreter;
import com.hubspot.jinjava.lib.filter.Filter;

/*
 * input could be Unix time stamp in MS unit or ISO8601 string
 */
public class DateFormat implements Filter {

	@Override
	public String getName() {
		return "dateformat";
	}

	@Override
	public Object filter(Object arg0, JinjavaInterpreter arg1, String... arg2) {
		DateTime d = null;

		if (arg0 == null) {
			d = DateTime.now();
		} else if (arg0 instanceof Long) {
			d = new DateTime((long) arg0);
		} else if (arg0 instanceof PyishDate) {
			d = new DateTime(((PyishDate) arg0).toDateTime());
		} else if (arg0 instanceof DateTime) {
			d = new DateTime(((PyishDate) arg0).toDateTime());
		} else {
			d = DateTime.now();
		}

		DateTimeFormatter f = DateTimeFormat.forPattern(arg2[0]);
		if (arg2.length == 2) {
			f = f.withZone(DateTimeZone.forID(arg2[1]));
		}
		return d.toString(f);
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {

		final Map<String, Object> value = new HashMap<String, Object>() {
			{

				put("name", "jia.liu");
				put("age", 29);
				put("logtime", "1433238542142");

			}
		};

		final ArrayList<String> key = new ArrayList<String>() {
			{

				add("name");
				add("3");
			}
		};
		HashMap binding = new HashMap() {
			{
				put("event", new ArrayList(Arrays.asList(key, value)));
			}
		};

		Context cc = new Context(JinManager.c, binding);
		String template, s;

		template = "{{event[1][\"logtime\"]|dateformat(\"YYYY.MM.dd\")}}";
		s = JinManager.jinjava.render(template, cc);
		System.out.println(s);
	}
}
