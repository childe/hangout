package org.ctrip.ops.sysdev.outputs;

import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;

public class BaseOutput implements Runnable {
	protected Map configs;
	protected ArrayBlockingQueue inputQueue;

	public BaseOutput(Map config, ArrayBlockingQueue inputQueue) {
		this.configs = configs;
		this.inputQueue = inputQueue;
	}

	protected void prepare() {
	};

	public void run() {
		while (true) {
			Object event = this.inputQueue.poll();
			if (event != null) {
				this.emit(event);
			}
		}
	}

	public void emit(Object event) {
	};
}
