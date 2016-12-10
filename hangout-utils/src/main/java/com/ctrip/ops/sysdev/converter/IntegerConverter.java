package com.ctrip.ops.sysdev.converter;

public class IntegerConverter implements Converter {

	public Object convert(Object from) {
		if (from instanceof Number) {
			return ((Number) from).intValue();
		} else if (from instanceof Boolean) {
			return ((Boolean) from).booleanValue() ? Integer.valueOf(1) : 0;
		} else if (from instanceof Enum) {
			return Integer.valueOf(((Enum<?>) from).ordinal());
		} else {
			return Integer.valueOf(from.toString().trim());
		}
	}
}
