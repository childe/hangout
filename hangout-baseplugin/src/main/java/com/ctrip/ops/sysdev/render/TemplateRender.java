package com.ctrip.ops.sysdev.render;

import java.util.Map;

public interface TemplateRender{

	public String render(Map event);

	public default String render(String template, Map event){
	    return render(event);
	};
}
