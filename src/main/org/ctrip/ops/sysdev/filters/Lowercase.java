package org.ctrip.ops.sysdev.filters;

import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;

import com.hubspot.jinjava.Jinjava;

public class Lowercase extends BaseFilter {
	public Lowercase(Map config, ArrayBlockingQueue inputQueue) {
		super(config, inputQueue);
	}

	private String src;

	protected void prepare() {
		this.src = (String) config.get("src");
	};

	@Override
	protected void filter(Map event) {
		if (event.containsKey(this.src)) {
			event.put(this.src, ((String)event.get(src)).toLowerCase());
		}
	}
}
