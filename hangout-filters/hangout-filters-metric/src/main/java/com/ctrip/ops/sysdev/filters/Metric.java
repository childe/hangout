package com.ctrip.ops.sysdev.filters;

import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import com.ctrip.ops.sysdev.baseplugin.BaseFilter;

import org.apache.log4j.Logger;
import com.ctrip.ops.sysdev.render.FreeMarkerRender;
import com.ctrip.ops.sysdev.render.TemplateRender;

@SuppressWarnings("ALL")
public class Metric extends BaseFilter {
	private static final Logger logger = Logger.getLogger(Metric.class.getName());

	public Metric(Map config) {
		super(config);
	}

	protected void prepare() {
	}

	@Override
	protected Map filter(final Map event) {

		return event;
	}
}
