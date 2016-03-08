package com.ctrip.ops.sysdev.filters;

import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;

public class Drop extends BaseFilter {
	public Drop(Map config) {
		super(config);
	}

	protected void prepare() {
	};

	protected Map filter(Map event) {
		return null;
	}
}
