package org.ctrip.ops.sysdev.outputs;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;

import org.apache.log4j.Logger;
import org.ctrip.ops.sysdev.filters.BaseFilter;
import org.ctrip.ops.sysdev.render.FreeMarkerRender;
import org.ctrip.ops.sysdev.render.JinjavaRender;
import org.ctrip.ops.sysdev.render.TemplateRender;

public class BaseOutput {
	private static final Logger logger = Logger.getLogger(BaseOutput.class
			.getName());

	protected Map config;
	protected List<TemplateRender> IF;

	public BaseOutput(Map config) {
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

		this.prepare();
	}

	protected void prepare() {
	};

	public void emit(Map event) {
	};
}
