package org.ctrip.ops.sysdev.filters;

import org.apache.log4j.Logger;
import org.json.simple.JSONValue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class Json extends BaseFilter {
	private static final Logger logger = Logger.getLogger(Json.class.getName());

	public Json(Map config) {
		super(config);
	}

	private String tagOnFailure;
	private String field;
	private ArrayList<String> removeFields;

	protected void prepare() {
		if (!config.containsKey("field")) {
			logger.error("no field configured in Json");
			System.exit(1);
		}
		this.field = (String) config.get("field");

		this.removeFields = (ArrayList<String>) this.config
				.get("remove_fields");

		if (config.containsKey("tag_on_failure")) {
			this.tagOnFailure = (String) config.get("tag_on_failure");
		} else {
			this.tagOnFailure = "jsonfail";
		}
	};

	@Override
	protected Map filter(final Map event) {
		Map<String, Object> obj = null;
		if (event.containsKey(this.field)) {
			obj = (HashMap<String, Object>) JSONValue.parse((String) event
					.get(field));

		}

		if (obj == null) {
			if (!event.containsKey("tags")) {
				event.put("tags",
						new ArrayList<String>(Arrays.asList(this.tagOnFailure)));
			} else {
				Object tags = event.get("tags");
				if (tags.getClass() == ArrayList.class
						&& ((ArrayList) tags).indexOf(this.tagOnFailure) == -1) {
					((ArrayList) tags).add(this.tagOnFailure);
				}
			}
		} else {
			event.putAll(obj);
			if (this.removeFields != null) {
				for (String f : this.removeFields) {
					event.remove(f);
				}
			}
		}
		return event;
	}
}
