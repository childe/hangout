package com.ctrip.ops.sysdev.filters;

import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;
import org.json.simple.JSONValue;

public class Json extends BaseFilter {
	private static final Logger logger = Logger.getLogger(Json.class.getName());

	public Json(Map config) {
		super(config);
	}

	private String field, target;

	protected void prepare() {
		if (!config.containsKey("field")) {
			logger.error("no field configured in Json");
			System.exit(1);
		}
		this.field = (String) config.get("field");

		this.target = (String) config.get("target");

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

		boolean success = obj != null;

		if (obj != null) {
			if (this.target == null || this.target.equals("")) {
				event.putAll(obj);
			} else {
				event.put(this.target, obj);
			}
		}

		this.postProcess(event, success);
		return event;
	}
}
