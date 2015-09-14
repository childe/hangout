package org.ctrip.ops.sysdev.filters;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;

import org.apache.log4j.Logger;

import com.hubspot.jinjava.Jinjava;

public class BaseFilter implements Runnable {

	private static final Logger logger = Logger.getLogger(BaseFilter.class
			.getName());

	protected Map config;
	protected List<String> IF;
	protected ArrayBlockingQueue inputQueue;
	protected ArrayBlockingQueue outputQueue;
	protected Jinjava jinjava;

	public BaseFilter(Map config, ArrayBlockingQueue inputQueue) {
		this.config = config;
		this.IF = (List<String>) this.config.get("if");
		this.jinjava = new Jinjava();

		this.inputQueue = inputQueue;

		int queueSize = 1000;
		if (this.config.containsKey("queueSize")) {
			queueSize = (int) this.config.get("queueSize");
		}

		this.outputQueue = new ArrayBlockingQueue(queueSize, false);

		this.prepare();
	}

	protected void prepare() {
	};

	protected void filter(Map event) {
	};

	public void run() {
		while (true) {
			Map event = (Map) this.inputQueue.poll();
			if (event != null) {

				if (this.IF != null) {
					for (String c : this.IF) {
						System.out.println(this.jinjava.render(c, event));
					}
				}

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

	public static void main(String[] args) {
		Jinjava jinjava = new Jinjava();
		long s = System.currentTimeMillis();
		Map<String, Object> context = new HashMap();
		context.put("message", "Ja-red");

		for (int i = 0; i < 10000; i++) {
			jinjava.render("{{\"-\" in message}}", context);
		}
		System.out.println(System.currentTimeMillis() - s);
	}
}
