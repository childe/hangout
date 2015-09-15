package org.ctrip.ops.sysdev.filters;

import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;

public class Uppercase extends BaseFilter {
	public Uppercase(Map config, ArrayBlockingQueue inputQueue) {
		super(config, inputQueue);
	}

	private String src;

	protected void prepare() {
		this.src = (String) config.get("src");
	};

	@Override
	protected void filter(Map event) {
		if (event.containsKey(this.src)) {
			event.put(this.src, ((String) event.get(src)).toUpperCase());
		}
	}
}
