package org.ctrip.ops.sysdev.filters;

import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;

public class Drop extends BaseFilter {
	public Drop(Map config, ArrayBlockingQueue inputQueue) {
		super(config, inputQueue);
	}

	protected void prepare() {
	};

	public Map filter(Map event) {
		return null;
	}
}
