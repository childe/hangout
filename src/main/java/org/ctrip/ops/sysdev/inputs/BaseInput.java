package org.ctrip.ops.sysdev.inputs;

import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;

import org.ctrip.ops.sysdev.filters.BaseFilter;
import org.ctrip.ops.sysdev.outputs.BaseOutput;

public class BaseInput {
	protected Map<String, Object> config;
	protected ArrayBlockingQueue messageQueue;
	protected BaseFilter[] filterProcessors;
	ArrayList<Map> outputs;

	public BaseInput(Map config, ArrayBlockingQueue messageQueue,
			BaseFilter[] filterProcessors, ArrayList<Map> outputs) {
		this.config = config;
		this.messageQueue = messageQueue;
		this.filterProcessors = filterProcessors.clone();
		this.outputs = outputs;
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
