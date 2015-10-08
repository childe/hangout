package org.ctrip.ops.sysdev.inputs;

import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;

public class BaseInput {
	protected Map<String, Object> config;
	protected ArrayBlockingQueue messageQueue;

	public BaseInput(Map config, ArrayBlockingQueue messageQueue) {
		this.config = config;
		this.messageQueue = messageQueue;
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
