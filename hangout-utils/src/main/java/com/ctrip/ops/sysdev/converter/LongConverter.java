package com.ctrip.ops.sysdev.converter;

public class LongConverter implements Converter {

	public Object convert(Object from) {
		if (from instanceof Number) {
			return ((Number) from).longValue();
		} else if (from instanceof Boolean) {
			return ((Boolean) from).booleanValue() ? Long.valueOf(1) : 0;
		} else {
			return Long.valueOf(from.toString());
		}
	}
}
