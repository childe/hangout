package org.ctrip.ops.sysdev.outputs;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;

import com.hubspot.jinjava.Jinjava;

public class BaseOutput implements Runnable {
	protected Map config;
	protected List<String> IF;
	protected Jinjava jinjava;
	protected ArrayBlockingQueue inputQueue;

	public BaseOutput(Map config, ArrayBlockingQueue inputQueue) {
		this.config = config;

		this.IF = (List<String>) this.config.get("if");
		this.jinjava = new Jinjava();

		this.inputQueue = inputQueue;
	}

	protected void prepare() {
	};

	public void run() {
		while (true) {
			Map event = (Map) this.inputQueue.poll();
			if (event != null) {
				if (this.IF != null) {
					for (String c : this.IF) {
						if (this.jinjava.render(c, event).equals("false")) {
							continue;
						}
					}
				}
				this.emit(event);
			}
		}
	}

	public void emit(Object event) {
	};
}
