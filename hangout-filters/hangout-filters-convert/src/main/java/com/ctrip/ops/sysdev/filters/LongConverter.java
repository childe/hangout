package com.ctrip.ops.sysdev.filters;

import com.ctrip.ops.sysdev.converter.Converter;

public class LongConverter implements Converter {

	public Object convert(Object from) {
		if (from instanceof Number) {
			return ((Number) from).longValue();
		} else if (from instanceof Boolean) {
			return (Boolean) from ? 1L : 0;
		} else {
			return Long.valueOf(from.toString());
		}
	}
}
