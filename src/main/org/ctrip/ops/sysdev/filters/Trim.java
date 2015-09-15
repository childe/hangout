package org.ctrip.ops.sysdev.filters;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;

import org.ctrip.ops.sysdev.utils.jinfilter.JinManager;

import com.hubspot.jinjava.Jinjava;

public class Trim extends BaseFilter {
	public Trim(Map config, ArrayBlockingQueue inputQueue) {
		super(config, inputQueue);
	}

	private ArrayList<String> target;

	protected void prepare() {
		this.target = (ArrayList<String>) config.get("target");
	};

	@Override
	protected void filter(final Map event) {
		for (String field : target) {
			if (event.containsKey(field)) {
				event.put(field, ((String) event.remove(field)).trim());
			}
		}
	}
}
