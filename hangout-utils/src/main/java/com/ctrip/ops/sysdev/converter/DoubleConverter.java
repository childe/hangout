package com.ctrip.ops.sysdev.converter;

public class DoubleConverter implements Converter {

	public Object convert(Object from) {
		if (from instanceof Number) {
			return ((Number) from).doubleValue();
		} else if (from instanceof Boolean) {
			return ((Boolean) from).booleanValue() ? Double.valueOf(1) : 0;
		} else {
			return Double.valueOf(from.toString().trim());
		}
	}
}
