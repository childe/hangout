package org.ctrip.ops.sysdev.outputs;

import java.util.List;
import java.util.Map;

import org.ctrip.ops.sysdev.utils.jinfilter.JinManager;

import com.hubspot.jinjava.Jinjava;

public class BaseOutput {
	protected Map config;
	protected List<String> IF;
	protected Jinjava jinjava = JinManager.jinjava;

	public BaseOutput(Map config) {
		this.config = config;

		this.IF = (List<String>) this.config.get("if");

		this.prepare();
	}

	protected void prepare() {
	};

	public void process(Map event) {
		if (this.IF != null) {
			for (String c : this.IF) {
				if (this.jinjava.render(c, event).equals("false")) {
					continue;
				}
			}
		}
		this.emit(event);
	}

	public void emit(Map event) {
	};
}
