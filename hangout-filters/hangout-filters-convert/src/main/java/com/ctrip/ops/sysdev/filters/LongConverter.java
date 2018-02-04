package com.ctrip.ops.sysdev.filters;

import java.util.Map;

public class LongConverter implements ConverterI {

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
