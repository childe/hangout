package org.ctrip.ops.sysdev.outputs;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;

public class BaseOutput {
	protected Map configs;
	protected List<ArrayBlockingQueue> preQueues;

	public BaseOutput(Map config, List<ArrayBlockingQueue> preQueues) {
		this.configs = configs;
		this.preQueues = preQueues;
	}

	public void emit() {
	};
}
