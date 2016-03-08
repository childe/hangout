package com.ctrip.ops.sysdev.filters;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import scala.Tuple3;

import org.apache.log4j.Logger;
import com.ctrip.ops.sysdev.converter.BooleanConverter;
import com.ctrip.ops.sysdev.converter.Converter;
import com.ctrip.ops.sysdev.converter.DoubleConverter;
import com.ctrip.ops.sysdev.converter.FloatConverter;
import com.ctrip.ops.sysdev.converter.IntegerConverter;
import com.ctrip.ops.sysdev.converter.LongConverter;
import com.ctrip.ops.sysdev.converter.StringConverter;

public class Convert extends BaseFilter {
	private static final Logger logger = Logger.getLogger(Convert.class
			.getName());

	private Map<String, Tuple3> f;

	public Convert(Map config) {
		super(config);
	}

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
		boolean success = true;
		while (it.hasNext()) {
			Map.Entry<String, Tuple3> entry = it.next();
			String field = entry.getKey();
			Tuple3 t3 = entry.getValue();
			try {
				event.put(field,
						((Converter) t3._1()).convert(event.get(field)));
			} catch (Exception e) {
				success = false;
				if ((boolean) t3._2()) {
					event.remove(field);
				} else {
					if (t3._3() != null) {
						event.put(field, t3._3());
					}
				}
			}
		}

		this.postProcess(event, success);

		return event;
	}
}
