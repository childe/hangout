package com.ctrip.ops.sysdev.filters;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;

import org.apache.log4j.Logger;
import com.ctrip.ops.sysdev.render.FreeMarkerRender;

public class Replace extends BaseFilter {
	private static final Logger logger = Logger.getLogger(Replace.class
			.getName());

	public Replace(Map config) {
		super(config);
	}

	private String src;
	private String value;

	protected void prepare() {
		this.src = (String) config.get("src");
		this.value = (String) config.get("value");

		try {
			this.render = new FreeMarkerRender(value, value);
		} catch (IOException e) {
			logger.fatal(e.getMessage());
			System.exit(1);
		}
	};

	@Override
	protected Map filter(final Map event) {
		if (event.containsKey(this.src)) {
			event.put(this.src, render.render(this.value, new HashMap() {
				{
					put("event", event);
				}
			}));
		}
		return event;
	}
}
