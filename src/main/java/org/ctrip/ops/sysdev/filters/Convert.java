package org.ctrip.ops.sysdev.filters;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.log4j.Logger;
import org.ctrip.ops.sysdev.converter.BooleanConverter;
import org.ctrip.ops.sysdev.converter.Converter;
import org.ctrip.ops.sysdev.converter.DoubleConverter;
import org.ctrip.ops.sysdev.converter.FloatConverter;
import org.ctrip.ops.sysdev.converter.IntegerConverter;
import org.ctrip.ops.sysdev.converter.LongConverter;
import org.ctrip.ops.sysdev.converter.StringConverter;

public class Convert extends BaseFilter {
	private static final Logger logger = Logger.getLogger(Convert.class
			.getName());

	public Convert(Map config) {
		super(config);
	}

	private Map<String, Converter> f;
	private String tagOnFailure;

	protected void prepare() {

		if (config.containsKey("tag_on_failure")) {
			this.tagOnFailure = (String) config.get("tag_on_failure");
		} else {
			this.tagOnFailure = "convertfail";
		}

		f = new HashMap<String, Converter>();
		Map<String, String> fields = (Map<String, String>) config.get("fields");
		Iterator<Entry<String, String>> it = fields.entrySet().iterator();
		while (it.hasNext()) {
			Map.Entry<String, String> entry = it.next();
			String field = entry.getKey();
			String value = entry.getValue();
			if (value.equalsIgnoreCase("long")) {
				f.put(field, new LongConverter());
			} else if (value.equalsIgnoreCase("integer")) {
				f.put(field, new IntegerConverter());
			} else if (value.equalsIgnoreCase("double")) {
				f.put(field, new DoubleConverter());
			} else if (value.equalsIgnoreCase("float")) {
				f.put(field, new FloatConverter());
			} else if (value.equalsIgnoreCase("string")) {
				f.put(field, new StringConverter());
			} else if (value.equalsIgnoreCase("boolean")) {
				f.put(field, new BooleanConverter());
			}
		}
	};

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	protected Map filter(final Map event) {
		Iterator<Entry<String, Converter>> it = f.entrySet().iterator();
		while (it.hasNext()) {
			Map.Entry<String, Converter> entry = it.next();
			String field = entry.getKey();
			Converter converter = entry.getValue();
			try {
				event.put(field, converter.convert(event.get(field)));
			} catch (Exception e) {
				if (!event.containsKey("tags")) {
					event.put(
							"tags",
							new ArrayList<String>(Arrays
									.asList(this.tagOnFailure)));
				} else {
					Object tags = event.get("tags");
					if (tags.getClass() == ArrayList.class
							&& ((ArrayList) tags).indexOf(this.tagOnFailure) == -1) {
						((ArrayList) tags).add(this.tagOnFailure);
					}
				}
			}
		}
		return event;
	}
}
