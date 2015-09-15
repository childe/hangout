package org.ctrip.ops.sysdev.filters;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;

import org.ctrip.ops.sysdev.utils.jinfilter.JinManager;

import com.hubspot.jinjava.Jinjava;

public class Add extends BaseFilter {
	public Add(Map config, ArrayBlockingQueue inputQueue) {
		super(config, inputQueue);
	}

	private String target;
	private String value;

	protected void prepare() {
		this.target = (String) config.get("target");
		this.value = (String) config.get("value");
	};

	@Override
	protected void filter(final Map event) {
		event.put(this.target, jinjava.render(this.value, new HashMap() {
			{
				put("event", event);
			}
		}));

	}
}
