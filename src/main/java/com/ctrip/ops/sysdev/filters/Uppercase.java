package com.ctrip.ops.sysdev.filters;

import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;

public class Uppercase extends BaseFilter {
	public Uppercase(Map config) {
		super(config);
	}

	private ArrayList<String> fields;

	protected void prepare() {
		this.fields = (ArrayList<String>) config.get("fields");
	};

	@Override
	protected Map filter(Map event) {
		for (String field : fields) {
			if (event.containsKey(field)) {
				event.put(field, ((String) event.get(field)).toUpperCase());
			}
		}
		return event;
	}
}
