package main.org.ctrip.ops.sysdev.inputs;

import java.util.Map;

public abstract class BaseInput {
	private Map config;

	public BaseInput(Map config) {
		this.config = config;
	}

	public abstract Map emit();
}
