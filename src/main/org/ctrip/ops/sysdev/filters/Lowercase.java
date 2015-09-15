package org.ctrip.ops.sysdev.filters;

import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;

public class Lowercase extends BaseFilter {
	public Lowercase(Map config, ArrayBlockingQueue inputQueue) {
		super(config, inputQueue);
	}

	private String fields;

	protected void prepare() {
		this.fields = (String) config.get("fields");
	};

	@Override
	protected void filter(Map event) {
		if (event.containsKey(this.fields)) {
			event.put(this.fields, ((String) event.get(fields)).toLowerCase());
		}
	}
}
