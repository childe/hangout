package org.ctrip.ops.sysdev.inputs;

import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;

public abstract class BaseInput {
	public BaseInput(Map config) {
	}

	public abstract Map emit();

	public abstract ArrayBlockingQueue getMessageQueue();
}
