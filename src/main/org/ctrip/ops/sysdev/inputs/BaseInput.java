package org.ctrip.ops.sysdev.inputs;

import java.util.Map;

public abstract class BaseInput {
	private Map config;

	public BaseInput(Map config) {
	}

	public abstract Map emit();
}
