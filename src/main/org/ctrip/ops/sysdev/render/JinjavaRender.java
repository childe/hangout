package org.ctrip.ops.sysdev.render;

import java.util.Map;

import org.apache.log4j.Logger;
import org.ctrip.ops.sysdev.filters.BaseFilter;
import org.ctrip.ops.sysdev.utils.jinfilter.JinManager;

import com.hubspot.jinjava.Jinjava;

public class JinjavaRender extends TemplateRender {
	private static final Logger logger = Logger.getLogger(JinjavaRender.class
			.getName());

	private Jinjava jinjava = JinManager.jinjava;

	public String render(Map event) {
		logger.error("JinjavaRender.render(Map) could not be used. shoule pass template when render");
		return null;
	}

	@Override
	public String render(String template, Map event) {
		return this.jinjava.render(template, event);
	}
}
