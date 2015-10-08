package org.ctrip.ops.sysdev.filters;

import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;

public class Lowercase extends BaseFilter {
	public Lowercase(Map config, ArrayBlockingQueue inputQueue) {
		super(config, inputQueue);
	}

	private ArrayList<String> fields;

	protected void prepare() {
		this.fields = (ArrayList<String>) config.get("fields");
	};

	@Override
	protected void filter(Map event) {
		for (String field : fields) {
			if (event.containsKey(field)) {
				event.put(field, ((String) event.get(field)).toLowerCase());
			}
		}
	}
}
