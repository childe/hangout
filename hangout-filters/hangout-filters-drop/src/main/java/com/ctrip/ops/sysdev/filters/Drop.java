package com.ctrip.ops.sysdev.filters;

import com.ctrip.ops.sysdev.baseplugin.BaseFilter;

import java.util.Map;

public class Drop extends BaseFilter {
	public Drop(Map config) {
		super(config);
	}

    protected Map filter(Map event) {
		return null;
	}
}
