package org.ctrip.ops.sysdev.outputs;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;

public abstract class BaseOutput {
	public BaseOutput(Map config, List<ArrayBlockingQueue> preQueues) {
	}

	public abstract void emit();
}
