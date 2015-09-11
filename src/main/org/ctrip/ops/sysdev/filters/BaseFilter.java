package org.ctrip.ops.sysdev.filters;

import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;

import org.apache.log4j.Logger;

public class BaseFilter implements Runnable {

	private static final Logger logger = Logger.getLogger("BaseFilter");

	protected Map config;
	protected ArrayBlockingQueue inputQueue;
	protected ArrayBlockingQueue outputQueue;

	public BaseFilter(Map config, ArrayBlockingQueue inputQueue) {
		this.config = config;
		this.inputQueue = inputQueue;

		int queueSize = 1000;
		if (this.config.containsKey("queueSize")) {
			queueSize = (int) this.config.get("queueSize");
		}

		this.outputQueue = new ArrayBlockingQueue(queueSize, false);
		logger.error(this.outputQueue.hashCode());

		this.prepare();
	}

	protected void prepare() {
	};

	protected void filter(Object event) {
	};

	public void run() {
		while (true) {
			Object event = this.inputQueue.poll();
			if (event != null) {
				this.filter(event);
				try {
					this.outputQueue.put(event);
				} catch (InterruptedException e) {
					logger.warn("put event to outMQ failed");
					logger.trace(e.getMessage());
				}
			}
		}
	}

	public ArrayBlockingQueue getOutputMQ() {
		return this.outputQueue;
	}
}
