package org.ctrip.ops.sysdev.filters;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;

import org.apache.log4j.Logger;
import org.ctrip.ops.sysdev.render.FreeMarkerRender;
import org.ctrip.ops.sysdev.render.TemplateRender;

public class BaseFilter implements Runnable {

	private static final Logger logger = Logger.getLogger(BaseFilter.class
			.getName());

	protected Map config;
	protected List<TemplateRender> IF;
	protected ArrayBlockingQueue inputQueue;
	protected ArrayBlockingQueue outputQueue;
	protected TemplateRender render;

	public BaseFilter(Map config, ArrayBlockingQueue inputQueue) {
		this.config = config;

		if (this.config.containsKey("if")) {
			IF = new ArrayList<TemplateRender>();
			for (String c : (List<String>) this.config.get("if")) {
				try {
					IF.add(new FreeMarkerRender(c, c));
				} catch (IOException e) {
					logger.fatal(e.getMessage());
					System.exit(1);
				}
			}
		} else {
			IF = null;
		}

		this.inputQueue = inputQueue;

		int queueSize = 20;
		if (this.config.containsKey("queueSize")) {
			queueSize = (int) this.config.get("queueSize");
		}

		this.outputQueue = new ArrayBlockingQueue(queueSize, false);

		this.prepare();
	}

	protected void prepare() {
	};

	public Map filter(Map event) {
		return null;
	};

	public void run() {
		try {
			while (true) {
				Map event = (Map) this.inputQueue.take();
				if (event != null) {
					boolean succuess = true;
					if (this.IF != null) {
						for (TemplateRender render : this.IF) {
							if (!render.render(event).equals("true")) {
								succuess = false;
								break;
							}
						}
					}
					if (succuess == true) {
						event = this.filter(event);
					}
					if (event != null) {
						this.outputQueue.put(event);
					}
				}
			}
		} catch (InterruptedException e) {
			logger.warn("put event to outMQ failed");
			logger.trace(e.getMessage());
		}
	}

	public ArrayBlockingQueue getOutputMQ() {
		return this.outputQueue;
	}
}
