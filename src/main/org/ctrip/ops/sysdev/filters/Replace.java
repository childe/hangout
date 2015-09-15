package org.ctrip.ops.sysdev.filters;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;

import org.ctrip.ops.sysdev.utils.jinfilter.JinManager;

import com.hubspot.jinjava.Jinjava;

public class Replace extends BaseFilter {
	public Replace(Map config, ArrayBlockingQueue inputQueue) {
		super(config, inputQueue);
	}

	private String src;
	private String value;

	protected void prepare() {
		this.src = (String) config.get("src");
		this.value = (String) config.get("value");
	};

	@Override
	protected void filter(final Map event) {
		if (event.containsKey(this.src)) {
			event.put(this.src, jinjava.render(this.value, new HashMap(){{put("event",event);}}));
		}
	}
}
