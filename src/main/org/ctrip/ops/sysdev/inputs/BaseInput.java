package org.ctrip.ops.sysdev.inputs;

import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;

public class BaseInput {
	protected Map<String, Object> config;
	protected ArrayBlockingQueue messageQueue;

	public BaseInput(Map config) {
		this.config = config;
		this.prepare();
	}

	public void prepare() {

	};

	public void emit() {

	};

	public ArrayBlockingQueue getMessageQueue() {
		return this.messageQueue;
	};
}
