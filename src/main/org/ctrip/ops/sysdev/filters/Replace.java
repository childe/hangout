package org.ctrip.ops.sysdev.filters;

import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;

import com.hubspot.jinjava.Jinjava;

public class Replace extends BaseFilter {
	public Replace(Map config, ArrayBlockingQueue inputQueue) {
		super(config, inputQueue);
	}

	private String src;
	private String value;
	private Jinjava jinjava;

	protected void prepare() {
		this.src = (String) config.get("src");
		this.value = (String) config.get("value");
		this.jinjava = new Jinjava();
	};

	@Override
	protected void filter(Map event) {
		if (event.containsKey(this.src)) {
			event.put(this.src, jinjava.render(this.value, event));
		}
	}
}
