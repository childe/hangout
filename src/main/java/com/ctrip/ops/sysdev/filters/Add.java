package com.ctrip.ops.sysdev.filters;

import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ArrayBlockingQueue;

import scala.Tuple2;

import org.apache.log4j.Logger;
import com.ctrip.ops.sysdev.render.FreeMarkerRender;
import com.ctrip.ops.sysdev.render.TemplateRender;

public class Add extends BaseFilter {
	private static final Logger logger = Logger.getLogger(Add.class.getName());

	public Add(Map config) {
		super(config);
	}

	private Map<String, TemplateRender> f;

	protected void prepare() {
		f = new HashMap<String, TemplateRender>();

		Map<String, String> fields = (Map<String, String>) config.get("fields");
		Iterator<Entry<String, String>> it = fields.entrySet().iterator();
		while (it.hasNext()) {
			Map.Entry<String, String> entry = it.next();

			String field = entry.getKey();
			String value = entry.getValue();

			try {
				this.f.put(field, new FreeMarkerRender(value, value));
			} catch (IOException e) {
				logger.fatal(e.getMessage());
				System.exit(1);
			}
		}
	};

	@Override
	protected Map filter(final Map event) {
		Iterator<Entry<String, TemplateRender>> it = f.entrySet().iterator();
		while (it.hasNext()) {
			Map.Entry<String, TemplateRender> entry = it.next();

			String field = entry.getKey();
			TemplateRender render = entry.getValue();
			event.put(field, render.render(event));
		}
		return event;
	}
}
