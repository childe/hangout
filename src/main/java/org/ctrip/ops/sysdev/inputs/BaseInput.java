package org.ctrip.ops.sysdev.inputs;

import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;

import org.ctrip.ops.sysdev.filters.BaseFilter;

public class BaseInput {
	protected Map<String, Object> config;
	protected ArrayBlockingQueue messageQueue;
	protected BaseFilter[] filterProcessors;

	public BaseInput(Map config, ArrayBlockingQueue messageQueue,
			BaseFilter[] filterProcessors) {
		this.config = config;
		this.messageQueue = messageQueue;
		this.filterProcessors = filterProcessors.clone();
		this.prepare();
	}

	protected void prepare() {
	};

	public void emit() {

	};

	public ArrayBlockingQueue getMessageQueue() {
		return this.messageQueue;
	};
}
