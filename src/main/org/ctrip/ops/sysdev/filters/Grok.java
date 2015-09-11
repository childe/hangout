package org.ctrip.ops.sysdev.filters;

import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;

public class Grok extends BaseFilter {

	public Grok(Map config, ArrayBlockingQueue preQueue) {
		super(config, preQueue);
	}

	@Override
	protected void filter(Object event) {
	};

}
