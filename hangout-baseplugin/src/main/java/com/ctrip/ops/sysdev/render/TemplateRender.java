package com.ctrip.ops.sysdev.render;

import java.util.Map;

public abstract class TemplateRender {

	public abstract String render(Map event);

	public abstract String render(String template, Map event);
}
