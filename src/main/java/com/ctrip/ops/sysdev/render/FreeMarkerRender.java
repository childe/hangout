package com.ctrip.ops.sysdev.render;

import java.io.IOException;
import java.io.StringWriter;
import java.util.Map;

import org.apache.log4j.Logger;
import com.ctrip.ops.sysdev.filters.BaseFilter;

import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;

public class FreeMarkerRender extends TemplateRender {
	private static final Logger logger = Logger.getLogger(BaseFilter.class
			.getName());

	private Template t;

	public FreeMarkerRender(String template, String templateName)
			throws IOException {
		Configuration cfg = new Configuration(Configuration.VERSION_2_3_22);
		this.t = new Template(templateName, template, cfg);
	}

	public FreeMarkerRender(String template) throws IOException {
		Configuration cfg = new Configuration(Configuration.VERSION_2_3_22);
		this.t = new Template("", template, cfg);
	}

	public String render(Map event) {
		StringWriter sw = new StringWriter();
		try {
			t.process(event, sw);
		} catch (Exception e) {
			logger.error(e.getMessage());
			logger.debug(event);
			return "";
		}
		try {
			sw.close();
		} catch (IOException e) {
			logger.error(e.getMessage());
		}
		return sw.toString();
	}

	@Override
	public String render(String template, Map event) {
		// actually it is just used to be compatible with jinjava
		return this.render(event);
	}
}
