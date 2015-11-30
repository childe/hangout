package org.ctrip.ops.sysdev.inputs;

import java.util.ArrayList;
import java.util.Map;

import org.ctrip.ops.sysdev.filters.BaseFilter;

public abstract class BaseInput {
	protected Map<String, Object> config;
	protected BaseFilter[] filterProcessors;
	ArrayList<Map> outputs;

	public BaseInput(Map config, BaseFilter[] filterProcessors,
			ArrayList<Map> outputs) {
		this.config = config;
		this.filterProcessors = filterProcessors.clone();
		this.outputs = outputs;
		this.prepare();
	}

	protected abstract void prepare();

	public abstract void emit();
}
