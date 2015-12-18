package org.ctrip.ops.sysdev.filters;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import scala.Tuple3;

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

	private Map<String, Tuple3> f;
	private String tagOnFailure;

	protected void prepare() {

		if (config.containsKey("tag_on_failure")) {
			this.tagOnFailure = (String) config.get("tag_on_failure");
		} else {
			this.tagOnFailure = "convertfail";
		}

		f = new HashMap<String, Tuple3>();
		Map<String, Map> fields = (Map<String, Map>) config.get("fields");
		Iterator<Entry<String, Map>> it = fields.entrySet().iterator();
		while (it.hasNext()) {
			Map.Entry<String, Map> entry = it.next();
			String field = entry.getKey();
			Map value = entry.getValue();
			Converter converter = null;
			Boolean remove_if_fail = false;
			Object setto_if_fail = null;
			if (((String) value.get("to")).equalsIgnoreCase("long")) {
				converter = new LongConverter();
			} else if (((String) value.get("to")).equalsIgnoreCase("integer")) {
				converter = new IntegerConverter();
			} else if (((String) value.get("to")).equalsIgnoreCase("double")) {
				converter = new DoubleConverter();
			} else if (((String) value.get("to")).equalsIgnoreCase("float")) {
				converter = new FloatConverter();
			} else if (((String) value.get("to")).equalsIgnoreCase("string")) {
				converter = new StringConverter();
			} else if (((String) value.get("to")).equalsIgnoreCase("boolean")) {
				converter = new BooleanConverter();
			}

			if (value.containsKey("remove_if_fail")) {
				remove_if_fail = (Boolean) value.get("remove_if_fail");
			}

			if (value.containsKey("setto_if_fail")) {
				setto_if_fail = value.get("setto_if_fail");
			}

			f.put(field, new Tuple3(converter, remove_if_fail, setto_if_fail));
		}
	};

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	protected Map filter(final Map event) {
		Iterator<Entry<String, Tuple3>> it = f.entrySet().iterator();
		while (it.hasNext()) {
			Map.Entry<String, Tuple3> entry = it.next();
			String field = entry.getKey();
			Tuple3 t3 = entry.getValue();
			try {
				event.put(field,
						((Converter) t3._1()).convert(event.get(field)));
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

				if ((boolean) t3._2()) {
					event.remove(field);
				} else {
					if (t3._3() != null) {
						event.put(field, t3._3());
					}
				}
			}
		}
		return event;
	}
}
